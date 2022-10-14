package network

import (
	"context"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/app/common"
	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/trace"
	"path/filepath"
	"sync"
	"time"
)

type clientJobTrace struct {
	client         *CiAPIClient
	config         common.RunnerConfig
	jobCredentials *common.JobCredentials
	cancelFunc     context.CancelFunc

	buffer *trace.Buffer

	lock          sync.RWMutex
	state         common.JobState
	failureReason common.JobFailureReason
	finished      chan bool

	sentTrace int //日志内容的 offset
	sentTime  time.Time

	updateInterval      time.Duration // 更新 job 状态及日志间隔时间
	forceSendInterval   time.Duration // 强制解锁时间
	finishRetryInterval time.Duration // 重试间隔时间
	maxTracePatchSize   int           //每次上传最大日志bytes数

	failuresCollector common.FailuresCollector
}

func (c *clientJobTrace) Success() {
	c.Fail(nil, common.NoneFailure)
}

func (c *clientJobTrace) Fail(err error, failureReason common.JobFailureReason) {
	c.lock.Lock()

	if c.state != common.Running {
		c.lock.Unlock()
		return
	}

	if err == nil {
		c.state = common.Success
	} else {
		c.setFailure(failureReason)
	}

	c.lock.Unlock()
	c.finish()
}

// 写入数据
func (c *clientJobTrace) Write(data []byte) (n int, err error) {
	return c.buffer.Write(data)
}

// 标记替换
func (c *clientJobTrace) SetMasked(masked []string) {
	c.buffer.SetMasked(masked)
}

func (c *clientJobTrace) SetCancelFunc(cancelFunc context.CancelFunc) {
	c.cancelFunc = cancelFunc
}
func (c *clientJobTrace) SetFailuresCollector(fc common.FailuresCollector) {
	c.failuresCollector = fc
}
func (c *clientJobTrace) IsStdout() bool {
	return false
}

// 设置失败
func (c *clientJobTrace) setFailure(reason common.JobFailureReason) {
	c.state = common.Failed
	c.failureReason = reason
	if c.failuresCollector != nil {
		c.failuresCollector.RecordFailure(reason, c.config.Name)
	}
}

func (c *clientJobTrace) start() {
	c.finished = make(chan bool)
	c.state = common.Running
	c.setupLogLimit()
	go c.watch()
}

func (c *clientJobTrace) finalTraceUpdate() {
	for c.anyTraceToSend() {
		switch c.sendPatch() {
		case common.UpdateSucceeded:
			// we continue sending till we succeed
			continue
		case common.UpdateAbort:
			return
		case common.UpdateNotFound:
			return
		case common.UpdateRangeMismatch:
			time.Sleep(c.finishRetryInterval)
		case common.UpdateFailed:
			time.Sleep(c.finishRetryInterval)
		}
	}
}

func (c *clientJobTrace) finalStatusUpdate() {
	for {
		switch c.sendUpdate() {
		case common.UpdateSucceeded:
			return
		case common.UpdateAbort:
			return
		case common.UpdateNotFound:
			return
		case common.UpdateRangeMismatch:
			return
		case common.UpdateFailed:
			time.Sleep(c.finishRetryInterval)
		}
	}
}

// finish 收尾刷新操作
func (c *clientJobTrace) finish() {
	c.buffer.Finish()
	c.finished <- true
	c.finalTraceUpdate()
	c.finalStatusUpdate()
	c.buffer.Close()
}

// 增量更新上传新的日志内容
func (c *clientJobTrace) incrementalUpdate() common.UpdateState {
	state := c.sendPatch()
	if state != common.UpdateSucceeded {
		return state
	}

	return c.touchJob()
}

// 判断是否有新的日志内容需要上传
func (c *clientJobTrace) anyTraceToSend() bool {
	c.lock.RLock()
	defer c.lock.RUnlock()

	return c.buffer.Size() != c.sentTrace
}

// 发送日志给 GATEWAY API
func (c *clientJobTrace) sendPatch() common.UpdateState {
	c.lock.RLock()
	content, err := c.buffer.Bytes(c.sentTrace, c.maxTracePatchSize)
	sentTrace := c.sentTrace
	c.lock.RUnlock()

	if err != nil {
		return common.UpdateFailed
	}

	if len(content) == 0 {
		return common.UpdateSucceeded
	}

	sentOffset, state := c.client.PatchTrace(c.jobCredentials, content, sentTrace)

	if state == common.UpdateSucceeded || state == common.UpdateRangeMismatch {
		c.lock.Lock()
		c.sentTime = time.Now()
		c.sentTrace = sentOffset
		c.lock.Unlock()
	}

	return state
}

// 发送 update job 状态,初始 Runner 状态, 只在第一次执行
func (c *clientJobTrace) touchJob() common.UpdateState {
	c.lock.RLock()
	// 确保间隔时间大于 forceSendInterval 才真正的去发送 jobs/update 降低网络风暴
	shouldRefresh := time.Since(c.sentTime) > c.forceSendInterval
	c.lock.RUnlock()

	if !shouldRefresh {
		return common.UpdateSucceeded
	}

	jobInfo := common.UpdateJobInfo{
		ID:    c.jobCredentials.ID,
		State: common.Running,
	}

	status := c.client.UpdateJob(c.config, c.jobCredentials, jobInfo)

	if status == common.UpdateSucceeded {
		c.lock.Lock()
		c.sentTime = time.Now()
		c.lock.Unlock()
	}

	return status
}

// 发送 update job 状态,只在最后一次执行
func (c *clientJobTrace) sendUpdate() common.UpdateState {
	c.lock.RLock()
	state := c.state
	c.lock.RUnlock()

	jobInfo := common.UpdateJobInfo{
		ID:            c.jobCredentials.ID,
		State:         state,
		FailureReason: c.failureReason,
	}

	status := c.client.UpdateJob(c.config, c.jobCredentials, jobInfo)

	if status == common.UpdateSucceeded {
		c.lock.Lock()
		c.sentTime = time.Now()
		c.lock.Unlock()
	}

	return status
}

// 判断是否异常
func (c *clientJobTrace) abort() bool {
	if c.cancelFunc != nil {
		c.cancelFunc()
		c.cancelFunc = nil
		return true
	}
	return false
}

// 开始 watch job 监控
func (c *clientJobTrace) watch() {
	for {
		select {
		case <-time.After(c.updateInterval):
			state := c.incrementalUpdate()
			if state == common.UpdateAbort && c.abort() {
				<-c.finished
				return
			}
			break

		case <-c.finished:
			return
		}
	}
}

func (c *clientJobTrace) setupLogLimit() {
	bytesLimit := c.config.OutputLimit * 1024 // convert to bytes
	if bytesLimit == 0 {
		bytesLimit = common.DefaultOutputLimit
	}
	c.buffer.SetLimit(bytesLimit)
}
func (c *clientJobTrace) GetState() common.JobState {
	return c.state
}
func newJobTrace(client *CiAPIClient, config common.RunnerConfig, jobCredentials *common.JobCredentials) (*clientJobTrace, error) {
	buffer, err := trace.New(filepath.Join(config.GetLogDir(), jobCredentials.ID+".log"))
	if err != nil {
		return nil, err
	}

	return &clientJobTrace{
		client:              client,
		config:              config,
		buffer:              buffer,
		jobCredentials:      jobCredentials,
		maxTracePatchSize:   common.DefaultTracePatchLimit,
		updateInterval:      common.UpdateInterval,
		forceSendInterval:   common.ForceTraceSentInterval,
		finishRetryInterval: common.UpdateRetryInterval,
	}, nil
}
