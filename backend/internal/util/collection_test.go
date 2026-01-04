package util

import (
	"testing"

	"github.com/stretchr/testify/require"
)

func TestMapSliceEmpty(t *testing.T) {
	out := MapSlice([]int{}, func(v int) string { return "x" })
	require.Empty(t, out)
}

func TestMapSliceValues(t *testing.T) {
	items := []int{1, 2, 3}
	out := MapSlice(items, func(v int) string { return "v" + string(rune('0'+v)) })
	require.Equal(t, []string{"v1", "v2", "v3"}, out)
}
