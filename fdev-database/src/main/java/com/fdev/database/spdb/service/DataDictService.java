package com.fdev.database.spdb.service;


import com.fdev.database.spdb.entity.DataDict;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface DataDictService {


    List<DataDict> queryByKey(DataDict dataDict) throws Exception;

    DataDict add(DataDict dataDict);

    void update(DataDict dataDict);

    List<String> queryFieldType(String fieldType);

    void downloadTemplate(HttpServletResponse resp);

    List<DataDict> queryIdByFields(List<String> all_field);
}
