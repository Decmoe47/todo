package util

// MapSlice maps a slice using mapper and preserves order.
func MapSlice[T any, R any](items []T, mapper func(T) R) []R {
	if len(items) == 0 {
		return []R{}
	}
	out := make([]R, 0, len(items))
	for _, item := range items {
		out = append(out, mapper(item))
	}
	return out
}
