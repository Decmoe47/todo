package entity

import (
	"time"

	"gorm.io/gorm"
)

type User struct {
	*Auditable
	Email         string
	Password      string
	Name          string
	LastLoginTime *time.Time
}

func NewUser(email, password, name string) *User {
	return &User{
		Auditable: &Auditable{Model: &gorm.Model{}},
		Email:     email,
		Password:  password,
		Name:      name,
	}
}

func NewUserWithIdAndEmail(id uint, email string) *User {
	return &User{
		Auditable: &Auditable{Model: &gorm.Model{ID: id}},
		Email:     email,
	}
}
