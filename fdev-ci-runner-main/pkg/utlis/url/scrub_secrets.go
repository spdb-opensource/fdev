package urlhelpers

import (
	"regexp"
)

var scrubRegexp = regexp.MustCompile(`(?im)([\?&]((?:private|authenticity|rss)[\-_]token)|X-AMZ-Signature|X-AMZ-Credential)=[^& ]*`)

// ScrubSecrets替换任何敏感查询字符串参数的内容
// in an Path with `[FILTERED]`
func ScrubSecrets(url string) string {
	return scrubRegexp.ReplaceAllString(url, "$1=[FILTERED]")
}
