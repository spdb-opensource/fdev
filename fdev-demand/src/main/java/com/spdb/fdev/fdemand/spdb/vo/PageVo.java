package com.spdb.fdev.fdemand.spdb.vo;

import java.util.List;

/**
 * 就随便写一个简单的Page封装吧
 */
public class PageVo<T> {

    private List<T> data;

    private Long total;

    public PageVo(List<T> data) {
        this.data = data;
        this.total = (long) data.size();
    }

    public PageVo(List<T> data, Long total) {
        this.data = data;
        this.total = total;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }
}
