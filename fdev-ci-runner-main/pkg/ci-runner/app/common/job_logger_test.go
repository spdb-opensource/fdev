package common

import (
	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/log"
	"github.com/fdev-ci/fdev-ci-runner/pkg/version"
	"testing"
)

func TestLog2(t *testing.T) {
	jobLogger := log.NewLogger()
	jobLogger.Println("&private_token=qwqweqweer", version.Get())
}
