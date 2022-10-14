package cmd

import (
	"errors"
	"fmt"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner-helper/cmd/artifactdownload"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner-helper/cmd/artifactupload"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner-helper/cmd/git"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner-helper/cmd/pluginafter"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner-helper/cmd/pluginbefore"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/cmd/run"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/cmd/version"
	"github.com/spf13/cobra"
	"os"
)

func NewDefaultCiRunnerCommand() *cobra.Command {
	cmds := &cobra.Command{
		Use:   "ci-runner",
		Short: "ci-runner",
		Run: func(cmd *cobra.Command, args []string) {
			fmt.Fprintf(os.Stderr, "execute %s args:%v error:%v\n", cmd.Name(), args, errors.New("unrecognized command"))
			os.Exit(1)
		},
	}

	cmds.AddCommand(version.NewCmdVersion())
	cmds.AddCommand(run.NewCmdRun())
	cmds.AddCommand(artifactupload.NewCmdArtifactUpload())
	cmds.AddCommand(artifactdownload.NewCmdArtifactDownload())
	cmds.AddCommand(pluginafter.NewCmdPluginAfter())
	cmds.AddCommand(git.NewCmdGit())
	cmds.AddCommand(pluginbefore.NewCmdPluginBefore())
	return cmds
}
