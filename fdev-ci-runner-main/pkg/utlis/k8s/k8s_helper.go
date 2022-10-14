package k8s

import (
	"errors"
	"fmt"
	"io/ioutil"
	"net/http"
	"regexp"
	"strings"
	"time"

	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/app/common"
	"github.com/sirupsen/logrus"
	api "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/rest"
	restclient "k8s.io/client-go/rest"
	"k8s.io/client-go/tools/clientcmd"
)

const (
	RFC1123NameMaximumLength         = 63
	RFC1123NotAllowedCharacters      = "[^-a-z0-9]"
	RFC1123NotAllowedStartCharacters = "^[^a-z0-9]+"
)

func getOutClusterClientConfig(config common.Kubernetes) (kubeConfig *restclient.Config, err error) {
	c, err := ioutil.ReadFile(config.Config)
	if err != nil {
		logrus.Fatalln(err)
	}
	return clientcmd.RESTConfigFromKubeConfig(c)
}

func loadDefaultKubectlConfig() (*restclient.Config, error) {
	config, err := clientcmd.NewDefaultClientConfigLoadingRules().Load()
	if err != nil {
		return nil, err
	}

	return clientcmd.NewDefaultClientConfig(*config, &clientcmd.ConfigOverrides{}).ClientConfig()
}

//首先从集群内获取config,失败则加载默认kubectl config
func guessClientConfig() (*restclient.Config, error) {
	// Try in cluster config first
	if inClusterCfg, err := rest.InClusterConfig(); err == nil {
		return inClusterCfg, nil
	}

	// in cluster config failed. Reading default kubectl config
	return loadDefaultKubectlConfig()
}

//获取kubectl 客户端连接配置，首先从外部配置文件读取，如果没有则优先加载集群内或者默认配置
func getKubeClientConfig(config common.Kubernetes) (kubeConfig *restclient.Config, err error) {
	if len(config.Config) > 0 {
		kubeConfig, err = getOutClusterClientConfig(config)
	} else {
		kubeConfig, err = guessClientConfig()
	}
	if err != nil {
		return nil, err
	}
	return kubeConfig, nil
	//c , err := ioutil.ReadFile("/Users/luotao/GoglandProjects/fdev-runner/admin.conf")
	//if err != nil {
	//	log.Fatalln(err)
	//}
	//return clientcmd.RESTConfigFromKubeConfig(c)
}
func GetKubeClient(config common.Kubernetes) (*kubernetes.Clientset, error) {
	restConfig, err := getKubeClientConfig(config)
	if err != nil {
		return nil, err
	}
	return kubernetes.NewForConfig(restConfig)
}

//检查pod是否在运行，如果是运行状态，则返回true，否则返回false
func isRunning(pod *api.Pod) (bool, error) {
	switch pod.Status.Phase {
	case api.PodRunning:
		return true, nil
	case api.PodSucceeded:
		return false, fmt.Errorf("pod already succeeded before it begins running")
	case api.PodFailed:
		return false, fmt.Errorf("pod status is failed")
	default:
		return false, nil
	}
}

type podPhaseResponse struct {
	done  bool
	phase api.PodPhase
	err   error
}

//感知pod的运行阶段，状态
func getPodPhase(c *kubernetes.Clientset, pod *api.Pod) podPhaseResponse {
	pod, err := c.CoreV1().Pods(pod.Namespace).Get(pod.Name, metav1.GetOptions{})
	if err != nil {
		return podPhaseResponse{true, api.PodUnknown, err}
	}

	ready, err := isRunning(pod)

	if err != nil {
		return podPhaseResponse{true, pod.Status.Phase, err}
	}

	if ready {
		return podPhaseResponse{true, pod.Status.Phase, nil}
	}

	// check status of containers
	for _, container := range pod.Status.ContainerStatuses {
		if container.Ready {
			continue
		}
		if container.State.Waiting == nil {
			continue
		}

		switch container.State.Waiting.Reason {
		case "ErrImagePull", "ImagePullBackOff":
			err = fmt.Errorf("image pull failed: %s", container.State.Waiting.Message)
			return podPhaseResponse{true, api.PodUnknown, err}
		}
	}
	logrus.Infof("Waiting for pod %s/%s to be running, status is %s \n", pod.Namespace, pod.Name, pod.Status.Phase)
	return podPhaseResponse{false, pod.Status.Phase, nil}

}

func GetKubeClientConfig(config common.Kubernetes) (kubeConfig *restclient.Config, err error) {
	if len(config.Config) > 0 {
		kubeConfig, err = getOutClusterClientConfig(config)
	} else {
		kubeConfig, err = guessClientConfig()
	}
	if err != nil {
		return nil, err
	}
	return kubeConfig, nil
	//c , err := ioutil.ReadFile("/Users/luotao/GoglandProjects/fdev-runner/admin.conf")
	//if err != nil {
	//	log.Fatalln(err)
	//}
	//return clientcmd.RESTConfigFromKubeConfig(c)
}

func CloseKubeClient(client *kubernetes.Clientset) bool {
	if client == nil {
		return false
	}
	rest, ok := client.CoreV1().RESTClient().(*restclient.RESTClient)
	if !ok || rest.Client == nil || rest.Client.Transport == nil {
		return false
	}
	if transport, ok := rest.Client.Transport.(*http.Transport); ok {
		transport.CloseIdleConnections()
		return true
	}
	return false
}

//  waitForPodRunning 使用 client 来监测 pod 何时到达 PodRunning 状态,
//  一旦到达 PodRunning,PodSucceeded,PodFailed 它将返回 PodPhase
// 对应 PodRunning 它还将等待,知道Pod 中所有的容器也都准备就绪。
// 如果调用 Pod 详情失败或达到超时,则会返回错误,可以配置超时和轮询值
func WaitForPodRunning(c *kubernetes.Clientset, pod *api.Pod) (api.PodPhase, error) {
	pollInterval := common.KubernetesPollInterval
	pollAttempts := common.KubernetesPollAttempts
	for i := 0; i <= pollAttempts; i++ {

		logrus.WithFields(logrus.Fields{
			"pod_name": pod.Name,
		}).Info("getPodPhase for pod")

		result := getPodPhase(c, pod)

		logrus.WithFields(logrus.Fields{
			"pod_name": pod.Name,
		}).Info("getPodPhase result:", result.done)

		if !result.done {
			time.Sleep(pollInterval)
			continue
		}
		return result.phase, result.err
	}
	return api.PodUnknown, errors.New("timedout waiting for pod to start")
}

// 让名字满足 rfc1123 规范
func MakeRFC1123Compatible(name string) string {
	name = strings.ToLower(name)

	nameNotAllowedChars := regexp.MustCompile(RFC1123NotAllowedCharacters)
	name = nameNotAllowedChars.ReplaceAllString(name, "")

	nameNotAllowedStartChars := regexp.MustCompile(RFC1123NotAllowedStartCharacters)
	name = nameNotAllowedStartChars.ReplaceAllString(name, "")

	if len(name) > RFC1123NameMaximumLength {
		name = name[0:RFC1123NameMaximumLength]
	}

	return name
}

// buildVariables converts a common.BuildVariables into a list of
// kubernetes EnvVar objects
func BuildVariables(bv common.JobVariables) []api.EnvVar {
	if bv == nil {
		return nil
	}
	var e []api.EnvVar
	for _, b := range bv {
		if len(b.Key) > 0 {
			e = append(e, api.EnvVar{
				Name:  b.Key,
				Value: b.Value,
			})
		}
	}
	return e
}
