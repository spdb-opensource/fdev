FROM xxx/mbpe-public-docker-local/library/frelease-base:latest

ENV LANG "en_US.utf8"
ENV TZ Asia/Shanghai

ADD fdev-release-0.0.1-SNAPSHOT.jar  /ebank/app.jar
ADD start.sh /ebank/start.sh
COPY scripts/ /scripts/
RUN chmod +x -R /ebank

ENTRYPOINT ["sh" , "/ebank/start.sh"]