package com.spdb.fdev.fdemand.spdb.dto.conf;

import java.util.List;

public class ConfDto {

    private List<ContentDto> results;

    private Integer start;
    private Integer limit;
    private Integer size;

    public List<ContentDto> getResults() {
        return results;
    }

    public void setResults(List<ContentDto> results) {
        this.results = results;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
