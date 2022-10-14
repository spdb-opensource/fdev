package file

import (
	"bufio"
	"crypto/md5"
	"encoding/json"
	"fmt"
	"io"
	"io/fs"
	"io/ioutil"
	"os"
	"path/filepath"
)

func CreateFile(content string, filePath string) error {
	file, err := os.OpenFile(filePath, os.O_RDWR|os.O_CREATE|os.O_TRUNC, os.ModePerm)
	if err != nil {
		return err
	}
	defer file.Close()
	_, err = file.WriteString(content)
	if err != nil {
		return err
	}
	return nil
}

func CheckMD5(md5 string, md5FilePath string) bool {
	file, err := os.OpenFile(md5FilePath, os.O_RDWR, os.ModePerm)
	if err != nil {
		return false
	}
	reader := bufio.NewReader(file)
	defer file.Close()
	content, _ := reader.ReadString('\n')
	return content == md5
}

// 判断所给路径文件/文件夹是否存在
func Exists(path string) bool {
	// os.Stat获取文件信息
	_, err := os.Stat(path)
	if err != nil {
		return os.IsExist(err)
	}
	return true
}

func GetMd5(filename string) (string, error) {
	f, err := ioutil.ReadFile(filename)
	if err != nil {
		return "", err
	}
	sum := md5.Sum(f)
	return fmt.Sprintf("%x", sum), nil
}

func CopyFile(srcFileName string, dstFileName string) (written int64, err error) {
	srcFile, err := os.Open(srcFileName)
	if err != nil {
		return 0, err
	}
	defer srcFile.Close()
	dstFile, err := CreateIfNotExists(dstFileName)
	if err != nil {
		return 0, err
	}
	defer dstFile.Close()
	return io.Copy(dstFile, srcFile)
}

/**
 * 同级目录下，由input.json生成plugin_profile文件
 */
func CreatePluginProfile(fileDir string) error {
	filePath := fmt.Sprintf("%s/%s", fileDir, "input.json")
	content, _ := ioutil.ReadFile(filePath)
	var data map[string]interface{}
	err := json.Unmarshal(content, &data)
	if err != nil {
		return err
	}
	line := ""
	for key, value := range data {
		line = fmt.Sprintf("%sexport %s=%s\n", line, key, value)
	}
	saveFile := fmt.Sprintf("%s/%s", fileDir, "plugin_profile")
	err = CreateFile(line, saveFile)
	if err != nil {
		return err
	}
	return nil
}

func CreateIfNotExists(p string) (*os.File, error) {
	if err := os.MkdirAll(filepath.Dir(p), 0755); err != nil {
		return nil, err
	}
	return os.Create(p)
}
func WriteFile(name string, data []byte, perm fs.FileMode) error {
	if err := os.MkdirAll(filepath.Dir(name), 0755); err != nil {
		return err
	}
	return os.WriteFile(name, data, perm)
}

func GetFileSize(path string) int64 {
	fi, err := os.Stat(path)
	if err != nil {
		return 0
	}
	return fi.Size()
}
