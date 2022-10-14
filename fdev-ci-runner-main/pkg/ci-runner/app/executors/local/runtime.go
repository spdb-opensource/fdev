package local

import (
	"bytes"
	"errors"
	"fmt"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner-helper/cmd/artifactdownload"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner-helper/cmd/artifactupload"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner-helper/cmd/pluginafter"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner-helper/cmd/pluginbefore"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/app/common"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/app/network"
	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/ansi"
	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/command"
	"github.com/sirupsen/logrus"
	"os"
	"path/filepath"
	"runtime"
	"strings"
	"time"
)

type Runtime struct {
	e   *Executor
	job *common.Job
}

var helper, _ = os.Executable()

func (r *Runtime) executePluginStage(pluginStage common.PluginStage, pluginData *common.PluginResponse, i int) error {
	r.job.PluginStage = pluginStage
	var err error
	var script string
	e := r.e
	switch pluginStage {
	case common.PluginStageBefore:
		runOptions := pluginbefore.RunOptions{
			Pipeline:      e.job.JobInfo.Pipeline,
			StageIndex:    e.job.JobInfo.StageIndex,
			JobIndex:      e.job.JobInfo.JobIndex,
			PluginIndex:   i,
			PluginName:    pluginData.PluginInfo.PluginName,
			PluginMd5:     pluginData.PluginInfo.PluginMd5,
			PluginVersion: pluginData.PluginInfo.PluginVersion,
			PluginAuthor:  pluginData.PluginInfo.PluginAuthor,
			PackagePath:   pluginData.PluginInfo.Execution.PackagePath,
			InputPath:     e.job.Runner.URL,
			URL:           e.job.Runner.MinioConfig.URL,
			PluginsBucket: e.job.Runner.MinioConfig.PluginsBucket,
			Token:         network.Client.Token,
		}
		pArgs, err := command.ToPArgs(runOptions)
		if err != nil {
			return err
		}
		script = fmt.Sprintf("%s plugin-before %s ", helper, pArgs)
	case common.PluginStageArtifactDownload:
		runOptions := artifactdownload.RunOptions{
			Pipeline:    e.job.JobInfo.Pipeline,
			StageIndex:  e.job.JobInfo.StageIndex,
			JobIndex:    e.job.JobInfo.JobIndex,
			PluginIndex: i,
			FdevURL:     e.job.Runner.URL,
			Token:       network.Client.Token,
		}
		pArgs, err := command.ToPArgs(runOptions)
		if err != nil {
			return err
		}
		script = fmt.Sprintf("%s artifact-download %s ", helper, pArgs)
	case common.PluginStageBuildScript:
		sb := bytes.Buffer{}
		// 根据plugin目录规则
		CiWorkspace := r.job.JobInfo.Variables.Get("CI_WORKSPACE")
		ciDataDirSub := fmt.Sprintf("/stage-%d/job-%d/plugin-%d", r.job.JobInfo.StageIndex, r.job.JobInfo.JobIndex, i)
		ciDataDir := filepath.Join(CiWorkspace, ciDataDirSub)
		if runtime.GOOS == "windows" {
			for _, b := range pluginData.PluginInfo.Variables {
				if len(b.Value) == 0 {
					continue
				}
				sb.WriteString(fmt.Sprintf("set %s=%s\r\n", b.Key, b.Value))
			}
			sb.WriteString(fmt.Sprintf("set ci_data_dir=%s\r\n", ciDataDir))
			sb.WriteString("set ci_data_input=input.json\r\n")
			sb.WriteString("set ci_data_output=output.json\r\n")
			sb.WriteString(fmt.Sprintf("cd /d %s \r\n", ciDataDir))
		} else {
			sb.WriteString("#!/usr/bin/env bash\n")
			sb.WriteString("set -eo pipefail\n")
			sb.WriteString("set +o noclobber\n")
			for _, b := range pluginData.PluginInfo.Variables {
				if len(b.Value) == 0 {
					continue
				}
				if strings.Contains(b.Value, "$") {
					sb.WriteString(fmt.Sprintf("export %s=%s\n", b.Key, b.Value))
				} else {
					sb.WriteString(fmt.Sprintf("export %s='%s'\n", b.Key, b.Value))
				}
			}
			//todo 需特殊处理
			sb.WriteString("export PATH=$JAVA_HOME/bin:$PATH\n")
			sb.WriteString("export PATH=$MAVEN_HOME/bin:$PATH\n")
			sb.WriteString(fmt.Sprintf("export ci_data_dir=%s\n", ciDataDir))
			sb.WriteString("export ci_data_input=input.json\n")
			sb.WriteString("export ci_data_output=output.json\n")
			sb.WriteString(fmt.Sprintf("cd %s \n", ciDataDir))
		}
		for _, b := range pluginData.PluginInfo.Execution.Demands {
			if runtime.GOOS == "windows" {
				sb.WriteString(b + "\r\n")
				sb.WriteString("\r\nIF %errorlevel% NEQ 0 exit /b %errorlevel%\r\n")
			} else {
				sb.WriteString(b + "\n")
			}
		}
		sb.WriteString(pluginData.PluginInfo.Execution.Target)
		script = sb.String()
	case common.PluginStageArtifactUpload:
		if len(pluginData.PluginInfo.Execution.Artifacts) == 0 {
			break
		}
		artifacts := ""
		for _, artifactName := range pluginData.PluginInfo.Execution.Artifacts {
			artifacts = artifacts + artifactName + " "
		}
		runOptions := artifactupload.RunOptions{
			Pipeline:            e.job.JobInfo.Pipeline,
			StageIndex:          e.job.JobInfo.StageIndex,
			JobIndex:            e.job.JobInfo.JobIndex,
			PluginIndex:         i,
			FdevURL:             e.job.Runner.URL,
			Path:                e.job.Runner.ArtifactPath,
			Token:               network.Client.Token,
			ArtifactUploadLimit: e.job.Runner.ArtifactUploadLimit,
		}
		pArgs, err := command.ToPArgs(runOptions)
		if err != nil {
			return err
		}
		script = fmt.Sprintf("%s artifact-upload %s %s ", helper, pArgs, artifacts)
	case common.PluginStageAfter:
		runOptions := pluginafter.RunOptions{
			Pipeline:         e.job.JobInfo.Pipeline,
			StageIndex:       e.job.JobInfo.StageIndex,
			JobIndex:         e.job.JobInfo.JobIndex,
			PluginIndex:      i,
			PluginOutputPath: e.job.Runner.URL,
			Token:            network.Client.Token,
		}
		pArgs, err := command.ToPArgs(runOptions)
		if err != nil {
			return err
		}
		script = fmt.Sprintf("%s plugin-after %s ", helper, pArgs)
	}
	if script != "" {
		err = <-r.e.RunInLocal(script)
	}
	return err
}

const GetSourcesBat = "echo " + ansi.FdevGreen + "Getting source from Git repository" + ansi.FdevRESET + "\r\n" +
	"git init %[1]s\r\n" +
	"IF %%errorlevel%% NEQ 0 exit /b %%errorlevel%%\r\n" +
	"\r\n" +
	"cd /d %[1]s\r\n" +
	"IF %%errorlevel%% NEQ 0 exit /b %%errorlevel%%\r\n" +
	"\r\n" +
	"git remote add origin %[2]s 2>NUL 1>NUL\r\n" +
	"IF %%errorlevel%% EQU 0 (\r\n" +
	" echo " + ansi.FdevGreen + "Created fresh repository." + ansi.FdevRESET + "\r\n" +
	") ELSE (\r\n" +
	" git remote set-url origin %[2]s\r\n" +
	" IF %%errorlevel%% NEQ 0 exit /b %%errorlevel%%\r\n" +
	")\r\n" +
	"git fetch origin\r\n" +
	"IF %%errorlevel%% NEQ 0 exit /b %%errorlevel%%\r\n" +
	"\r\n" +
	"echo " + ansi.FdevGreen + "Checking out origin/%[3]s as %[3]s..." + ansi.FdevRESET + "\r\n" +
	"git checkout -B %[3]s origin/%[3]s\r\n" +
	"IF %%errorlevel%% NEQ 0 exit /b %%errorlevel%%\r\n"

func (r *Runtime) executeJobStage(stage common.JobStage) error {
	r.job.CurrentStage = stage
	var err error
	switch stage {
	case common.JobStageBeforeScript:
		// 为以后留 before 钩子
		break
	case common.JobStageGetSources:
		err = <-r.e.RunInLocal(fmt.Sprintf("%s git ", helper))
	case common.JobStageAfterScript:
		// 为以后留 after 钩子
		break
	}
	if err != nil && strings.Contains(err.Error(), "command terminated with exit code") {
		return &common.BuildError{Inner: err}
	}
	return err
}

func (r *Runtime) ExecuteScript() error {
	err := r.executeJobStage(common.JobStageBeforeScript)
	if err == nil {
		err = r.executeJobStage(common.JobStageGetSources)
	}
	if err == nil {
		apiclient := network.GetCiAPIClient()
		for i := 0; i < r.job.JobInfo.Plugins.Count; i++ {
			//todo  临时方案睡眠给后台充分时间处理拼接plugin 环境变量,后续考虑 kafaka 事件触发
			time.Sleep(3 * time.Second)
			if err != nil {
				break
			}
			pluginData, healthy := apiclient.RequestPlugin(r.job.JobInfo.Pipeline, r.job.JobInfo.StageIndex, r.job.JobInfo.JobIndex, i, r.job.Token)
			if !healthy {
				logrus.Errorln("RequestPlugin is not healthy !")
				err = errors.New("RequestPlugin is not healthy")
				break
			}
			logrus.WithFields(logrus.Fields{
				"pluginData": "pluginData",
			}).Debugln(pluginData)
			if pluginData == nil {
				break
			}
			if err == nil {
				err = r.executePluginStage(common.PluginStageBefore, pluginData, i)
			}
			if err == nil {
				err = r.executePluginStage(common.PluginStageArtifactDownload, pluginData, i)
			}
			if err == nil {
				err = r.executePluginStage(common.PluginStageBuildScript, pluginData, i)
			}
			if err == nil {
				err = r.executePluginStage(common.PluginStageArtifactUpload, pluginData, i)
			}
			if err == nil {
				err = r.executePluginStage(common.PluginStageAfter, pluginData, i)
			}
		}
	}
	if err == nil {
		_, _ = r.e.Trace.Write([]byte(ansi.FdevGreen + "Job succeeded" + ansi.FdevRESET))
		err = r.executeJobStage(common.JobStageAfterScript)
	}
	return err

}
