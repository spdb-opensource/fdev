package com.spdb.fdev.pipeline.dao;

import com.spdb.fdev.pipeline.entity.Images;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Map;

public interface IImageDao {

    List<Images> queryImages(Query query);

    List<Map> queryImagesAsMap(Query query);

    Page<Map> queryImagesAsMap(Query query, Pageable pageable);

    Page<Images> queryImages(Query query, Pageable pageable);

    long countQuery(Query query);

    Images save(Images images);

    Map saveAsMap(Images images);

    Images findImageById(String id);
}
