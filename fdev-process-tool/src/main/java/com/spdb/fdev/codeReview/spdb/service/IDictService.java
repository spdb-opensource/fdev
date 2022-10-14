package com.spdb.fdev.codeReview.spdb.service;

import com.spdb.fdev.codeReview.spdb.entity.DictEntity;

import java.util.List;
import java.util.Map;

/**
 * @Author liux81
 * @DATE 2021/11/11
 */
public interface IDictService {
    void add(DictEntity dictEntity);

    List<DictEntity> query(Map param);
}
