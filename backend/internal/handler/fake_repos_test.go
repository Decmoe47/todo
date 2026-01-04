package handler

import (
	"context"
	"sync"
	"todo/internal/model/entity"
	"todo/internal/repository"

	"gorm.io/gorm"
)

type fakeUserRepo struct{}

func (f *fakeUserRepo) GetByEmail(ctx context.Context, email string) (*entity.User, error) {
	return nil, gorm.ErrRecordNotFound
}

func (f *fakeUserRepo) GetByID(ctx context.Context, id uint) (*entity.User, error) {
	return nil, gorm.ErrRecordNotFound
}

func (f *fakeUserRepo) HasByEmail(ctx context.Context, email string) (bool, error) {
	return false, nil
}

func (f *fakeUserRepo) SelectByEmail(ctx context.Context, email string) ([]*entity.User, error) {
	return []*entity.User{}, nil
}

func (f *fakeUserRepo) SelectByName(ctx context.Context, name string) ([]*entity.User, error) {
	return []*entity.User{}, nil
}

func (f *fakeUserRepo) Save(ctx context.Context, user *entity.User) error {
	return nil
}

type fakeTodoListRepo struct {
	mu     sync.Mutex
	nextID uint
	items  map[uint]*entity.TodoList
}

func newFakeTodoListRepo() *fakeTodoListRepo {
	return &fakeTodoListRepo{nextID: 1, items: map[uint]*entity.TodoList{}}
}

func (f *fakeTodoListRepo) SelectExcludingInbox(ctx context.Context, userID uint) ([]*entity.TodoList, error) {
	f.mu.Lock()
	defer f.mu.Unlock()
	out := []*entity.TodoList{}
	for _, list := range f.items {
		if list.Inbox {
			continue
		}
		if list.CreatedByID == userID {
			out = append(out, list)
		}
	}
	return out, nil
}

func (f *fakeTodoListRepo) GetInbox(ctx context.Context, userID uint) (*entity.TodoList, error) {
	f.mu.Lock()
	defer f.mu.Unlock()
	for _, list := range f.items {
		if list.Inbox && list.CreatedByID == userID {
			return list, nil
		}
	}
	return nil, gorm.ErrRecordNotFound
}

func (f *fakeTodoListRepo) GetByID(ctx context.Context, id uint) (*entity.TodoList, error) {
	f.mu.Lock()
	defer f.mu.Unlock()
	list, ok := f.items[id]
	if !ok {
		return nil, gorm.ErrRecordNotFound
	}
	return list, nil
}

func (f *fakeTodoListRepo) Save(ctx context.Context, todoList *entity.TodoList) (*entity.TodoList, error) {
	f.mu.Lock()
	defer f.mu.Unlock()
	if todoList.Auditable == nil {
		todoList.Auditable = &entity.Auditable{Model: &gorm.Model{}}
	}
	if todoList.Model == nil {
		todoList.Model = &gorm.Model{}
	}
	if todoList.ID == 0 {
		todoList.ID = f.nextID
		f.nextID++
	}
	f.items[todoList.ID] = todoList
	return todoList, nil
}

func (f *fakeTodoListRepo) Update(ctx context.Context, todoList *entity.TodoList) (*entity.TodoList, error) {
	f.mu.Lock()
	defer f.mu.Unlock()
	f.items[todoList.ID] = todoList
	return todoList, nil
}

func (f *fakeTodoListRepo) Delete(ctx context.Context, id uint) error {
	f.mu.Lock()
	defer f.mu.Unlock()
	delete(f.items, id)
	return nil
}

type fakeTodoRepo struct {
	mu     sync.Mutex
	nextID uint
	items  map[uint]*entity.Todo
}

func newFakeTodoRepo() *fakeTodoRepo {
	return &fakeTodoRepo{nextID: 1, items: map[uint]*entity.Todo{}}
}

func (f *fakeTodoRepo) SelectByListID(ctx context.Context, listID uint) ([]*entity.Todo, error) {
	f.mu.Lock()
	defer f.mu.Unlock()
	out := []*entity.Todo{}
	for _, todo := range f.items {
		if todo.BelongedListID == listID {
			out = append(out, todo)
		}
	}
	return out, nil
}

func (f *fakeTodoRepo) GetByID(ctx context.Context, id uint) (*entity.Todo, error) {
	f.mu.Lock()
	defer f.mu.Unlock()
	todo, ok := f.items[id]
	if !ok {
		return nil, gorm.ErrRecordNotFound
	}
	return todo, nil
}

func (f *fakeTodoRepo) SelectByIDs(ctx context.Context, ids []uint) ([]*entity.Todo, error) {
	f.mu.Lock()
	defer f.mu.Unlock()
	out := []*entity.Todo{}
	for _, id := range ids {
		if todo, ok := f.items[id]; ok {
			out = append(out, todo)
		}
	}
	return out, nil
}

func (f *fakeTodoRepo) Save(ctx context.Context, todo *entity.Todo) (*entity.Todo, error) {
	f.mu.Lock()
	defer f.mu.Unlock()
	if todo.Auditable == nil {
		todo.Auditable = &entity.Auditable{Model: &gorm.Model{}}
	}
	if todo.Model == nil {
		todo.Model = &gorm.Model{}
	}
	if todo.ID == 0 {
		todo.ID = f.nextID
		f.nextID++
	}
	f.items[todo.ID] = todo
	return todo, nil
}

func (f *fakeTodoRepo) Update(ctx context.Context, todo *entity.Todo) (*entity.Todo, error) {
	f.mu.Lock()
	defer f.mu.Unlock()
	f.items[todo.ID] = todo
	return todo, nil
}

func (f *fakeTodoRepo) SoftDelete(ctx context.Context, id uint) error {
	f.mu.Lock()
	defer f.mu.Unlock()
	delete(f.items, id)
	return nil
}

func (f *fakeTodoRepo) HardDelete(ctx context.Context, id uint) error {
	return f.SoftDelete(ctx, id)
}

func (f *fakeTodoRepo) DeleteByBelongedListID(ctx context.Context, belongedListID uint) error {
	f.mu.Lock()
	defer f.mu.Unlock()
	for id, todo := range f.items {
		if todo.BelongedListID == belongedListID {
			delete(f.items, id)
		}
	}
	return nil
}

type fakeTxMgr struct{}

func (f *fakeTxMgr) WithTx(ctx context.Context, fn func(ctx context.Context) error) error {
	return fn(ctx)
}

var _ repository.User = (*fakeUserRepo)(nil)
var _ repository.TodoList = (*fakeTodoListRepo)(nil)
var _ repository.Todo = (*fakeTodoRepo)(nil)
var _ repository.TxManager = (*fakeTxMgr)(nil)
