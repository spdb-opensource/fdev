package minio

import (
	"context"
	"github.com/minio/minio-go/v7"
	"github.com/minio/minio-go/v7/pkg/credentials"
	"github.com/sirupsen/logrus"
)

var instance *Helper

type Config struct {
	URL       string
	Bucket    string
	AccessKey string
	SecretKey string
}
type Helper struct {
	minioConfig Config
	minioClient *minio.Client
}

func (mh *Helper) PutObject(objectName string, filePath string) (minio.UploadInfo, error) {
	return mh.minioClient.FPutObject(context.Background(), mh.minioConfig.Bucket, objectName, filePath, minio.PutObjectOptions{ContentType: "application/text"})
}

func (mh *Helper) GetObject(objectName string, filePath string) error {
	return mh.minioClient.FGetObject(context.Background(),
		mh.minioConfig.Bucket,
		objectName,
		filePath,
		minio.GetObjectOptions{})
}

func NewMinioHelper(minioConfig Config) *Helper {
	if instance == nil {
		client, err := minio.New(minioConfig.URL, &minio.Options{Creds: credentials.NewStaticV4(minioConfig.AccessKey, minioConfig.SecretKey, "")})
		if err != nil {
			logrus.Errorln(err)
			return nil
		}
		instance = &Helper{minioClient: client}
		instance.minioConfig = minioConfig
	}
	return instance
}
