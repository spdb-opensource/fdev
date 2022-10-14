package com.spdb.fdev.fdemand.base.dict;

public class UIEnum {

    /**
     * 需求状态类型
     */
    public enum UIDesignEnum {
        NORELATE("不涉及", "noRelate"),
        NOLOADNOT("未上传", "uploadNot"),
        UPLOADED("已上传","uploaded"),
        AUDITWAIT( "待审核", "auditWait"),
        AUDITIN("审核中", "auditIn"),
        AUDITPASS("审核通过" , "auditPass"),
        AUDITPASSNOT("审核不通过" , "auditPassNot"),
        COMPLETEAUDIT("审核完成", "completedAudit"),
        ABNORMALSHUTDOWN("异常关闭", "abnormalShutdown");
 

        private String name;
        private String value;

        UIDesignEnum(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }
    
}

