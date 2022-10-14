package com.spdb.fdev.fdevapp.spdb.service.impl;

import com.spdb.fdev.fdevapp.spdb.dao.IAppShaDao;
import com.spdb.fdev.fdevapp.spdb.entity.AppSha;
import com.spdb.fdev.fdevapp.spdb.service.IAppShaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * @author xxx
 * @date 2019/7/1 18:11
 */
@Service
public class AppShaServiceImpl implements IAppShaService {

    private static Logger logger = LoggerFactory.getLogger(AppShaServiceImpl.class);

    @Autowired
    private IAppShaDao appShaDao;

    /**
     * 配合持续集成 判断当前 分支 是否要继续允许
     *
     * @param gitlab_id
     * @param sha
     * @param branch
     * @return
     * @throws Exception
     */
    @Override
    public Map getSha(Integer gitlab_id, String sha, String branch) throws Exception {
        Map<String, Boolean> map = new HashMap<>();

        AppSha result = this.appShaDao.getAppSha("gitlab_id", gitlab_id);
        if (null == result) {
            // 说明 数据库里没有当前数据
            AppSha appSha = new AppSha();
            appSha.setGitlab_id(gitlab_id);
            LinkedHashSet<String> hashSet = new LinkedHashSet<>();
            hashSet.add(sha);
            appSha.setSha(hashSet);
            appSha.setBranch(branch);
            try {
                this.appShaDao.addAppSha(appSha);
            } catch (Exception e) {
                logger.error("error message:"+e);
                map.put("flag", false);
                return map;
            }
            map.put("flag", true);
        } else {
            LinkedHashSet resultSha = result.getSha();
            if (resultSha.add(sha)) {
                // 不存在则 更新当前数据
                result.setSha(resultSha);
                try {
                    this.appShaDao.updateAppSha(result);
                } catch (Exception e) {
                    logger.error("error message:"+e);
                    map.put("flag", false);
                    return map;
                }
                map.put("flag", true);
            } else {
                // 说明存在当前sha
                map.put("flag", false);
            }
        }
        return map;
    }
}
