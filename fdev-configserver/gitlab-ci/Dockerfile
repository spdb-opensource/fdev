FROM xxx/library/fdev-base:0.2

ENV LANG "en_US.utf8"
ENV TZ Asia/Shanghai

ADD fdev-configserver-0.0.1-SNAPSHOT.jar  /ebank/app.jar
ADD start.sh /ebank/start.sh 

ENTRYPOINT ["sh" , "/ebank/start.sh"]
