package com.spdb.fdev.base.dict;

import org.omg.CORBA.PUBLIC_MEMBER;

import java.util.ArrayList;
import java.util.List;

public class Dict {

    private Dict() {
    }

    public static final String DATA = "data";
    public static final String NAMESPACE = "namespace";
    public static final String VALUE = "value";
    public static final String LABEl = "label";
    public static final String FIELD = "field";
    public static final String TAG = "tag";
    public static final String NULL = "null";
    /**
     * 蓝鲸相关
     **/
    public static final String MODIFYKEY = "modifykey";
    public static final String DEPLOYMENT = "deployment";
    public static final String CLUSTER = "cluster";
    public static final String POD = "pod";
    public static final String INFO = "info";
    public static final String LAST_MODIFIED_DATE = "last_modified_date";
    public static final String CONDITION = "condition";
    public static final String COUNT = "count";
    public static final String OPERATOR = "operator";
    public static final String BK_OBJ_ID = "bk_obj_id";
    public static final String COMMON = "common";
    public static final String PERSONAL = "personal";
    public static final String TYPE = "type";
    public static final String MODIFYDEPLOYMENT = "modifykey.deployment";
    public static final String MODIFYCLUSTER = "modifykey.cluster";
    public static final String MODIFYNAMESPACE = "modifykey.namespace";
    public static final String DEPLOYNAME = "deploy_name";
    public static final String SUBJECT = "subject";
    public static final String CONTENT = "content";
    public static final String TO = "to";


    public static final String CAASDEPLOYMENT = "caas_deployment";
    public static final String CAASPOD = "caas_pod";
    public static final String CAASMODIFYKEY = "caas_modifyKey";

    public static final String SCCDEPLOY = "scc_deploy";
    public static final String SCCPOD = "scc_pod";
    public static final String SCCMODIFYKEY = "scc_modifyKey";

    public static final String DEPLOYSCC = "yaml_scc";
    public static final String PODSCC = "pod_scc";
    public static final String OWNERCODE = "owner_code";
    public static final String CLUSTERCODE = "cluster_code";
    public static final String RESOURCECODE = "resource_code";
    public static final String NAMESPACECODE = "namespace_code";

    public static final String CAASDEPLOYCOUNT = "caas_deploy_count";
    public static final String CAASDEPLOYDETAIL = "caas_deploy_detail";
    public static final String CAASPODCOUNT = "caas_pod_count";
    public static final String CAASPODDETAIL = "caas_pod_detail";
    public static final String SCCDEPLOYCOUNT = "scc_deploy_count";
    public static final String SCCDEPLOYDETAIL = "scc_deploy_detail";
    public static final String SCCPODCOUNT = "scc_pod_count";
    public static final String SCCPODDETAIL = "scc_pod_detail";
    public static final String CAASSHK1= "caas_shk1";
    public static final String CAASSHK2= "caas_shk2";
    public static final String CAASHFK1= "caas_hfk1";
    public static final String CAASHFK2= "caas_hfk2";
    public static final String SCCSHK1= "scc_shk1";
    public static final String SCCSHK2= "scc_shk2";
    public static final String SCCHFK1= "scc_hfk1";
    public static final String SCCHFK2= "scc_hfk2";


    public static final String SHK1 = "shk1";
    public static final String SHK2 = "shk2";
    public static final String HFK1 = "hfk1";
    public static final String HFK2 = "hfk2";

    public static final String SHK1GRAY = "shk1-gray";
    public static final String SHK2GRAY = "shk2-gray";
    public static final String HFK1GRAY = "hfk1-gray";
    public static final String HFK2GRAY = "hfk2-gray";
    public static final String GRAY = "gray";
    public static final String MBPERUZ10 = "mbper-uz10";
    public static final String MBPERUZ11= "mbper-uz11";
    public static final String MBPERUZ12 = "mbper-uz12";
    public static final String MBPERUZ30 = "mbper-uz30";
    public static final String MBPERUZ31 = "mbper-uz31";
    public static final String MBPERUZ32 = "mbper-uz32";

    public static final String CAAS = "caas";
    public static final String SCC = "scc";
    public static final String CNSHK1 = "上海K1";
    public static final String CNSHK2 = "上海K2";
    public static final String CNHFK1 = "合肥K1";
    public static final String CNHFK2 = "合肥K2";


    public static final String CLUSTERS = "clusters";
    public static final String VLANS = "vlans";
    public static final String NAMESPACES = "namespaces";
    public static final String VLAN = "vlan";
    public static final String TEAM = "team";

    public static final String CPULIMITS = "cpu_limits";
    public static final String CPUREQUESTS = "cpu_requests";
    public static final String MEMORYLIMITS = "memory_limits";
    public static final String MEMORYREQUESTS = "memory_requests";
    public static final String REPLICAS = "replicas";
    public static final String HOSTNAME = "hostName";
    public static final String IP = "ip";
    public static final String NAME = "name";
    public static final String NAMEEN = "name_en";
    public static final String VOLUMEMOUNTS = "volumemounts";
    public static final String VOLUMEMOUNTSUP = "volumeMounts";
    public static final String VOLUMES= "volumes";
    public static final String MOUNTPATH = "mountPath";
    public static final String SUBPATH = "subPath";
    public static final String SPRING_CLOUD_CONFIG_URI = "SPRING_CLOUD_CONFIG_URI";
    public static final String EUREKA_CLIENT_SERVICEURL_DEFAULTZONE = "EUREKA_CLIENT_SERVICEURL_DEFAULTZONE";
    public static final String CONFIGCENTOR = "config_center";
    public static final String EUREKA = "eureka";
    public static final String HOSTALIAS = "hostalias";
    public static final String HOSTNAMES = "hostnames";
    public static final String DNSCONFIG = "dnsconfig";
    public static final String DNSPOLICY = "dnspolicy";
    public static final String DNSPOLICYUP = "dnsPolicy";
    public static final String ALLOCATED_IP_SEGMENT = "allocated_ip_segment";
    public static final String PRESTOP = "prestop";
    public static final String STRATEGYTYPE = "strategytype";
    public static final String STRATEGY = "strategy";
    public static final String ENV = "env";
    public static final String INITCONTAINERS = "initContainers";
    public static final String ENVFROM = "envfrom";
    public static final String ENVFROMUP = "envFrom";
    public static final String DEPLOY = "deploy";
    public static final String RUN = "run";
    public static final String STORAGE = "storage";
    public static final String CONFIG = "config";
    public static final String YAML = "yaml";
    public static final String IMAGEPULLSECRETS = "imagepullsecrets";

    public static final String SPEC = "spec";
    public static final String CONTAINERS = "containers";
    public static final String IMAGE = "image";
    public static final String RESOURCES = "resources";
    public static final String LIMITS = "limits";
    public static final String REQUESTS = "requests";
    public static final String CPU = "cpu";
    public static final String MEMORY ="memory";
    public static final String TEMPLATE ="template";
    public static final String PERSISTENTVOLUMECLAIM = "persistentVolumeClaim";
    public static final String CLAIMNAME = "claimName";
    public static final String HOSTPATH = "hostPath";
    public static final String PATH = "path";

    public static final String[] SCCSHK1CLUSTER= {"k8-sh01b01", "k8-sh02b01"," k8-sh01d01", "k8-sh02d01","k8-sh00b01", "k8-sh00d01", "k8-sh00t01", "k8-sh01w01", "k8-sh02w01", "dcsp-sh00c01", "icps-sh21a01", "k8-sh01x01", "k8-sh02x01"};
    public static final String[] SCCSHK2CLUSTER= {"k8-sh00b02", "k8-sh00d02", "k8-sh00t02", "dcsp-sh00c02", "icps-sh22a02"};
    public static final String[] SCCHFK1CLUSTER= {"k8-hf01b01", "k8-hf02b01", "k8-hf01d01", "k8-hf02d01", "k8-hf00b01", "k8-hf00d01", "k8-hf00t01", "k8-hf01w01", "k8-hf02w01", "dcsp-hf00c01", "icps-hf21a01", "k8-hf01x01", "k8-hf02x01"};
    public static final String[] SCCHFK2CLUSTER= {"k8-hf00b02", "k8-hf00d02", "k8-hf00t02", "dcsp-hf00c02"};
    //业务网段
    public static final String[] SCCBIZ ={"k8-sh01b01", "k8-sh02b01", "k8-hf01b01", "k8-hf02b01", "k8-sh00b01", "k8-sh00b02", "k8-hf00b01", "k8-hf00b02", "k8-sh01w01", "8-sh02w01", "k8-hf01w01", "k8-hf02w01", "k8-sh01x01", "k8-sh02x01", "k8-hf01x01", "k8-hf02x01"};
    //网银网段
    public static final String[] SCCDMZ = {"k8-sh01d01","k8-sh02d01", "k8-hf01d01", "k8-hf02d01", "k8-sh00d01", "k8-sh00d02", "k8-hf00d01", "k8-hf00d02", "k8-sh00t01", "k8-sh00t02", "k8-hf00t01", "k8-hf00t02" };

    public static final String ACTIVEENV = "active_env";
    public static final String PRO = "pro";
    public static final String PAGE = "page";
    public static final String PAGESIZE = "pageSize";
    public static final String PARAMS = "params";
    public static final String KIND = "kind";
    public static final String PLATFORM = "platform";
    public static final String GROUP = "group";
    public static final String SPDBMANAGERS = "spdb_managers";
    public static final String DEVMANAGERS = "dev_managers";
    public static final String DEPLOYLIST = "deploy_list";
    public static final String TOTAL = "total";
    public static final String SOURCE = "source";
    public static final String BACK = "back";
    public static final String FDEVUSERPATH = "/fapp/api/app/getAppByNameEns" ;

}

