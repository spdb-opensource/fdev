package zip

import (
	"archive/zip"
	"fmt"
	file_helper "github.com/fdev-ci/fdev-ci-runner/pkg/utlis/file"
	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/log"
	"io"
	"os"
	"path/filepath"
	"strings"
)

var logger = log.NewLogger()

// 压缩多个文件压缩成一个工件，传入到
// filename: 压缩完成后的zip文件的全路径
// files: 需要添加到zip文件里面的文件
// ciProjectDir: 项目的根路径
func ArtifactsZip(filename string, files []string, ciProjectDir string) (allAbsoluteFiles []string, err error) {
	for i := range files {
		absFile := filepath.Join(ciProjectDir, files[i])
		matches, err := filepath.Glob(absFile)
		if err != nil {
			return allAbsoluteFiles, err
		}
		if len(matches) == 0 {
			logger.Warningf("%v: no matching files", files[i])
		} else {
			fmt.Printf("%v: found %d matching files and directories\n", files[i], len(matches))
			for i := range matches {
				if i >= 10 {
					fmt.Println("...")
					break
				}
				subdir := strings.Replace(matches[i], ciProjectDir, "", 1)
				fmt.Println(subdir)
			}
		}
		allAbsoluteFiles = append(allAbsoluteFiles, matches...)
	}

	allFiles := make([]*os.File, 0, 10)
	for i := range allAbsoluteFiles {
		f, err := os.Open(allAbsoluteFiles[i])
		if err != nil {
			return allAbsoluteFiles, err
		}
		allFiles = append(allFiles, f)
	}
	err = Compress(allFiles, filename, ciProjectDir)
	if err != nil {
		return allAbsoluteFiles, err
	}
	return allAbsoluteFiles, nil
}

// 压缩文件
// files 文件数组，可以是不同dir下的文件或者文件夹
// dest 压缩文件存放地址
func Compress(files []*os.File, dest string, parentDir string) error {
	d, err := file_helper.CreateIfNotExists(dest)
	if err != nil {
		return err
	}
	defer d.Close()
	w := zip.NewWriter(d)
	defer w.Close()
	for _, file := range files {
		err := compress(file, "", w, parentDir)
		if err != nil {
			return err
		}
	}
	return nil
}

func compress(file *os.File, prefix string, zw *zip.Writer, parentDir string) error {
	info, err := file.Stat()
	if err != nil {
		return err
	}
	if info.IsDir() {
		prefix = prefix + "/" + info.Name()
		fileInfos, err := file.Readdir(-1)
		if err != nil {
			return err
		}
		for _, fi := range fileInfos {
			f, err := os.Open(file.Name() + "/" + fi.Name())
			if err != nil {
				return err
			}
			err = compress(f, prefix, zw, parentDir)
			if err != nil {
				return err
			}
		}
	} else {
		header, err := zip.FileInfoHeader(info)
		header.Name = strings.TrimPrefix(strings.Replace(file.Name(), parentDir, "", -1), string(os.PathSeparator))

		if err != nil {
			return err
		}
		writer, err := zw.CreateHeader(header)
		if err != nil {
			return err
		}
		_, err = io.Copy(writer, file)
		file.Close()
		if err != nil {
			return err
		}
	}
	return nil
}

//解压
func DeCompress(zipFile, dest string) error {
	reader, err := zip.OpenReader(zipFile)
	if err != nil {
		return err
	}
	defer reader.Close()
	for _, file := range reader.File {
		rc, err := file.Open()
		if err != nil {
			return err
		}
		defer rc.Close()
		filename := filepath.Join(dest, file.Name)
		w, err := file_helper.CreateIfNotExists(filename)
		if err != nil {
			return err
		}
		defer w.Close()
		_, err = io.Copy(w, rc)
		if err != nil {
			return err
		}
		w.Close()
		rc.Close()
	}
	return nil
}

func getDir(path string) string {
	return subString(path, 0, strings.LastIndex(path, "/"))
}

func subString(str string, start, end int) string {
	rs := []rune(str)
	length := len(rs)

	if start < 0 || start > length {
		panic("start is wrong")
	}

	if end < start || end > length {
		panic("end is wrong")
	}

	return string(rs[start:end])
}
