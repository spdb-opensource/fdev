package com.spdb.fdev.fdevtask.spdb.service;

import org.springframework.stereotype.Service;

public interface InterfaceApi {
    /**
     *
     * @param name 应用名
     * @param branch 开发分支
     */
    void delete(String name,String branch);
}
