package cmd

import (
	"errors"
	"fmt"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner-helper/cmd/artifactdownload"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner-helper/cmd/artifactupload"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner-helper/cmd/git"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner-helper/cmd/pluginafter"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner-helper/cmd/pluginbefore"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner-helper/cmd/version"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/cmd/run"
	"github.com/spf13/cobra"
	"os"
)

func NewDefaultCiRunnerHelperCommand() *cobra.Command {
	cmds := &cobra.Command{
		Use:   "ci-runner-helper",
		Short: "ci-runner-helper",
		Run: func(cmd *cobra.Command, args []string) {
			fmt.Fprintf(os.Stderr, "execute %s args:%v error:%v\n", cmd.Name(), args, errors.New("unrecognized command"))
			os.Exit(1)
		},
	}

	// 添加一个子命令就使用 cmds.AddCommand
	cmds.AddCommand(version.NewCmdVersion())
	cmds.AddCommand(run.NewCmdRun())
	cmds.AddCommand(artifactupload.NewCmdArtifactUpload())
	cmds.AddCommand(artifactdownload.NewCmdArtifactDownload())
	cmds.AddCommand(pluginafter.NewCmdPluginAfter())
	cmds.AddCommand(git.NewCmdGit())
	cmds.AddCommand(pluginbefore.NewCmdPluginBefore())
	return cmds
}
