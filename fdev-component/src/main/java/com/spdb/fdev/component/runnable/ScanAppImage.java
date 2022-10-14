package com.spdb.fdev.component.runnable;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.Util;
import com.spdb.fdev.component.entity.ImageApplication;
import com.spdb.fdev.component.service.IGitlabSerice;
import com.spdb.fdev.component.service.IImageApplicationService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * 扫描应用使用镜像信息
 */
@Component(Constants.SCAN_APP_IMAGE)
public class ScanAppImage extends BaseRunnable {

    private static final Logger logger = LoggerFactory.getLogger(ScanAppImage.class);

    @Autowired
    private IGitlabSerice gitlabSerice;

    @Autowired
    private IImageApplicationService imageApplicationService;

    @Override
    public void run() {
        Map param = super.getParam();
        //获取扫描参数
        String id = (String) param.get(Dict.ID);//应用id
        String name_en = (String) param.get(Dict.NAME_EN);
        Integer gitlab_id = (Integer) param.get(Dict.GITLAB_PROJECT_ID);
        try {
            //根据应用信息， 删除应用和镜像关系
            imageApplicationService.deleteAllByApplicationId(id);
            String fileDir = "gitlab-ci%2FDockerfile";
            String dockerFileContent = gitlabSerice.getGitLabFileContent(gitlab_id, fileDir, Dict.MASTER);
            if (dockerFileContent.contains("\r\n")) {
                dockerFileContent = dockerFileContent.replace("\r\n", "\n");
            }
            String[] contentSplit = dockerFileContent.split("\n");
            for (int i = 0; i < contentSplit.length; i++) {
                String line = contentSplit[i];
                if (StringUtils.isEmpty(line)) {
                    continue;
                }
                if (line.startsWith("FROM")) {
                    String[] split = line.split("/");
                    String imageName = split[split.length - 1].trim();
                    String[] image = imageName.split(":");
                    ImageApplication imageApplication = new ImageApplication();
                    imageApplication.setApplication_id(id);
                    imageApplication.setImage_name(image[0]);
                    imageApplication.setImage_tag(image[1]);
                    imageApplication.setUpdate_time(Util.simdateFormat(new Date()));
                    //查询当前应用和镜像关联是否存在，存在则进行更新
                    ImageApplication query = imageApplicationService.queryByApplicationAndImage(id, imageApplication.getImage_name());
                    if (null == query) {
                        imageApplicationService.save(imageApplication);
                    } else {
                        query.setUpdate_time(imageApplication.getUpdate_time());
                        query.setImage_tag(imageApplication.getImage_tag());
                        imageApplicationService.update(query);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("{},ScanAppImage更新镜像和应用信息失败,{}", name_en, e.getMessage());
        }

    }
}
