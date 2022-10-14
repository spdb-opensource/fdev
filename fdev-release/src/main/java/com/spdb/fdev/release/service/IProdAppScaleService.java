package com.spdb.fdev.release.service;

import com.spdb.fdev.release.entity.ProdAppScale;

import java.util.List;
import java.util.Map;

public interface IProdAppScaleService {

    ProdAppScale addAppScale(ProdAppScale prodAppScale) throws Exception;

    void updateAppScale(Map<String, Object> map) throws Exception;

    void deleteAppScale(Map<String, Object> map);

    List<Map<String, Object>> queryAppScale(Map<String, Object> map) throws Exception;

}
