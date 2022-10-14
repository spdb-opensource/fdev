package common

import (
	"fmt"
	"net"
	"os"
	"path/filepath"
	"strings"
	"time"

	"github.com/BurntSushi/toml"
	helpers "github.com/fdev-ci/fdev-ci-runner/pkg/utlis"
)

// 执行器的配置
type RunnerConfig struct {
	//配置名称
	Name     string `toml:"name"`
	Executor string `toml:"executor"`
	// 并发度
	Concurrent  int `toml:"concurrent"`
	OutputLimit int `toml:"output_limit,omitzero" long:"output-limit" env:"RUNNER_OUTPUT_LIMIT" description:"Maximum build trace size in kilobytes"`
	// 日志级别
	LogLevel            string      `toml:"log_level"`
	BuildDir            string      `toml:"build_dir"`
	CleanCron           string      `toml:"clean_cron"`
	CleanFileAgo        Duration    `toml:"clean_file_ago"`
	Log                 string      `toml:"log"`
	ListenAddress       string      `toml:"listen_address,omitempty" json:"listen_address"`
	KafkaConfig         KafkaConfig `toml:"kafka"`
	Kubernetes          Kubernetes  `toml:"kubernetes"`
	Local               Local       `toml:"local"`
	MinioConfig         MinioConfig `toml:"minio"`
	ArtifactPath        string      `toml:"artifact_path"`
	Docker              Docker      `toml:"docker"`
	GitConfig           GitConfig   `toml:"git"`
	ArtifactUploadLimit int         `toml:"artifact_upload_limit"`
	RunnerCredentials
}

type RunnerCredentials struct {
	URL   string `toml:"url" json:"url" short:"u" long:"url" env:"CI_SERVER_URL" required:"true" description:"Runner Path"`
	Token string `toml:"token" json:"token" short:"t" long:"token" env:"CI_SERVER_TOKEN" required:"true" description:"Runner token"`
}

type Docker struct {
	DNS             []string       `toml:"dns,omitempty" json:"dns" long:"dns" env:"DOCKER_DNS" description:"A list of DNS servers for the container to use"`
	DockerWorkspace string         `toml:"workspace"`
	Privileged      bool           `toml:"privileged,omitzero" json:"privileged" long:"privileged" env:"DOCKER_PRIVILEGED" description:"Give extended privileges to container"`
	Volumes         []DockerVolume `toml:"volumes,omitempty" json:"volumes" long:"volumes" env:"DOCKER_VOLUMES" description:"Bind-mount a volume and create it if it doesn't exist prior to mounting. Can be specified multiple times once per mountpoint, e.g. --docker-volumes 'test0:/test0' --docker-volumes 'test1:/test1'"`
}
type DockerVolume struct {
	Name     string `toml:"name"`
	Source   string `toml:"source"`
	Target   string `toml:"target"`
	ReadOnly bool   `toml:"read_only"`
}
type MinioConfig struct {
	URL             string `toml:"url"`
	RunnerBucket    string `toml:"runner_bucket"`
	ArtifactsBucket string `toml:"artifacts_bucket"`
	PluginsBucket   string `toml:"plugins_bucket"`
	LogsBucket      string `toml:"logs_bucket"`
}

type GitConfig struct {
	Name     string `toml:"name"`
	Password string `toml:"password"`
}

type KafkaConfig struct {
	Read   Read   `toml:"read"`
	Writer Writer `toml:"writer"`
}

type Read struct {
	BrokerList string `toml:"brokerList"`
	Topic      string `toml:"topic"`
	GroupID    string `toml:"groupID"`
}
type Writer struct {
	BrokerList string `toml:"brokerList"`
	Topic      string `toml:"topic"`
}

type Kubernetes struct {
	Config      string   `toml:"config,omitempty"`
	Namespace   string   `toml:"namespace,omitempty"`
	DNS         []string `toml:"dns,omitempty"`
	HelperImage string   `toml:"helper_image,omitempty"`
}

type Local struct {
	BuildsDir string `toml:"builds_dir,omitempty"`
	CacheDir  string `toml:"cache_dir,omitempty"`
}

// 解析配置文件
func (config *RunnerConfig) LoadConfig(configFile string) error {
	if _, err := toml.DecodeFile(configFile, config); err != nil {
		return err
	}
	return nil
}
func (config *RunnerConfig) GetBuildDir() string {
	if len(config.BuildDir) == 0 {
		if config.Executor != "local" {
			return "/"
		}
		userHomeDir, err := os.UserHomeDir()
		if err == nil {
			return filepath.Join(userHomeDir, ".ci-runner")
		}
	}
	return filepath.Join(config.BuildDir)
}
func (config *RunnerConfig) GetLogDir() string {
	return filepath.Dir(config.GetRunnerLog())
}
func (config *RunnerConfig) GetRunnerLog() string {
	if len(config.Log) == 0 {
		userHomeDir, err := os.UserHomeDir()
		if err == nil {
			return filepath.Join(userHomeDir, ".ci-runner", "logs", "ci-runner.log")
		}
	}
	return filepath.Join(config.Log)
}

// 获取 ListenAddress 配置地址
func (config *RunnerConfig) GetListenAddress() (string, error) {
	address := config.ListenAddress
	if config.ListenAddress != "" {
		address = ":9252"
	}
	_, port, err := net.SplitHostPort(address)
	if err != nil && !strings.Contains(err.Error(), "missing port in address") {
		return "", err
	}

	if len(port) == 0 {
		return fmt.Sprintf("%s:%d", address, DefaultMetricsServerPort), nil
	}
	return address, nil
}
func (c *RunnerCredentials) GetURL() string {
	return c.URL
}
func (c *RunnerCredentials) GetToken() string {
	return c.Token
}
func (c *RunnerCredentials) ShortDescription() string {
	return helpers.ShortenToken(c.Token)
}

type Duration struct {
	time.Duration
}

func (d *Duration) UnmarshalText(text []byte) error {
	var err error
	d.Duration, err = time.ParseDuration(string(text))
	return err
}
