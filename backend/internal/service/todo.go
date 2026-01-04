package service

import (
	"context"
	"strconv"
	"time"
	"todo/internal/constant"
	"todo/internal/constant/enum/respcode"
	"todo/internal/errs"
	"todo/internal/model/entity"
	"todo/internal/model/request"
	"todo/internal/model/response"
	"todo/internal/repository"
	"todo/internal/util"

	"github.com/cockroachdb/errors"
	"gorm.io/gorm"
)

type TodoService struct {
	todoRepo     repository.Todo
	todoListRepo repository.TodoList
	userRepo     repository.User
}

type NewTodoServiceOptions struct {
	TodoRepo     repository.Todo
	TodoListRepo repository.TodoList
	UserRepo     repository.User
}

func NewTodoService(opts *NewTodoServiceOptions) *TodoService {
	return &TodoService{
		todoRepo:     opts.TodoRepo,
		todoListRepo: opts.TodoListRepo,
		userRepo:     opts.UserRepo,
	}
}

func (s *TodoService) GetTodos(ctx context.Context, userID uint, listID string) ([]*response.Todo, error) {
	resolvedListID, err := s.resolveListID(ctx, userID, listID)
	if err != nil {
		return nil, err
	}
	if err := s.ensureTodoListOwned(ctx, userID, resolvedListID); err != nil {
		return nil, err
	}

	todos, err := s.todoRepo.SelectByListID(ctx, resolvedListID)
	if err != nil {
		return nil, errs.NewInternalServerErr(err, "Failed to select todos by list id %d", resolvedListID)
	}

	return util.MapSlice(todos, toTodoResponse), nil
}

func (s *TodoService) AddTodo(ctx context.Context, userID uint, req *request.TodoAdd) (*response.Todo, error) {
	user, err := s.userRepo.GetByID(ctx, userID)
	if err != nil && !errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, errs.NewInternalServerErr(err, "Failed to get user by id")
	} else if user != nil && user.DeletedAt.Time.Before(time.Now()) {
		return nil, errs.NewBusinessErr(respcode.UserAlreadyExists)
	}

	resolvedListID, err := s.resolveListID(ctx, userID, req.BelongedListID)
	if err != nil {
		return nil, err
	}
	if err := s.ensureTodoListOwned(ctx, userID, resolvedListID); err != nil {
		return nil, err
	}

	todo := entity.NewTodo(req.Content, req.DueDate, resolvedListID, userID)
	saved, err := s.todoRepo.Save(ctx, todo)
	if err != nil {
		return nil, errs.NewInternalServerErr(err, "Failed to save todo")
	}

	return toTodoResponse(saved), nil
}

func (s *TodoService) DeleteTodo(ctx context.Context, userID uint, req *request.TodoDelete) error {
	_, err := s.getTodoOwned(ctx, userID, req.ID)
	if err != nil {
		return err
	}

	if req.SoftDeleted == nil {
		return errs.NewBusinessErr(respcode.InvalidParam)
	}

	if *req.SoftDeleted {
		if err := s.todoRepo.SoftDelete(ctx, req.ID); err != nil {
			return errs.NewInternalServerErr(err, "Failed to soft delete todo")
		}
		return nil
	}

	if err := s.todoRepo.HardDelete(ctx, req.ID); err != nil {
		return errs.NewInternalServerErr(err, "Failed to hard delete todo")
	}
	return nil
}

func (s *TodoService) UpdateTodo(ctx context.Context, userID uint, req *request.TodoUpdate) (*response.Todo, error) {
	todo, err := s.getTodoOwned(ctx, userID, req.ID)
	if err != nil {
		return nil, err
	}

	ensureAuditable(&todo.Auditable)
	todo.Content = req.Content
	todo.Done = *req.Done
	todo.DueDate = req.DueDate
	todo.Description = req.Description
	todo.UpdatedByID = &userID

	updated, err := s.todoRepo.Update(ctx, todo)
	if err != nil {
		return nil, errs.NewInternalServerErr(err, "Failed to update todo")
	}
	return toTodoResponse(updated), nil
}

func (s *TodoService) ToggleTodo(ctx context.Context, userID uint, req *request.TodoToggle) (*response.Todo, error) {
	todo, err := s.getTodoOwned(ctx, userID, req.ID)
	if err != nil {
		return nil, err
	}

	ensureAuditable(&todo.Auditable)
	todo.Done = !todo.Done
	todo.UpdatedByID = &userID

	updated, err := s.todoRepo.Update(ctx, todo)
	if err != nil {
		return nil, errs.NewInternalServerErr(err, "Failed to toggle todo")
	}
	return toTodoResponse(updated), nil
}

func (s *TodoService) MoveTodo(ctx context.Context, userID uint, req *request.TodoMove) (*response.Todo, error) {
	todo, err := s.getTodoOwned(ctx, userID, req.ID)
	if err != nil {
		return nil, err
	}

	targetListID, err := s.resolveListID(ctx, userID, req.TargetListID)
	if err != nil {
		return nil, err
	}
	if err := s.ensureTodoListOwned(ctx, userID, targetListID); err != nil {
		return nil, err
	}

	ensureAuditable(&todo.Auditable)
	todo.BelongedListID = targetListID
	todo.UpdatedByID = &userID

	updated, err := s.todoRepo.Update(ctx, todo)
	if err != nil {
		return nil, errs.NewInternalServerErr(err, "Failed to move todo")
	}
	return toTodoResponse(updated), nil
}

func (s *TodoService) resolveListID(ctx context.Context, userID uint, listID string) (uint, error) {
	if listID == constant.TodoListInboxID {
		inbox, err := s.todoListRepo.GetInbox(ctx, userID)
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return 0, errs.NewBusinessErr(respcode.TodoListNotFound)
		} else if err != nil {
			return 0, errs.NewInternalServerErr(err, "Failed to get inbox list")
		}
		return inbox.ID, nil
	}

	parsed, err := strconv.ParseUint(listID, 10, 64)
	if err != nil {
		return 0, errs.NewBusinessErr(respcode.InvalidParam)
	}
	return uint(parsed), nil
}

func (s *TodoService) ensureTodoListOwned(ctx context.Context, userID uint, listID uint) error {
	list, err := s.todoListRepo.GetByID(ctx, listID)
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return errs.NewBusinessErr(respcode.TodoListNotFound)
	} else if err != nil {
		return errs.NewInternalServerErr(err, "Failed to get todo list by id")
	}

	if list.CreatedByID != userID {
		return errs.NewBusinessErr(respcode.PermissionDenied)
	}
	return nil
}

func (s *TodoService) getTodoOwned(ctx context.Context, userID uint, todoID uint) (*entity.Todo, error) {
	todo, err := s.todoRepo.GetByID(ctx, todoID)
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, errs.NewBusinessErr(respcode.TodoNotFound)
	} else if err != nil {
		return nil, errs.NewInternalServerErr(err, "Failed to get todo by id")
	}

	if todo.CreatedByID != userID {
		return nil, errs.NewBusinessErr(respcode.PermissionDenied)
	}
	return todo, nil
}

func ensureAuditable(auditable **entity.Auditable) {
	if *auditable == nil {
		*auditable = &entity.Auditable{Model: &gorm.Model{}}
	}
}

func toTodoResponse(todo *entity.Todo) *response.Todo {
	var updatedAt *time.Time
	if todo.UpdatedByID != nil && !todo.UpdatedAt.IsZero() {
		updatedAt = &todo.UpdatedAt
	}
	return &response.Todo{
		ID:             todo.ID,
		Content:        todo.Content,
		DueDate:        todo.DueDate,
		Done:           todo.Done,
		Description:    todo.Description,
		BelongedListID: todo.BelongedListID,
		CreatedBy:      todo.CreatedByID,
		CreatedAt:      todo.CreatedAt,
		UpdatedBy:      todo.UpdatedByID,
		UpdatedAt:      updatedAt,
	}
}
