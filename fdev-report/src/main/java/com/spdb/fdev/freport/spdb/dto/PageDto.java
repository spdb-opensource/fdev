package com.spdb.fdev.freport.spdb.dto;

import lombok.Data;

@Data
public class PageDto {

    private Integer index;

    private Integer size;

    public int[] getPageCondition() {
        return new int[]{size * (index - 1), size};
    }
}
