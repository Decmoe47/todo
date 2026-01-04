package service

import (
	"fmt"
	"net/smtp"
	"todo/internal/config"
	"todo/internal/errs"

	"github.com/jordan-wright/email"
)

type MailService struct {
	mailCfg *config.Mail
	addr    string
}

func NewMail(mailCfg *config.Mail) *MailService {
	return &MailService{
		mailCfg: mailCfg,
		addr:    fmt.Sprintf("%s:%d", mailCfg.Host, mailCfg.Port),
	}
}

func (s *MailService) Send(to []string, content string) error {
	e := email.NewEmail()
	e.From = s.mailCfg.From
	e.To = to
	e.Subject = "Hello Go"
	e.Bcc = []string{s.mailCfg.From}
	e.Text = []byte(content)
	err := e.Send(s.addr, smtp.PlainAuth("", s.mailCfg.Username, s.mailCfg.Password, s.mailCfg.Host))
	if err != nil {
		return errs.NewInternalServerErr(err, "Failed to send email")
	}
	return nil
}
