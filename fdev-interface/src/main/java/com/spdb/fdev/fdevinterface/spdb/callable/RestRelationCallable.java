package com.spdb.fdev.fdevinterface.spdb.callable;

import com.esotericsoftware.minlog.Log;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevinterface.base.dict.Constants;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.base.utils.FileUtil;
import com.spdb.fdev.fdevinterface.spdb.entity.RestRelation;
import org.apache.commons.collections.CollectionUtils;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(Dict.REST_RELATION_CALLABLE)
public class RestRelationCallable extends BaseScanCallable {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    // 路径相关
    @Value(value = "${path.rest.client.transport}")
    private String restTransport;
    @Value(value = "${path.rest.client.web.transport}")
    private String restWebTransport;

    @Override
    public Map call() {
        logger.info(Constants.REST_CALLING_SCAN_START);
        Map returnMap = new HashMap();
        String transportFilePath;
        try {
            if (super.getAppServiceId().contains(Dict.WEB)) {
                // web项目路径：src\main\resources\config\channel\pe-transport.xml
                transportFilePath = FileUtil.getFilePath(super.getSrcPathList(), mainResources, restWebTransport);
            } else {
                // 非web项目路径：src\main\resources\config\spdb\common\channel\rest_transport.xml
                transportFilePath = FileUtil.getFilePath(super.getSrcPathList(), mainResources, restTransport);
            }
            if (StringUtils.isEmpty(transportFilePath)) {
            	logger.info("rest_transport.xml或pe-transport.xml文件路径不正确");
                return returnMap;
            }
            // 解析transport.xml文件
            List<RestRelation> restRelationList = analysisXmlFile(transportFilePath);
            // 持久化relation
            interfaceRelationService.saveRestRelation(restRelationList, super.getAppServiceId(), super.getBranchName());
            returnMap.put(Dict.SUCCESS, Constants.REST_CALLING_SCAN_SUCCESS);
            logger.info(Constants.REST_CALLING_SCAN_END);
            return returnMap;
        } catch (FdevException e) {
            returnMap.put(Dict.ERROR, errorMessageUtil.get(e));
            logger.error("{}", errorMessageUtil.get(e));
            return returnMap;
        }
    }

    /**
     * 解析配置Rest接口调用关系的xml文件
     *
     * @param transportFilePath
     * @return
     */
    public List<RestRelation> analysisXmlFile(String transportFilePath) {
        List<RestRelation> restRelationList = new ArrayList<>();
        // 获取根元素
        Element root = FileUtil.getXmlRootElement(transportFilePath, ErrorConstants.REST_CALLING_SCAN_ERROR);
        List<Element> beanList = root.elements(Dict.BEAN);
        if (CollectionUtils.isEmpty(beanList)) {
            return restRelationList;
        }
        // 获取元素信息
        for (Element bean : beanList) {
            // 获取beanid 判断是不是restUriMapping
            String beanId = bean.attributeValue(Dict.ID);
            if (!Dict.RESTURIMAPPING.equals(beanId)) {
                continue;
            }
            // 解析bean id="restUriMapping"的结点
            restRelationList = getParamList(bean);
        }
        return restRelationList;
    }

    /**
     * 解析bean id="restUriMapping"的结点
     *
     * @param bean
     * @return
     */
    public List<RestRelation> getParamList(Element bean) {
        List<RestRelation> restRelationList = new ArrayList<>();
        Element propsList = bean.element(Dict.PROPS);
        // 获取props的孩子节点
        List<Element> paramList = propsList.elements(Dict.PARAM);
        if (CollectionUtils.isNotEmpty(paramList)) {
            // 获取param的属性值
            for (Element param : paramList) {
                String url = param.attributeValue(Dict.NAME);
                if (!StringUtils.isEmpty(url)) {
                    String[] arr = url.split("//");
                    String[] array = arr[1].split("/");
                    String serviceProvider = array[0].toLowerCase();
                    String transId = array[2];
                    // 获取param的内容
                    RestRelation restRelation = new RestRelation();
                    restRelation.setTransId(transId);
                    restRelation.setServiceId(serviceProvider);
                    restRelation.setUri(url);
                    restRelation.setIsNew(1);
                    restRelation.setInterfaceType(Dict.REST);
                    restRelationList.add(restRelation);
                }
            }
        }
        return restRelationList;
    }

}
