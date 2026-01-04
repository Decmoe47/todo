package main

import (
	"os"
	"path/filepath"
	"strings"
	"testing"

	"github.com/alicebob/miniredis/v2"
	"github.com/stretchr/testify/require"
)

func TestRunSuccess(t *testing.T) {
	mr, err := miniredis.Run()
	require.NoError(t, err)
	defer mr.Close()

	parts := strings.Split(mr.Addr(), ":")
	require.Len(t, parts, 2)

	tmp := t.TempDir()
	cfgPath := filepath.Join(tmp, "config.yaml")
	cfg := []byte("redis:\n  host: " + parts[0] + "\n  port: " + parts[1] + "\n  password: ''\nsecurity:\n  access_token_ttl: 1\n  refresh_token_ttl: 2\n  secret: s\n")
	require.NoError(t, os.WriteFile(cfgPath, cfg, 0644))

	err = run(cfgPath, func(int) {})
	require.NoError(t, err)
}

func TestRunConfigError(t *testing.T) {
	err := run("no-such-config.yaml", func(int) {})
	require.Error(t, err)
}

func TestRunRedisError(t *testing.T) {
	tmp := t.TempDir()
	cfgPath := filepath.Join(tmp, "config.yaml")
	cfg := []byte("redis:\n  host: 127.0.0.1\n  port: 1\n  password: ''\nsecurity:\n  access_token_ttl: 1\n  refresh_token_ttl: 2\n  secret: s\n")
	require.NoError(t, os.WriteFile(cfgPath, cfg, 0644))

	err := run(cfgPath, func(int) {})
	require.Error(t, err)
}
