package common

import (
	"encoding/json"
	"testing"
)

func TestRunnerConfig_LoadConfig(t *testing.T) {
	config := RunnerConfig{
		Log: "/log/ci-runner.log",
	}
	err := config.LoadConfig("../../../../config/config_yuxinqi.toml")
	if err != nil {
		t.Error(err)
	}
	bytes, err := json.MarshalIndent(config, "", " ")
	if err != nil {
		t.Error(err)
	}
	t.Log(string(bytes))
}
