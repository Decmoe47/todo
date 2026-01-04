package response

import "time"

type TodoList struct {
	ID    uint   `json:"id"`
	Name  string `json:"name"`
	Inbox bool   `json:"inbox"`

	CreatedBy uint       `json:"createdBy"`
	CreatedAt time.Time  `json:"createdAt"`
	UpdatedBy *uint      `json:"updatedBy"`
	UpdatedAt *time.Time `json:"updatedAt"`
}
