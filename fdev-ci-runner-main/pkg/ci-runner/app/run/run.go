package run

import (
	_ "embed"
	"fmt"
	"io/ioutil"
	"net"
	"net/http"
	"os"
	"os/exec"
	"os/signal"
	"path/filepath"
	"runtime"
	"sync"
	"syscall"
	"time"

	"github.com/BurntSushi/toml"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/app/common"
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/app/network"
	prometheus_helper "github.com/fdev-ci/fdev-ci-runner/pkg/prometheus"
	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/file"
	rotatelogs "github.com/lestrrat-go/file-rotatelogs"
	"github.com/mitchellh/go-ps"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promhttp"
	"github.com/robfig/cron"
	"github.com/sirupsen/logrus"
)

var (
	config = common.RunnerConfig{
		Name:              "ci-runner-new-my",
		Executor:          "local",
		Concurrent:        10,
		LogLevel:          "info",
		RunnerCredentials: common.RunnerCredentials{URL: "xxx"},
		ArtifactPath:      "fdev-generic-local/fdev-ci-artifacts",
		CleanCron:         "0 0 1 * * ?",
		CleanFileAgo:      common.Duration{Duration: 24 * time.Hour},
		MinioConfig: common.MinioConfig{
			URL:             "xxx:9000",
			RunnerBucket:    "fdev-ci-runner",
			ArtifactsBucket: "fdev-ci-artifacts",
			PluginsBucket:   "fdev-ci-plugins",
			LogsBucket:      "fdev-ci-logs",
		},
	} // ci runner 配置文件
	currentWorkers int // 当前运行的job并发数
	countLock      sync.Mutex
	stopSignals    = make(chan os.Signal)
	stopSignal     os.Signal
	updating       bool
)

// 设置日志
func initLog(logfile string) {
	content, err := rotatelogs.New(
		logfile+"-%Y%m%d%H%M",
		rotatelogs.WithLinkName(logfile),
		rotatelogs.WithRotationCount(30),
		rotatelogs.WithRotationTime(24*time.Hour), //rotate 最小为1分钟轮询。默认60s  低于1分钟就按1分钟来
	)
	if err == nil {
		logrus.SetOutput(content)
	} else {
		logrus.SetOutput(os.Stdout)
	}
	logrus.SetFormatter(&logrus.JSONFormatter{TimestampFormat: "2006-01-02T15:04:05.999"})
	level, err := logrus.ParseLevel(config.LogLevel)
	if err != nil {
		level = logrus.DebugLevel
	}
	logrus.SetLevel(level)
	logrus.AddHook(&prometheus_helper.LogHookInstance)
}

// 加载配置文件
func initConfig(cfgFile string) {
	if file.Exists(cfgFile) {
		if _, err := toml.DecodeFile(cfgFile, &config); err != nil {
			logrus.WithError(err).Fatal("Failed to load config file")
		}
	}
}

// 配置 prometheus metrics
func initMetricsAndDebugServer() {
	listenAddress, err := config.GetListenAddress()
	if err != nil {
		logrus.Errorf("invalid listen address: %s", err.Error())
		return
	}

	listener, err := net.Listen("tcp", listenAddress)
	if err != nil {
		logrus.WithError(err).Fatal("Failed to create listener for metrics server")
	}

	mux := http.NewServeMux()
	serveMetrics(mux)
	go func() {
		err := http.Serve(listener, mux)
		if err != nil {
			logrus.WithError(err).Fatal("Metrics server terminated")
		}
	}()

	logrus.
		WithField("address", listenAddress).
		Info("Metrics server listening")
}

func serveMetrics(mux *http.ServeMux) {
	registry := prometheus.NewRegistry()
	// 暴露 程序版本
	//registry.MustRegister(versionCollector)
	// Metrics about the runner's business logic.
	registry.MustRegister(prometheus_helper.NewConfigCollector(&config))
	registry.MustRegister(prometheus_helper.NewBuildCollector(&config))
	// Metrics about API connections
	registry.MustRegister(prometheus_helper.RequestStatusesCollector)
	// Metrics about jobs failures
	registry.MustRegister(prometheus_helper.FailuresCollectorInstance)
	// Metrics about catched errors
	registry.MustRegister(&prometheus_helper.LogHookInstance)
	// 暴露关于进程的Go-specific指标(GC stats, goroutines，等等)。
	registry.MustRegister(prometheus.NewGoCollector())
	// 暴露与go无关的进程指标(内存使用、文件描述符等)。
	registry.MustRegister(prometheus.NewProcessCollector(prometheus.ProcessCollectorOpts{}))

	mux.Handle("/metrics", promhttp.HandlerFor(registry, promhttp.HandlerOpts{}))

}

// ci-runner run 命令入口函数
func Run(cfgFile string) {
	logrus.Info("	CI runner Run")
	initConfig(cfgFile)
	initLog(config.GetRunnerLog())
	//初始化网络请求
	network.Client.Init(config.URL, config.Executor).SetName(config.Name)
	if len(config.Token) > 0 {
		network.Client.SetToken(config.Token)
	} else {
		//获取runner_token
		runnerToken, err := network.Client.RequestRunnerToken()
		if err != nil {
			logrus.Errorln("register runner error", err)
		} else {
			logrus.Infoln("token:", runnerToken.Token)
		}
	}
	c := cron.New()
	//自动升级
	if config.Executor == "local" {
		//先进行一次升级检查
		autoUpdate()
		//如果父进程和本进程可执行文件相同，说明是升级后重启，则杀死父进程
		executable, _ := os.Executable()
		process, err := ps.FindProcess(os.Getppid())
		if err == nil && process != nil {
			if process.Executable() == filepath.Base(executable) {
				logrus.Info("update finish kill parent process", os.Getppid(), process.Executable())
				findProcess, _ := os.FindProcess(os.Getppid())
				err = findProcess.Signal(os.Kill)
				if err != nil {
					logrus.Errorln("failed to kill parent process")
					panic(err)
				}
				time.Sleep(5 * time.Second)
			}
		}
		c.AddFunc("0 0 0 * * ?", autoUpdate)
	}
	c.AddFunc(config.CleanCron, autoClean)
	c.Start()
	initMetricsAndDebugServer()
	go handleGracefulShutdown()
	go ShowCurrentWorkersCount()
	run()
}

//自动升级
func autoUpdate() {
	var objectName string
	if runtime.GOOS == "windows" {
		objectName = "ci-runner-windows.exe"
	}
	if runtime.GOOS == "linux" {
		objectName = "ci-runner-linux"
	}
	if runtime.GOOS == "darwin" {
		objectName = "ci-runner-macos"
	}
	executable, _ := os.Executable()
	etag, err := network.Client.GetMinioMd5(config.MinioConfig.RunnerBucket, objectName)
	if err != nil {
		return
	}
	selfMd5, err := file.GetMd5(executable)
	if err != nil {
		panic(err)
	}
	if len(etag) > 0 && etag != selfMd5 {
		updating = true
		fmt.Println("stop request new job and prepare update...")
		err := os.Rename(executable, executable+".old")
		if err != nil {
			panic(err)
		}
		err = network.Client.DownloadMinio(config.MinioConfig.RunnerBucket, objectName, executable)
		if err != nil {
			fmt.Println("failed to download new version runner")
			updating = false
			return
		}
		//等待当前运行的job完成后再启动新进程
		for currentWorkers > 0 {
			time.Sleep(5 * time.Second)
		}
		cmd := exec.Command(executable, os.Args[1:]...)
		cmd.Stdin = os.Stdin
		cmd.Stdout = os.Stdout
		cmd.Stderr = os.Stderr
		err = cmd.Start()
		if err != nil {
			logrus.Errorln("failed to start new version runner", err)
			updating = false
			return
		}
	}
}
func autoClean() {
	if config.CleanFileAgo.Duration == 0 || config.Executor != "local" {
		return
	}
	buildDir := config.GetBuildDir()
	workspace := filepath.Join(buildDir, "workspace")
	fileInfo, err := ioutil.ReadDir(workspace)
	if err != nil {
		logrus.Errorln(err.Error())
	}
	now := time.Now()
	for _, info := range fileInfo {
		if info.Name()[0] == '.' {
			continue
		}
		if diff := now.Sub(info.ModTime()); diff > config.CleanFileAgo.Duration {
			err := os.RemoveAll(filepath.Join(workspace, info.Name()))
			if err != nil {
				logrus.Errorln(err.Error())
			}
		}
	}
}

// 优雅停机
func handleGracefulShutdown() error {
	signal.Notify(stopSignals, syscall.SIGQUIT, syscall.SIGTERM, os.Interrupt, os.Kill)
	stopSignal = <-stopSignals
	logrus.
		WithField("StopSignal", stopSignal).
		Warning("Starting graceful shutdown, waiting for builds to finish")
	for currentWorkers > 0 {
		time.Sleep(time.Second)
	}

	os.Exit(0)
	return nil
}

// ci runner 业务 run 入口函数
func run() {
	// 未到并发上线,每隔 3s 获取 job
	for {
		if currentWorkers < config.Concurrent && stopSignal == nil && !updating {
			jobData, healthy := network.Client.RequestJob()
			if !healthy {
				logrus.Errorln("Runner is not healthy and will be disabled!")
			}
			if jobData != nil {
				go func() {
					AddJob()
					defer RemoveJob()
					jobName := fmt.Sprintf("runner-%s-%d-%d-%d",
						config.ShortDescription(),
						jobData.JobInfo.ProjectID,
						jobData.JobInfo.PipelineNumber,
						jobData.JobInfo.JobNumber)

					jobCredentials := &common.JobCredentials{
						ID:    jobName,
						Token: jobData.Token,
					}

					trace, _ := network.Client.ProcessJob(config, jobCredentials)
					trace.SetFailuresCollector(prometheus_helper.FailuresCollectorInstance)
					newJob := common.Job{
						Runner:      config,
						JobResponse: *jobData,
						Trace:       trace,
						JobName:     jobName,
						CreatedAt:   time.Now(),
					}
					prometheus_helper.BuildCollectorInstance.AddJob(&newJob)
					//执行任务
					err := newJob.Run()
					if err != nil {
						logrus.WithField("error", err).Println("run with err")
					}
					prometheus_helper.BuildCollectorInstance.RemoveJob(&newJob)
				}()

			}
		}
		// 正式版本
		time.Sleep(3 * time.Second)
	}
}
func AddJob() {
	countLock.Lock()
	defer countLock.Unlock()
	currentWorkers++
}
func RemoveJob() {
	countLock.Lock()
	defer countLock.Unlock()
	currentWorkers--
}
func ShowCurrentWorkersCount() {
	for {
		logrus.WithField("currentWorkers", currentWorkers).Infoln("show current workers count")
		time.Sleep(3 * time.Second)
	}
}
