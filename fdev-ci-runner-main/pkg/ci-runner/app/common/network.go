package common

import (
	"context"
	"io"
)

type UpdateState int         // 日志内容上传的状态标识
type JobState string         // job 状态标识
type JobFailureReason string // job 失败的原因(系统级别简要分析)

const (
	Pending JobState = "pending"
	Running JobState = "running"
	Failed  JobState = "failed"
	Success JobState = "success"
)

const (
	NoneFailure         JobFailureReason = ""
	ScriptFailure       JobFailureReason = "script_failure"
	RunnerSystemFailure JobFailureReason = "runner_system_failure"
	JobExecutionTimeout JobFailureReason = "job_execution_timeout"
)

const (
	UpdateSucceeded UpdateState = iota
	UpdateNotFound
	UpdateAbort
	UpdateFailed
	UpdateRangeMismatch
)

// prometheus 异常收集器
type FailuresCollector interface {
	RecordFailure(reason JobFailureReason, runnerDescription string)
}

type VersionInfo struct {
	Name         string `json:"name,omitempty"`
	Version      string `json:"version,omitempty"`
	Revision     string `json:"revision,omitempty"`
	Platform     string `json:"platform,omitempty"`
	Architecture string `json:"architecture,omitempty"`
	Executor     string `json:"executor,omitempty"`
}

// JobResponse 为解析 json 的结构体
type JobResponse struct {
	ID      string  `json:"id"`
	Token   string  `json:"token"`
	JobInfo JobInfo `json:"job_info"`
}

type JobInfo struct {
	//流水线名称
	Pipeline string `json:"pipeline"`
	// 流水线编号
	PipelineNumber int `json:"pipeline_number"`
	// 项目编号
	ProjectID int `json:"project_id"`
	// 阶段
	Stage string `json:"stage"`
	// 阶段编号
	StageIndex int `json:"stage_index"`
	// 任务名称
	JobName string `json:"job_name"`
	// 任务编号
	JobNumber int `json:"job_number"`
	// 任务索引
	JobIndex int `json:"job_index"`
	// 时间戳
	Timestamp int64 `json:"timestamp"`
	// 镜像信息
	Image Image `json:"image"`
	// k8s service
	Services Services `json:"services,omitempty"`
	// 存储卷
	Volumes []Volume `json:"volumes,omitempty"`
	// job相关的变量
	Variables JobVariables `json:"variables,omitempty"`
	// 插件相关数据
	Plugins Plugins `json:"plugins"`
	// 超时时间
	Timeout int32 `json:"timeout,omitempty"`
	// TODO
	GitStrategy string `json:"git_strategy"`
}

type Plugins struct {
	// 插件数量
	Count int `json:"count"`
}

type PluginRequest struct {
	Pipeline    string `json:"pipeline"`
	StageIndex  int    `json:"stage_index"`
	JobIndex    int    `json:"job_index"`
	PluginIndex int    `json:"plugin_index"` // 该job 的 plugin 下标
	Token       string `json:"job_token"`    // job 的 token

}
type PluginResponse struct {
	ID         int        `json:"id"`
	Token      string     `json:"token"`
	PluginInfo PluginInfo `json:"plugin_info"`
}
type PluginInfo struct {
	PluginName    string       `json:"plugin_name"`
	PluginMd5     string       `json:"plugin_md5"`
	PluginVersion string       `json:"plugin_version"`
	PluginAuthor  string       `json:"plugin_author"`
	Execution     Execution    `json:"execution"`
	Variables     JobVariables `json:"variables,omitempty"`
}

type Execution struct {
	Language    string   `json:"language"`
	PackagePath string   `json:"package_path"`
	Demands     []string `json:"demands"`
	Artifacts   []string `json:"artifacts,omitempty"`
	Target      string   `json:"target"`
}

type Volume struct {
	// 卷名
	Name string `json:"name"`
	// 挂载路径
	MountPath string `json:"mount_path"`
	// 是否只读
	ReadOnly bool `json:"read_only"`
	// 主机路径
	HostPath string `json:"host_path"`
}

type Services []Image

type Image struct {
	// 镜像名
	Name string `json:"name"`
	// 镜像别名
	Alias      string   `json:"alias,omitempty"`
	Command    []string `json:"command,omitempty"`
	Entrypoint []string `json:"entrypoint,omitempty"`
}
type ObjectInfo struct {
	Md5 string `json:"md5"`
}
type JobResultRequest struct {
	Info    VersionInfo `json:"info,omitempty"`
	Token   string      `json:"token,omitempty"`
	JobInfo JobInfo     `json:"job_info"`
	Data    ResultData  `json:"result_data"`
}
type FdevResponse struct {
	Code string      `json:"code"`
	Msg  string      `json:"msg"`
	Data interface{} `json:"data"`
}
type ResultData struct {
	Status      string `json:"status"`
	Code        string `json:"code"`
	Message     string `json:"message"`
	MinioLogURL string `json:"minio_log_url"`
}

// UpdateJobRequest 更新 job 的 request 内容
type UpdateJobRequest struct {
	Info          VersionInfo      `json:"info,omitempty"`
	Token         string           `json:"token,omitempty"`
	State         JobState         `json:"state,omitempty"`
	FailureReason JobFailureReason `json:"failure_reason,omitempty"`
}

// JobCredentials 是 job 请求的凭证,带上 pipeline,stageIndex,jobIndex 方便后端定位
type JobCredentials struct {
	ID         string
	Token      string
	Pipeline   string
	StageIndex int
	JobIndex   int
}

// UpdateJobInfo 是更新 job 状态的内容
type UpdateJobInfo struct {
	ID            string
	State         JobState
	FailureReason JobFailureReason
}

// Job 日志跟踪接口
type JobTrace interface {
	io.Writer
	Success()
	Fail(err error, failureReason JobFailureReason)
	SetCancelFunc(cancelFunc context.CancelFunc)
	SetFailuresCollector(fc FailuresCollector)
	SetMasked(values []string)
	IsStdout() bool
	GetState() JobState
}
