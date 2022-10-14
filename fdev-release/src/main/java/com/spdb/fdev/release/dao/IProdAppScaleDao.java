package com.spdb.fdev.release.dao;

import com.spdb.fdev.release.entity.ProdAppScale;

import java.util.List;
import java.util.Map;

public interface IProdAppScaleDao {

    ProdAppScale addAppScale(ProdAppScale prodAppScale);

    void updateAppScale(String prod_id,String application_id,List<String> deploy_type,List<Map<String,Object>> new_env_scales);

    void deleteAppScale(Map<String, Object> map);

    List<ProdAppScale> queryAppScale(Map<String, Object> map);

    ProdAppScale queryAppScaleById(String prod_id,String application_id);

}
