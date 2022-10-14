package log

import (
	"fmt"
	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/ansi"
	urlhelpers "github.com/fdev-ci/fdev-ci-runner/pkg/utlis/url"
	"github.com/sirupsen/logrus"
	"os"
	"strings"
	"time"
)

const ColorKey = "color"

type Logger struct {
	*logrus.Logger
}
type Formatter struct{}

func (l *Formatter) Format(entry *logrus.Entry) ([]byte, error) {
	now := time.Now().Format("2006-01-02 15:04:05")
	entry.Message = " " + now + " [" + entry.Level.String() + "] " + entry.Message + " "
	withColor, ok := entry.Data[ColorKey]
	if ok {
		entry.Message = fmt.Sprint(withColor) + entry.Message + ansi.FdevRESET
	} else {
		switch entry.Level {
		case logrus.PanicLevel:
			entry.Message = ansi.FdevRed + entry.Message + ansi.FdevRESET
		case logrus.FatalLevel:
			entry.Message = ansi.FdevRed + entry.Message + ansi.FdevRESET
		case logrus.ErrorLevel:
			entry.Message = ansi.FdevRed + entry.Message + ansi.FdevRESET
		case logrus.WarnLevel:
			entry.Message = ansi.FdevYellow + entry.Message + ansi.FdevRESET
		case logrus.InfoLevel:
		case logrus.DebugLevel:
		case logrus.TraceLevel:
		default:
			break
		}
	}
	for k := range entry.Data {
		if k != ColorKey {
			entry.Message += " " + k + "=" + fmt.Sprint(entry.Data[k])
		}
	}
	if !strings.HasSuffix(entry.Message, "\n") {
		entry.Message += "\n"
	}
	entry.Message = urlhelpers.ScrubSecrets(entry.Message)
	return []byte(entry.Message), nil
}
func (l *Logger) WithGreen() *logrus.Entry {
	return l.WithField(ColorKey, ansi.FdevGreen)
}
func NewLogger() *Logger {
	logger := logrus.New()
	logger.SetOutput(os.Stdout)
	logger.SetFormatter(&Formatter{})
	return &Logger{logger}
}
