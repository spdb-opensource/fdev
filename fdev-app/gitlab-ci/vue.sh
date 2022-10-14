#!/bin/bash

projectname=$1

mkdir /tmp/$projectname

cp -r /ebank/fdevapp-base-vue/* /tmp/$projectname

sed -i "s/fdevapp-base-vue/$projectname/g" /tmp/$projectname/package.json

sed -i "s/fdevapp-base-vue/$projectname/g" /tmp/$projectname/package-lock.json

sed -i "s/fdevapp-base-vue/$projectname/g" /tmp/$projectname/README.md

sed -i "s/fdevapp-base-vue/$projectname/g" /tmp/$projectname/public/index.html

echo "vue create success"
