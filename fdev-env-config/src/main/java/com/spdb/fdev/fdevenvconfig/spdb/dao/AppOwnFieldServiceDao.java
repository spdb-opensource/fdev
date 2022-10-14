package com.spdb.fdev.fdevenvconfig.spdb.dao;

import com.spdb.fdev.fdevenvconfig.spdb.entity.AppOwnField;

import java.util.List;


public interface AppOwnFieldServiceDao {

    AppOwnField save(AppOwnField appOwnField);

    void update(AppOwnField appOwnField);

    AppOwnField queryByAppIdAndEnvId(AppOwnField appOwnField);

    List<AppOwnField> query(AppOwnField appOwnField) throws Exception;

    void delete(String app_id,String env_id);
}
