package database

import (
	"strings"
	"testing"
	"todo/internal/config"

	"github.com/alicebob/miniredis/v2"
	"github.com/stretchr/testify/require"
)

func TestNewRedisClient(t *testing.T) {
	mr, err := miniredis.Run()
	require.NoError(t, err)
	defer mr.Close()

	host, portStr, _ := strings.Cut(mr.Addr(), ":")
	port := mustAtoi(t, portStr)

	client, err := NewRedisClient(&config.Redis{Host: host, Port: port})
	require.NoError(t, err)
	require.NoError(t, client.Close())
}

func TestNewRedisClientError(t *testing.T) {
	_, err := NewRedisClient(&config.Redis{Host: "127.0.0.1", Port: 1})
	require.Error(t, err)
}

func mustAtoi(t *testing.T, s string) int {
	var n int
	for _, ch := range s {
		n = n*10 + int(ch-'0')
	}
	return n
}
