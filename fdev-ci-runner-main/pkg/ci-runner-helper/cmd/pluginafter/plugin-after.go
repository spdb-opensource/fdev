package pluginafter

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"os"
	"path/filepath"

	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/app/network"
	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/command"
	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/log"
	"github.com/spf13/cobra"
)

type PluginJSON struct {
	Pipeline     string       `json:"pipeline"`
	StageIndex   int          `json:"stage_index"`
	JobIndex     int          `json:"job_index"`
	PluginIndex  int          `json:"plugin_index"`
	PluginOutput PluginOutput `json:"plugin_output"`
}
type PluginOutput struct {
	Status    string                 `json:"status"`
	Message   string                 `json:"message"`
	ErrorCode int                    `json:"errorCode"`
	Type      string                 `json:"type"`
	Data      map[string]interface{} `json:"data"`
}

type RunOptions struct {
	Pipeline         string
	StageIndex       int
	JobIndex         int
	PluginIndex      int
	PluginOutputPath string
	Token            string
}

var logger = log.NewLogger()

/**
* 上一步插件执行的输出文件中的JSON数据
* {CI_WORKSPACE}/stage-{StageIndex}/job-{JobIndex}/plugin-{PluginIndex}/output.json
* 发送请求上传,保存插件运行输出数据到流水线记录表中
 */
func NewCmdPluginAfter() *cobra.Command {
	opt := &RunOptions{}

	cmd := &cobra.Command{
		Use:   "plugin-after",
		Short: "plugin-after",
		Run: func(cmd *cobra.Command, args []string) {
			ciWorkspace := os.Getenv("CI_WORKSPACE")
			// 拼接四级目录
			outputFileSub := fmt.Sprintf("/stage-%d/job-%d/plugin-%d/output.json",
				opt.StageIndex, opt.JobIndex, opt.PluginIndex)
			outputFile := filepath.Join(ciWorkspace, outputFileSub)
			pluginJSON := &PluginJSON{
				Pipeline:    opt.Pipeline,
				StageIndex:  opt.StageIndex,
				JobIndex:    opt.JobIndex,
				PluginIndex: opt.PluginIndex,
			}

			var pluginOutput PluginOutput
			data, err := ioutil.ReadFile(outputFile)
			if err != nil {
				logger.Errorln("read output err:", err.Error())
				os.Exit(1)
			}
			err = json.Unmarshal(data, &pluginOutput)
			if err != nil {
				logger.Errorln("json Unmarshal err:", err.Error())
				os.Exit(1)
			}
			pluginJSON.PluginOutput = pluginOutput

			network.Client.Init(opt.PluginOutputPath, "").SetToken(opt.Token)
			err = network.Client.UploadOutput(pluginJSON)
			if err != nil {
				logger.Errorln("PostJson failed", err.Error())
				os.Exit(1)
			}
			if pluginOutput.Status != "success" {
				logger.Errorf("plugin exit with code:%d,msg:%s", pluginOutput.ErrorCode, pluginOutput.Message)
				os.Exit(1)
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
