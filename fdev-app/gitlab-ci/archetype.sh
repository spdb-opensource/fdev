#!/bin/bash

groupId=$1
artifactId=$2
applicationName=$3
version=$4

cd /tmp && mvn archetype:generate -DgroupId=com.spdb -DartifactId=$applicationName -DpackageName=com.spdb -Dversion=0.0.1-SNAPSHOT -DapplicationName=$applicationName -DarchetypeGroupId=$groupId -DarchetypeArtifactId=$artifactId -DarchetypeVersion=$version -DinteractiveMode=false


