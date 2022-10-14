module github.com/fdev-ci/fdev-ci-runner

replace github.com/fdev-ci/fdev-ci-runner => ./

go 1.16

require (
	github.com/BurntSushi/toml v0.3.1
	github.com/go-resty/resty/v2 v2.5.0
	github.com/imdario/mergo v0.3.11 // indirect
	github.com/lestrrat-go/file-rotatelogs v2.4.0+incompatible
	github.com/lestrrat-go/strftime v1.0.4 // indirect
	github.com/markelog/trie v0.0.0-20171230083431-098fa99650c0
	github.com/minio/minio-go/v7 v7.0.6
	github.com/mitchellh/go-ps v1.0.0
	github.com/prometheus/client_golang v0.9.3
	github.com/prometheus/client_model v0.0.0-20190129233127-fd36f4220a90
	github.com/robfig/cron v1.2.0
	github.com/sanity-io/litter v1.5.0 // indirect
	github.com/sirupsen/logrus v1.7.0
	github.com/spf13/cobra v1.1.3
	github.com/spf13/pflag v1.0.5
	github.com/stretchr/testify v1.6.1
	k8s.io/api v0.0.0-20190620084959-7cf5895f2711
	k8s.io/apimachinery v0.0.0-20190612205821-1799e75a0719
	k8s.io/client-go v0.0.0-20190620085101-78d2af792bab
	k8s.io/utils v0.0.0-20200731180307-f00132d28269 // indirect
)
