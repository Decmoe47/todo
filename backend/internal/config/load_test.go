package config

import (
	"os"
	"path/filepath"
	"testing"

	"github.com/stretchr/testify/require"
)

func TestLoad(t *testing.T) {
	dir := t.TempDir()
	path := filepath.Join(dir, "config.yaml")
	yaml := []byte("logging:\n  level: info\nredis:\n  host: localhost\n  port: 6379\n  password: ''\nsecurity:\n  access_token_ttl: 1\n  refresh_token_ttl: 2\n  secret: s\n")
	require.NoError(t, os.WriteFile(path, yaml, 0644))

	cfg, err := Load(path)
	require.NoError(t, err)
	require.Equal(t, "info", cfg.Logging.Level)
	require.Equal(t, "localhost", cfg.Redis.Host)
	require.Equal(t, 6379, cfg.Redis.Port)
	require.Equal(t, "s", cfg.Security.Secret)
}

func TestLoadError(t *testing.T) {
	_, err := Load("no-such-file.yaml")
	require.Error(t, err)
}
