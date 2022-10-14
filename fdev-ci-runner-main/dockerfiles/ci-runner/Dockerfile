FROM centos:base
COPY ci-runner-linux /usr/bin/ci-runner

RUN mkdir -p /logs
CMD ["ci-runner","run"]