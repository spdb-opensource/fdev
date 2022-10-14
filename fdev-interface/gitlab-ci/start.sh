#!/usr/bin/env bash
echo "current slug: "$CI_ENVIRONMENT_SLUG
SPRING_PROFILE_ACTIVE=''
if [[ `echo ${CI_ENVIRONMENT_SLUG} | grep -i master` ]]
then
         SPRING_PROFILE_ACTIVE=pro
elif [[ `echo ${CI_ENVIRONMENT_SLUG} | grep -i uat` ]]
then
         SPRING_PROFILE_ACTIVE=uat
elif [[ `echo ${CI_ENVIRONMENT_SLUG} | grep -i sit` ]]
then
         SPRING_PROFILE_ACTIVE=sit
elif [[ `echo ${CI_ENVIRONMENT_SLUG} | grep -i rel` ]]
then
         SPRING_PROFILE_ACTIVE=rel
else
         echo 'illegal branch'
         exit 1
fi

echo "current profile: "$SPRING_PROFILE_ACTIVE
if [[ `echo ${CI_ENVIRONMENT_SLUG} | grep -i master` ]]
then
    java -javaagent:/usr/skywalking/agent/skywalking-agent.jar -jar -Xmx2048m -Xms2048m -DSPRING_PROFILE_ACTIVE=$SPRING_PROFILE_ACTIVE -Dspring.cloud.config.uri=http://fdev-configserver-master-svc:8080 /ebank/app.jar
else
    java -javaagent:/usr/skywalking/agent/skywalking-agent.jar -jar -Xmx2048m -Xms2048m -DSPRING_PROFILE_ACTIVE=$SPRING_PROFILE_ACTIVE -Dspring.cloud.config.uri=http://fdev-configserver-test-svc:8080 /ebank/app.jar
fi