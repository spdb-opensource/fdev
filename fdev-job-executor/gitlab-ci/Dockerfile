FROM xxx/mbpe-public-docker-local/library/centos:base

ADD fdev-job-executor-0.0.1-SNAPSHOT.jar  /ebank/app.jar
ADD start.sh /ebank/start.sh

ENTRYPOINT ["sh" , "/ebank/start.sh"]