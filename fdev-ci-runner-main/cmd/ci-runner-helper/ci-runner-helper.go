package main

import (
	"fmt"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner-helper/cmd"
	"os"
)

func main() {
	command := cmd.NewDefaultCiRunnerHelperCommand()
	if err := command.Execute(); err != nil {
		fmt.Fprintf(os.Stderr, "%v\n", err)
		os.Exit(1)
	}
}
