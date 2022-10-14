package com.spdb.fdev.freport.spdb.vo;

import com.spdb.fdev.freport.spdb.dto.PageDto;
import lombok.Data;

import java.util.List;

/**
 * 就随便写一个简单的Page封装吧
 */
@Data
public class PageVo<T> {

    private List<T> data;

    private Long total;

    public PageVo() {
    }

    public PageVo(List<T> data, Long total) {
        this.data = data;
        this.total = total;
    }
}
