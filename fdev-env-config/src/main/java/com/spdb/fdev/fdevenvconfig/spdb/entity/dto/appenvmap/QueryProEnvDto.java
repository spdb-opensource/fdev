package com.spdb.fdev.fdevenvconfig.spdb.entity.dto.appenvmap;

import com.spdb.fdev.fdevenvconfig.spdb.entity.AppEnvMapping;

import java.util.List;
import java.util.Map;

/**
 * @author xxx
 * @date 2020/6/1 13:33
 */
public class QueryProEnvDto extends AppEnvMapping {

    private List<Map> envInfo;

    public List<Map> getEnvInfo() {
        return envInfo;
    }

    public void setEnvInfo(List<Map> envInfo) {
        this.envInfo = envInfo;
    }
}
