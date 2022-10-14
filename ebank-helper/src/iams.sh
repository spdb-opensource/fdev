if [ -e "/ebank/spdb/temp/iams/agent-boot-1.0.0-SNAPSHOT.jar" ];then
  cp -r /ebank/spdb/temp/iams /ebank
  export JAVA_OPTIONS=$JAVA_OPTIONS"  -javaagent:/ebank/iams/agent-boot-1.0.0-SNAPSHOT.jar=agent_lib=/ebank/iams/agent-core-1.0.0-SNAPSHOT.jar;serverAddr=xxx;namespace=sit1-dmz;username=nacos_fresource;password={AES}iw2ZS7pKkM/jZStHNEifYw==;nacos_group=per_config;nacos_data_id=spdb-mock-agent.properties "

fi
