package com.spdb.fdev.fdemand.spdb.dao;

import com.spdb.fdev.fdemand.spdb.dto.DemandAssessDto;
import com.spdb.fdev.fdemand.spdb.entity.DemandAssess;
import com.spdb.fdev.fdemand.spdb.entity.DictEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DictDao {

    List<DictEntity> query(DictEntity dict);

    DictEntity queryByCode(String code);

    List<DictEntity> queryByTypes(Set<String> types);

    List<DictEntity> queryByCodes(List<String> codes);
}
