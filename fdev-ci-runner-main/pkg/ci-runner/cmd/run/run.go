package run

import (
	"fmt"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/app/run"
	"github.com/fdev-ci/fdev-ci-runner/pkg/version"
	"github.com/spf13/cobra"
)

type Options struct {
	ConfigFile string
}

func NewCmdRun() *cobra.Command {
	o := &Options{}

	cmd := &cobra.Command{
		Use:   "run",
		Short: "run ci-runner",
		Run: func(cmd *cobra.Command, args []string) {
			fmt.Println("ci-runner", version.Get())
			run.Run(o.ConfigFile)
		},
	}
	addRunFlags(cmd, o)
	return cmd
}
func addRunFlags(cmd *cobra.Command, opt *Options) {
	cmd.Flags().StringVarP(&opt.ConfigFile, "config", "c", "/etc/ci-runner/config.toml", "config file ")
}
