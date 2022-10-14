package com.spdb.fdev.codeReview.spdb.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.codeReview.spdb.entity.CodeFile;

import java.util.List;

/**
 * @Author liux81
 * @DATE 2021/11/12
 */
public interface IFileDao {

    List<CodeFile> query(CodeFile codeFile) throws JsonProcessingException;

    void save(CodeFile codeFile);

    void updateById(CodeFile codeFile) throws JsonProcessingException;

    void delete(String id);

    List<CodeFile> queryFilesByOrderId(String orderId);
}
