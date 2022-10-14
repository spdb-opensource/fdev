package com.spdb.fdev.freport.spdb.vo;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.Set;

@Data
public class CommitStatisticsVo {

    private String userId;

    private String email;

    private String userNameCn;

    private String userNameEn;

    private String company;

    private String group;

    private Set<String> role;

    private Integer total;

    private Integer additions;

    private Integer deletions;

    private String startDate;

    private String endDate;

}
