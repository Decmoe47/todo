package service

import (
	"context"
	"todo/internal/model/entity"
	"todo/internal/repository"

	"github.com/stretchr/testify/mock"
)

type MockUserRepo struct {
	mock.Mock
}

func (m *MockUserRepo) GetByEmail(ctx context.Context, email string) (*entity.User, error) {
	args := m.Called(ctx, email)
	var user *entity.User
	if u, ok := args.Get(0).(*entity.User); ok {
		user = u
	}
	return user, args.Error(1)
}

func (m *MockUserRepo) GetByID(ctx context.Context, id uint) (*entity.User, error) {
	args := m.Called(ctx, id)
	var user *entity.User
	if u, ok := args.Get(0).(*entity.User); ok {
		user = u
	}
	return user, args.Error(1)
}

func (m *MockUserRepo) HasByEmail(ctx context.Context, email string) (bool, error) {
	args := m.Called(ctx, email)
	return args.Bool(0), args.Error(1)
}

func (m *MockUserRepo) SelectByEmail(ctx context.Context, email string) ([]*entity.User, error) {
	args := m.Called(ctx, email)
	users, _ := args.Get(0).([]*entity.User)
	return users, args.Error(1)
}

func (m *MockUserRepo) SelectByName(ctx context.Context, name string) ([]*entity.User, error) {
	args := m.Called(ctx, name)
	users, _ := args.Get(0).([]*entity.User)
	return users, args.Error(1)
}

func (m *MockUserRepo) Save(ctx context.Context, user *entity.User) error {
	args := m.Called(ctx, user)
	return args.Error(0)
}

type MockTodoRepo struct {
	mock.Mock
}

func (m *MockTodoRepo) SelectByListID(ctx context.Context, listID uint) ([]*entity.Todo, error) {
	args := m.Called(ctx, listID)
	items, _ := args.Get(0).([]*entity.Todo)
	return items, args.Error(1)
}

func (m *MockTodoRepo) GetByID(ctx context.Context, id uint) (*entity.Todo, error) {
	args := m.Called(ctx, id)
	var todo *entity.Todo
	if t, ok := args.Get(0).(*entity.Todo); ok {
		todo = t
	}
	return todo, args.Error(1)
}

func (m *MockTodoRepo) SelectByIDs(ctx context.Context, ids []uint) ([]*entity.Todo, error) {
	args := m.Called(ctx, ids)
	items, _ := args.Get(0).([]*entity.Todo)
	return items, args.Error(1)
}

func (m *MockTodoRepo) Save(ctx context.Context, todo *entity.Todo) (*entity.Todo, error) {
	args := m.Called(ctx, todo)
	var saved *entity.Todo
	if t, ok := args.Get(0).(*entity.Todo); ok {
		saved = t
	}
	return saved, args.Error(1)
}

func (m *MockTodoRepo) Update(ctx context.Context, todo *entity.Todo) (*entity.Todo, error) {
	args := m.Called(ctx, todo)
	var saved *entity.Todo
	if t, ok := args.Get(0).(*entity.Todo); ok {
		saved = t
	}
	return saved, args.Error(1)
}

func (m *MockTodoRepo) SoftDelete(ctx context.Context, id uint) error {
	args := m.Called(ctx, id)
	return args.Error(0)
}

func (m *MockTodoRepo) HardDelete(ctx context.Context, id uint) error {
	args := m.Called(ctx, id)
	return args.Error(0)
}

func (m *MockTodoRepo) DeleteByBelongedListID(ctx context.Context, belongedListID uint) error {
	args := m.Called(ctx, belongedListID)
	return args.Error(0)
}

type MockTodoListRepo struct {
	mock.Mock
}

func (m *MockTodoListRepo) SelectExcludingInbox(ctx context.Context, userID uint) ([]*entity.TodoList, error) {
	args := m.Called(ctx, userID)
	items, _ := args.Get(0).([]*entity.TodoList)
	return items, args.Error(1)
}

func (m *MockTodoListRepo) GetInbox(ctx context.Context, userID uint) (*entity.TodoList, error) {
	args := m.Called(ctx, userID)
	var list *entity.TodoList
	if t, ok := args.Get(0).(*entity.TodoList); ok {
		list = t
	}
	return list, args.Error(1)
}

func (m *MockTodoListRepo) GetByID(ctx context.Context, id uint) (*entity.TodoList, error) {
	args := m.Called(ctx, id)
	var list *entity.TodoList
	if t, ok := args.Get(0).(*entity.TodoList); ok {
		list = t
	}
	return list, args.Error(1)
}

func (m *MockTodoListRepo) Save(ctx context.Context, todoList *entity.TodoList) (*entity.TodoList, error) {
	args := m.Called(ctx, todoList)
	var saved *entity.TodoList
	if t, ok := args.Get(0).(*entity.TodoList); ok {
		saved = t
	}
	return saved, args.Error(1)
}

func (m *MockTodoListRepo) Update(ctx context.Context, todoList *entity.TodoList) (*entity.TodoList, error) {
	args := m.Called(ctx, todoList)
	var saved *entity.TodoList
	if t, ok := args.Get(0).(*entity.TodoList); ok {
		saved = t
	}
	return saved, args.Error(1)
}

func (m *MockTodoListRepo) Delete(ctx context.Context, id uint) error {
	args := m.Called(ctx, id)
	return args.Error(0)
}

type MockTxManager struct {
	mock.Mock
}

func (m *MockTxManager) WithTx(ctx context.Context, fn func(ctx context.Context) error) error {
	args := m.Called(ctx)
	if args.Get(0) != nil {
		return args.Error(0)
	}
	return fn(ctx)
}

var _ repository.User = (*MockUserRepo)(nil)
var _ repository.Todo = (*MockTodoRepo)(nil)
var _ repository.TodoList = (*MockTodoListRepo)(nil)
var _ repository.TxManager = (*MockTxManager)(nil)
