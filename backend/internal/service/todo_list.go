package service

import (
	"context"
	"time"
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

type TodoListService struct {
	todoListRepo repository.TodoList
	todoRepo     repository.Todo
	userRepo     repository.User
	txMgr        repository.TxManager
}

type NewTodoListServiceOptions struct {
	TodoListRepo repository.TodoList
	TodoRepo     repository.Todo
	UserRepo     repository.User
	TxMgr        repository.TxManager
}

func NewTodoListService(opts *NewTodoListServiceOptions) *TodoListService {
	return &TodoListService{
		todoListRepo: opts.TodoListRepo,
		todoRepo:     opts.TodoRepo,
		userRepo:     opts.UserRepo,
		txMgr:        opts.TxMgr,
	}
}

func (s *TodoListService) GetCustomTodoLists(ctx context.Context, userID uint) ([]*response.TodoList, error) {
	lists, err := s.todoListRepo.SelectExcludingInbox(ctx, userID)
	if err != nil {
		return nil, errs.NewInternalServerErr(err, "Failed to select todo lists excluding inbox")
	}
	return util.MapSlice(lists, toTodoListResponse), nil
}

func (s *TodoListService) AddTodoList(ctx context.Context, userID uint, req *request.TodoListAdd) (*response.TodoList, error) {
	list, err := s.userRepo.GetByID(ctx, userID)
	if err != nil && !errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, errs.NewInternalServerErr(err, "Failed to get list by id")
	} else if list != nil && list.DeletedAt.Time.Before(time.Now()) {
		return nil, errs.NewBusinessErr(respcode.ListAlreadyExists)
	}

	newList := entity.NewTodoList(req.Name, userID, false)
	saved, err := s.todoListRepo.Save(ctx, newList)
	if err != nil {
		return nil, errs.NewInternalServerErr(err, "Failed to save todo list")
	}
	return toTodoListResponse(saved), nil
}

func (s *TodoListService) UpdateTodoList(ctx context.Context, userID uint, req *request.TodoListUpdate) (*response.TodoList, error) {
	list, err := s.getTodoListOwned(ctx, userID, req.ID)
	if err != nil {
		return nil, err
	}

	ensureAuditable(&list.Auditable)
	list.Name = req.Name
	list.UpdatedByID = &userID

	updated, err := s.todoListRepo.Update(ctx, list)
	if err != nil {
		return nil, errs.NewInternalServerErr(err, "Failed to update todo list")
	}
	return toTodoListResponse(updated), nil
}

func (s *TodoListService) DeleteTodoList(ctx context.Context, userID uint, req *request.TodoListDelete) error {
	return s.txMgr.WithTx(ctx, func(txCtx context.Context) error {
		if _, err := s.getTodoListOwned(txCtx, userID, req.ID); err != nil {
			return err
		}

		if err := s.todoRepo.DeleteByBelongedListID(txCtx, req.ID); err != nil {
			return errs.NewInternalServerErr(err, "Failed to delete todos by list id")
		}
		if err := s.todoListRepo.Delete(txCtx, req.ID); err != nil {
			return errs.NewInternalServerErr(err, "Failed to delete todo list")
		}
		return nil
	})
}

func toTodoListResponse(list *entity.TodoList) *response.TodoList {
	var updatedAt *time.Time
	if list.UpdatedByID != nil && !list.UpdatedAt.IsZero() {
		updatedAt = &list.UpdatedAt
	}
	return &response.TodoList{
		ID:        list.ID,
		Name:      list.Name,
		Inbox:     list.Inbox,
		CreatedBy: list.CreatedByID,
		CreatedAt: list.CreatedAt,
		UpdatedBy: list.UpdatedByID,
		UpdatedAt: updatedAt,
	}
}

func (s *TodoListService) getTodoListOwned(ctx context.Context, userID uint, listID uint) (*entity.TodoList, error) {
	list, err := s.todoListRepo.GetByID(ctx, listID)
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, errs.NewBusinessErr(respcode.TodoListNotFound)
	} else if err != nil {
		return nil, errs.NewInternalServerErr(err, "Failed to get todo list by id")
	}

	if list.CreatedByID != userID {
		return nil, errs.NewBusinessErr(respcode.PermissionDenied)
	}
	return list, nil
}
