package com.spdb.fdev.codeReview.base.dict;

/**
 * @Author liux81
 * @DATE 2021/11/9
 */
public class OrderEnum {

    public enum  OrderStatusEnum{
        TO_AUDIT("待审核", 1),
        AUDITING("审核中", 2),
        OFFLINE_RECHECK("需线下复审",3),
        MEETING_RECHECK( "需会议复审", 4),
        FIRST_PASS("初审通过", 5),
        OFFLINE_PASS("线下复审通过" , 6),
        MEETING_PASS("会议复审通过" , 7),
        REFUSE("拒绝", 8);

        private String name;
        private Integer value;

        OrderStatusEnum(String name, Integer value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }
    }
}
