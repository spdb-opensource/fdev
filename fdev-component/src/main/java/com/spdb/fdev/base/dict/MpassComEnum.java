package com.spdb.fdev.base.dict;

public class MpassComEnum {

    /**
     * 新增release优化需求类型
     */
    public enum IssueTypeEnum {

        OPTIMIZE("功能新增", "0"), BUGFIX("问题修复", "1");
        private String name;
        private String value;

        IssueTypeEnum(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    /**
     * 需求开发阶段
     */
    public enum DevIssueStageEnum {
        CREATE("新增阶段", "0"), ALPHA("内测阶段", "1"), BETA("公测阶段", "2"), RC("试运行阶段", "3");
        private String name;
        private String value;

        DevIssueStageEnum(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
