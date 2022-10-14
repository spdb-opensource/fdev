package network

import (
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/app/common"
	"github.com/stretchr/testify/require"
	"testing"
	"time"
)

var (
	jobConfig = common.RunnerConfig{
		RunnerCredentials: common.RunnerCredentials{
			URL:   "http://localhost:8080",
			Token: "xxyyzz",
		},
	}
	jobCredentials = &common.JobCredentials{ID: "1"}
	//jobOutputLimit = common.RunnerConfig{OutputLimit: 1}
)

func TestIgnoreStatusChange(t *testing.T) {

	client := GetCiAPIClient()

	b, err := newJobTrace(client, jobConfig, jobCredentials)
	require.NoError(t, err)

	b.start()
	time.Sleep(1000 * time.Second)
	//b.Success()
	//b.Fail(errors.New("test"), "script_failure")
}
