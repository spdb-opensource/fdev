package kubernetes

import (
	"errors"
	"fmt"
	"path/filepath"
	"runtime"
	"strings"
	"time"

	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/app/common"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/app/executors"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/app/network"
	k8s_helper "github.com/fdev-ci/fdev-ci-runner/pkg/utlis/k8s"
	"github.com/sirupsen/logrus"
	api "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
)

type Executor struct {
	job         *common.Job
	kubeClient  *kubernetes.Clientset
	options     *kubernetesOptions
	pod         *api.Pod
	podName     string
	buildFinish chan error
	//BuildLog      *os.File
	Trace         common.JobTrace
	ObjectName    string
	IsSystemError bool
}

type kubernetesOptions struct {
	Image    common.Image    //build image
	Services common.Services // services image
}

//准备阶段，连接k8s cluster,并进行pod初始化
func (e *Executor) Prepare(job *common.Job) error {
	logrus.Info("Prepare")
	e.buildFinish = make(chan error, 1)
	e.IsSystemError = false
	e.job = job
	e.Trace = job.Trace
	e.podName = job.JobName

	// 创建 build log
	//filename := fmt.Sprintf("%s/%s.log", e.job.Runner.TempLogDir, e.podName)
	//build_log, err := os.Create(filename)
	//
	//if err != nil {
	//	return err
	//}
	//e.BuildLog = build_log

	// 设置 build log 在 minio 的目录位置
	timeStr := time.Now().Format("2006-01-02")
	e.ObjectName = fmt.Sprintf("%s/%s", timeStr, filepath.Base(e.podName))

	// 连接 K8s cluster
	var err error
	e.kubeClient, err = k8s_helper.GetKubeClient(e.job.Runner.Kubernetes)
	if err != nil {
		logrus.WithFields(logrus.Fields{
			"pod_name": e.podName,
		}).Errorln("connection k8s faild with error:", err)
		e.IsSystemError = true
		return errors.New("connection k8s faild with error")
	}
	// 处理 options config 相关
	e.prepareOptions()
	variables := job.JobInfo.Variables
	job.JobInfo.Variables = append(variables, common.JobVariable{
		Key:   "CI_PLUGIN_CACHE",
		Value: "/workspace/.plugins",
	}, common.JobVariable{
		Key:   "FDEV_URL",
		Value: job.Runner.URL,
	})
	// k8s pod 初始化
	err = e.setupBuildPod()
	if err != nil {
		logrus.WithFields(logrus.Fields{
			"pod_name": e.podName,
		}).Errorln("setupBuildPod with error: ", err)
		e.IsSystemError = true
		return err
	}
	return nil
}

// 处理options config相关
func (e *Executor) prepareOptions() {
	e.options = &kubernetesOptions{}
	e.options.Image = e.job.JobInfo.Image
	for _, service := range e.job.JobInfo.Services {
		if service.Name == "" {
			continue
		}
		e.options.Services = append(e.options.Services, service)
	}
}

// 运行阶段，主要逻辑在这里面。会在运行时依次下载插件并执行
func (e *Executor) Run() error {
	logrus.Info("Run")
	var err error

	runtime := &Runtime{e: e}
	err = runtime.executeScript()
	return err

}

// 初始化设置 pod
func (e *Executor) setupBuildPod() error {

	logrus.WithFields(logrus.Fields{
		"pod_name": e.podName,
	}).Info("setupBuildPod")

	services := make([]api.Container, len(e.options.Services))
	for i, service := range e.options.Services {
		services[i] = e.buildContainer(fmt.Sprintf("svc-%d", i), service.Name)
	}
	//todo step labels
	//todo step annotations
	//todo step imagePullSecrets
	command := []string{"sh", "-c", common.BashDetectShell}
	//command := []string{"sh","-c","tail -f /dev/null"}

	buildContainer := e.buildContainer(buildContainerName, e.options.Image.Name)
	helperContainer := e.buildContainer(helperContainerName, e.job.Runner.Kubernetes.HelperImage)
	buildContainer.Command = command
	helperContainer.Command = command

	PodDNSConfig := api.PodDNSConfig{
		Nameservers: e.job.Runner.Kubernetes.DNS,
		Searches:    nil,
		Options:     nil,
	}

	podResource := &api.Pod{
		ObjectMeta: metav1.ObjectMeta{
			Name:      e.podName,
			Namespace: e.job.Runner.Kubernetes.Namespace,
		},
		Spec: api.PodSpec{
			Volumes: e.getVolumes(),
			Containers: append([]api.Container{
				buildContainer, helperContainer,
			}, services...),
			RestartPolicy: api.RestartPolicyNever,
			DNSConfig:     &PodDNSConfig,
		},
	}
	// 创建 pod,添加重试功能
	pod, err := e.kubeClient.CoreV1().Pods(e.job.Runner.Kubernetes.Namespace).Create(podResource)
	if err != nil {
		logrus.WithFields(logrus.Fields{
			"pod_name": e.podName,
		}).Warnln("setupBuildPod create pod [1] with error: ", err)

		time.Sleep(30 * time.Second)
		pod, err = e.kubeClient.CoreV1().Pods(e.job.Runner.Kubernetes.Namespace).Get(e.podName, metav1.GetOptions{})
		if err != nil {
			pod, err = e.kubeClient.CoreV1().Pods(e.job.Runner.Kubernetes.Namespace).Create(podResource)
			if err != nil {
				logrus.WithFields(logrus.Fields{
					"pod_name": e.podName,
				}).Warnln("setupBuildPod create pod [2] with error: ", err)

				time.Sleep(30 * time.Second)
				pod, err = e.kubeClient.CoreV1().Pods(e.job.Runner.Kubernetes.Namespace).Get(e.podName, metav1.GetOptions{})
				if err != nil {
					pod, err = e.kubeClient.CoreV1().Pods(e.job.Runner.Kubernetes.Namespace).Create(podResource)
					if err != nil {
						return err
					}
				}

			}
		}

	}
	e.pod = pod
	return nil

}

//在容器中执行脚本
func (e *Executor) runInContainer(name string, script string) <-chan error {
	errc := make(chan error, 1)

	go func() {
		// 等待pod 到达podRunning状态
		status, err := k8s_helper.WaitForPodRunning(e.kubeClient, e.pod)
		if err != nil {
			e.buildFinish <- err
			return
		}
		if status != api.PodRunning {
			e.IsSystemError = true
			e.buildFinish <- fmt.Errorf("pod failed to enter running state: %s", status)
			return
		}

		logrus.WithFields(logrus.Fields{
			"pod_name": e.podName,
		}).Info("pod state is PodRunning")

		// 获取kubectl 客户端的配置
		config, err := k8s_helper.GetKubeClientConfig(e.job.Runner.Kubernetes)
		if err != nil {
			e.buildFinish <- err
			return
		}
		command := []string{"sh", "-c", common.BashDetectShell}

		exec := ExecOptions{
			PodName:       e.pod.Name,
			Namespace:     e.pod.Namespace,
			ContainerName: name,
			Command:       command,
			In:            strings.NewReader(script),
			Out:           e.Trace,
			Err:           e.Trace,
			Stdin:         true,
			Config:        config,
			Client:        e.kubeClient,
			Executor:      &DefaultRemoteExecutor{},
		}
		//调用接口执行脚本
		errc <- exec.Run()
	}()
	return errc

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

	// Wait for signals: abort, timeout or finish
	//select {
	//case <-time.After(time.Duration(e.getTimeout()) * time.Second):
	//	logrus.WithFields(logrus.Fields{
	//		"pod_name": e.podName,
	//	}).Warn("Build Timeout")
	//	resultData.Status = "error"
	//	resultData.Code = "-1"
	//	resultData.Message = "Build Timeout"
	//	//kafka.ErrorKafka(s.Config.KafkaConfig,s.pod_name,s.Build.MetaData,url,errors.New("timeout"),s.IsSystemError)
	//
	//case err := <-e.buildFinish:
	//	// command finished
	//	if err != nil {
	//		logrus.WithFields(logrus.Fields{
	//			"pod_name": e.podName,
	//		}).Warn("build faild with error: ", err)
	//		resultData.Status = "error"
	//		resultData.Code = "-1"
	//		resultData.Message = "Build Faild"
	//		//url := fmt.Sprintf("%s/minio/%s/%s",s.Config.MinioConfig.Url,s.Config.MinioConfig.Bucket,s.ObjectName)
	//		//kafka.ErrorKafka(s.Config.KafkaConfig,s.pod_name,s.Build.MetaData,url,err,s.IsSystemError)
	//
	//	} else {
	//		logrus.WithFields(logrus.Fields{
	//			"pod_name": e.podName,
	//		}).Info("Build succeeded")
	//		resultData.Status = "success"
	//		resultData.Code = "0"
	//		resultData.Message = "Build Success"
	//		//url := fmt.Sprintf("%s/minio/%s/%s",s.Config.MinioConfig.Url,s.Config.MinioConfig.Bucket,s.ObjectName)
	//		//kafka.SucceededKafka(s.Config.KafkaConfig,s.pod_name,s.Build.MetaData,url)
	//	}
	//}

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

//将job执行失败的结果发给服务端
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

//任务执行结束之后，进行一些收尾工作。删除用于执行任务的临时的k8s Pod,断开k8s连接等
func (e *Executor) Cleanup() error {
	logrus.Info("Cleanup")

	if e.pod != nil {
		err := e.kubeClient.CoreV1().Pods(e.pod.Namespace).Delete(e.pod.Name, &metav1.DeleteOptions{})
		if err != nil {
			time.Sleep(5 * time.Second)
			err = e.kubeClient.CoreV1().Pods(e.pod.Namespace).Delete(e.pod.Name, &metav1.DeleteOptions{})
			if err != nil {
				time.Sleep(5 * time.Second)
				err = e.kubeClient.CoreV1().Pods(e.pod.Namespace).Delete(e.pod.Name, &metav1.DeleteOptions{})
				if err != nil {
					logrus.WithFields(logrus.Fields{
						"pod_name": e.pod.Name,
					}).Warn("Error cleaning up pod with error: ", err)
				}
			}

		}
	}
	k8s_helper.CloseKubeClient(e.kubeClient)

	logrus.WithFields(logrus.Fields{
		"pod_name": e.podName,
	}).Info("Pod Cleanup")
	e.cleanBuildLog()
	return nil
}

// 发送接口上传插件执行的日志
func (e *Executor) cleanBuildLog() {
	// 组装minio config
	if e.Trace != nil {
		filename := filepath.Join(e.job.Runner.GetLogDir(), e.podName+".log")
		err := network.Client.UploadLog(
			e.job.Runner.MinioConfig.LogsBucket,
			e.ObjectName,
			filename,
		)
		if err != nil {
			logrus.WithFields(logrus.Fields{
				"pod_name": e.podName,
			}).Error("upload log with error:", err)
		}
		logrus.WithFields(logrus.Fields{
			"pod_name":   e.podName,
			"objectName": e.ObjectName,
		}).Infoln("upload log finish")
		//os.Remove(e.BuildLog.Name())
	}
}

// 生成容器
func (e *Executor) buildContainer(name string, image string) api.Container {
	privileged := true

	liveness := api.Probe{
		Handler: api.Handler{
			Exec: &api.ExecAction{Command: []string{"sh", "-c", "kill me"}},
		},
		InitialDelaySeconds: e.getTimeout(),
		TimeoutSeconds:      1,
		PeriodSeconds:       1,
		SuccessThreshold:    1,
		FailureThreshold:    1,
	}
	return api.Container{
		Name:      name,
		Image:     image,
		Resources: api.ResourceRequirements{},
		SecurityContext: &api.SecurityContext{
			Privileged: &privileged,
		},
		Env:           k8s_helper.BuildVariables(e.job.JobInfo.Variables),
		VolumeMounts:  e.getVolumeMounts(),
		Stdin:         true,
		LivenessProbe: &liveness,
		//Command: command,

	}
}

//todo repo 目录的 nas 判断,当没有 nas 时使用 repo 的 empty。
// 获得所有 VolumeMounts
func (e *Executor) getVolumeMounts() (mounts []api.VolumeMount) {
	//mounts = append(mounts, api.VolumeMount{
	//	Name:      "repo",
	//	MountPath: "/workspace",
	//})
	for _, mount := range e.job.JobInfo.Volumes {
		mounts = append(mounts, api.VolumeMount{
			Name:      mount.Name,
			MountPath: mount.MountPath,
			ReadOnly:  mount.ReadOnly,
		})
	}

	return
}

// 获得所有的 Volume
func (e *Executor) getVolumes() (volumes []api.Volume) {

	//volumes = append(volumes, api.Volume{
	//	Name: "repo",
	//	VolumeSource: api.VolumeSource{
	//		EmptyDir: &api.EmptyDirVolumeSource{},
	//	},
	//})

	for _, volume := range e.job.JobInfo.Volumes {
		path := volume.HostPath
		// Make backward compatible with syntax introduced in version 9.3.0
		if path == "" {
			path = volume.HostPath
		}

		volumes = append(volumes, api.Volume{
			Name: volume.Name,
			VolumeSource: api.VolumeSource{
				HostPath: &api.HostPathVolumeSource{
					Path: path,
				},
			},
		})
	}

	return
}

func (e *Executor) getTimeout() int32 {
	if e.job.JobInfo.Timeout != 0 {
		return e.job.JobInfo.Timeout
	}
	return common.DefaultLivessTimeout

}

func createFn() common.Executor {
	return &Executor{}
}
func init() {
	common.RegisterExecutor("kubernetes",
		executors.DefaultExecutorProvider{Creator: createFn})
}
