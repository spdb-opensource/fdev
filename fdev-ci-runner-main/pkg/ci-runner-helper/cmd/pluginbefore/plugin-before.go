package pluginbefore

import (
	"fmt"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/app/network"
	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/command"
	file_helper "github.com/fdev-ci/fdev-ci-runner/pkg/utlis/file"
	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/log"
	"github.com/spf13/cobra"
	"os"
	"path/filepath"
)

type PluginJSONInput struct {
	Pipeline    string `json:"pipeline"`
	StageIndex  int    `json:"stage_index"`
	JobIndex    int    `json:"job_index"`
	PluginIndex int    `json:"plugin_index"`
}

type RunOptions struct {
	Pipeline      string
	StageIndex    int
	JobIndex      int
	PluginIndex   int
	PluginName    string
	PluginMd5     string
	PluginVersion string
	PluginAuthor  string
	PackagePath   string
	InputPath     string
	URL           string
	PluginsBucket string
	Token         string
}

var logger = log.NewLogger()

func NewCmdPluginBefore() *cobra.Command {
	opt := &RunOptions{}

	cmd := &cobra.Command{
		Use:   "plugin-before",
		Short: "plugin before",
		Run: func(cmd *cobra.Command, args []string) {
			network.Client.Init(opt.InputPath, "").SetToken(opt.Token)
			pluginJSONInput := &PluginJSONInput{
				Pipeline:    opt.Pipeline,
				StageIndex:  opt.StageIndex,
				JobIndex:    opt.JobIndex,
				PluginIndex: opt.PluginIndex,
			}
			ciWorkspace := os.Getenv("CI_WORKSPACE")
			// 拼接四级目录
			ciDataSubDir := fmt.Sprintf("/stage-%d/job-%d/plugin-%d", opt.StageIndex, opt.JobIndex, opt.PluginIndex)
			ciDataDir := filepath.Join(ciWorkspace, ciDataSubDir)
			if !file_helper.Exists(ciDataDir) {
				// 创建目录
				err := os.MkdirAll(ciDataDir, os.ModePerm)
				if err != nil {
					logger.Errorln("Error creating directory", err.Error())
					os.Exit(1)
				}
			}
			pluginCache := filepath.Join(os.Getenv("CI_PLUGIN_CACHE"), opt.PluginsBucket, opt.PackagePath)
			//client, err := minio.New(opt.Path, &minio.Options{Creds: credentials.NewStaticV4(opt.AccessKey, opt.SecretKey, "")})
			//if err != nil {
			//	logger.Errorln("create minio client failed", err.Error())
			//	os.Exit(1)
			//}
			if !file_helper.Exists(pluginCache) {
				logger.Infoln("downloading plugin...")
				err := network.Client.DownloadMinio(opt.PluginsBucket, opt.PackagePath, pluginCache)
				//err := client.FGetObject(context.Background(), opt.PluginsBucket, opt.PackagePath, pluginCache, minio.GetObjectOptions{})
				if err != nil {
					logger.Errorln("downloading plugin failed", err.Error())
					os.Exit(1)
				}
			} else {
				//info, err := network.Client.GetPluginMd5(opt.PluginsBucket, opt.PackagePath)
				//info, err := client.StatObject(context.Background(), opt.PluginsBucket, opt.PackagePath, minio.StatObjectOptions{})
				//if err != nil {
				//	logger.Errorln("get object stat failed", err.Error())
				//	os.Exit(1)
				//}
				md5, err := file_helper.GetMd5(pluginCache)
				if err != nil {
					logger.Errorln("check plugin md5 failed", err.Error())
					os.Exit(1)
				}
				if opt.PluginMd5 != md5 {
					logger.Infoln("downloading plugin...")
					err := network.Client.DownloadMinio(opt.PluginsBucket, opt.PackagePath, pluginCache)
					//err := client.FGetObject(context.Background(), opt.PluginsBucket, opt.PackagePath, pluginCache, minio.GetObjectOptions{})
					if err != nil {
						logger.Errorln("downloading plugin failed", err.Error())
						os.Exit(1)
					}
				} else {
					logger.Infoln("plugin cache hit...")
				}
			}
			_, err := file_helper.CopyFile(pluginCache, filepath.Join(ciDataDir, filepath.Base(pluginCache)))
			if err != nil {
				logger.Errorln("copy plugin from cache with error", err.Error())
				os.Exit(1)
			}
			// 下载插件 input json
			savefile := filepath.Join(ciDataDir, "input.json")
			err = network.Client.DownloadInput(pluginJSONInput, savefile)
			if err != nil {
				logger.Errorln("download input.json with error", err.Error())
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
