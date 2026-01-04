package service

import (
	"context"
	"testing"
	"time"
	"todo/internal/constant"
	"todo/internal/constant/enum/respcode"
	"todo/internal/errs"
	"todo/internal/model/entity"
	"todo/internal/model/request"

	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/require"
	"gorm.io/gorm"
)

func TestTodoResolveListIDInbox(t *testing.T) {
	todoListRepo := &MockTodoListRepo{}
	todoListRepo.On("GetInbox", mockAnyContext, uint(1)).Return(&entity.TodoList{Auditable: &entity.Auditable{Model: &gorm.Model{ID: 10}, CreatedByID: 1}}, nil)

	svc := NewTodoService(&NewTodoServiceOptions{TodoListRepo: todoListRepo})
	id, err := svc.resolveListID(context.Background(), 1, constant.TodoListInboxID)
	require.NoError(t, err)
	require.Equal(t, uint(10), id)
}

func TestTodoResolveListIDInvalid(t *testing.T) {
	svc := NewTodoService(&NewTodoServiceOptions{})
	_, err := svc.resolveListID(context.Background(), 1, "bad")
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.InvalidParam, be.Code)
}

func TestTodoResolveListIDInboxNotFound(t *testing.T) {
	todoListRepo := &MockTodoListRepo{}
	todoListRepo.On("GetInbox", mockAnyContext, uint(1)).Return((*entity.TodoList)(nil), gorm.ErrRecordNotFound)

	svc := NewTodoService(&NewTodoServiceOptions{TodoListRepo: todoListRepo})
	_, err := svc.resolveListID(context.Background(), 1, constant.TodoListInboxID)
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.TodoListNotFound, be.Code)
}

func TestTodoResolveListIDInboxError(t *testing.T) {
	todoListRepo := &MockTodoListRepo{}
	todoListRepo.On("GetInbox", mockAnyContext, uint(1)).Return((*entity.TodoList)(nil), errs.ErrVerifyToken)

	svc := NewTodoService(&NewTodoServiceOptions{TodoListRepo: todoListRepo})
	_, err := svc.resolveListID(context.Background(), 1, constant.TodoListInboxID)
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.InternalServerError, be.Code)
}

func TestTodoEnsureListOwned(t *testing.T) {
	todoListRepo := &MockTodoListRepo{}
	todoListRepo.On("GetByID", mockAnyContext, uint(2)).Return(&entity.TodoList{Auditable: &entity.Auditable{CreatedByID: 3}}, nil)

	svc := NewTodoService(&NewTodoServiceOptions{TodoListRepo: todoListRepo})
	err := svc.ensureTodoListOwned(context.Background(), 1, 2)
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.PermissionDenied, be.Code)
}

func TestTodoEnsureListOwnedNotFound(t *testing.T) {
	todoListRepo := &MockTodoListRepo{}
	todoListRepo.On("GetByID", mockAnyContext, uint(2)).Return((*entity.TodoList)(nil), gorm.ErrRecordNotFound)

	svc := NewTodoService(&NewTodoServiceOptions{TodoListRepo: todoListRepo})
	err := svc.ensureTodoListOwned(context.Background(), 1, 2)
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.TodoListNotFound, be.Code)
}

func TestTodoEnsureListOwnedError(t *testing.T) {
	todoListRepo := &MockTodoListRepo{}
	todoListRepo.On("GetByID", mockAnyContext, uint(2)).Return((*entity.TodoList)(nil), errs.ErrVerifyToken)

	svc := NewTodoService(&NewTodoServiceOptions{TodoListRepo: todoListRepo})
	err := svc.ensureTodoListOwned(context.Background(), 1, 2)
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.InternalServerError, be.Code)
}

func TestTodoGetTodoOwnedNotFound(t *testing.T) {
	todoRepo := &MockTodoRepo{}
	todoRepo.On("GetByID", mockAnyContext, uint(1)).Return((*entity.Todo)(nil), gorm.ErrRecordNotFound)

	svc := NewTodoService(&NewTodoServiceOptions{TodoRepo: todoRepo})
	_, err := svc.getTodoOwned(context.Background(), 1, 1)
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.TodoNotFound, be.Code)
}

func TestTodoGetTodoOwnedPermission(t *testing.T) {
	todoRepo := &MockTodoRepo{}
	todoRepo.On("GetByID", mockAnyContext, uint(1)).Return(&entity.Todo{Auditable: &entity.Auditable{CreatedByID: 2}}, nil)

	svc := NewTodoService(&NewTodoServiceOptions{TodoRepo: todoRepo})
	_, err := svc.getTodoOwned(context.Background(), 1, 1)
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.PermissionDenied, be.Code)
}

func TestTodoGetTodoOwnedError(t *testing.T) {
	todoRepo := &MockTodoRepo{}
	todoRepo.On("GetByID", mockAnyContext, uint(1)).Return((*entity.Todo)(nil), errs.ErrVerifyToken)

	svc := NewTodoService(&NewTodoServiceOptions{TodoRepo: todoRepo})
	_, err := svc.getTodoOwned(context.Background(), 1, 1)
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.InternalServerError, be.Code)
}

func TestTodoAddTodoSuccess(t *testing.T) {
	userRepo := &MockUserRepo{}
	userRepo.On("GetByID", mockAnyContext, uint(1)).Return((*entity.User)(nil), gorm.ErrRecordNotFound)

	todoListRepo := &MockTodoListRepo{}
	todoListRepo.On("GetByID", mockAnyContext, uint(2)).Return(&entity.TodoList{Auditable: &entity.Auditable{CreatedByID: 1}}, nil)

	todoRepo := &MockTodoRepo{}
	todoRepo.On("Save", mockAnyContext, mock.AnythingOfType("*entity.Todo")).Return(&entity.Todo{Auditable: &entity.Auditable{Model: &gorm.Model{ID: 1}, CreatedByID: 1}, Content: "c", BelongedListID: 2}, nil)

	svc := NewTodoService(&NewTodoServiceOptions{TodoRepo: todoRepo, TodoListRepo: todoListRepo, UserRepo: userRepo})
	resp, err := svc.AddTodo(context.Background(), 1, &request.TodoAdd{Content: "c", BelongedListID: "2"})
	require.NoError(t, err)
	require.Equal(t, uint(1), resp.ID)
}

func TestTodoAddTodoUserError(t *testing.T) {
	userRepo := &MockUserRepo{}
	userRepo.On("GetByID", mockAnyContext, uint(1)).Return((*entity.User)(nil), errs.ErrVerifyToken)

	svc := NewTodoService(&NewTodoServiceOptions{UserRepo: userRepo})
	_, err := svc.AddTodo(context.Background(), 1, &request.TodoAdd{Content: "c", BelongedListID: "2"})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.InternalServerError, be.Code)
}

func TestTodoAddTodoInvalidListID(t *testing.T) {
	userRepo := &MockUserRepo{}
	userRepo.On("GetByID", mockAnyContext, uint(1)).Return((*entity.User)(nil), gorm.ErrRecordNotFound)

	svc := NewTodoService(&NewTodoServiceOptions{UserRepo: userRepo})
	_, err := svc.AddTodo(context.Background(), 1, &request.TodoAdd{Content: "c", BelongedListID: "bad"})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.InvalidParam, be.Code)
}

func TestTodoAddTodoPermission(t *testing.T) {
	userRepo := &MockUserRepo{}
	userRepo.On("GetByID", mockAnyContext, uint(1)).Return((*entity.User)(nil), gorm.ErrRecordNotFound)

	todoListRepo := &MockTodoListRepo{}
	todoListRepo.On("GetByID", mockAnyContext, uint(2)).Return(&entity.TodoList{Auditable: &entity.Auditable{CreatedByID: 2}}, nil)

	svc := NewTodoService(&NewTodoServiceOptions{TodoListRepo: todoListRepo, UserRepo: userRepo})
	_, err := svc.AddTodo(context.Background(), 1, &request.TodoAdd{Content: "c", BelongedListID: "2"})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.PermissionDenied, be.Code)
}

func TestTodoAddTodoSaveError(t *testing.T) {
	userRepo := &MockUserRepo{}
	userRepo.On("GetByID", mockAnyContext, uint(1)).Return((*entity.User)(nil), gorm.ErrRecordNotFound)

	todoListRepo := &MockTodoListRepo{}
	todoListRepo.On("GetByID", mockAnyContext, uint(2)).Return(&entity.TodoList{Auditable: &entity.Auditable{CreatedByID: 1}}, nil)

	todoRepo := &MockTodoRepo{}
	todoRepo.On("Save", mockAnyContext, mock.AnythingOfType("*entity.Todo")).Return((*entity.Todo)(nil), errs.ErrVerifyToken)

	svc := NewTodoService(&NewTodoServiceOptions{TodoRepo: todoRepo, TodoListRepo: todoListRepo, UserRepo: userRepo})
	_, err := svc.AddTodo(context.Background(), 1, &request.TodoAdd{Content: "c", BelongedListID: "2"})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.InternalServerError, be.Code)
}

func TestTodoAddTodoDeletedUser(t *testing.T) {
	deleted := time.Now().Add(-time.Hour)
	userRepo := &MockUserRepo{}
	userRepo.On("GetByID", mockAnyContext, uint(1)).Return(&entity.User{Auditable: &entity.Auditable{Model: &gorm.Model{DeletedAt: gorm.DeletedAt{Time: deleted, Valid: true}}}}, nil)

	svc := NewTodoService(&NewTodoServiceOptions{UserRepo: userRepo})
	_, err := svc.AddTodo(context.Background(), 1, &request.TodoAdd{Content: "c", BelongedListID: "2"})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.UserAlreadyExists, be.Code)
}

func TestTodoDeleteTodoSoft(t *testing.T) {
	todoRepo := &MockTodoRepo{}
	todoRepo.On("GetByID", mockAnyContext, uint(1)).Return(&entity.Todo{Auditable: &entity.Auditable{CreatedByID: 1}}, nil)
	todoRepo.On("SoftDelete", mockAnyContext, uint(1)).Return(nil)

	svc := NewTodoService(&NewTodoServiceOptions{TodoRepo: todoRepo})
	soft := true
	err := svc.DeleteTodo(context.Background(), 1, &request.TodoDelete{ID: 1, SoftDeleted: &soft})
	require.NoError(t, err)
}

func TestTodoDeleteTodoSoftError(t *testing.T) {
	todoRepo := &MockTodoRepo{}
	todoRepo.On("GetByID", mockAnyContext, uint(1)).Return(&entity.Todo{Auditable: &entity.Auditable{CreatedByID: 1}}, nil)
	todoRepo.On("SoftDelete", mockAnyContext, uint(1)).Return(errs.ErrVerifyToken)

	svc := NewTodoService(&NewTodoServiceOptions{TodoRepo: todoRepo})
	soft := true
	err := svc.DeleteTodo(context.Background(), 1, &request.TodoDelete{ID: 1, SoftDeleted: &soft})
	require.Error(t, err)
}

func TestTodoDeleteTodoHard(t *testing.T) {
	todoRepo := &MockTodoRepo{}
	todoRepo.On("GetByID", mockAnyContext, uint(1)).Return(&entity.Todo{Auditable: &entity.Auditable{CreatedByID: 1}}, nil)
	todoRepo.On("HardDelete", mockAnyContext, uint(1)).Return(nil)

	svc := NewTodoService(&NewTodoServiceOptions{TodoRepo: todoRepo})
	soft := false
	err := svc.DeleteTodo(context.Background(), 1, &request.TodoDelete{ID: 1, SoftDeleted: &soft})
	require.NoError(t, err)
}

func TestTodoDeleteTodoHardError(t *testing.T) {
	todoRepo := &MockTodoRepo{}
	todoRepo.On("GetByID", mockAnyContext, uint(1)).Return(&entity.Todo{Auditable: &entity.Auditable{CreatedByID: 1}}, nil)
	todoRepo.On("HardDelete", mockAnyContext, uint(1)).Return(errs.ErrVerifyToken)

	svc := NewTodoService(&NewTodoServiceOptions{TodoRepo: todoRepo})
	soft := false
	err := svc.DeleteTodo(context.Background(), 1, &request.TodoDelete{ID: 1, SoftDeleted: &soft})
	require.Error(t, err)
}

func TestTodoDeleteTodoMissingFlag(t *testing.T) {
	todoRepo := &MockTodoRepo{}
	todoRepo.On("GetByID", mockAnyContext, uint(1)).Return(&entity.Todo{Auditable: &entity.Auditable{CreatedByID: 1}}, nil)

	svc := NewTodoService(&NewTodoServiceOptions{TodoRepo: todoRepo})
	err := svc.DeleteTodo(context.Background(), 1, &request.TodoDelete{ID: 1})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.InvalidParam, be.Code)
}

func TestTodoUpdateTodo(t *testing.T) {
	todoRepo := &MockTodoRepo{}
	updated := &entity.Todo{Auditable: &entity.Auditable{Model: &gorm.Model{ID: 1}, CreatedByID: 1}}
	todoRepo.On("GetByID", mockAnyContext, uint(1)).Return(updated, nil)
	todoRepo.On("Update", mockAnyContext, mock.AnythingOfType("*entity.Todo")).Return(updated, nil)

	svc := NewTodoService(&NewTodoServiceOptions{TodoRepo: todoRepo})
	done := true
	desc := "d"
	resp, err := svc.UpdateTodo(context.Background(), 1, &request.TodoUpdate{ID: 1, Content: "c", Done: &done, Description: &desc})
	require.NoError(t, err)
	require.Equal(t, uint(1), resp.ID)
}

func TestTodoUpdateTodoError(t *testing.T) {
	todoRepo := &MockTodoRepo{}
	updated := &entity.Todo{Auditable: &entity.Auditable{Model: &gorm.Model{ID: 1}, CreatedByID: 1}}
	todoRepo.On("GetByID", mockAnyContext, uint(1)).Return(updated, nil)
	todoRepo.On("Update", mockAnyContext, mock.AnythingOfType("*entity.Todo")).Return((*entity.Todo)(nil), errs.ErrVerifyToken)

	svc := NewTodoService(&NewTodoServiceOptions{TodoRepo: todoRepo})
	done := true
	_, err := svc.UpdateTodo(context.Background(), 1, &request.TodoUpdate{ID: 1, Content: "c", Done: &done})
	require.Error(t, err)
}

func TestTodoToggleTodo(t *testing.T) {
	todoRepo := &MockTodoRepo{}
	updated := &entity.Todo{Auditable: &entity.Auditable{Model: &gorm.Model{ID: 1}, CreatedByID: 1}, Done: false}
	todoRepo.On("GetByID", mockAnyContext, uint(1)).Return(updated, nil)
	todoRepo.On("Update", mockAnyContext, mock.AnythingOfType("*entity.Todo")).Return(updated, nil)

	svc := NewTodoService(&NewTodoServiceOptions{TodoRepo: todoRepo})
	resp, err := svc.ToggleTodo(context.Background(), 1, &request.TodoToggle{ID: 1})
	require.NoError(t, err)
	require.Equal(t, uint(1), resp.ID)
}

func TestTodoToggleTodoError(t *testing.T) {
	todoRepo := &MockTodoRepo{}
	updated := &entity.Todo{Auditable: &entity.Auditable{Model: &gorm.Model{ID: 1}, CreatedByID: 1}, Done: false}
	todoRepo.On("GetByID", mockAnyContext, uint(1)).Return(updated, nil)
	todoRepo.On("Update", mockAnyContext, mock.AnythingOfType("*entity.Todo")).Return((*entity.Todo)(nil), errs.ErrVerifyToken)

	svc := NewTodoService(&NewTodoServiceOptions{TodoRepo: todoRepo})
	_, err := svc.ToggleTodo(context.Background(), 1, &request.TodoToggle{ID: 1})
	require.Error(t, err)
}

func TestTodoMoveTodo(t *testing.T) {
	todoRepo := &MockTodoRepo{}
	updated := &entity.Todo{Auditable: &entity.Auditable{Model: &gorm.Model{ID: 1}, CreatedByID: 1}}
	todoRepo.On("GetByID", mockAnyContext, uint(1)).Return(updated, nil)
	todoRepo.On("Update", mockAnyContext, mock.AnythingOfType("*entity.Todo")).Return(updated, nil)

	todoListRepo := &MockTodoListRepo{}
	todoListRepo.On("GetByID", mockAnyContext, uint(2)).Return(&entity.TodoList{Auditable: &entity.Auditable{CreatedByID: 1}}, nil)

	svc := NewTodoService(&NewTodoServiceOptions{TodoRepo: todoRepo, TodoListRepo: todoListRepo})
	resp, err := svc.MoveTodo(context.Background(), 1, &request.TodoMove{ID: 1, TargetListID: "2"})
	require.NoError(t, err)
	require.Equal(t, uint(1), resp.ID)
}

func TestTodoMoveTodoListNotFound(t *testing.T) {
	todoRepo := &MockTodoRepo{}
	updated := &entity.Todo{Auditable: &entity.Auditable{Model: &gorm.Model{ID: 1}, CreatedByID: 1}}
	todoRepo.On("GetByID", mockAnyContext, uint(1)).Return(updated, nil)

	todoListRepo := &MockTodoListRepo{}
	todoListRepo.On("GetByID", mockAnyContext, uint(2)).Return((*entity.TodoList)(nil), gorm.ErrRecordNotFound)

	svc := NewTodoService(&NewTodoServiceOptions{TodoRepo: todoRepo, TodoListRepo: todoListRepo})
	_, err := svc.MoveTodo(context.Background(), 1, &request.TodoMove{ID: 1, TargetListID: "2"})
	var be *errs.BusinessErr
	require.ErrorAs(t, err, &be)
	require.Equal(t, respcode.TodoListNotFound, be.Code)
}

func TestTodoMoveTodoUpdateError(t *testing.T) {
	todoRepo := &MockTodoRepo{}
	updated := &entity.Todo{Auditable: &entity.Auditable{Model: &gorm.Model{ID: 1}, CreatedByID: 1}}
	todoRepo.On("GetByID", mockAnyContext, uint(1)).Return(updated, nil)
	todoRepo.On("Update", mockAnyContext, mock.AnythingOfType("*entity.Todo")).Return((*entity.Todo)(nil), errs.ErrVerifyToken)

	todoListRepo := &MockTodoListRepo{}
	todoListRepo.On("GetByID", mockAnyContext, uint(2)).Return(&entity.TodoList{Auditable: &entity.Auditable{CreatedByID: 1}}, nil)

	svc := NewTodoService(&NewTodoServiceOptions{TodoRepo: todoRepo, TodoListRepo: todoListRepo})
	_, err := svc.MoveTodo(context.Background(), 1, &request.TodoMove{ID: 1, TargetListID: "2"})
	require.Error(t, err)
}

func TestTodoGetTodos(t *testing.T) {
	todoRepo := &MockTodoRepo{}
	todoRepo.On("SelectByListID", mockAnyContext, uint(2)).Return([]*entity.Todo{{Auditable: &entity.Auditable{Model: &gorm.Model{ID: 1}, CreatedByID: 1}, Content: "c", BelongedListID: 2}}, nil)

	todoListRepo := &MockTodoListRepo{}
	todoListRepo.On("GetByID", mockAnyContext, uint(2)).Return(&entity.TodoList{Auditable: &entity.Auditable{CreatedByID: 1}}, nil)

	svc := NewTodoService(&NewTodoServiceOptions{TodoRepo: todoRepo, TodoListRepo: todoListRepo})
	resp, err := svc.GetTodos(context.Background(), 1, "2")
	require.NoError(t, err)
	require.Len(t, resp, 1)
}

func TestTodoGetTodosSelectError(t *testing.T) {
	todoRepo := &MockTodoRepo{}
	todoRepo.On("SelectByListID", mockAnyContext, uint(2)).Return([]*entity.Todo{}, errs.ErrVerifyToken)

	todoListRepo := &MockTodoListRepo{}
	todoListRepo.On("GetByID", mockAnyContext, uint(2)).Return(&entity.TodoList{Auditable: &entity.Auditable{CreatedByID: 1}}, nil)

	svc := NewTodoService(&NewTodoServiceOptions{TodoRepo: todoRepo, TodoListRepo: todoListRepo})
	_, err := svc.GetTodos(context.Background(), 1, "2")
	require.Error(t, err)
}

func TestEnsureAuditableExisting(t *testing.T) {
	auditable := &entity.Auditable{Model: &gorm.Model{}}
	ensureAuditable(&auditable)
	require.NotNil(t, auditable)
}

func TestTodoToTodoResponseUpdatedAt(t *testing.T) {
	updated := time.Now()
	updatedBy := uint(2)
	resp := toTodoResponse(&entity.Todo{Auditable: &entity.Auditable{Model: &gorm.Model{ID: 1, UpdatedAt: updated}, UpdatedByID: &updatedBy}})
	require.NotNil(t, resp.UpdatedAt)
}
