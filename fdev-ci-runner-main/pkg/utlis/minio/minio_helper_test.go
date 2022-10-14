package minio

import "testing"
import "github.com/stretchr/testify/assert"

func TestNewMinioHelper(t *testing.T) {

	minioConfig := &Config{
		URL:       "xxx:9000",
		Bucket:    "fdev-ci",
		AccessKey: "admin",
		SecretKey: "12345678",
	}
	minioClient := NewMinioHelper(*minioConfig)
	assert.NotNil(t, minioClient)
	objectName := "2020-12-08/xxyyzz/stage-0/job-0/plugin-0/artifacts.zip"
	err := minioClient.GetObject(objectName, "/tmp/artifacts.zip")
	assert.NoError(t, err)
}
