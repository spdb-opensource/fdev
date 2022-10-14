package prometheus

import (
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/app/common"
	"github.com/prometheus/client_golang/prometheus"
)

var (
	concurrentDesc = prometheus.NewDesc(
		"fdev_ci_runner_concurrent",
		"The current value of concurrent setting",
		nil,
		nil,
	)
	//limitDesc = prometheus.NewDesc(
	//	"gitlab_runner_limit",
	//	"The current value of concurrent setting",
	//	[]string{"runner"},
	//	nil,
	//)
)

type ConfigCollector struct {
	config *common.RunnerConfig
}

// Describe implements prometheus.Collector.
func (mr *ConfigCollector) Describe(ch chan<- *prometheus.Desc) {
	ch <- concurrentDesc
	//ch <- limitDesc
}

// Collect implements prometheus.Collector.
func (mr *ConfigCollector) Collect(ch chan<- prometheus.Metric) {
	config := mr.config

	ch <- prometheus.MustNewConstMetric(
		concurrentDesc,
		prometheus.GaugeValue,
		float64(config.Concurrent),
	)

	//for _, runner := range config.Runners {
	//	ch <- prometheus.MustNewConstMetric(
	//		limitDesc,
	//		prometheus.GaugeValue,
	//		float64(runner.Limit),
	//		runner.ShortDescription(),
	//	)
	//}
}

func NewConfigCollector(config *common.RunnerConfig) *ConfigCollector {
	return &ConfigCollector{config}
}
