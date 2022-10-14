FROM xxx/mbpe-public-docker-local/library/frelease-base:latest

ENV LANG "en_US.utf8"
ENV TZ Asia/Shanghai

ADD fdev-component-0.0.1-SNAPSHOT.jar  /ebank/app.jar
ADD start.sh /ebank/start.sh
ADD apache-maven-3.6.0-bin.tar.gz /usr/share
COPY settings.xml /usr/share/apache-maven-3.6.0/conf/settings.xml
COPY scripts/* /scripts/

#设置node
ADD node-v12.13.1-linux-x64.tar.xz /usr/share
ENV NODE_HOME=/usr/share/node-v12.13.1-linux-x64

ENV PATH=/usr/share/node-v12.13.1-linux-x64/bin:/usr/share/apache-maven-3.6.0/bin:/usr/share/jdk1.8.0_151/bin:$PATH

ENTRYPOINT ["sh" , "/ebank/start.sh"]