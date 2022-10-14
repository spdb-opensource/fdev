package com.spdb.fdev.component.dao;

import com.spdb.fdev.component.entity.TagRecord;

public interface ITagRecordDao {

    TagRecord save(TagRecord tagRecord) throws Exception;

    TagRecord findByMidAndGid(String application_id, String merge_request_id) throws Exception;
}
