if [ -d "/ebank/spdb/params/config/rasp-2020-09-04/rasp/" ];then
  cp -r /ebank/spdb/params/config/rasp-2020-09-04/rasp /ebank
  export JAVA_OPTIONS=$JAVA_OPTIONS" -javaagent:/ebank/rasp/rasp.jar"
fi