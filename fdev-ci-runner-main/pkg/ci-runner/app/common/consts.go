package common

import "time"

const NAME = "super-runner"
const VERSION = "2020-12-08"
const DefaultMetricsServerPort = 9252                //prometheus metrics 端口
const DefaultLivessTimeout = 1800                    // pod 生产时间
const KubernetesPollInterval = 10 * time.Second      // 每次查询 pod 详情 间隔时间
const KubernetesPollAttempts = 100                   // 尝试获得 Pod 详情次数
const SSHConnectRetries = 3                          // ssh 重试次数
const SSHRetryInterval = 3                           //ssh 间隔时间
const DefaultDockerWorkspace = "/workspace"          // docker executor worspace dir
const DefaultNetworkClientTimeout = 60 * time.Minute // http 底层 timeout 超时时间
const DefaultOutputLimit = 4 * 1024 * 1024           // 日志文件大小 4MB
const DefaultTracePatchLimit = 1024 * 1024           // 每次上传日志大小 1MB
const UpdateInterval = 3 * time.Second               // 每隔 3s 监控一个job 状态及上传日志
const ForceTraceSentInterval = 30 * time.Second      // 30s 强制解锁
const UpdateRetryInterval = 3 * time.Second          // 30s 重试间隔时间

// api
const PluginsInput = "/api/v4/plugins/input"
const PluginsOutput = "/api/v4/plugins/output"
const ArtifactWebhook = "/api/v4/artifacts/webhook"

const BashDetectShell = `
if [ -x /usr/local/bin/bash ]; then
	exec /usr/local/bin/bash 
elif [ -x /usr/bin/bash ]; then
	exec /usr/bin/bash 
elif [ -x /bin/bash ]; then
	exec /bin/bash 
elif [ -x /usr/local/bin/sh ]; then
	exec /usr/local/bin/sh 
elif [ -x /usr/bin/sh ]; then
	exec /usr/bin/sh 
elif [ -x /bin/sh ]; then
	exec /bin/sh 
elif [ -x /busybox/sh ]; then
	exec /busybox/sh 
else
	echo shell not found
	exit 1
fi

`

const GetSourcesShell = `
set -eo pipefail
set +o noclobber
echo -e '\033[1;32mFetching changes\033[0m'
echo 'Initialized empty Git repository in %[2]s'
git init %[1]s
cd %[1]s
if git remote add origin %[2]s >/dev/null 2>/dev/null; then
  echo  -e '\033[1;32mCreated fresh repository\033[0m'
else
  git remote set-url origin %[2]s
fi
git fetch origin
echo -e '\033[1;32mChecking out origin/%[3]s as %[3]s...\033[0m'
git checkout -B %[3]s origin/%[3]s
`
