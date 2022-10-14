package urlhelpers

import (
	"net/url"
	"strings"
)

func CleanURL(value string) (ret string) {
	u, err := url.Parse(value)
	if err != nil {
		return
	}
	u.User = nil
	u.RawQuery = ""
	u.Fragment = ""
	return u.String()
}

func GetObjectName(u string) string {
	s := "artifactory/"
	return u[strings.Index(u, s)+len(s):]
}
