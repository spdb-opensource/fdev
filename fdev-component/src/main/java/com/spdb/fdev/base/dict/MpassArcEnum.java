package com.spdb.fdev.base.dict;

public class MpassArcEnum {
    /**
     * mpass骨架优化需求阶段
     */
    public enum IssueStageEnum {
        CREATE("新增阶段", "0"), ALPHA("内测阶段", "1"), RELEASE("正式发布", "2");
        private String name;
        private String value;

        IssueStageEnum(String name, String value) {
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
