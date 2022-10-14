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
else
         echo 'illegal branch'
         exit 1
fi
 
echo "current profile: "$SPRING_PROFILE_ACTIVE
java -jar -Xmx256m -DSPRING_PROFILE_ACTIVE=$SPRING_PROFILE_ACTIVE /ebank/app.jar
