package com.spdb.fdev.freport.spdb.dto;

import com.spdb.fdev.freport.spdb.entity.user.User;
import lombok.Data;

import java.util.List;

@Data
public class CommitStatisticsDto {

    private String startDate;

    private String endDate;

    private User user;

    private PageDto page;

    private List<String> statisticRange;

    // 是否包含子组 ，true 是，false 不是
    private Boolean includeChild;

}