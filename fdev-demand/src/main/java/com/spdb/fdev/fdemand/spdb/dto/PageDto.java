package com.spdb.fdev.fdemand.spdb.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class PageDto {

    private Integer pageNum;

    private Integer pageSize;

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Pageable getPageable(){
        if (pageNum == null || pageSize == null) return null;
        return PageRequest.of(pageNum - 1, pageSize);
    }

}
