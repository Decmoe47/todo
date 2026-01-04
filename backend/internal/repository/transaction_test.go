package repository

import (
	"context"
	"errors"
	"testing"
	"todo/internal/model/entity"

	"github.com/glebarez/sqlite"
	"github.com/stretchr/testify/require"
	"gorm.io/gorm"
)

func newTxTestDB(t *testing.T) *gorm.DB {
	db, err := gorm.Open(sqlite.Open(":memory:"), &gorm.Config{})
	require.NoError(t, err)
	require.NoError(t, db.AutoMigrate(&entity.User{}))
	return db
}

func TestGormTxManagerCommit(t *testing.T) {
	db := newTxTestDB(t)
	mgr := NewGormTxManager(db, nil)

	err := mgr.WithTx(context.Background(), func(ctx context.Context) error {
		return DBFromCtx(ctx, db).Create(&entity.User{Email: "a@b.com", Name: "A"}).Error
	})
	require.NoError(t, err)

	var count int64
	require.NoError(t, db.Model(&entity.User{}).Count(&count).Error)
	require.Equal(t, int64(1), count)
}

func TestGormTxManagerRollback(t *testing.T) {
	db := newTxTestDB(t)
	mgr := NewGormTxManager(db, nil)

	err := mgr.WithTx(context.Background(), func(ctx context.Context) error {
		_ = DBFromCtx(ctx, db).Create(&entity.User{Email: "a@b.com", Name: "A"}).Error
		return errVerify
	})
	require.Error(t, err)

	var count int64
	require.NoError(t, db.Model(&entity.User{}).Count(&count).Error)
	require.Equal(t, int64(0), count)
}

func TestGormTxManagerPanic(t *testing.T) {
	db := newTxTestDB(t)
	mgr := NewGormTxManager(db, nil)

	require.Panics(t, func() {
		_ = mgr.WithTx(context.Background(), func(ctx context.Context) error {
			panic("boom")
		})
	})
}

var errVerify = errors.New("verify")

func TestDBFromCtx(t *testing.T) {
	db := newTxTestDB(t)
	base := DBFromCtx(context.Background(), db)
	require.Equal(t, db, base)

	tx := db.Begin()
	ctx := withTx(context.Background(), tx)

	got := DBFromCtx(ctx, db)
	require.Equal(t, tx, got)
	_, ok := TxFromCtx(context.Background())
	require.False(t, ok)

	_ = tx.Rollback().Error
}

func TestGormTxManagerExistingTx(t *testing.T) {
	db := newTxTestDB(t)
	tx := db.Begin()
	ctx := withTx(context.Background(), tx)

	mgr := NewGormTxManager(db, nil)
	err := mgr.WithTx(ctx, func(ctx context.Context) error {
		return nil
	})
	require.NoError(t, err)

	_ = tx.Rollback().Error
}
