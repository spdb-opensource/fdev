package com.spdb.fdev.fdemand.spdb.service;

import com.spdb.fdev.fdemand.spdb.dto.DemandAssessDto;
import com.spdb.fdev.fdemand.spdb.entity.DemandAssess;
import com.spdb.fdev.fdemand.spdb.entity.DictEntity;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DictService {

    List<DictEntity> query(DictEntity dict) throws Exception;

    DictEntity queryByCode(String code) throws Exception;

    List<DictEntity> queryByTypes(Set<String> types) throws Exception;

    Map<String, String> getCodeNameMap(List<DictEntity> dicts) throws Exception;

    Map<String, String> getCodeNameMapByTypes(Set<String> types) throws Exception;
}
