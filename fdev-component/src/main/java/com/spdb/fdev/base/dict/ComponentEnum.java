package com.spdb.fdev.base.dict;

public class ComponentEnum {

    /**
     * 基础镜像类型
     */
    public enum ImageTypeEnum {
        MIRIOSERVICE("微服务", "mirioservice"), CONTAINERIZATION("容器化", "containerization");

        private String name;
        private String value;

        ImageTypeEnum(String name, String value) {
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


    /**
     * 基础镜像目标环境
     */
    public enum TargetEnvEnum {
        TEST("测试环境版本", "test"), PRO("生产环境版本", "pro");

        private String name;
        private String value;

        TargetEnvEnum(String name, String value) {
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


    /**
     * 基础镜像阶段标识
     */
    public enum ImageStageEnum {
        DEV("开发测试", "dev"), TRIAL("试用期", "trial"), RELEASE("正式推广", "release"), INVALID("无效", "invalid");
        private String name;
        private String value;

        ImageStageEnum(String name, String value) {
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


    /**
     * 基础镜像优化需求阶段
     */
    public enum IssueStageEnum {
        CREATE("新增阶段", "0"), DEV("开发阶段", "1"), TRIAL("试用阶段", "2"), RELEASE("正式发布", "3");
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

    /**
     * 组件优化需求阶段
     */
    public enum ComponentIssueStageEnum {
        CREATE("新增阶段", "0"), ALPHA("内测阶段", "1"), RC("候选发布阶段", "2"), RELEASE("正式发布", "3"), PASSED("已发布", "4");
        private String name;
        private String value;

        ComponentIssueStageEnum(String name, String value) {
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

    /**
     * 骨架优化需求阶段
     */
    public enum ArchetypeIssueStageEnum {
        CREATE("新增阶段", "0"), ALPHA("内测阶段", "1"), RELEASE("正式发布", "2"), PASSED("已发布", "3");
        private String name;
        private String value;

        ArchetypeIssueStageEnum(String name, String value) {
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

    public enum AppTypeEnum {
        IOS("IOS应用"), Android("Android应用"), Java("Java微服务"), Vue("Vue应用"), Container("容器化项目");
        private String value;

        AppTypeEnum(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

}
