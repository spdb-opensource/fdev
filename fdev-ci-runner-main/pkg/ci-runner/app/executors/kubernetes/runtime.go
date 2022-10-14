package kubernetes

import (
	"bytes"
	"errors"
	"fmt"
	"strings"
	"time"

	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner-helper/cmd/artifactdownload"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner-helper/cmd/artifactupload"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner-helper/cmd/pluginafter"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner-helper/cmd/pluginbefore"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/app/common"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/app/network"
	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/ansi"
	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/command"
	"github.com/sirupsen/logrus"
)

type Runtime struct {
	e *Executor
}

const (
	buildContainerName  = "build"
	helperContainerName = "helper"
)

// pod 运行时的状态机
type PodRuntimeState string

const (
	PodRunStatePending      PodRuntimeState = "pending"
	PodRunRuntimeRunning    PodRuntimeState = "running"
	PodRunRuntimeFinished   PodRuntimeState = "finished"
	PodRunRuntimeCanceled   PodRuntimeState = "canceled"
	PodRunRuntimeTerminated PodRuntimeState = "terminated"
	PodRunRuntimeTimedout   PodRuntimeState = "timedout"
)

func (r *Runtime) executePluginStage(pluginStage common.PluginStage, pluginData *common.PluginResponse, i int) error {

	var err error
	var script string
	var container string
	e := r.e
	switch pluginStage {

	//  插件执行前，调用ci-runner-helper plugin-before命令
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
		script = fmt.Sprintf("ci-runner-helper plugin-before %s ", pArgs)
		container = helperContainerName

	//构建物下载，调用ci-runner-helper artifact-download命令
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
		script = fmt.Sprintf("ci-runner-helper artifact-download %s ", pArgs)
		container = helperContainerName

	// 构建执行脚本
	case common.PluginStageBuildScript:
		sb := bytes.Buffer{}
		sb.WriteString("#!/usr/bin/env bash\n")
		sb.WriteString("set -eo pipefail\n")
		sb.WriteString("set +o noclobber\n")
		for _, b := range pluginData.PluginInfo.Variables {
			if len(b.Value) == 0 {
				continue
			}
			if strings.Contains(b.Value, "$") {
				sb.WriteString(fmt.Sprintf("export %s=%s \n", b.Key, b.Value))
			} else {
				sb.WriteString(fmt.Sprintf("export %s='%s' \n", b.Key, b.Value))
			}
			if strings.Contains(b.Key, "_HOME") {
				sb.WriteString(fmt.Sprintf("export PATH=%s/bin:$PATH \n", b.Value))
			}
		}
		// 根据plugin目录规则
		ciDataDir := fmt.Sprintf("%s/stage-%d/job-%d/plugin-%d",
			e.job.JobInfo.Variables.Get("CI_WORKSPACE"), e.job.JobInfo.StageIndex, e.job.JobInfo.JobIndex, i)
		sb.WriteString(fmt.Sprintf("export ci_data_dir=%s \n", ciDataDir))
		sb.WriteString("export ci_data_input=input.json\n")
		sb.WriteString("export ci_data_output=output.json\n")
		sb.WriteString(fmt.Sprintf("cd %s \n", ciDataDir))
		for _, b := range pluginData.PluginInfo.Execution.Demands {
			sb.WriteString(b + "\n")
		}
		sb.WriteString(pluginData.PluginInfo.Execution.Target)

		script = sb.String()
		container = buildContainerName

	// 构建上传
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
		script = fmt.Sprintf("ci-runner-helper artifact-upload %s %s ", pArgs, artifacts)
		container = helperContainerName

	// 插件运行后
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
		script = fmt.Sprintf("ci-runner-helper plugin-after %s ", pArgs)
		container = helperContainerName

	}

	logrus.Debugln(fmt.Sprintf(
		"Starting in container %q State:%s Script: %s",
		e.podName,
		pluginStage,
		script,
	))
	if script != "" && container != "" {
		//在容器中执行脚本
		err = <-e.runInContainer(container, script)
	}

	return err
}

func (r *Runtime) executeJobStage(jobStage common.JobStage) error {
	var err error
	switch jobStage {
	case common.JobStageBeforeScript:
		// 为以后留 before 钩子
		break
	case common.JobStageGetSources:
		err = <-r.e.runInContainer(helperContainerName, "ci-runner-helper git")

	case common.JobStageAfterScript:
		// 为以后留 after 钩子
		break
	}
	if err != nil && strings.Contains(err.Error(), "command terminated with exit code") {
		return &common.BuildError{Inner: err}
	}
	return err
}

// 运行阶段，主要逻辑在这里面。会在运行时依次下载插件并执行
func (r *Runtime) executeScript() error {
	err := r.executeJobStage(common.JobStageBeforeScript)
	if err == nil {
		err = r.executeJobStage(common.JobStageGetSources)
	}
	if err == nil {
		apiclient := network.GetCiAPIClient()
		for i := 0; i < r.e.job.JobInfo.Plugins.Count; i++ {
			//todo  临时方案睡眠给后台充分时间处理拼接plugin 环境变量,后续考虑 kafaka 事件触发
			time.Sleep(3 * time.Second)
			if err != nil {
				break
			}
			pluginData, healthy := apiclient.RequestPlugin(r.e.job.JobInfo.Pipeline, r.e.job.JobInfo.StageIndex, r.e.job.JobInfo.JobIndex, i, r.e.job.Token)
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
