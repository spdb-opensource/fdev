package com.fdev.database.spdb.service;


import com.fdev.database.spdb.entity.DictRecord;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

public interface DictRecordService {


    Map queryDictRecord(String sys_id, String database_type, String database_name, String field_en_name, int per_page, int page) throws Exception;

    void add(DictRecord dictRecord);

    List<DictRecord> query(DictRecord dictRecord) throws Exception;

    void update(DictRecord dictRecord);

    void impDictRecords(MultipartFile[] files);
}
