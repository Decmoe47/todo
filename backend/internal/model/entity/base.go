package entity

import "gorm.io/gorm"

type Auditable struct {
	*gorm.Model
	CreatedByID uint  `gorm:"column:created_by"`
	UpdatedByID *uint `gorm:"column:updated_by"`

	CreatedBy *User `gorm:"foreignKey:CreatedByID"`
	UpdatedBy *User `gorm:"foreignKey:UpdatedByID"`
}
