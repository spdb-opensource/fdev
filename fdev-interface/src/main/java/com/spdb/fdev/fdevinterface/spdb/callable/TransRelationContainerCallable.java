package com.spdb.fdev.fdevinterface.spdb.callable;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevinterface.base.dict.Constants;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.base.utils.FileUtil;
import com.spdb.fdev.fdevinterface.base.utils.GitlabTransportUtil;
import com.spdb.fdev.fdevinterface.base.utils.TimeUtils;
import com.spdb.fdev.fdevinterface.spdb.entity.TransRelation;
import com.spdb.fdev.fdevinterface.spdb.service.TransRelationService;
import org.apache.commons.collections.CollectionUtils;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.net.URI;
import java.util.*;

@Component(Dict.TRANS_RELATION_CONTAINER_CALLABLE)
@RefreshScope
public class TransRelationContainerCallable extends BaseScanCallable {

    private Logger logger = LoggerFactory.getLogger(TransRelationContainerCallable.class);

    @Value("${path.trans.conf}")
    private String transConfFile;
    @Value("${ebank.common.mob.url}")
    private String ebankCommonMobUrl;
    @Autowired
    private GitlabTransportUtil gitlabTransportUtil;
    @Autowired
    private TransRelationService transRelationService;

    @Override
    public Object call() {
        logger.info(Constants.TRANS_RELATION_SCAN_START);
        Map returnMap = new HashMap();
        // 获取配置文件路径
        List<String> confPathList = getTransConfigPath();
        if (CollectionUtils.isEmpty(confPathList)) {
            return returnMap;
        }
        try {
            List<TransRelation> transRelationList = getSaveRelationList(confPathList);
            transRelationService.save(transRelationList, super.getAppServiceId(), super.getBranchName(), Dict.CLIENT);
            logger.info(Constants.TRANS_RELATION_SCAN_END);
            returnMap.put(Dict.SUCCESS, Constants.TRANS_RELATION_SCAN_SUCCESS);
            return returnMap;
        } catch (FdevException e) {
            returnMap.put(Dict.ERROR, errorMessageUtil.get(e));
            logger.error("{}", errorMessageUtil.get(e));
            return returnMap;
        }
    }

    /**
     * 组装真正的微服务名称
     *
     * @return
     * @throws Exception
     */
    private List<TransRelation> getSaveRelationList(List<String> confPathList) {
        List<TransRelation> transRelationList = getTransRelation(confPathList);
        Map<String, String> map = getEbankCommonMobProp();
        for (TransRelation transRelation : transRelationList) {
            String serviceId = transRelation.getServiceId();
            if (serviceId == null) {
                if (Dict.SPDB_CLI_MOBCLI.equals(super.getAppServiceId())) {
                    transRelation.setServiceId(Dict.SPDB_MBANK_PER);
                } else {
                    transRelation.setServiceId("");
                }
                continue;
            }
            if (map.containsKey(serviceId)) {
                transRelation.setServiceId(map.get(serviceId));
            }
        }
        return transRelationList;
    }

    /**
     * 获取配置文件路径src/main/webapp/WEB-INF/conf/trans-conf.xml  和 trans-conf2.xml
     *
     * @return
     */
    private List<String> getTransConfigPath() {
        List<String> confPathList = new ArrayList<>();
        String transConfFilePath = null;
        for (String src : super.getSrcPathList()) {
            transConfFilePath = src + transConfFile;
            File file = new File(transConfFilePath);
            if (!file.exists()) {
                return confPathList;
            }
        }
        if (!StringUtils.isEmpty(transConfFilePath)) {
            String confPath1 = transConfFilePath + super.pathJoin + Constants.TRANS_CONFIG_XML;
            File file1 = new File(confPath1);
            if (file1.exists()) {
                confPathList.add(confPath1);
            }
            String confPath2 = transConfFilePath + super.pathJoin + Constants.TRANS_CONFIG2_XML;
            File file2 = new File(confPath2);
            if (file2.exists()) {
                confPathList.add(confPath2);
            }
        }
        return confPathList;
    }

    /**
     * 获取配置文件的trans
     *
     * @return
     */
    private List<TransRelation> getTransRelation(List<String> confPathList) {
        List<TransRelation> transRelationList = new ArrayList<>();

        for (String confPath : confPathList) {
            Element root = FileUtil.getXmlRootElement(confPath, ErrorConstants.TRANS_RELATION_SCAN_ERROR);
            List<Element> beanList = root.elements(Dict.BEAN);
            if (CollectionUtils.isEmpty(beanList)) {
                return transRelationList;
            }
            // 获取元素信息
            for (Element bean : beanList) {
                TransRelation transRelation = new TransRelation();
                transRelation.setServiceCalling(super.getAppServiceId());
                transRelation.setBranch(super.getBranchName());
                transRelation.setChannel(Dict.CLIENT);
                transRelation.setCreateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
                String beanId = bean.attributeValue(Dict.ID);
                transRelation.setTransId(beanId);
                List<Element> propList = bean.elements(Dict.PROPERTY);
                for (Element property : propList) {
                    String propName = property.attributeValue(Dict.NAME);
                    if (Dict.WEIWAPURL.equals(propName)) {
                        transRelation.setServiceId(property.attributeValue(Dict.VALUE));
                    }
                }
                transRelationList.add(transRelation);
            }
        }
        return transRelationList;
    }

    /**
     * 解析配置文件，获得真正的微服务名称
     *
     * @return
     */
    private Map<String, String> getEbankCommonMobProp() {
        Map<String, String> map = new HashMap<>();
        try {
            String content = getGitLabFileContent();
            String[] lines = content.split("\n");
            // 去重
            Set<String> hashSet = new HashSet<>();
            for (String line : lines) {
                if (!line.contains("#") && line.split("=").length == 2) {
                    hashSet.add(line);
                }
            }
            for (String str : hashSet) {
                String key = str.split("=")[0].trim();
                String value = str.split("=")[1].trim();
                map.put(key, value);
            }
        } catch (Exception e) {
            logger.error("{}", Constants.ANALYSIS_FILE_ERROR + "mobper_prosh的ebank_common_mob.properties" + "，" + e.getMessage());
        }
        return map;
    }

    /**
     * 获得mobper_prosh的ebank_common_mob.properties文件内容，此处只获取生产上的
     *
     * @return
     */
    private String getGitLabFileContent() {
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add(Dict.PRIVATE_TOKEN, "KzdcV1psJCVsj5eAVv5n");
        URI uri = UriComponentsBuilder.fromHttpUrl(ebankCommonMobUrl).build(true).toUri();
        return (String) gitlabTransportUtil.submitGet(uri, header);
    }

}
