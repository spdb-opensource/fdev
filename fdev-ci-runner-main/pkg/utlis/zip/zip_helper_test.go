package zip

import (
	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/file"
	"os"
	"path/filepath"
	"testing"
)

func TestCompress(t *testing.T) {
	homeDir, err := os.UserHomeDir()
	if err != nil {
		t.Fatal(err)
	}
	testPath := filepath.Join(homeDir, "TestCompress")
	f1, err := file.CreateIfNotExists(filepath.Join(testPath, "/01/mspmk-web-p2b/target/luotao.jar"))
	if err != nil {
		t.Fatal(err)
	}
	defer f1.Close()
	f2, err := file.CreateIfNotExists(filepath.Join(testPath + "/01/mspmk-web-p2b/luotao.jar"))
	if err != nil {
		t.Fatal(err)
	}
	defer f2.Close()
	f3, err := file.CreateIfNotExists(filepath.Join(testPath + "/01/mspmk-web-p2b/dist/index.html"))
	if err != nil {
		t.Fatal(err)
	}
	defer f3.Close()
	//var files = []*os.File{f1, f2, f3}
	files := make([]string, 0, 10)
	files = append(files, testPath+"/01/mspmk-web-p2b/target/")
	files = append(files, testPath+"/01/mspmk-web-p2b/*.jar")
	files = append(files, testPath+"/01/mspmk-web-p2b/dist/index.html")

	dest := filepath.Join(testPath, "/02/test2.zip")
	ciProjectDir := filepath.Join(testPath, "01")
	_, err = ArtifactsZip(dest, files, ciProjectDir)
	if err != nil {
		t.Fatal(err)
	}
}
func TestDeCompress(t *testing.T) {
	homeDir, err := os.UserHomeDir()
	if err != nil {
		t.Fatal(err)
	}
	testPath := filepath.Join(homeDir, "TestCompress")
	zipPath := filepath.Join(testPath, "/02/test2.zip")
	destPath := filepath.Join(testPath, "/02")
	err = DeCompress(zipPath, destPath)
	if err != nil {
		t.Fatal(err)
	}
}
