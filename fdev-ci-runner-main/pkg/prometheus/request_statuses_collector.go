package prometheus

import (
	"github.com/prometheus/client_golang/prometheus"
	"strconv"
	"sync"
)

var apiRequestStatuses = prometheus.NewDesc(
	"fdev_ci_runner_api_request_statuses_total",
	"The total number of api requests, partitioned by runner, endpoint and status.",
	[]string{"runner", "endpoint", "status"},
	nil,
)

type APIEndpoint string

const (
	APIEndpointRequestJob APIEndpoint = "request_job"
	APIEndpointUpdateJob  APIEndpoint = "update_job"
	APIEndpointPatchTrace APIEndpoint = "patch_trace"
)

type apiRequestStatusPermutation struct {
	runnerID string
	endpoint APIEndpoint
	status   int
}

type APIRequestStatusesMap struct {
	internal map[apiRequestStatusPermutation]int
	lock     sync.RWMutex
}

func (arspm *APIRequestStatusesMap) Append(runnerID string, endpoint APIEndpoint, status int) {
	arspm.lock.Lock()
	defer arspm.lock.Unlock()

	permutation := apiRequestStatusPermutation{runnerID: runnerID, endpoint: endpoint, status: status}

	if _, ok := arspm.internal[permutation]; !ok {
		arspm.internal[permutation] = 0
	}

	arspm.internal[permutation]++
}

// Describe implements prometheus.Collector.
func (arspm *APIRequestStatusesMap) Describe(ch chan<- *prometheus.Desc) {
	ch <- apiRequestStatuses
}

// Collect implements prometheus.Collector.
func (arspm *APIRequestStatusesMap) Collect(ch chan<- prometheus.Metric) {
	arspm.lock.RLock()
	defer arspm.lock.RUnlock()

	for permutation, count := range arspm.internal {
		ch <- prometheus.MustNewConstMetric(
			apiRequestStatuses,
			prometheus.CounterValue,
			float64(count),
			permutation.runnerID,
			string(permutation.endpoint),
			strconv.Itoa(permutation.status),
		)
	}
}

func NewAPIRequestStatusesMap() *APIRequestStatusesMap {
	return &APIRequestStatusesMap{
		internal: make(map[apiRequestStatusPermutation]int),
	}
}

var (
	RequestStatusesCollector = NewAPIRequestStatusesMap()
)
