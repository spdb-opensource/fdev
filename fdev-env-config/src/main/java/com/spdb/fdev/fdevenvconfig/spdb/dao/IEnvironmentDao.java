package com.spdb.fdev.fdevenvconfig.spdb.dao;

import com.spdb.fdev.fdevenvconfig.spdb.entity.Environment;

import java.util.List;

/**
 * @author xxx
 * @date 2019/7/5 13:11
 */
public interface IEnvironmentDao {
    List<Environment> query(Environment environment);

    Environment save(Environment environment);

    void delete(String id, String opno);

    Environment update(Environment environment) throws Exception;

    Environment queryById(Environment environment);

    Environment queryById(String id);

    List<Environment> queryByLabels(List<String> labels, String status);

    List<Environment> getByLabels(List<String> labels, String status);

    List<Environment> queryAutoEnv(Environment environment) throws Exception;

    Environment queryByNameEn(String envNameEn);

    List<Environment> queryByLabelsFuzzy(List<String> labels);

    Environment queryByNameCN(String env_name);

}
