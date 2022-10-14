package com.auto.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.auto.entity.ElementDic;
import com.auto.entity.User;

@Repository
public interface ElementDicMapper {

    void addElementDic(ElementDic elementDic) throws Exception;

    List<ElementDic> queryElementDic(@Param("search") String search, @Param("valid") String valid) throws Exception;
    
    List<ElementDic> queryElementDicByMethod(@Param("elementDicMethod") String elementDicMethod, @Param("elementDicId") String elementDicId) throws Exception;

    void deleteElementDic(@Param("elementDicId") String elementDicId, @Param("userNameEn") String userNameEn, @Param("time") String time) throws Exception;

    void updateElementDic(@Param("elementDicId") String elementDicId, @Param("elementDicMethod") String elementDicMethod,
                    @Param("elementDicParam") String elementDicParam, @Param("methodDesc") String methodDesc, 
                    @Param("userNameEn") String userNameEn, @Param("time") String time) throws Exception;
}