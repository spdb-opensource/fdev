package common

import (
	"fmt"
	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/ansi"
	url_helpers "github.com/fdev-ci/fdev-ci-runner/pkg/utlis/url"
	"github.com/sirupsen/logrus"
	"io"
)

type JobLogger struct {
	log   JobTrace
	entry *logrus.Entry
}

func (e *JobLogger) WithFields(fields logrus.Fields) JobLogger {
	return NewBuildLogger(e.log, e.entry.WithFields(fields))
}

func (e *JobLogger) SendRawLog(args ...interface{}) {
	if e.log != nil {
		fmt.Fprint(e.log, args...)
	}
}

func (e *JobLogger) sendLog(logger func(args ...interface{}), logPrefix string, args ...interface{}) {
	if e.log != nil {
		logLine := url_helpers.ScrubSecrets(logPrefix + fmt.Sprint(args...))
		e.SendRawLog(logLine)
		e.SendRawLog(ansi.FdevRESET, "\n")

		if e.log.IsStdout() {
			return
		}
	}

	if len(args) == 0 {
		return
	}

	logger(args...)
}

func (e *JobLogger) WriterLevel(level logrus.Level) *io.PipeWriter {
	return e.entry.WriterLevel(level)
}

func (e *JobLogger) Debugln(args ...interface{}) {
	if e.entry == nil {
		return
	}
	e.entry.Debugln(args...)
}

func (e *JobLogger) Println(args ...interface{}) {
	if e.entry == nil {
		return
	}
	e.sendLog(e.entry.Debugln, ansi.FdevWhite, args...)
}

func (e *JobLogger) Infoln(args ...interface{}) {
	if e.entry == nil {
		return
	}
	e.sendLog(e.entry.Println, ansi.FdevGreen, args...)
}

func (e *JobLogger) Warningln(args ...interface{}) {
	if e.entry == nil {
		return
	}
	e.sendLog(e.entry.Warningln, ansi.FdevYellow+"WARNING: ", args...)
}

func (e *JobLogger) SoftErrorln(args ...interface{}) {
	if e.entry == nil {
		return
	}
	e.sendLog(e.entry.Warningln, ansi.FdevRed+"ERROR: ", args...)
}

func (e *JobLogger) Errorln(args ...interface{}) {
	if e.entry == nil {
		return
	}
	e.sendLog(e.entry.Errorln, ansi.FdevRed+"ERROR: ", args...)
}

func NewBuildLogger(log JobTrace, entry *logrus.Entry) JobLogger {
	return JobLogger{
		log:   log,
		entry: entry,
	}
}
