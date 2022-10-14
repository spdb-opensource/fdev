package com.spdb.fdev.release.dao;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by xxx on 下午3:16.
 */
public class FreleaseDao {

    protected ObjectMapper objectMapper = new ObjectMapper();

    public FreleaseDao() {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

}
