package service

import (
	"strings"
	"testing"
	"todo/internal/config"

	"github.com/stretchr/testify/require"
)

func TestMailServiceSend(t *testing.T) {
	addr, stop := startSMTPServer(t)
	defer stop()

	parts := strings.Split(addr, ":")
	mailSvc := NewMail(&config.Mail{Host: parts[0], Port: mustAtoi(t, parts[1]), Username: "u", Password: "p", From: "from@test"})

	require.NoError(t, mailSvc.Send([]string{"to@test"}, "hello"))
}
