package local

import (
	"bytes"
	"fmt"
	"io/ioutil"
	"os/exec"
	"path/filepath"
	"runtime"
	"time"

	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/app/common"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/app/executors"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/app/network"
	"github.com/sirupsen/logrus"
)

type Executor struct {
	job           *common.Job
	buildFinish   chan error
	Trace         common.JobTrace
	ObjectName    string
	IsSystemError bool
}

func (e *Executor) Prepare(job *common.Job) error {
	logrus.Info("Prepare")
	e.buildFinish = make(chan error, 1)
	e.IsSystemError = false
	e.job = job
	e.Trace = job.Trace
	timeStr := time.Now().Format("2006-01-02")
	e.ObjectName = fmt.Sprintf("%s/%s", timeStr, filepath.Base(e.job.JobName))
	variables := job.JobInfo.Variables
	for i, variable := range variables {
		if variable.Key == "CI_WORKSPACE" {
			variables[i].Value = filepath.Join(job.Runner.GetBuildDir(), variable.Value)
		}
		if variable.Key == "CI_PROJECT_DIR" {
			variables[i].Value = filepath.Join(job.Runner.GetBuildDir(), variable.Value)
		}
	}
	job.JobInfo.Variables = append(variables, common.JobVariable{
		Key:   "CI_PLUGIN_CACHE",
		Value: filepath.Join(job.Runner.GetBuildDir(), ".plugins"),
	}, common.JobVariable{
		Key:   "FDEV_URL",
		Value: job.Runner.URL,
	})
	return nil
}

func (e *Executor) Run() error {
	logrus.Info("Run")
	var err error

	r := &Runtime{e: e, job: e.job}
	err = r.ExecuteScript()
	return err
}

/**
* 单个Job任务的插件执行完成之后，
* 发送接口上传Job执行的最终结果状态
 */
func (e *Executor) Wait() error {
	logrus.Info("Wait")
	apiclient := network.GetCiAPIClient()
	resultData := &common.ResultData{
		MinioLogURL: fmt.Sprintf("%s/minio/%s/%s", e.job.Runner.MinioConfig.URL, e.job.Runner.MinioConfig.LogsBucket, e.ObjectName),
	}
	resultData.Status = "success"
	resultData.Code = "0"
	resultData.Message = "Build Success"
	jobResult := &common.JobResultRequest{
		Info: common.VersionInfo{
			Name:         common.NAME,
			Version:      common.VERSION,
			Platform:     runtime.GOOS,
			Architecture: runtime.GOARCH,
			Executor:     e.job.Runner.Executor,
		},
		Token:   e.job.Token,
		JobInfo: e.job.JobInfo,
		Data:    *resultData,
	}
	apiclient.RequestJobResult(*jobResult)
	e.Trace.Success()
	return nil
}

// job执行失败则发送服务端
func (e *Executor) SendError(err error) {
	apiclient := network.GetCiAPIClient()
	jobResult := &common.JobResultRequest{
		Info: common.VersionInfo{
			Name:         common.NAME,
			Version:      common.VERSION,
			Platform:     runtime.GOOS,
			Architecture: runtime.GOARCH,
			Executor:     e.job.Runner.Executor,
		},
		Token:   e.job.Token,
		JobInfo: e.job.JobInfo,
		Data: common.ResultData{
			Status:      "error",
			Code:        "-1",
			Message:     err.Error(),
			MinioLogURL: fmt.Sprintf("%s/minio/%s/%s", e.job.Runner.MinioConfig.URL, e.job.Runner.MinioConfig.LogsBucket, e.ObjectName),
		},
	}
	apiclient.RequestJobResult(*jobResult)
	e.Trace.Fail(err, common.ScriptFailure)
}

func (e *Executor) Cleanup() error {
	logrus.Info("Cleanup")
	e.cleanBuildLog()
	return nil
}

// 发送接口上传插件执行的日志
func (e *Executor) cleanBuildLog() {
	if e.Trace != nil {
		filename := filepath.Join(e.job.Runner.GetLogDir(), e.job.JobName+".log")
		err := network.Client.UploadLog(
			e.job.Runner.MinioConfig.LogsBucket,
			e.ObjectName,
			filename,
		)
		if err != nil {
			logrus.WithFields(logrus.Fields{
				"job_name": e.job.JobName,
			}).Error("Minio PutObject with error:", err)
		}
		logrus.WithFields(logrus.Fields{
			"job_name":   e.job.JobName,
			"objectName": e.ObjectName,
		}).Infoln("" +
			"Minio PutObject ok")
		//os.Remove(e.BuildLog.Name())
	}
}

func createFn() common.Executor {
	return &Executor{}
}
func init() {
	common.RegisterExecutor("local", executors.DefaultExecutorProvider{Creator: createFn})
}

func (e *Executor) RunInLocal(script string) <-chan error {
	errc := make(chan error, 1)
	go func() {
		var cmd *exec.Cmd
		sb := bytes.Buffer{}
		if runtime.GOOS == "windows" {
			scriptDir, err := ioutil.TempDir("", "build_script")
			if err != nil {
				errc <- err
				return
			}
			scriptFile := filepath.Join(scriptDir, "script.cmd")
			sb.WriteString("@echo off\r\n")
			//sb.WriteString("setlocal enableextensions\r\n")
			//sb.WriteString("setlocal enableDelayedExpansion\r\n")
			sb.WriteString("chcp 65001>nul\r\n")
			for _, variable := range e.job.JobInfo.Variables {
				sb.WriteString("set " + variable.Key + "=" + variable.Value + "\r\n")
			}
			sb.WriteString(script)
			sb.WriteString("\r\nIF %errorlevel% NEQ 0 exit /b %errorlevel%\r\n")
			err = ioutil.WriteFile(scriptFile, sb.Bytes(), 0700)
			if err != nil {
				errc <- err
				return
			}
			cmd = exec.Command("cmd", "/C", scriptFile)
		} else {
			cmd = exec.Command("sh", "-c", common.BashDetectShell)
			for _, variable := range e.job.JobInfo.Variables {
				sb.WriteString("export " + variable.Key + "=" + variable.Value + "\n")
			}
			sb.WriteString(script + "\n")
			cmd.Stdin = &sb
		}
		cmd.Stdout = e.Trace
		cmd.Stderr = e.Trace
		logrus.Info("script:", script)
		err := cmd.Run()
		if err != nil {
			logrus.WithField("error", err).Error("exec script error")
		}
		errc <- err
	}()
	return errc
}
