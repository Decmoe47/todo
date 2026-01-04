package service

import (
	"context"
	"testing"
	"time"
	"todo/internal/constant/enum/respcode"
	"todo/internal/errs"
	"todo/internal/model/entity"
	"todo/internal/model/request"

	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/require"
	"gorm.io/gorm"
)

func TestTodoListGetCustom(t *testing.T) {
	repo := &MockTodoListRepo{}
	repo.On("SelectExcludingInbox", mockAnyContext, uint(1)).Return([]*entity.TodoList{{Auditable: &entity.Auditable{Model: &gorm.Model{ID: 1}}, Name: "L"}}, nil)

	svc := NewTodoListService(&NewTodoListServiceOptions{TodoListRepo: repo})
	resp, err := svc.GetCustomTodoLists(context.Background(), 1)
	require.NoError(t, err)
	require.Len(t, resp, 1)
}

func TestTodoListGetCustomError(t *testing.T) {
	repo := &MockTodoListRepo{}
	repo.On("SelectExcludingInbox", mockAnyContext, uint(1)).Return([]*entity.TodoList{}, errs.ErrVerifyToken)

	svc := NewTodoListService(&NewTodoListServiceOptions{TodoListRepo: repo})
	_, err := svc.GetCustomTodoLists(context.Background(), 1)
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.InternalServerError, be.Code)
}

func TestTodoListAdd(t *testing.T) {
	userRepo := &MockUserRepo{}
	userRepo.On("GetByID", mockAnyContext, uint(1)).Return((*entity.User)(nil), gorm.ErrRecordNotFound)

	repo := &MockTodoListRepo{}
	repo.On("Save", mockAnyContext, mock.AnythingOfType("*entity.TodoList")).Return(&entity.TodoList{Auditable: &entity.Auditable{Model: &gorm.Model{ID: 2}}, Name: "L"}, nil)

	svc := NewTodoListService(&NewTodoListServiceOptions{TodoListRepo: repo, UserRepo: userRepo})
	resp, err := svc.AddTodoList(context.Background(), 1, &request.TodoListAdd{Name: "L"})
	require.NoError(t, err)
	require.Equal(t, uint(2), resp.ID)
}

func TestTodoListAddGetByIDError(t *testing.T) {
	userRepo := &MockUserRepo{}
	userRepo.On("GetByID", mockAnyContext, uint(1)).Return((*entity.User)(nil), errs.ErrVerifyToken)

	svc := NewTodoListService(&NewTodoListServiceOptions{UserRepo: userRepo})
	_, err := svc.AddTodoList(context.Background(), 1, &request.TodoListAdd{Name: "L"})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.InternalServerError, be.Code)
}

func TestTodoListAddSaveError(t *testing.T) {
	userRepo := &MockUserRepo{}
	userRepo.On("GetByID", mockAnyContext, uint(1)).Return((*entity.User)(nil), gorm.ErrRecordNotFound)

	repo := &MockTodoListRepo{}
	repo.On("Save", mockAnyContext, mock.AnythingOfType("*entity.TodoList")).Return((*entity.TodoList)(nil), errs.ErrVerifyToken)

	svc := NewTodoListService(&NewTodoListServiceOptions{TodoListRepo: repo, UserRepo: userRepo})
	_, err := svc.AddTodoList(context.Background(), 1, &request.TodoListAdd{Name: "L"})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.InternalServerError, be.Code)
}

func TestTodoListAddDeletedUser(t *testing.T) {
	deleted := time.Now().Add(-time.Hour)
	userRepo := &MockUserRepo{}
	userRepo.On("GetByID", mockAnyContext, uint(1)).Return(&entity.User{Auditable: &entity.Auditable{Model: &gorm.Model{DeletedAt: gorm.DeletedAt{Time: deleted, Valid: true}}}}, nil)

	svc := NewTodoListService(&NewTodoListServiceOptions{UserRepo: userRepo})
	_, err := svc.AddTodoList(context.Background(), 1, &request.TodoListAdd{Name: "L"})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.ListAlreadyExists, be.Code)
}

func TestTodoListUpdate(t *testing.T) {
	repo := &MockTodoListRepo{}
	repo.On("GetByID", mockAnyContext, uint(1)).Return(&entity.TodoList{Auditable: &entity.Auditable{Model: &gorm.Model{ID: 1}, CreatedByID: 1}}, nil)
	repo.On("Update", mockAnyContext, mock.AnythingOfType("*entity.TodoList")).Return(&entity.TodoList{Auditable: &entity.Auditable{Model: &gorm.Model{ID: 1}}, Name: "N"}, nil)

	svc := NewTodoListService(&NewTodoListServiceOptions{TodoListRepo: repo})
	resp, err := svc.UpdateTodoList(context.Background(), 1, &request.TodoListUpdate{ID: 1, Name: "N"})
	require.NoError(t, err)
	require.Equal(t, "N", resp.Name)
}

func TestTodoListUpdateNotFound(t *testing.T) {
	repo := &MockTodoListRepo{}
	repo.On("GetByID", mockAnyContext, uint(1)).Return((*entity.TodoList)(nil), gorm.ErrRecordNotFound)

	svc := NewTodoListService(&NewTodoListServiceOptions{TodoListRepo: repo})
	_, err := svc.UpdateTodoList(context.Background(), 1, &request.TodoListUpdate{ID: 1, Name: "N"})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.TodoListNotFound, be.Code)
}

func TestTodoListUpdatePermission(t *testing.T) {
	repo := &MockTodoListRepo{}
	repo.On("GetByID", mockAnyContext, uint(1)).Return(&entity.TodoList{Auditable: &entity.Auditable{CreatedByID: 2}}, nil)

	svc := NewTodoListService(&NewTodoListServiceOptions{TodoListRepo: repo})
	_, err := svc.UpdateTodoList(context.Background(), 1, &request.TodoListUpdate{ID: 1, Name: "N"})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.PermissionDenied, be.Code)
}

func TestTodoListUpdateError(t *testing.T) {
	repo := &MockTodoListRepo{}
	repo.On("GetByID", mockAnyContext, uint(1)).Return(&entity.TodoList{Auditable: &entity.Auditable{Model: &gorm.Model{ID: 1}, CreatedByID: 1}}, nil)
	repo.On("Update", mockAnyContext, mock.AnythingOfType("*entity.TodoList")).Return((*entity.TodoList)(nil), errs.ErrVerifyToken)

	svc := NewTodoListService(&NewTodoListServiceOptions{TodoListRepo: repo})
	_, err := svc.UpdateTodoList(context.Background(), 1, &request.TodoListUpdate{ID: 1, Name: "N"})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.InternalServerError, be.Code)
}

func TestTodoListDelete(t *testing.T) {
	todoRepo := &MockTodoRepo{}
	todoRepo.On("DeleteByBelongedListID", mockAnyContext, uint(1)).Return(nil)

	repo := &MockTodoListRepo{}
	repo.On("GetByID", mockAnyContext, uint(1)).Return(&entity.TodoList{Auditable: &entity.Auditable{CreatedByID: 1}}, nil)
	repo.On("Delete", mockAnyContext, uint(1)).Return(nil)

	txMgr := &MockTxManager{}
	txMgr.On("WithTx", mockAnyContext).Return(nil)

	svc := NewTodoListService(&NewTodoListServiceOptions{TodoListRepo: repo, TodoRepo: todoRepo, TxMgr: txMgr})
	require.NoError(t, svc.DeleteTodoList(context.Background(), 1, &request.TodoListDelete{ID: 1}))
}

func TestTodoListDeleteNotFound(t *testing.T) {
	repo := &MockTodoListRepo{}
	repo.On("GetByID", mockAnyContext, uint(1)).Return((*entity.TodoList)(nil), gorm.ErrRecordNotFound)

	txMgr := &MockTxManager{}
	txMgr.On("WithTx", mockAnyContext).Return(nil)

	svc := NewTodoListService(&NewTodoListServiceOptions{TodoListRepo: repo, TxMgr: txMgr})
	err := svc.DeleteTodoList(context.Background(), 1, &request.TodoListDelete{ID: 1})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.TodoListNotFound, be.Code)
}

func TestTodoListDeleteTodoError(t *testing.T) {
	todoRepo := &MockTodoRepo{}
	todoRepo.On("DeleteByBelongedListID", mockAnyContext, uint(1)).Return(errs.ErrVerifyToken)

	repo := &MockTodoListRepo{}
	repo.On("GetByID", mockAnyContext, uint(1)).Return(&entity.TodoList{Auditable: &entity.Auditable{CreatedByID: 1}}, nil)

	txMgr := &MockTxManager{}
	txMgr.On("WithTx", mockAnyContext).Return(nil)

	svc := NewTodoListService(&NewTodoListServiceOptions{TodoListRepo: repo, TodoRepo: todoRepo, TxMgr: txMgr})
	err := svc.DeleteTodoList(context.Background(), 1, &request.TodoListDelete{ID: 1})
	require.Error(t, err)
}

func TestTodoListDeleteListError(t *testing.T) {
	todoRepo := &MockTodoRepo{}
	todoRepo.On("DeleteByBelongedListID", mockAnyContext, uint(1)).Return(nil)

	repo := &MockTodoListRepo{}
	repo.On("GetByID", mockAnyContext, uint(1)).Return(&entity.TodoList{Auditable: &entity.Auditable{CreatedByID: 1}}, nil)
	repo.On("Delete", mockAnyContext, uint(1)).Return(errs.ErrVerifyToken)

	txMgr := &MockTxManager{}
	txMgr.On("WithTx", mockAnyContext).Return(nil)

	svc := NewTodoListService(&NewTodoListServiceOptions{TodoListRepo: repo, TodoRepo: todoRepo, TxMgr: txMgr})
	err := svc.DeleteTodoList(context.Background(), 1, &request.TodoListDelete{ID: 1})
	require.Error(t, err)
}

func TestTodoListDeleteTxError(t *testing.T) {
	txMgr := &MockTxManager{}
	txMgr.On("WithTx", mockAnyContext).Return(errs.ErrVerifyToken)

	svc := NewTodoListService(&NewTodoListServiceOptions{TxMgr: txMgr})
	err := svc.DeleteTodoList(context.Background(), 1, &request.TodoListDelete{ID: 1})
	require.Error(t, err)
}

func TestTodoListToTodoListResponseUpdatedAt(t *testing.T) {
	updated := time.Now()
	updatedBy := uint(2)
	resp := toTodoListResponse(&entity.TodoList{Auditable: &entity.Auditable{Model: &gorm.Model{ID: 1, UpdatedAt: updated}, UpdatedByID: &updatedBy}})
	require.NotNil(t, resp.UpdatedAt)
}
