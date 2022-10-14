package artifactupload

import (
	"fmt"
	"os"
	"path"
	"path/filepath"
	"time"

	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/app/network"
	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/ansi"
	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/command"
	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/file"
	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/log"
	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/zip"
	"github.com/spf13/cobra"
)

type RunOptions struct {
	Pipeline            string
	StageIndex          int
	JobIndex            int
	PluginIndex         int
	FdevURL             string
	Path                string
	Token               string
	ArtifactUploadLimit int
}

type ArtifactOutput struct {
	Pipeline    string `json:"pipeline"`
	StageIndex  int    `json:"stage_index"`
	JobIndex    int    `json:"job_index"`
	PluginIndex int    `json:"plugin_index"`
	ObjectName  string `json:"object_name"`
}

var logger = log.NewLogger()

/**
* 上一步执行完插件脚本之后，如果插件中配置了保存构建物字段的路径
* 根据传入参数在{CI_PROJECT_DIR}目录中匹配到的文件，
* 将构建物压缩打包到
* {CI_WORKSPACE}/stage-{StageIndex}/job-{JobIndex}/plugin-{PluginIndex}/artifacts.zip
* 通过发送接口上传到流水线交互的后台应用保存
 */
func NewCmdArtifactUpload() *cobra.Command {
	opt := &RunOptions{}

	cmd := &cobra.Command{
		Use:   "artifact-upload",
		Short: "artifact upload",
		Run: func(cmd *cobra.Command, args []string) {
			network.Client.Init(opt.FdevURL, "").SetToken(opt.Token)
			// 项目根路径
			ciProjectDir := os.Getenv("CI_PROJECT_DIR")
			ciWorkspace := os.Getenv("CI_WORKSPACE")
			// 根据plugin目录规则
			ciDataDirSub := fmt.Sprintf("/stage-%d/job-%d/plugin-%d",
				opt.StageIndex, opt.JobIndex, opt.PluginIndex)
			ciDataDir := filepath.Join(ciWorkspace, ciDataDirSub)
			// 如果查到有文件,才开始打包上传
			if len(args) > 0 {
				fmt.Println(ansi.FdevGreen + "Uploading artifacts..." + ansi.FdevRESET)
				artifactZip := fmt.Sprintf("%s/%s", ciDataDir, "artifacts.zip")
				allFiles, err := zip.ArtifactsZip(artifactZip, args, ciProjectDir)
				if len(allFiles) == 0 {
					return
				}
				if err != nil {
					logger.Errorln("zipping files failed..", err.Error())
					os.Exit(1)
				}

				timeStr := time.Now().Format("2006-01-02")
				artifactPath := path.Join(opt.Path, fmt.Sprintf("%s/%s/stage-%d/job-%d/plugin-%d/artifacts.zip",
					timeStr,
					opt.Pipeline,
					opt.StageIndex,
					opt.JobIndex,
					opt.PluginIndex))
				artifactOutput := ArtifactOutput{
					Pipeline:    opt.Pipeline,
					StageIndex:  opt.StageIndex,
					JobIndex:    opt.JobIndex,
					PluginIndex: opt.PluginIndex,
					ObjectName:  artifactPath,
				}
				if opt.ArtifactUploadLimit == 0 || file.GetFileSize(artifactZip) < int64(opt.ArtifactUploadLimit) {
					// 发送请求接口上传构建物打包压缩后的文件
					err = network.Client.UploadArtifacts(artifactPath, artifactZip)
					if err != nil {
						logger.Errorln("upload artifacts with err:", err.Error())
						os.Exit(1)
					}
					// 发送请求接口到后台服务更新记录运行流水线记录表构建物字段相关的信息
					err = network.Client.WebhookArtifacts(artifactOutput)
					if err != nil {
						logger.Errorln("PostJson failed", err.Error())
						os.Exit(1)
					}
					if len(allFiles) != 0 {
						fmt.Printf("Uploading artifacts from coordinator... ok       objectName=%s\n", artifactPath)
					}
				} else {
					// 发送请求接口到后台服务更新运行流水线记录表构建物字段相关的信息
					err = network.Client.WebhookArtifacts(artifactOutput)
					if err != nil {
						logger.Errorln("PostJson failed", err.Error())
						os.Exit(1)
					}
					fmt.Println("skip Uploading artifacts")
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
