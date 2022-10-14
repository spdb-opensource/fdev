package com.spdb.fdev.freport.spdb.vo;

import lombok.Data;

@Data
public class Product {

    private String period;//周期区间

    private Integer count;//统计数

    public Product(String period, Integer count) {
        this.period = period;
        this.count = count;
    }

}
