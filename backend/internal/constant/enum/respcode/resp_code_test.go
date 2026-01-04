package respcode

import (
	"testing"

	"github.com/stretchr/testify/require"
)

func TestRespCodeAccessors(t *testing.T) {
	require.Equal(t, 0, Ok.Code())
	require.Equal(t, "", Ok.Message())

	require.Equal(t, 10000, InternalServerError.Code())
	require.NotEmpty(t, InternalServerError.Message())
}
