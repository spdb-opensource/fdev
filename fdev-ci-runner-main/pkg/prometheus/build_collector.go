package prometheus

import (
	"github.com/fdev-ci/fdev-ci-runner/pkg/ci-runner/app/common"
	"github.com/prometheus/client_golang/prometheus"
	"sync"
	"time"
)

var (
	JobsTotal = prometheus.NewCounterVec(
		prometheus.CounterOpts{
			Name: "fdev_ci_runner_jobs_total",
			Help: "Total number of handled jobs",
		},
		[]string{"runner"},
	)
	JobDurationHistogram = prometheus.NewHistogramVec(
		prometheus.HistogramOpts{
			Name:    "fdev_ci_runner_job_duration_seconds",
			Help:    "Histogram of job durations",
			Buckets: []float64{30, 60, 300, 600, 1800, 3600, 7200, 10800, 18000, 36000},
		},
		[]string{"runner"},
	)
	numBuildsDesc = prometheus.NewDesc(
		"fdev_ci_runner_jobs",
		"The current number of running builds.",
		[]string{"runner", "job_state", "job_stage", "plugin_stage"},
		nil,
	)
	requestConcurrencyDesc = prometheus.NewDesc(
		"fdev_ci_runner_request_concurrency",
		"The current number of concurrent requests for a new job",
		[]string{"runner"},
		nil,
	)
	requestConcurrencyExceededDesc = prometheus.NewDesc(
		"fdev_ci_runner_request_concurrency_exceeded_total",
		"Counter tracking exceeding of request concurrency",
		[]string{"runner"},
		nil,
	)
	BuildCollectorInstance = &BuildCollector{}
)

type statePermutation struct {
	runner      string
	jobState    common.JobState
	jobStage    common.JobStage
	pluginStage common.PluginStage
}
type BuildCollector struct {
	runner                     string
	jobs                       []*common.Job
	requests                   int
	requestConcurrencyExceeded int
	lock                       sync.Mutex
}

func (b *BuildCollector) AddJob(job *common.Job) {
	b.lock.Lock()
	defer b.lock.Unlock()
	b.jobs = append(b.jobs, job)
	JobsTotal.WithLabelValues(job.Runner.Token).Inc()
}
func (b *BuildCollector) AcquireRequest() bool {
	b.lock.Lock()
	defer b.lock.Unlock()
	b.requests++
	return true
}
func (b *BuildCollector) ReleaseRequest() bool {
	b.lock.Lock()
	defer b.lock.Unlock()
	b.requests--
	return false
}

func (b *BuildCollector) RemoveJob(job *common.Job) bool {
	b.lock.Lock()
	defer b.lock.Unlock()
	JobDurationHistogram.
		WithLabelValues(job.Runner.Token).
		Observe(time.Since(job.CreatedAt).Seconds())
	for idx, build := range b.jobs {
		if build == job {
			b.jobs = append(b.jobs[0:idx], b.jobs[idx+1:]...)

			return true
		}
	}
	return false
}

func statesAndStages(jobs []*common.Job) map[statePermutation]int {
	data := make(map[statePermutation]int)
	for _, job := range jobs {
		state := newStatePermutationFromBuild(job)
		data[state]++
	}
	return data
}
func newStatePermutationFromBuild(job *common.Job) statePermutation {
	return statePermutation{
		runner:      job.Runner.Token,
		jobState:    job.Trace.GetState(),
		jobStage:    job.CurrentStage,
		pluginStage: job.PluginStage,
	}
}

// Describe implements prometheus.Collector.
func (b *BuildCollector) Describe(ch chan<- *prometheus.Desc) {
	ch <- numBuildsDesc
	ch <- requestConcurrencyDesc
	ch <- requestConcurrencyExceededDesc

	JobsTotal.Describe(ch)
	JobDurationHistogram.Describe(ch)
}

// Collect implements prometheus.Collector.
func (b *BuildCollector) Collect(ch chan<- prometheus.Metric) {
	jobs := statesAndStages(b.jobs)
	for state, count := range jobs {
		ch <- prometheus.MustNewConstMetric(
			numBuildsDesc,
			prometheus.GaugeValue,
			float64(count),
			b.runner,
			string(state.jobState),
			string(state.jobStage),
			string(state.pluginStage),
		)
	}

	ch <- prometheus.MustNewConstMetric(
		requestConcurrencyDesc,
		prometheus.GaugeValue,
		float64(b.requests),
		b.runner,
	)

	ch <- prometheus.MustNewConstMetric(
		requestConcurrencyExceededDesc,
		prometheus.CounterValue,
		float64(b.requestConcurrencyExceeded),
		b.runner,
	)

	JobsTotal.Collect(ch)
	JobDurationHistogram.Collect(ch)
}

func NewBuildCollector(config *common.RunnerConfig) *BuildCollector {
	BuildCollectorInstance = &BuildCollector{runner: config.Token}
	return BuildCollectorInstance
}
