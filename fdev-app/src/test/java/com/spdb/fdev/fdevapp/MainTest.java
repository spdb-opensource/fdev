package com.spdb.fdev.fdevapp;

import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import org.apache.commons.lang.StringUtils;

public class MainTest {
    public static void main(String[] args) {
        String name = "aaaa";
        name = CommonUtils.urlEncode(name);
        if (StringUtils.isNotBlank(name) && name.length() > 10) {
            name = name.substring(0, 10);
        }
        System.out.println("name:" + name);
    }
}