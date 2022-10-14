package com.spdb.fdev.fdemand.base.dict;

public class DemandDocEnum {
    /**
     * 需求状态类型
     */
    public enum DemandDocTypeEnum {
        DEMAND_INSTRUCTION("需求说明书", "demandInstruction"),
        TECH_PLAN("技术方案", "techPlan"),
        DEMAND_REVIEW_RESULT("需求评审决议表", "demandReviewResult"),
        MEETMINUTES("会议纪要", "meetMinutes"),
        OTHER_RELATED_FILE("其他相关材料", "otherRelatedFile"),
        CONFLUENCE_FILE("confluence文档", "confluenceFile"),
        DEMAND_PLAN_INSTRUCTION("需求规格说明书", "demandPlanInstruction"),
        INNER_TEST_REPORT("内测报告", "innerTestReport"),
        DEFER_EMAIL("暂缓邮件", "deferEmail"),//内测报告专用
        BUSINESS_TEST_REPORT("业测报告", "businessTestReport"),
        LAUNCH_CONFIRM("上线确认书", "launchConfirm"),
        DEMAND_ASSESS_REPORT("需求评估表", "demandAssessReport"),
        DEMAND_PLAN_CONFIRM("需归确认材料", "demandPlanConfirm");

        private String name;
        private String value;

        DemandDocTypeEnum(String name, String value) {
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

