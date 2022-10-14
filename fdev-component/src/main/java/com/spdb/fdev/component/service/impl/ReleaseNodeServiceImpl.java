package com.spdb.fdev.component.service.impl;

import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.component.dao.IReleaseNodeDao;
import com.spdb.fdev.component.entity.ReleaseNode;
import com.spdb.fdev.component.service.IMpassRelaseIssueService;
import com.spdb.fdev.component.service.IReleaseNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ReleaseNodeServiceImpl implements IReleaseNodeService {

    @Autowired
    IReleaseNodeDao iReleaseNodeDao;

    @Override
    public ReleaseNode saveTag(Map<String, String> map) throws Exception {
        String app_id = map.get("app_id");
        String release_node_name = map.get("release_node_name");
        String tag = map.get("tag");
        List<String> list = new ArrayList<String>();
        ReleaseNode releaseNode = iReleaseNodeDao.queryByAppIdAndReleaseNodeName(app_id, release_node_name);

        if (CommonUtils.isNullOrEmpty(releaseNode)) {
            releaseNode = new ReleaseNode();
            releaseNode.setRelease_node_name(release_node_name);
            releaseNode.setApp_id(app_id);
            list.add(tag);
            releaseNode.setTag_list(list);
            return iReleaseNodeDao.save(releaseNode);
        } else {
            list = releaseNode.getTag_list();
            list.add(tag);

            ReleaseNode releaseNode1 = new ReleaseNode(releaseNode.getId(),app_id,release_node_name,list);
            return iReleaseNodeDao.update(releaseNode1);
        }

    }

    @Override
    public List<String> queryTags(Map<String, String> map) throws Exception {
        String app_id = map.get("app_id");
        String release_node_name = map.get("release_node_name");
        ReleaseNode releaseNode = iReleaseNodeDao.queryByAppIdAndReleaseNodeName(app_id, release_node_name);
        if (!CommonUtils.isNullOrEmpty(releaseNode)) {
            return releaseNode.getTag_list();

        } else {
            return new ArrayList<>();
        }
    }

}