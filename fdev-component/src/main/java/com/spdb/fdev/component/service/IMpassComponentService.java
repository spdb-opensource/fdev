package com.spdb.fdev.component.service;

import com.spdb.fdev.component.entity.MpassComponent;

import java.util.List;

public interface IMpassComponentService {

    List query(MpassComponent mpassComponent) throws Exception;

    MpassComponent save(MpassComponent mpassComponent) throws Exception;

    void delete(MpassComponent mpassComponent) throws Exception;

    MpassComponent queryById(String id) throws Exception;

    MpassComponent queryByGitlabUrl(String web_url);

    MpassComponent update(MpassComponent mpassComponent) throws Exception;

    List<MpassComponent> queryByUserId(String user_id);

    MpassComponent queryByGitlabId(String gitlabId);
}
