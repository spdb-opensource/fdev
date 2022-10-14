package version

import (
	"fmt"
	"github.com/fdev-ci/fdev-ci-runner/pkg/version"
	"github.com/spf13/cobra"
)

func NewCmdVersion() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "version",
		Short: "Print the version of ci-runner-helper",
		Run: func(cmd *cobra.Command, args []string) {
			fmt.Println("ci-runner-helper", version.Get())
		},
	}
	return cmd
}
