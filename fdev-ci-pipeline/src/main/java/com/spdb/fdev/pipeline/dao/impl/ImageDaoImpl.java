package com.spdb.fdev.pipeline.dao.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.pipeline.dao.IImageDao;
import com.spdb.fdev.pipeline.entity.Images;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class ImageDaoImpl implements IImageDao {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public List<Images> queryImages(Query query) {
        return mongoTemplate.find(query, Images.class);
    }

    @Override
    public List<Map> queryImagesAsMap(Query query) {
        return mongoTemplate.find(query, Images.class).stream().map(images -> objectMapper.convertValue(images, Map.class)).collect(Collectors.toList());
    }

    @Override
    public Page<Map> queryImagesAsMap(Query query, Pageable pageable) {
        long count = countQuery(query);
        if (pageable == null) {
            pageable = PageRequest.of(0, Math.toIntExact(count), Sort.Direction.DESC, Dict._ID);
        } else {
            query.with(pageable);
        }
        return PageableExecutionUtils.getPage(
                queryImagesAsMap(query),
                pageable,
                () -> count
        );
    }

    public Page<Images> queryImages(Query query, Pageable pageable) {
        long count = countQuery(query);
        if (pageable == null) {
            pageable = PageRequest.of(0, Math.toIntExact(count), Sort.Direction.DESC, Dict._ID);
        } else {
            query.with(pageable);
        }
        return PageableExecutionUtils.getPage(
                queryImages(query),
                pageable,
                () -> count
        );
    }

    public long countQuery(Query query) {
        return mongoTemplate.count(query, Images.class);
    }

    @Override
    public Images save(Images images) {
        return mongoTemplate.save(images);
    }

    @Override
    public Map saveAsMap(Images images) {
        return objectMapper.convertValue(mongoTemplate.save(images), Map.class);
    }

    @Override
    public Images findImageById(String id) {
        Query query = Query.query(Criteria.where(Dict.IMAGEID).is(id));
        return mongoTemplate.findOne(query, Images.class);
    }
}
