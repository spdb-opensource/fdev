package com.spdb.fdev.fdevinterface.base.dict;

public class Constants {

    private Constants() {
    }

    /**
     * 数字相关
     */
    public static final Integer FIVE = 5;
    public static final Integer TEN = 10;
    public static final Integer HUNDRED = 100;

    //关键字符串
    public static final String JSON_FILE = ".json";
    public static final String XML_FILE = ".xml";
    public static final String TAR_FILE = ".tar";
    public static final String TRANS_FILE = "trans_";
    public static final String TRS_FILE = "trs_";
    public static final String TRS_DEMO = "trs_demo.xml";
    public static final String SOAP_MAP_ONE = "wsclient_mapping.xml";
    public static final String SOAP_MAP_TWO = "client_mapping.xml";
    public static final String REST_HEADER = "RestHeader.json";
    public static final String SCHEMA_CONFIG = "schema-config.xml";
    public static final String TRANS_REQ_HEADER = "XmlRequestPacket.xml";
    public static final String TRANS_RSP_HEADER = "XmlResponsePacket.xml";
    public static final String TRANS_JSON_HEADER = "appHeader.json";
    public static final String JAVA_STRING = "java.lang.string";
    public static final String KEYED_DATA = "com.csii.ebank.core.keyeddata";
    public static final String INDEXED_DATA = "com.csii.ebank.core.indexeddata";
    public static final String SOAP_HEADER = "SoapHeader.xsd";
    public static final String EXAMPLE_JSON = "json-schema-example.json";
    public static final String TRANS_CONFIG_JS = "transConfig.js";
    public static final String TRANS_CONFIG_XML = "trans-conf.xml";
    public static final String TRANS_CONFIG2_XML = "trans-conf2.xml";
    public static final String CENTRAL_JSON = "central.json";
    public static final String TOTAL_TAR_PREFIX = "stages_config_package_";
    public static final String SLASH = "/";
    public static final String AUTO_SCAN = "自动";
    public static final String HAND_SCAN = "手动";
    public static final String FILECONTEXT = "/repository/files/";
    public static final String XROUTER_DIR = "xrouter";
    public static final String REPO_JSON = "repo.json";
    public static final String URL = "url";
    public static final String UNDER_LINE = "_";
    /**
     * 只是为了占位用
     */
    public static final String EMPTY_STRING = "empty";
    public static final String DOWNLOAD_URL = "/finterface/api/interface/downloadTar/";


    // 提示语
    public static final String REST_PROVIDER_SCAN_SUCCESS = "REST接口提供方扫描成功";
    public static final String REST_CALLING_SCAN_SUCCESS = "REST接口调用方扫描成功！";
    public static final String SOAP_PROVIDER_SCAN_SUCCESS = "SOAP接口提供方扫描成功！";
    public static final String SOAP_CALLING_SCAN_SUCCESS = "SOAP接口调用方扫描成功！";
    public static final String SOP_CALLING_SCAN_SUCCESS = "SOP接口调用方扫描成功！";
    public static final String COMMON_SOAP_SCAN_SUCCESS = "msper-web-common-service SOAP接口调用方扫描成功！";
    public static final String TRANS_SCAN_SUCCESS = "交易提供方扫描成功！";
    public static final String TRANS_RELATION_SCAN_SUCCESS = "交易调用方扫描成功！";
    public static final String REST_PROVIDER_SCAN_START = "REST接口提供方开始扫描！";
    public static final String REST_PROVIDER_SCAN_END = "REST接口提供方扫描结束！";
    public static final String REST_CALLING_SCAN_START = "REST接口调用方开始扫描！";
    public static final String REST_CALLING_SCAN_END = "REST接口调用方扫描结束！";
    public static final String SOAP_PROVIDER_SCAN_START = "SOAP接口提供方开始扫描！";
    public static final String SOAP_PROVIDER_SCAN_END = "SOAP接口提供方扫描结束！";
    public static final String SOAP_PROVIDER_SCAN_ERROR = "SOAP接口提供方扫描出错：";
    public static final String SOAP_CALLING_SCAN_START = "SOAP接口调用方开始扫描！";
    public static final String SOAP_CALLING_SCAN_END = "SOAP接口调用方扫描结束！";
    public static final String SOP_CALLING_SCAN_START = "SOP接口调用方开始扫描！";
    public static final String SOP_CALLING_SCAN_END = "SOP接口调用方扫描结束！";
    public static final String TRANS_SCAN_START = "交易提供方开始扫描！";
    public static final String TRANS_SCAN_END = "交易提供方扫描结束！";
    public static final String TRANS_RELATION_SCAN_START = "交易调用方开始扫描！";
    public static final String TRANS_RELATION_SCAN_END = "交易调用方扫描结束！";
    public static final String COMMON_SOAP_SCAN_START = "CommonSOAP接口提供方开始扫描！";
    public static final String COMMON_SOAP_SCAN_END = "CommonSOAP接口提供方扫描结束！";
    public static final String COMMON_SOAP_SCAN_ERROR = "CommonSOAP接口提供方扫描出错：";
    public static final String ANALYSIS_FILE_ERROR = "解析以下文件出错，";
    public static final String INTERFACE_STATISTICS_SCAN_START = "接口统计开始扫描！";
    public static final String INTERFACE_STATISTICS_SCAN_END = "接口统计扫描结束！";
    public static final String INTERFACE_STATISTICS_SCAN_SUCCESS = "接口统计扫描成功！";
    public static final String VUE_ROUTER_SCAN_SUCCESS = "VUE项目路由扫描成功！";
    public static final String VUE_ROUTER_SCAN_START = "VUE项目路由扫描开始！";
    public static final String VUE_ROUTER_SCAN_END = "VUE项目路由扫描结束！";
    /**
     * 接口申请邮件通知
     */
    public static final String INTERFACE_APPLY = "email.interface.apply";
    /**
     * 接口审批邮件通知
     */
    public static final String INTERFACE_APPROVE = "email.interface.approve";
    /**
     * 接口调用关系表文件前缀
     */
    public static final String CONFIG_FILE_PRE = "interfaceCall";
    /**
     * 接口调用关系表文件夹
     */
    public static final String INTERFACECALL = "interfacecall/";
    /**
     * 生成的扫描记录文件前缀
     */
    public static final String SCAN_RECORD_FILE = "scanRecord";
    /**
     * 扫描记录文件目录
     */
    public static final String SCAN_RECORD_FILE_PATH = "scanRecord/";

}
