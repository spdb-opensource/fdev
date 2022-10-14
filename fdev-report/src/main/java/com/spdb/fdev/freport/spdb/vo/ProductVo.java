package com.spdb.fdev.freport.spdb.vo;

import lombok.Data;

import java.util.List;

@Data
public class ProductVo {

    private String tips;//提示

    private List<Product> product;//需求类型

    public ProductVo(String tips, List<Product> product) {
        this.tips = tips;
        this.product = product;
    }

}
