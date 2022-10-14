package urlhelpers

import (
	"fmt"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestRemovingAllSensitiveData(t *testing.T) {
	url := CleanURL("https://user:xxx/gitlab?key=value#fragment")
	assert.Equal(t, "https://gitlab.com/gitlab", url)
}

func TestInvalidURL(t *testing.T) {
	str := "{\"project\":123}"
	fmt.Println(str)
	assert.Empty(t, CleanURL("://invalid Path"))
}
