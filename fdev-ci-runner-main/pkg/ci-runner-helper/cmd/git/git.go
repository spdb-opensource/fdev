package git

import (
	"fmt"
	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/ansi"
	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/file"
	"github.com/fdev-ci/fdev-ci-runner/pkg/utlis/log"
	urlhelpers "github.com/fdev-ci/fdev-ci-runner/pkg/utlis/url"
	"github.com/spf13/cobra"
	"os"
	"os/exec"
)

var logger = log.NewLogger()

func NewCmdGit() *cobra.Command {

	cmd := &cobra.Command{
		Use:   "git",
		Short: "git clone source code",
		Run: func(cmd *cobra.Command, args []string) {
			ciProjectURL := os.Getenv("CI_PROJECT_URL")
			ciProjectBranch := os.Getenv("CI_PROJECT_BRANCH")
			ciProjectDir := os.Getenv("CI_PROJECT_DIR")
			if file.Exists(ciProjectDir) {
				fmt.Println(ansi.FdevGreen + "Git repository Exists." + ansi.FdevRESET)
				_ = gitClean(ciProjectDir)
				os.Exit(0)
			}
			fmt.Println(ansi.FdevGreen + "Git clone..." + ansi.FdevRESET)
			fmt.Println("From Git repository " + urlhelpers.CleanURL(ciProjectURL) + " branch " + ciProjectBranch)
			c := exec.Command("git", "clone", "--quiet", ciProjectURL, "-b", ciProjectBranch, ciProjectDir)
			output, err := c.CombinedOutput()
			if err != nil {
				logger.Errorln(string(output))
				os.Exit(1)
			}
		},
	}
	return cmd

}

func gitClean(dir string) error {
	c := exec.Command("git", "clean", "-xdff")
	c.Dir = dir
	output, err := c.CombinedOutput()
	if err != nil {
		logger.Errorln(string(output))
		return err
	}
	return nil
}
