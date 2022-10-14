GO              ?= GO15VENDOREXPERIMENT=1 go
GOPATH          := $(firstword $(subst :, ,$(shell $(GO) env GOPATH)))
GOLANGCI_LINT   ?= $(GOPATH)/bin/golangci-lint
BIN_DIR         ?= $(shell pwd)/bin
GIT_BRANCH      ?= `git symbolic-ref --short -q HEAD`
GIT_COMMIT      ?= `git rev-parse --short HEAD`
BUILD_DATE      ?= `date +%FT%T%z`
LDFLAGS		    ?= -ldflags "-w -s -X github.com/fdev-ci/fdev-ci-runner/pkg/version.GitBranch=${GIT_BRANCH}  -X  github.com/fdev-ci/fdev-ci-runner/pkg/version.GitCommit=${GIT_COMMIT} -X github.com/fdev-ci/fdev-ci-runner/pkg/version.BuildDate=${BUILD_DATE}"




ci-runner: clean lint linux
ci-runner-helper: helper-linux

lint:
	@echo ">> linting code"
	@$(GOLANGCI_LINT) run

linux:
	GOARCH=amd64 GOOS=linux go build  -o ${BIN_DIR}/ci-runner-linux ${LDFLAGS} cmd/ci-runner/ci-runner.go

macos:
	GOARCH=amd64 GOOS=darwin go build  -o ${BIN_DIR}/ci-runner-macos ${LDFLAGS} cmd/ci-runner/ci-runner.go

windows:
	GOARCH=amd64 GOOS=windows go build  -o ${BIN_DIR}/ci-runner-windows ${LDFLAGS} cmd/ci-runner/ci-runner.go

linux-deploy:
	scp -P9099 bin/ci-runner-linux appservice@xxx:/appservice/luotao

helper-linux:
	GOARCH=amd64 GOOS=linux go build  -o ${BIN_DIR}/ci-runner-helper-linux ${LDFLAGS} cmd/ci-runner-helper/ci-runner-helper.go

helper-macos:
	GOARCH=amd64 GOOS=darwin go build  -o ${BIN_DIR}/ci-runner-helper-macos ${LDFLAGS} cmd/ci-runner-helper/ci-runner-helper.go

helper-windows:
	GOARCH=amd64 GOOS=windows go build  -o ${BIN_DIR}/ci-runner-helper-windows ${LDFLAGS} cmd/ci-runner-helper/ci-runner-helper.go

helper-deploy:
	scp -P9099 bin/ci-runner-helper-linux appservice@xxx:/appservice/luotao

clean:
	rm -rf bin/*
