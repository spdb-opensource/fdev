package com.spdb.fdev.fdevfootprint.base.dict;


public class Dict {

    private Dict() {
    }
    
    //数据库字段信息
    public static final String CHANNEL_NO = "channel_no";
    public static final String CHANNEL_NAME = "channel_name";
    public static final String STATUS = "status";
    public static final String AUTO_PERCENT = "auto_percent";
    public static final String CREATE_TIME = "create_time";
    public static final String UPDATE_TIME = "update_time";
    public static final String SWITCH_MODEL = "switch_model";
    
    //客户端足迹采集服务请求相关字段
    public static final String FDEV = "fdev";
    public static final String FDEV_NEW = "fdev-new";
    public static final String FTMS = "ftms";
    public static final String CHANNEL = "channel";
    public static final String TIME = "time";
    public static final String USER = "user";
    public static final String MASTER_ID = "master_id";
    public static final String CLIENT = "client";
    public static final String IP = "ip";
    public static final String EVENT_CONTEXT = "event_context";
    public static final String AUTO_FLAG = "auto_flag";
    public static final String REFERER = "Referer";
    public static final String BROWSE = "browse";
    public static final String USER_MARK = "user_mark";
    public static final String OS = "os";
    public static final String DATA = "data";
    public static final String RANDOM = "random";
    public static final String T = "t";
    public static final String BROWSER = "browser";
    public static final String VIEW = "view";
    public static final String EVENT_OBJECT = "event_object";
    public static final String MAC = "mac";
    public static final String IMEI = "imei";
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String POSITION = "position";
    public static final String AUTO_SWITCH = "autoSwitch";
    public static final String HOST = "Host";
    
    //配置相关字段信息
    public static final String SWITCH = "switch";
    public static final String ACQUISITION = "acquisition";
    public static final String SWITCH_HTTP_SERVER = "switchHttpServer";
    public static final String ACQUISITION_HTTP_SERVER = "acquisitionHttpServer";
    public static final String HTTP_SERVER_SWITCH = "httpserver.switch";
    public static final String HTTP_SERVER_ACQUISITION = "httpserver.acquisition";
    public static final String HTTP_SERVER_INBOUND_HANDLER = "httpServerInboundHandler";
    public static final String HTTP_SWITCH_SERVER_INB_HANDLER = "httpSwitchServerInbHandler";
    public static final String KAFKA_PRODUCER_EXECUTOR = "kafkaProducerExecutor";

}
