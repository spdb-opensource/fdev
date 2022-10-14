FROM xxx/mbpe-public-docker-local/library/fdev-base:0.2

ADD fdev-job-scheduler-0.0.1-SNAPSHOT.jar  /ebank/app.jar
ADD start.sh /ebank/start.sh

ENTRYPOINT ["sh" , "/ebank/start.sh"]