package com.spdb.fdev.component.dao;

import com.spdb.fdev.component.entity.MpassArchetype;
import com.spdb.fdev.component.entity.MpassComponent;

import java.util.List;


public interface IMpassComponentDao {



    List query(MpassComponent mpassComponent) throws Exception;

    MpassComponent save(MpassComponent mpassComponent) throws Exception;

    void delete(MpassComponent mpassComponent) throws Exception;

    MpassComponent queryById(String id) throws Exception;

    MpassComponent queryByGitlabUrl(String web_url);

    MpassComponent update(MpassComponent mpassComponent) throws Exception;

    List<MpassComponent> queryByUserId(String user_id);

    MpassComponent queryByNpmname(String npm_name);

    Integer queryDataByType(String start_date,String end_date);

    MpassComponent queryByGitlabId(String gitlabId);

    List<MpassComponent> getMpassComponentByIds(List params) throws Exception;


}
