package repository

import (
	"context"
	"database/sql"
	"log/slog"

	"github.com/cockroachdb/errors"
	"gorm.io/gorm"
)

type TxManager interface {
	WithTx(ctx context.Context, fn func(ctx context.Context) error) error
}

type GormTxManager struct {
	db     *gorm.DB
	txOpts *sql.TxOptions
}

// txOpts 可传 nil；需要隔离级别/只读之类就传 &sql.TxOptions{}
func NewGormTxManager(db *gorm.DB, txOpts *sql.TxOptions) *GormTxManager {
	return &GormTxManager{db: db, txOpts: txOpts}
}

func (m *GormTxManager) WithTx(ctx context.Context, fn func(ctx context.Context) error) (err error) {
	// 已经在事务里：直接复用（避免“事务套事务”）
	if _, ok := TxFromCtx(ctx); ok {
		return fn(ctx)
	}

	tx := m.db.WithContext(ctx).Begin(m.txOpts)
	if tx.Error != nil {
		return errors.WithStack(tx.Error)
	}

	defer func() {
		// panic 一定回滚，然后把 panic 继续抛出去
		if r := recover(); r != nil {
			err = tx.Rollback().Error
			slog.Error("transaction rollback failed for panic", "error", errors.WithStack(err))
			panic(r)
		}

		// fn 返回 error：回滚
		if err != nil {
			if rbErr := tx.Rollback().Error; rbErr != nil {
				if errors.Is(rbErr, gorm.ErrInvalidTransaction) {
					slog.Warn("rollback invalid transaction", "error", errors.WithStack(rbErr))
				} else {
					err = errors.Join(err, errors.Wrap(rbErr, "rollback failed"))
				}
			}
			return
		}

		// fn 成功：提交
		if cErr := tx.Commit().Error; cErr != nil {
			err = errors.WithStack(cErr)
		}
	}()

	// 把 tx 注入 ctx，交给 repo 用 DBFromCtx(ctx, baseDB) 取
	err = fn(withTx(ctx, tx))
	return errors.WithStack(err)
}

type txKey struct{}

// withTx 把 tx 放进 ctx
func withTx(ctx context.Context, tx *gorm.DB) context.Context {
	return context.WithValue(ctx, txKey{}, tx)
}

// TxFromCtx 从 ctx 拿 tx
func TxFromCtx(ctx context.Context) (*gorm.DB, bool) {
	tx, ok := ctx.Value(txKey{}).(*gorm.DB)
	return tx, ok && tx != nil
}

// DBFromCtx repo 内部用：有 tx 用 tx，否则用 base db
func DBFromCtx(ctx context.Context, base *gorm.DB) *gorm.DB {
	if tx, ok := TxFromCtx(ctx); ok {
		return tx
	}
	return base
}
