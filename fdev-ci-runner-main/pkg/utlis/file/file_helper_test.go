package file

import (
	"fmt"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestCreateFile(t *testing.T) {
	err := CreateFile("12123", "/Users/luolei/Desktop/03/test.md5")
	assert.NoError(t, err)
}
func TestCheckMD5(t *testing.T) {
	result := CheckMD5("12123", "/Users/luolei/Desktop/03/test.md5")
	fmt.Print(result)
}

func TestCreatePluginProfile(t *testing.T) {
	err := CreatePluginProfile("/tmp/logs")
	assert.NoError(t, err)
}
