package executors

import (
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/app/common"
)

type DefaultExecutorProvider struct {
	Creator func() common.Executor
}

func (e DefaultExecutorProvider) CanCreate() bool {
	return e.Creator != nil
}

func (e DefaultExecutorProvider) Create() common.Executor {
	if e.Creator == nil {
		return nil
	}
	return e.Creator()
}
