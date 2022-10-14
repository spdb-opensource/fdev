package network

import (
	"encoding/json"
	"errors"
	"fmt"
	"net"
	"net/http"
	"runtime"
	"time"

	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/app/common"
	prometheus_helper "github.com/fdev-ci/fdev-ci-runner/pkg/prometheus"
	file_helper "github.com/fdev-ci/fdev-ci-runner/pkg/utlis/file"
	"github.com/fdev-ci/fdev-ci-runner/pkg/version"
	"github.com/go-resty/resty/v2"
	"github.com/sirupsen/logrus"
)

const RunnerToken = "runner-token"

const RunnerTokenUrl = "api/v4/jobs/register"
const JobsRequestUrl = "api/v4/jobs/request"
const JobsTraceUrl = "api/v4/jobs/trace"
const JobWebhookUrl = "api/v4/jobs/webhook"
const JobsUpdateUrl = "api/v4/jobs/update"
const PluginsRequestUrl = "api/v4/plugins/request"
const PluginsInput = "api/v4/plugins/input"
const PluginsOutput = "api/v4/plugins/output"
const ArtifactWebhook = "api/v4/artifacts/webhook"
const ArtifactRequest = "api/v4/artifacts/request"
const DownloadMinio = "api/plugin/downloadMinio"
const GetMinioMd5 = "api/v4/getMinioMd5"
const UploadMinio = "api/plugin/uploadMinio"
const DownloadArt = "api/pipeline/downLoadArtifacts"
const UploadArt = "api/plugin/uploadArt"

var Client *CiAPIClient

type CiAPIClient struct {
	hostUrl string
	common.VersionInfo
	Token  string
	client *resty.Client
}

func (n *CiAPIClient) RequestArtifact(body interface{}, response interface{}) error {
	r := n.client.R()
	_, err := r.
		SetHeader(RunnerToken, n.Token).
		SetHeader("Content-Type", "application/json").
		SetBody(body).
		SetResult(response).
		Post(ArtifactRequest)
	if err != nil {
		return err
	}
	return nil
}
func (n *CiAPIClient) DownloadInput(body interface{}, saveFile string) error {
	r := n.client.R()
	_, err := r.
		SetHeader(RunnerToken, n.Token).
		SetHeader("Content-Type", "application/json").
		SetOutput(saveFile).
		SetBody(body).
		Post(PluginsInput)
	if err != nil {
		return err
	}
	return nil
}
func (n *CiAPIClient) UploadOutput(body interface{}) error {
	r := n.client.R()
	_, err := r.
		SetHeader(RunnerToken, n.Token).
		SetHeader("Content-Type", "application/json").
		SetBody(body).
		Post(PluginsOutput)
	if err != nil {
		return err
	}
	return nil
}
func (n *CiAPIClient) WebhookArtifacts(body interface{}) error {
	r := n.client.R()
	_, err := r.
		SetHeader(RunnerToken, n.Token).
		SetHeader("Content-Type", "application/json").
		SetBody(body).
		Post(ArtifactWebhook)
	if err != nil {
		return err
	}
	return nil
}

type RegisterToken struct {
	Token string `json:"token"`
}

func (n *CiAPIClient) RequestRunnerToken() (runnerToken *RegisterToken, err error) {
	r := n.client.R()
	response, err := r.
		SetHeader(RunnerToken, n.Token).
		SetHeader("Content-Type", "application/json").
		SetResult(&runnerToken).
		SetBody(map[string]interface{}{"info": n.VersionInfo}).
		Post(RunnerTokenUrl)
	if err != nil {
		logrus.WithField("error", err).Errorln("request runner token with error")
		return
	}
	if runnerToken == nil || len(runnerToken.Token) == 0 {
		err = errors.New("runner token is empty")
		logrus.WithField("error", err).Errorln("runner token is empty")
		return
	} else {
		n.Token = runnerToken.Token
	}
	logrus.WithField("runner-token", runnerToken).
		WithField("status", response.Status()).
		Println("request runner token finish")
	return
}

// 向fdev获取job,成功则返回JobResponse数据，否则返回health=false
func (n *CiAPIClient) RequestJob() (jobResponse *common.JobResponse, healthy bool) {
	prometheus_helper.BuildCollectorInstance.AcquireRequest()
	defer prometheus_helper.BuildCollectorInstance.ReleaseRequest()
	r := n.client.R()
	response, err := r.SetHeader(RunnerToken, n.Token).
		SetHeader("Content-Type", "application/json").
		SetResult(&jobResponse).
		Post(JobsRequestUrl)
	if err != nil {
		logrus.WithField("error", err).Errorln("request job with error")
		return
	}
	if response != nil {
		logrus.WithField("status", response.Status()).Println("request job with status")
		switch response.StatusCode() {
		case http.StatusCreated:
			logrus.WithFields(logrus.Fields{
				"job": jobResponse.ID,
			}).Println("Checking for jobs...", "received")
			healthy = true
		case http.StatusForbidden:
			logrus.Errorln("Checking for jobs...", "forbidden")
			jobResponse = nil
			healthy = false
		case http.StatusNoContent:
			logrus.Debugln("Checking for jobs...", "nothing")
			jobResponse = nil
			healthy = true
		default:
			logrus.WithField("status", response.Status()).Warningln("Checking for jobs...", "failed")
			jobResponse = nil
			healthy = false
		}
		prometheus_helper.RequestStatusesCollector.Append(n.Token, prometheus_helper.APIEndpointRequestJob, response.StatusCode())
	}
	return
}

// 开始处理job的事件，追踪记录日志
func (n *CiAPIClient) ProcessJob(config common.RunnerConfig, jobCredentials *common.JobCredentials) (common.JobTrace, error) {
	/*
		@auth
		@description 修改job 日志文件名,并永久保存
	*/
	trace, err := newJobTrace(n, config, jobCredentials)
	if err != nil {
		return nil, err
	}
	logrus.WithFields(logrus.Fields{
		"job": jobCredentials.ID,
	}).Println("trace log file name", trace.buffer.GetLogFileName())

	trace.start()
	return trace, nil
}

func (n *CiAPIClient) RequestPlugin(pipeline string, stageIndex int, jobIndex int, pluginIndex int, token string) (pluginResponse *common.PluginResponse, healthy bool) {
	pluginRequest := common.PluginRequest{
		Pipeline:    pipeline,
		StageIndex:  stageIndex,
		JobIndex:    jobIndex,
		PluginIndex: pluginIndex,
		Token:       token,
	}
	r := n.client.R()
	response, err := r.
		SetHeader(RunnerToken, n.Token).
		SetHeader("Content-Type", "application/json").
		SetBody(pluginRequest).
		SetResult(&pluginResponse).
		Post(PluginsRequestUrl)
	if err != nil {
		logrus.WithField("error", err).Errorln("request plugin with error")
		return
	}
	if response != nil {
		logrus.WithField("status", response.Status()).Println("request plugin with status")
		switch response.StatusCode() {
		case http.StatusCreated:
			logrus.WithFields(logrus.Fields{
				"plugin": pluginResponse.ID,
			}).Println("Checking for plugins...", "received")
			healthy = true
		case http.StatusForbidden:
			logrus.Errorln("Checking for plugins...", "forbidden")
			pluginResponse = nil
			healthy = false
		case http.StatusNoContent:
			logrus.Debugln("Checking for plugins...", "nothing")
			pluginResponse = nil
			healthy = true
		default:
			logrus.WithField("status", response.StatusCode()).Warningln("Checking for plugins...", "failed")
			pluginResponse = nil
			healthy = false
		}
	}
	return
}

//将job执行结果发给服务端
func (n *CiAPIClient) RequestJobResult(request common.JobResultRequest) {
	r := n.client.R()
	response, err := r.
		SetHeader(RunnerToken, n.Token).
		SetHeader("Content-Type", "application/json").
		SetBody(&request).
		Post(JobWebhookUrl)
	if err != nil {
		logrus.WithField("error", err).Errorln("request plugin with error")
		return
	}
	if response != nil {
		logrus.WithField("status", response.Status()).Println("request job result with status")
		switch response.StatusCode() {
		case http.StatusOK:
			logrus.Println("Post job webhook ok")
		case http.StatusForbidden:
			logrus.Errorln("Post job webhook...", "forbidden")
		default:
			logrus.Errorln("Post job webhook...", "failed")
		}
	}
}

func (n *CiAPIClient) PatchTrace(jobCredentials *common.JobCredentials, content []byte, startOffset int) (int, common.UpdateState) {

	baseLog := logrus.WithFields(logrus.Fields{
		"job_id":      jobCredentials.ID,
		"pipeline":    jobCredentials.Pipeline,
		"stage_index": jobCredentials.StageIndex,
		"job_index":   jobCredentials.StageIndex,
		"JOB-TOKEN":   jobCredentials.Token,
	})

	if len(content) == 0 {
		baseLog.Debugln("Appending trace to coordinator...", "skipped due to empty patch")
		return startOffset, common.UpdateSucceeded
	}

	endOffset := startOffset + len(content)
	contentRange := fmt.Sprintf("%d-%d", startOffset, endOffset-1)
	r := n.client.R()
	response, err := r.
		SetHeader(RunnerToken, n.Token).
		SetHeader("Content-Range", contentRange).
		SetHeader("JOB-TOKEN", jobCredentials.Token).
		SetHeader("Content-Type", "text/plain").
		SetBody(content).Patch(JobsTraceUrl)
	if err != nil {
		baseLog.Errorln("Appending trace to coordinator...", "error", err.Error())
		return startOffset, common.UpdateFailed
	}

	prometheus_helper.RequestStatusesCollector.Append(n.Token, prometheus_helper.APIEndpointPatchTrace, response.StatusCode())

	tracePatchResponse := NewTracePatchResponse(response.RawResponse)
	log := baseLog.WithFields(logrus.Fields{
		"sent-log":   contentRange,
		"job-log":    tracePatchResponse.RemoteRange,
		"job-status": tracePatchResponse.RemoteState,
		"code":       response.StatusCode,
		"status":     response.Status,
	})
	if response != nil {
		if tracePatchResponse.IsAborted() {
			log.Warningln("Appending trace to coordinator...", "aborted")
			return startOffset, common.UpdateAbort
		}
		switch response.StatusCode() {
		case http.StatusAccepted:
			log.Debugln("Appending trace to coordinator...", "ok")
			return endOffset, common.UpdateSucceeded
		case http.StatusNotFound:
			log.Warningln("Appending trace to coordinator...", "not-found")
			return startOffset, common.UpdateNotFound
		case http.StatusRequestedRangeNotSatisfiable:
			log.Warningln("Appending trace to coordinator...", "range mismatch")
			return tracePatchResponse.NewOffset(), common.UpdateRangeMismatch
		}
	}
	log.Warningln("Appending trace to coordinator...", "failed")
	return startOffset, common.UpdateFailed
}
func (n *CiAPIClient) UploadArtifacts(path string, file string) error {
	response, err := n.client.R().SetFormData(map[string]string{
		"path": path,
	}).SetFile("file", file).SetResult(&common.FdevResponse{}).Post(UploadArt)
	if err != nil {
		return err
	}
	if response.StatusCode() != http.StatusCreated && response.StatusCode() != http.StatusOK {
		return fmt.Errorf("upload artifacts %s", response.Status())
	} else {
		fdevResponse := response.Result().(*common.FdevResponse)
		if fdevResponse.Code != "AAAAAAA" {
			return fmt.Errorf("upload artifacts %s", fdevResponse.Msg)
		}
	}
	return nil
}
func (n *CiAPIClient) DownloadArtifacts(path string, file string) error {
	response, err := n.client.R().SetQueryParam("objectName", path).Get(DownloadArt)
	if err != nil {
		return err
	}
	if response.StatusCode() != http.StatusCreated && response.StatusCode() != http.StatusOK {
		return fmt.Errorf("download artifacts %s", response.Status())
	}
	if json.Valid(response.Body()) {
		fdevResponse := common.FdevResponse{}
		err := json.Unmarshal(response.Body(), &fdevResponse)
		if err != nil {
			return err
		}
		if fdevResponse.Code != "AAAAAAA" {
			return fmt.Errorf("upload artifacts %s", fdevResponse.Msg)
		}
	}
	err = file_helper.WriteFile(file, response.Body(), 0655)
	if err != nil {
		return err
	}
	return nil
}

func (n *CiAPIClient) GetMinioMd5(bucket string, object string) (string, error) {
	response, err := n.client.R().
		SetResult(&common.ObjectInfo{}).
		SetBody(map[string]string{
			"bucket": bucket,
			"object": object,
		}).Post(GetMinioMd5)
	if err != nil {
		return "", err
	}
	if response.StatusCode() != http.StatusCreated && response.StatusCode() != http.StatusOK {
		return "", fmt.Errorf("get minio md5 %s", response.Status())
	}
	return response.Result().(*common.ObjectInfo).Md5, nil
}

func (n *CiAPIClient) DownloadMinio(bucket string, object string, file string) error {
	response, err := n.client.R().SetQueryParams(map[string]string{
		"bucket": bucket,
		"object": object,
	}).Get(DownloadMinio)
	if err != nil {
		return err
	}
	if response.StatusCode() != http.StatusCreated && response.StatusCode() != http.StatusOK {
		return fmt.Errorf("download plugin %s", response.Status())
	}
	if json.Valid(response.Body()) {
		fdevResponse := common.FdevResponse{}
		err := json.Unmarshal(response.Body(), &fdevResponse)
		if err != nil {
			return err
		}
		if fdevResponse.Code != "AAAAAAA" {
			return fmt.Errorf("upload artifacts %s", fdevResponse.Msg)
		}
	}
	err = file_helper.WriteFile(file, response.Body(), 0755)
	if err != nil {
		return err
	}
	return nil
}

func (n *CiAPIClient) UploadLog(bucket string, object string, file string) error {
	response, err := n.client.R().SetFormData(map[string]string{
		"bucket": bucket,
		"object": object,
	}).SetFile("file", file).SetResult(&common.FdevResponse{}).Post(UploadMinio)
	if err != nil {
		return err
	}
	if response.StatusCode() != http.StatusCreated && response.StatusCode() != http.StatusOK {
		return fmt.Errorf("upload log %s", response.Status())
	} else {
		fdevResponse := response.Result().(*common.FdevResponse)
		if fdevResponse.Code != "AAAAAAA" {
			return fmt.Errorf("upload artifacts %s", fdevResponse.Msg)
		}
	}
	return nil
}

func (n *CiAPIClient) UpdateJob(config common.RunnerConfig, jobCredentials *common.JobCredentials, jobInfo common.UpdateJobInfo) common.UpdateState {
	return common.UpdateSucceeded
	//request := common.UpdateJobRequest{
	//	Info:          n.VersionInfo,
	//	Token:         jobCredentials.Token,
	//	State:         jobInfo.State,
	//	FailureReason: jobInfo.FailureReason,
	//}
	//
	//r := n.client.R()
	//response, err := r.SetHeader(RunnerToken, n.Token).
	//	SetHeader("Content-Type", "application/json").
	//	SetBody(&request).
	//	Put(JobsUpdateUrl)
	//if err != nil {
	//	logrus.Errorln("update job with error", err)
	//	return common.UpdateFailed
	//}
	//prometheus_helper.RequestStatusesCollector.Append(config.RunnerCredentials.Token, prometheus_helper.APIEndpointUpdateJob, response.StatusCode())
	//
	//remoteJobStateResponse := NewRemoteJobStateResponse(response.RawResponse)
	//log := logrus.WithFields(logrus.Fields{
	//	"code":       response.StatusCode(),
	//	"job":        jobInfo.ID,
	//	"job-status": remoteJobStateResponse.RemoteState,
	//})
	//if remoteJobStateResponse.IsAborted() {
	//	log.Warningln("Submitting job to coordinator...", "aborted")
	//	return common.UpdateAbort
	//}
	//switch response.StatusCode() {
	//case http.StatusOK:
	//	log.Debugln("Submitting job to coordinator...", "ok")
	//	return common.UpdateSucceeded
	//case http.StatusNotFound:
	//	log.Warningln("Submitting job to coordinator...", "aborted")
	//	return common.UpdateAbort
	//case http.StatusForbidden:
	//	log.WithField("status", http.StatusForbidden).Errorln("Submitting job to coordinator...", "forbidden")
	//	return common.UpdateAbort
	//}
	//log.WithField("status", response.StatusCode()).Warningln("Submitting job to coordinator...", "failed")
	//return common.UpdateFailed
}

func (n *CiAPIClient) Init(hostUrl string, executor string) *CiAPIClient {
	var restyClient = resty.New().
		SetTransport(&http.Transport{
			Proxy: http.ProxyFromEnvironment,
			DialContext: (&net.Dialer{
				Timeout:   30 * time.Second,
				KeepAlive: 30 * time.Second,
			}).DialContext,
			MaxIdleConns:          100,
			IdleConnTimeout:       90 * time.Second,
			TLSHandshakeTimeout:   10 * time.Second,
			ExpectContinueTimeout: 1 * time.Second,
			ResponseHeaderTimeout: 10 * time.Minute,
			MaxIdleConnsPerHost:   100,
		}).
		SetTimeout(100 * time.Second).
		SetRetryMaxWaitTime(60 * time.Second).
		SetRetryCount(10).
		SetHostURL(hostUrl)
	if logrus.GetLevel() == logrus.DebugLevel {
		restyClient.SetLogger(logrus.StandardLogger())
		restyClient.SetDebug(true)
	}
	restyClient.SetHeader("source", "back")
	restyClient.AddRetryCondition(func(response *resty.Response, err error) bool {
		return response.StatusCode() >= 500
	})
	Client = &CiAPIClient{
		hostUrl: hostUrl,
		client:  restyClient,
		VersionInfo: common.VersionInfo{
			Version:      version.GitBranch,
			Revision:     version.GitCommit,
			Platform:     runtime.GOOS,
			Architecture: runtime.GOARCH,
			Executor:     executor,
		},
	}
	return Client
}

func (n *CiAPIClient) SetName(name string) *CiAPIClient {
	n.Name = name
	return n
}
func (n *CiAPIClient) SetToken(token string) *CiAPIClient {
	n.Token = token
	return n
}

func GetCiAPIClient() *CiAPIClient {
	return Client
}
