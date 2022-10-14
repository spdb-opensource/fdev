package main

import (
	"fmt"
	_ "github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/app/executors/kubernetes"
	_ "github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/app/executors/local"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/cmd"
	"os"
)

func main() {
	command := cmd.NewDefaultCiRunnerCommand()
	if err := command.Execute(); err != nil {
		fmt.Fprintf(os.Stderr, "%v\n", err)
		os.Exit(1)
	}
}
