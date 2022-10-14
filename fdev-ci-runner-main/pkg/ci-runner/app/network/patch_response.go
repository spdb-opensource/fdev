package network

import (
	"net/http"
	"strconv"
	"strings"
)

type TracePatchResponse struct {
	*RemoteJobStateResponse

	RemoteRange string
}

func (p *TracePatchResponse) NewOffset() int {
	remoteRangeParts := strings.Split(p.RemoteRange, "-")
	if len(remoteRangeParts) == 2 {
		newOffset, _ := strconv.Atoi(remoteRangeParts[1])
		return newOffset
	}

	return 0
}

func NewTracePatchResponse(response *http.Response) *TracePatchResponse {
	return &TracePatchResponse{
		RemoteJobStateResponse: NewRemoteJobStateResponse(response),
		RemoteRange:            response.Header.Get("Range"),
	}
}

type RemoteJobStateResponse struct {
	StatusCode  int
	RemoteState string
}

func (r *RemoteJobStateResponse) IsAborted() bool {
	if r.RemoteState == "canceled" || r.RemoteState == "failed" {
		return true
	}

	if r.StatusCode == http.StatusForbidden {
		return true
	}

	return false
}

func NewRemoteJobStateResponse(response *http.Response) *RemoteJobStateResponse {
	if response == nil {
		return &RemoteJobStateResponse{}
	}

	return &RemoteJobStateResponse{
		StatusCode:  response.StatusCode,
		RemoteState: response.Header.Get("Job-Status"),
	}
}
