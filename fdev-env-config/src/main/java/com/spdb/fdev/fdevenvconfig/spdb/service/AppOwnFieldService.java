package com.spdb.fdev.fdevenvconfig.spdb.service;

import com.spdb.fdev.fdevenvconfig.spdb.entity.AppOwnField;

import java.util.List;


public interface AppOwnFieldService {

    AppOwnField save(AppOwnField appOwnField);

    void update(AppOwnField appOwnField);

    AppOwnField queryByAppIdAndEnvId(AppOwnField appOwnField);

    List<AppOwnField>query(AppOwnField appOwnField) throws Exception;

    void deleteByAppId(String app_id,String env_id);
}
