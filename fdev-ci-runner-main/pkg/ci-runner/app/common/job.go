package common

import (
	"encoding/json"
	"errors"
	"fmt"
	"time"

	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/ansi"
	"github.com/sirupsen/logrus"
)

type Job struct {
	// 执行器配置
	Runner RunnerConfig
	// 任务执行响应
	JobResponse
	// 日志跟踪对象
	Trace JobTrace
	// 任务名称
	JobName string
	// 任务运行时状态
	CurrentStage JobStage
	// plugin 插件运行时状态
	PluginStage PluginStage
	// 创建时间
	CreatedAt time.Time
}

//执行任务,分为四个阶段：分别是准备(Prepare) -> 执行(Run) -> 等待执行结束(Wait) -> 收尾(Cleanup)
func (j *Job) Run() error {
	var err error
	provider := GetExecutor(j.Runner.Executor)
	if provider == nil {
		err = errors.New("couldn't get provider")
		return err
	}
	executor := provider.Create()

	jsonByte, _ := json.Marshal(&j.JobResponse)
	log := logrus.WithFields(logrus.Fields{
		"job_name": j.JobName,
		"executor": j.Runner.Executor,
		"json":     string(jsonByte),
	})
	log.Info("job start")

	if err == nil {
		err = executor.Prepare(j)
	}
	if err == nil {
		err = executor.Run()
	}
	if err == nil {
		err = executor.Wait()
	} else {
		// 记录执行失败也打印失败日志到job log
		_, _ = j.Trace.Write([]byte(fmt.Sprintf(ansi.FdevRed+"ERROR: Job failed:%s"+ansi.FdevRESET+"\n", err.Error())))
		executor.SendError(err)
	}

	_ = executor.Cleanup()

	log.Info("job end")

	return err
}

func (j *Job) GetRemoteURL() string {
	return j.JobInfo.Variables.Get("CI_PROJECT_URL")
}

// job 运行时的状态机
type JobStage string

const (
	JobStageBeforeScript JobStage = "before_script"
	JobStageGetSources   JobStage = "get_sources"
	JobStageAfterScript  JobStage = "after_script"
)

// plugin 运行时的状态机
type PluginStage string

const (
	// 插件执行前
	PluginStageBefore PluginStage = "plugin-before"
	// 构建物下载
	PluginStageArtifactDownload PluginStage = "artifact-download"
	// 构建脚本
	PluginStageBuildScript PluginStage = "build_script"
	// 构建物上传
	PluginStageArtifactUpload PluginStage = "artifact-upload"
	// 插件执行后
	PluginStageAfter PluginStage = "plugin-after"
)
