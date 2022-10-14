package artifactdownload

import (
	"fmt"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/app/network"
	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/ansi"
	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/command"
	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/file"
	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/log"
	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/zip"
	"github.com/spf13/cobra"
	"os"
	"path/filepath"
)

type Artifacts struct {
	Artifact []Artifact `json:"artifacts"`
}
type Artifact struct {
	Pipeline    string `json:"pipeline"`
	StageIndex  int    `json:"stage_index"`
	JobIndex    int    `json:"job_index"`
	PluginIndex int    `json:"plugin_index"`
	ObjectName  string `json:"object_name"`
}

type RunOptions struct {
	Pipeline    string
	StageIndex  int
	JobIndex    int
	PluginIndex int
	FdevURL     string
	Token       string
}

var logger = log.NewLogger()

func NewCmdArtifactDownload() *cobra.Command {
	opt := &RunOptions{}

	cmd := &cobra.Command{
		Use:   "artifact-download",
		Short: "artifact download",
		Run: func(cmd *cobra.Command, args []string) {
			network.Client.Init(opt.FdevURL, "").SetToken(opt.Token)
			var artifacts Artifacts
			request := map[string]string{"pipeline": opt.Pipeline}
			err := network.Client.RequestArtifact(request, &artifacts)
			if err != nil {
				logger.Errorln("PostArtifacts with err:", err.Error())
				os.Exit(1)
			}
			if len(artifacts.Artifact) == 0 {
				os.Exit(0)
			}
			fmt.Println(ansi.FdevGreen + "Downloading artifacts..." + ansi.FdevRESET)
			ciWorkspace := os.Getenv("CI_WORKSPACE")

			for _, artifact := range artifacts.Artifact {
				// 拼接四级目录
				ciDataDirSub := fmt.Sprintf("stage-%d/job-%d/plugin-%d", artifact.StageIndex, artifact.JobIndex, artifact.PluginIndex)
				ciDataDir := filepath.Join(ciWorkspace, ciDataDirSub)
				artifactZip := filepath.Join(ciDataDir, "artifacts.zip")

				if !file.Exists(artifactZip) {
					err = network.Client.DownloadArtifacts(artifact.ObjectName, artifactZip)
					if err != nil {
						logger.Errorln("download artifacts with err:", err.Error())
						os.Exit(1)
					}
					fmt.Printf("Downloading artifacts from coordinator... ok        objectName=%s\n", artifact.ObjectName)
				} else {
					fmt.Println("artifacts exists...")
				}
				// 解压
				ciProjectDir := os.Getenv("CI_PROJECT_DIR")
				err = zip.DeCompress(artifactZip, ciProjectDir)
				if err != nil {
					logger.Errorln("zip decompress with err:", err.Error())
					os.Exit(1)
				}
			}
		},
	}

	addRunFlags(cmd, opt)
	return cmd
}
func addRunFlags(cmd *cobra.Command, opt *RunOptions) {
	err := command.ToPFlags(opt, cmd.Flags())
	if err != nil {
		panic(err)
	}
}
