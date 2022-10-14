FROM xxx/mbpe-public-docker-local/library/fdev-base:0.2
ADD fdev-app-0.0.1-SNAPSHOT.jar  /ebank/app.jar
ADD start.sh /ebank/start.sh
ADD archetype.sh /ebank/archetype.sh
COPY vue.sh /ebank
COPY fdevapp-base-vue /ebank/fdevapp-base-vue
RUN chmod +x -R /ebank

ENTRYPOINT ["sh" , "/ebank/start.sh"]