package com.auto.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.auto.entity.Element;

@Repository
public interface ElementMapper {

    void addElement(Element element) throws Exception;

    List<Element> queryElement(@Param("search") String search, @Param("valid") String valid) throws Exception;
    
    List<Element> queryElementByName(@Param("elementName") String elementName, @Param("elementId") String elementId) throws Exception;

    void deleteElement(@Param("elementId") String elementId, @Param("userNameEn") String userNameEn, @Param("time") String time) throws Exception;

    void updateElement(@Param("elementId") String elementId, @Param("elementType") String elementType,
                    @Param("elementContent") String elementContent, @Param("elementName") String elementName, 
                    @Param("elementDir") String elementDir, @Param("userNameEn") String userNameEn, 
                    @Param("time") String time) throws Exception;
}