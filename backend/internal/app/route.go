package app

import (
	"todo/internal/handler"
	"todo/internal/service"

	"github.com/gin-gonic/gin"
)

func registerAuth(api *gin.RouterGroup, authSvc *service.AuthService) {
	handler := handler.NewAuthHandler(authSvc)

	api.POST("/login", handler.Login)
	api.POST("/register", handler.Register)
	api.POST("/refresh-token", handler.RefreshToken)
	api.POST("/logout", handler.Logout)
	api.POST("/send-verify-code", handler.SendVerifyCode)
}

func registerUser(api *gin.RouterGroup, userSvc *service.UserService) {
	handler := handler.NewUserHandler(userSvc)

	api.GET("/:userId", handler.GetUser)
	api.POST("/by-token", handler.GetUserByToken)
	api.POST("/search", handler.SearchUser)
	api.POST("/:userId/update", handler.UpdateUser)
}

func registerTodo(api *gin.RouterGroup, todoSvc *service.TodoService) {
	handler := handler.NewTodoHandler(todoSvc)

	api.GET("/todos", handler.GetTodos)
	api.POST("/todos/add", handler.AddTodo)
	api.POST("/todos/delete", handler.DeleteTodo)
	api.POST("/todos/update", handler.UpdateTodo)
	api.POST("/todos/toggle", handler.ToggleTodo)
	api.POST("/todos/move", handler.MoveTodo)
}

func registerTodoList(api *gin.RouterGroup, todoListSvc *service.TodoListService) {
	handler := handler.NewTodoListHandler(todoListSvc)

	api.GET("/todoLists/custom", handler.GetCustomTodoLists)
	api.POST("/todoLists/add", handler.AddTodoList)
	api.POST("/todoLists/update", handler.UpdateTodoList)
	api.POST("/todoLists/delete", handler.DeleteTodoList)
}
