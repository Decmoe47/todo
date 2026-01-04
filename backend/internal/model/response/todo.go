package response

import "time"

type Todo struct {
	ID             uint       `json:"id"`
	Content        string     `json:"content"`
	DueDate        *time.Time `json:"dueDate"`
	Done           bool       `json:"done"`
	Description    *string    `json:"description"`
	BelongedListID uint       `json:"belongedListId"`

	CreatedBy uint       `json:"createdBy"`
	CreatedAt time.Time  `json:"createdAt"`
	UpdatedBy *uint      `json:"updatedBy"`
	UpdatedAt *time.Time `json:"updatedAt"`
}
