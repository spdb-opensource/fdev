FROM xxx/mbpe-public-docker-local/library/fdev-base:0.2
ADD fdev-report-0.0.1-SNAPSHOT.jar  /ebank/app.jar
ADD start.sh /ebank/start.sh
RUN chmod +x -R /ebank
ENTRYPOINT ["sh" , "/ebank/start.sh"]