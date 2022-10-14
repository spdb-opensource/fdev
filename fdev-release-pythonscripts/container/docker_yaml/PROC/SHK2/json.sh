#!/bin/sh

function paramfillnull
{
unset kind
unset obname
unset replicasnu
unset obyml
unset namespace
unset stime
unset gtime
unset containername
unset port
unset tag
unset imagename
unset imagespacename
unset imagespaceip
unset ctime
unset msvcstatustext
unset msvcdowntext
unset msvcdeletetext
unset msvcuptext
unset preliminarycheck
unset checkanddown
unset zeroreplica
unset applyyaml
unset updateimage
unset scalereplicas
unset checkandup
unset deletepo
unset deletefirstpo
unset container_num
unset port_num
unset tag_num
unset imagename_num
unset imagespaceip_num
unset imagespacename_num
unset ctime_num
unset msvcstatustext_num
unset msvcdowntext_num
unset msvcdeletetext_num
unset msvcuptext_num

unset check_namespace 
unset check_deploymentname
unset check_metadatalabels
unset check_matchlabels
unset check_templatemetadatalabels
unset check_replicas
unset check_volumes
unset check_hostaliases
unset check_imagepullsecrets
unset check_dnspolicy
unset check_nameservers
unset check_strategy
unset check_matchexpressionskey
unset check_matchexpressionsvalues
unset check_containersname
unset check_limitscpu
unset check_limitsmemory
unset check_requestscpu
unset check_requestsmemory
unset check_ports
unset check_env
unset check_envfrom
unset check_prestop
unset check_image
unset check_volumemounts
}

function paramautfill
{
	if [ ! ${check_namespace} ];then
		check_namespace=1
	fi
	if [ ! ${check_deploymentname} ];then
		check_deploymentname=1
	fi
	if [ ! ${check_metadatalabels} ];then
		check_metadatalabels=1
	fi
	if [ ! ${check_matchlabels} ];then
		check_matchlabels=1
	fi
	if [ ! ${check_templatemetadatalabels} ];then
		check_templatemetadatalabels=1
	fi
        if [ ! ${check_replicas} ];then
                check_replicas=1
        fi
	if [ ! ${check_volumes} ];then
		check_volumes=1
	fi
	if [ ! ${check_hostaliases} ];then
		check_hostaliases=1
	fi
	if [ ! ${check_imagepullsecrets} ];then
		check_imagepullsecrets=1
	fi
	if [ ! ${check_dnspolicy} ];then
		check_dnspolicy=1
	fi
	if [ ! ${check_nameservers} ];then
		check_nameservers=1
	fi
	if [ ! ${check_strategy} ];then
		check_strategy=1
	fi
	if [ ! ${check_matchexpressionskey} ];then
		check_matchexpressionskey=1
	fi
	if [ ! ${check_matchexpressionsvalues} ];then
		check_matchexpressionsvalues=1
	fi
	if [ ! ${check_containersname} ];then
		check_containersname=1
	fi
	if [ ! ${check_limitscpu} ];then
		check_limitscpu=1
	fi
	if [ ! ${check_requestscpu} ];then
		check_requestscpu=1
	fi
	if [ ! ${check_limitsmemory} ];then
		check_limitsmemory=1
	fi
	if [ ! ${check_requestsmemory} ];then
		check_requestsmemory=1
	fi
	if [ ! ${check_ports} ];then
		check_ports=1
	fi
	if [ ! ${check_env} ];then
		check_env=1
	fi
	if [ ! ${check_envfrom} ];then
		check_envfrom=1
	fi
	if [ ! ${check_prestop} ];then
		check_prestop=1
	fi	
	if [ ! ${check_image} ];then
		check_image=1
	fi	
	if [ ! ${check_volumemounts} ];then
		check_volumemounts=1
	fi

	if [ ! ${kind} ];then
		kind=deployment
	fi
	if [ ! ${obname} ];then
		obname=default
	fi	
	if [ ! ${replicasnu} ];then
		replicasnu=2
	fi
	if [ ! ${obyml} ];then
		obyml=default
	fi
	if [ ! ${namespace} ];then
		namespace=${namespace_org}
	fi
	if [ ! ${stime} ];then
		stime=15
	fi
	if [ ! ${gtime} ];then
		gtime=7
	fi
	if [ ! ${containername} ];then
		containername=Autofill
		container_num=${#containername[@]}
	else
		container_num=${#containername[@]}	
	fi
	if [ ! ${tag} ];then
		tag=default
		tag_num=${#tag[@]}
	else
		tag_num=${#tag[@]}
	fi
	if [ ! ${imagename} ];then
		imagename=${obname}
		imagename_num=${#imagename[@]}
	else
		imagename_num=${#imagename[@]}
	fi
	if [ ! ${port} ];then
		port=8080
		port_num=${#port[@]}
	else
		port_num=${#port[@]}
	fi
	if [ ! ${imagespacename} ];then
		imagespacename=${imagespacename_org}
		imagespacename_num=0
	else
	imagespacename_num=${#imagespacename[@]}	
	fi
	if [ ! ${imagespaceip} ];then
		imagespaceip=${imagespaceip_org}
		imagespaceip_num=0
	else
		imagespaceip_num=${#imagespaceip[@]}	
	fi
	if [ ! ${ctime} ];then
		ctime=15
		ctime_num=0
	else
	ctime_num=${#ctime[@]}
	fi

	if [ ! ${msvcstatustext} ];then
		for ((yy=0;yy<${container_num};yy++))
		do
			msvcstatustext[${yy}]="curl --fail --silent http://xxx:${port[${yy}]}/getstatus"
		done
		msvcstatustext_num=0
	else
		msvcstatustext_num=${#msvcstatustext[@]}
	fi
	if [ ! ${msvcdowntext} ];then
		for ((yy=0;yy<${container_num};yy++))
		do
			msvcdowntext[${yy}]="curl --fail --silent http://xxx:${port[${yy}]}/down"
		done
		msvcdowntext_num=0
	else
		msvcdowntext_num=${#msvcdowntext[@]}
	fi
	if [ ! ${msvcdeletetext} ];then
		for ((yy=0;yy<${container_num};yy++))
		do
			msvcdeletetext[${yy}]="curl --fail --silent http://xxx:${port[${yy}]}/delete"
		done
		msvcdeletetext_num=0
	else
		msvcdeletetext_num=${#msvcdeletetext[@]}
	fi
	if [ ! ${msvcuptext} ];then
		for ((yy=0;yy<${container_num};yy++))
		do
			msvcuptext[${yy}]="curl --fail --silent http://xxx:${port[${yy}]}/up"
		done
		msvcuptext_num=0
	else
		msvcuptext_num=${#msvcuptext[@]}
	fi	
		
	
	if [ ! ${preliminarycheck} ];then
		preliminarycheck=0
	fi
	if [ ! ${checkanddown} ];then
		checkanddown=0
	fi
	if [ ! ${zeroreplica} ];then
		zeroreplica=0
	fi
	if [ ! ${applyyaml} ];then
		applyyaml=0
	fi
	if [ ! ${updateimage} ];then
		updateimage=0
	fi
	if [ ! ${scalereplicas} ];then
		scalereplicas=0
	fi
	if [ ! ${checkandup} ];then
		checkandup=0
	fi
	if [ ! ${deletepo} ];then
		deletepo=0
	fi	
	if [ ! ${deletefirstpo} ];then
		deletefirstpo=0
	fi	
	
	if [ ${port_num} -ne ${container_num} -o ${tag_num} -ne ${container_num} -o ${imagename_num} -ne ${container_num} ];then
			showError 1 "Please check  the number of (port or tag or imagename),the number of these object must equal the number of container."	
	fi
	
	if [ ${imagespaceip_num} -ne ${container_num} -a ${imagespaceip_num} -gt 0 ];then
		showError 1 "Please check  the number of (imagespaceip),the number of these object must equal the number of container."	
	fi
	if [ ${imagespacename_num} -ne ${container_num} -a ${imagespacename_num} -gt 0 ];then
		showError 1 "Please check  the number of (imagespacename),the number of these object must equal the number of container."	
	fi
	if [ ${ctime_num} -ne ${container_num} -a ${ctime_num} -gt 0 ];then
		showError 1 "Please check  the number of (ctime),the number of these object must equal the number of container."	
	fi
	if [ ${msvcstatustext_num} -ne ${container_num} -a ${msvcstatustext_num} -gt 0 ];then
		showError 1 "Please check  the number of (msvcstatustext),the number of these object must equal the number of container."	
	fi
	if [ ${msvcdowntext_num} -ne ${container_num} -a ${msvcdowntext_num} -gt 0 ];then
		showError 1 "Please check  the number of (msvcdowntext),the number of these object must equal the number of container."	
	fi
	if [ ${msvcdeletetext_num} -ne ${container_num} -a ${msvcdeletetext_num} -gt 0 ];then
		showError 1 "Please check  the number of (msvcdeletetext),the number of these object must equal the number of container."	
	fi
	if [ ${msvcuptext_num} -ne ${container_num} -a ${msvcuptext_num} -gt 0 ];then
		showError 1 "Please check  the number of (msvcuptext),the number of these object must equal the number of container."	
	fi

	
	if [ ${container_num} -gt 1 ];then
		if [ ${port_num} -ne ${container_num} -o ${tag_num} -ne ${container_num} -o  ${imagename_num} -ne ${container_num} ];then
			showError 1 "Please check  the number of (port or tag or imagename),the number of these object must equal the number of container."		
		else
			if [ ${imagespaceip_num} -eq 0 ];then
				for ((ww=0;ww<${container_num};ww++))
				do
					imagespaceiptmp=${imagespaceip}
					imagespaceip[${ww}]=${imagespaceiptmp}
				done
			fi
			if [ ${imagespacename_num} -eq 0 ];then
				for ((ww=0;ww<${container_num};ww++))
				do
					imagespacenametmp=${imagespacename}
					imagespacename[${ww}]=${imagespacenametmp}
				done
			fi
			if [ ${ctime_num} -eq 0 ];then
				for ((ww=0;ww<${container_num};ww++))
				do
					ctimetmp=${ctime}
					ctime[${ww}]=${ctimetmp}
				done
			fi

		fi
		
	fi

	for ((ww=0;ww<${container_num};ww++))
	do
		msvcstatustexttmp=${msvcstatustext[${ww}]}
		msvcstatustexttmp=`echo ${msvcstatustexttmp}|sed 's#\/#\\\/#g'`
		msvcstatustext[${ww}]=${msvcstatustexttmp}
		msvcdowntexttmp=${msvcdowntext[${ww}]}
		msvcdowntexttmp=`echo ${msvcdowntexttmp}|sed 's#\/#\\\/#g'`
		msvcdowntext[${ww}]=${msvcdowntexttmp}
		msvcdeletetexttmp=${msvcdeletetext[${ww}]}
		msvcdeletetexttmp=`echo ${msvcdeletetexttmp}|sed 's#\/#\\\/#g'`
		msvcdeletetext[${ww}]=${msvcdeletetexttmp}
		msvcuptexttmp=${msvcuptext[${ww}]}
		msvcuptexttmp=`echo ${msvcuptexttmp}|sed 's#\/#\\\/#g'`
		msvcuptext[${ww}]=${msvcuptexttmp}
	done
	container_num=${#containername[@]}
	port_num=${#port[@]}
	tag_num=${#tag[@]}
	imagename_num=${#imagename[@]}
	
	imagespaceip_num=${#imagespaceip[@]}
	imagespacename_num=${#imagespacename[@]}
	ctime_num=${#ctime[@]}
	msvcstatustext_num=${#msvcstatustext[@]}
	msvcdowntext_num=${#msvcdowntext[@]}
	msvcdeletetext_num=${#msvcdeletetext[@]}
	msvcuptext_num=${#msvcuptext[@]}
	
	if [ ${port_num} -ne ${container_num} -o ${tag_num} -ne ${container_num} -o  ${imagename_num} -ne ${container_num} -o ${imagespaceip_num} -ne ${container_num} -o ${imagespacename_num} -ne ${container_num} -o  ${ctime_num} -ne ${container_num} -o ${msvcstatustext_num} -ne ${container_num} -o ${msvcdowntext_num} -ne ${container_num} -o  ${msvcdeletetext_num} -ne ${container_num} -o ${msvcuptext_num} -ne ${container_num} ];then
			showError 1 "Please check  the number of ( port or tag or imagename or imagespaceip or imagespacename or ctime OR msvcstatus OR msvcdown OR msvcdelete OR msvcup),the number of these object must equal the number of container."	
	fi


}



function checkparam
{
	case ${module} in
	docker)
		if [ ! ${tag} ];then
			showError 1 "ERROR. Please input image tag."
		fi
		if [ ! ${obname} ];then
			showError 1 "ERROR. Please input object name."
		fi
		if [ ! ${port} ];then
			showError 1 "ERROR. Please input port name."
		fi

			;;
	docker_all)
		if [ ! ${tag} ];then
			showError 1 "ERROR. Please input image tag."
		fi
		if [ ! ${obname} ];then
			showError 1 "ERROR. Please input object name."
		fi
			;;
	docker_stopall)
		if [ ! ${obname} ];then
			showError 1 "ERROR. Please input object name."
		fi
			;;
	docker_startall)
		if [ ! ${replicasnu} ];then
			showError 1 "ERROR. Please input replicas number."
		fi
		if [ ! ${obname} ];then
			showError 1  "ERROR. Please input object name."
		fi
		if [ ! ${port} ];then
			showError 1 "ERROR. Please input port name."
		fi
			;;
	docker_scale)
		if [ ! ${replicasnu} ];then
			showError 1 "ERROR. Please input replicas number."
		fi
		if [ ! ${obname} ];then
			showError 1 "ERROR. Please input object name."
		fi
		if [ ! ${port} ];then
			showError 1  "ERROR. Please input port name."
		fi
			;;
	docker_yaml)
		if [ ! ${obyml} ];then
		showError 1  "ERROR. Please input object yml."
		fi
		if [ ! ${port} ];then
		showError 1 "ERROR. Please input port name."
		fi
			;;
	docker_restart)
		if [ ! ${obname} ];then
		showError 1 "ERROR. Please input object name."
		fi
		if [ ! ${port} ];then
		showError 1 "ERROR. Please input port name."
		fi
			;;
	*)
		showError 1 "ERROR.no found ${module} in update stopupdate stop start scale deployyml restart"
		;;
	esac	
}

function switchparamfill
{
	case ${module} in
	docker)
		updateimage=1
			;;
	docker_all)
		updateimage=1
			;;
	docker_stopall)
		zeroreplica=1
			;;
	docker_startall)
		scalereplicas=1
			;;
	docker_scale)
		scalereplicas=1
			;;
	docker_yaml)
		applyyaml=1
			;;
	docker_restart)
		deletepo=1
			;;
	*)
		showError 1 "ERROR.no found ${module} in docker docker_all docker_stopall docker_startall docker_scale docker_yaml docker_restart"
		;;
	esac	
}


function showError
{
  if [ $# -lt 1 ]
  then
    showError 1 "Insufficient or excess input for showError."
  fi
  if [ $1 -ne 0 ]
  then
    echo "ERROR. `date +'%FT%T'`. $2" 1>&2
    exit 1
  fi
}

function sedConfig
{
	cfg_orgvalue=$1
	cfg_value=$2
	if [ $# -ne 2 ]
	then
		showError 1 "Insufficient or excess input for sedConfig."
	fi
	
	sed -i "s/${cfg_orgvalue}/${cfg_value}/g" ${obname}.json
	
	if [ $? -ne 0 ]
	then
		showError 1 "Sed ${obname}.json from ${cfg_orgvalue} to ${cfg_value} fail."
	exit 1
	fi
}

function getConfig
{
	if [ $# -ne 2 ]
	then
		showError 1 "Insufficient or excess input for getConfig."
	fi
		cfg_value=(`echo "$1" | jq "$2" | sed 's/"//g'`) >> /dev/null 2>&1
	if [ $? -eq 0 ]
	then
		echo ${cfg_value[@]}
	else
		echo
		return 233
	fi
}

if [ $# -ne 4 ]; then
	showError 1 "ERROR.${module}- Insufficient or excess input."
fi

namespace_org=$1
imagespaceip_org=$2
imagespacename_org=$3
containerstring_org=\{\"name\"\:\"containernameexample\"\,\"image\"\:\"imagespaceipexample\/imagespacenameexample\/imagenameexample:tagexample\"\,\"lifecycle\"\:\{\"mSvcStatus\"\:\{\"exec\"\:\"msvcstatustext\"\}\,\"mSvcDown\"\:\{\"exec\"\:\"msvcdowntext\"\}\,\"mSvcDelete\"\:\{\"exec\"\:\"msvcdeletetext\"\}\,\"mSvcUp\"\:\{\"exec\"\:\"msvcuptext\"\}\}\,\"creatingTime\"\:ctimeexample\}
module=$4
f_update_cfg=template.json
echo "INFO. Inputing a JSON-format configuration file."
#f_update_cfg=`jq '.' ${f_update_cfg} -c`
if [ $? -eq 0 ];then
	showError $? "The file inputed is MALFORMED or NOT EXISTED."
fi



pwd=`pwd`
cd ${pwd}
dos2unix order.txt
dos2unix template.json

obj_c=(`awk -F"%" '{print NF}' order.txt`)
obj_r=`awk -F"%" '{print NR}' order.txt|tail -n 1`
ord_num=0
#generate ${module}-template.json
cat order.txt|while read line
do
	paramfillnull
	switchparamfill
	for ((yy=1;yy<=${obj_c[${ord_num}]};yy++))
	do
		kv=`echo ${line}|awk -F"%" '{print $'$yy'}'`
		key=`echo ${kv}|awk -F":" '{print $1}'`
		value=`echo ${kv}|awk -F":" '{print $2}'`
		zz=`echo $value|awk -F"," '{print NF}'`
		valuetmp=`echo ${kv}|awk -F":" '{print $2}'`
		bb=1
		if [ $zz -gt 1 ];then
			if [ ${key} = msvcstatustext ];then
				for ((aa=0;aa<${zz};aa++))
				do
					value[${aa}]=`echo ${valuetmp}|awk -F"," '{print $'$bb'}'`
					let bb+=1
				done
			elif [ ${key} = msvcdowntext ];then
				for ((aa=0;aa<${zz};aa++))
				do
					value[${aa}]=`echo ${valuetmp}|awk -F"," '{print $'$bb'}'`
					let bb+=1
				done
			elif [ ${key} = msvcuptext ];then
				for ((aa=0;aa<${zz};aa++))
				do
					value[${aa}]=`echo ${valuetmp}|awk -F"," '{print $'$bb'}'`
					let bb+=1
				done
			elif [ ${key} = msvcdeletetext ];then
				for ((aa=0;aa<${zz};aa++))
				do
					value[${aa}]=`echo ${valuetmp}|awk -F"," '{print $'$bb'}'`
					let bb+=1
				done
			else
				value=(${value//,/ })
			fi
			##value="("`echo $a|sed 's/\,/ /g'`")"
			for ((ww=0;ww<${zz};ww++))
			do
				value[$ww]=`echo ${value[$ww]}|sed 's% %\#%g'`
				eval $key[${ww}]=${value[$ww]}
			done
		else
			eval $key=$value
		fi
	done
	checkparam
	paramautfill
	ord_num=`expr ${ord_num} + 1`

	cp template.json ${obname}.json
	sedConfig "objectexample" "${kind}"
	sedConfig "obnameexample" "${obname}"
	sedConfig "tenantexample" "${namespace}"
	sedConfig "replicasnuexample" "${replicasnu}"
	sedConfig "obymlexample" "${obyml}"
	sedConfig "stimeexample" "${stime}"
	sedConfig "gtimeexample" "${gtime}"
	sedConfig "preliminarycheck" "${preliminarycheck}"
	sedConfig "checkanddown" "${checkanddown}"
	sedConfig "zeroreplica" "${zeroreplica}"
	sedConfig "applyyaml" "${applyyaml}"		
	sedConfig "updateimage" "${updateimage}"	
	sedConfig "scalereplicas" "${scalereplicas}"	
	sedConfig "checkandup" "${checkandup}"	
	sedConfig "deletepo" "${deletepo}"
	sedConfig "deletefirstpo" "${deletefirstpo}"


	sedConfig "Check_namespace" "${check_namespace}"
	sedConfig "Check_deploymentname" "${check_deploymentname}"
	sedConfig "Check_metadatalabels" "${check_metadatalabels}"
	sedConfig "Check_matchlabels" "${check_matchlabels}"
	sedConfig "Check_templatemetadatalabels" "${check_templatemetadatalabels}"
        sedConfig "Check_replicas" "${check_replicas}"
	sedConfig "Check_volumes" "${check_volumes}"
	sedConfig "Check_hostaliases" "${check_hostaliases}"
	sedConfig "Check_imagepullsecrets" "${check_imagepullsecrets}"
	sedConfig "Check_dnspolicy" "${check_dnspolicy}"
	sedConfig "Check_nameservers" "${check_nameservers}"
	sedConfig "Check_strategy" "${check_strategy}"
	sedConfig "Check_matchexpressionskey" "${check_matchexpressionskey}"
	sedConfig "Check_matchexpressionsvalues" "${check_matchexpressionsvalues}"
	sedConfig "Check_containersname" "${check_containersname}"
	sedConfig "Check_limitscpu" "${check_limitscpu}"
	sedConfig "Check_limitsmemory" "${check_limitsmemory}"
	sedConfig "Check_requestscpu" "${check_requestscpu}"
	sedConfig "Check_requestsmemory" "${check_requestsmemory}"
	sedConfig "Check_ports" "${check_ports}"
	sedConfig "Check_env" "${check_env}"
	sedConfig "Check_Envfrom" "${check_envfrom}"
	sedConfig "Check_prestop" "${check_prestop}"
	sedConfig "Check_image" "${check_image}"
	sedConfig "Check_volumemounts" "${check_volumemounts}"


	containerstring_normal=

	for ((zz=0;zz<${container_num};zz++))
	do

		containerstring[${zz}]=`echo $containerstring_org|sed "s/containernameexample/${containername[${zz}]}/g"|sed "s/imagespaceipexample/${imagespaceip[${zz}]}/g"|sed "s/imagespacenameexample/${imagespacename[${zz}]}/g"|sed "s/imagenameexample/${imagename[${zz}]}/g"|sed "s/tagexample/${tag[${zz}]}/g"|sed "s/portexample/${port[${zz}]}/g"|sed "s/ctimeexample/${ctime[${zz}]}/g"|sed "s/msvcstatustext/${msvcstatustext[${zz}]}/g"|sed "s/msvcdowntext/${msvcdowntext[${zz}]}/g"|sed "s/msvcdeletetext/${msvcdeletetext[${zz}]}/g"|sed "s/msvcuptext/${msvcuptext[${zz}]}/g"`
        containerstring[${zz}]=`echo ${containerstring[${zz}]}|sed 's/\"/\\\"/g'`
		containerstring[${zz}]=`echo ${containerstring[${zz}]}|sed 's#\/#\\\/#g'`
		if [ $zz -eq 0 ];then
			containerstring_normal=${containerstring_normal}${containerstring[${zz}]}
		else
			containerstring_normal=${containerstring_normal},${containerstring[${zz}]}
		fi
	done
		sedConfig "containerstring_normal" "${containerstring_normal}"
#       containerstring_normal=`echo ${containerstring_normal}|sed "s/\'//g"`
#       sed -i "s/containerstring_normal/${containerstring_normal}/g" ${module}-${obname}.json
	
done

