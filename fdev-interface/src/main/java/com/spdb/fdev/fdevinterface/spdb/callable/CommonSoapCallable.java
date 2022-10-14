package com.spdb.fdev.fdevinterface.spdb.callable;

import com.google.common.collect.Lists;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevinterface.base.dict.Constants;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.base.utils.CommonUtil;
import com.spdb.fdev.fdevinterface.base.utils.FileUtil;
import com.spdb.fdev.fdevinterface.base.utils.SopSoapUtil;
import com.spdb.fdev.fdevinterface.base.utils.TimeUtils;
import com.spdb.fdev.fdevinterface.spdb.dto.Param;
import com.spdb.fdev.fdevinterface.spdb.entity.SoapRelation;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(Dict.COMMON_SOAP_CALLABLE)
public class CommonSoapCallable extends BaseScanCallable {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    // 路径相关
    @Value(value = "${msper.web.common.service.interface}")
    private String commonInterface;

    @Override
    public Map call() {
        logger.info(Constants.COMMON_SOAP_SCAN_START);
        Map returnMap = new HashMap();
        try {
            String interfacePath = FileUtil.getFilePath(super.getSrcPathList(), mainResources, commonInterface);
            List<SoapRelation> soapRelationList;
            String path = gitSrcMainResources + commonInterface;
            // 获取GitLab上的Repository ID
            List<Map> repositoryList = gitLabService.getProjectsRepository(super.getProjectId(), super.getAppServiceId(), super.getBranchName(), path);
            // 获取数据库数据
            List<SoapRelation> oldSoapRelationList = interfaceRelationService.getSoapRelationList(super.getAppServiceId(), super.getBranchName());
            if (CollectionUtils.isEmpty(oldSoapRelationList)) {
                // 解析全部接口报文体
                soapRelationList = scanAll(repositoryList, interfacePath);
            } else {
                // 解析有变化的接口报文体
                soapRelationList = scanChange(repositoryList, interfacePath, oldSoapRelationList);
            }
            interfaceRelationService.saveSoapRelationList(soapRelationList);
            logger.info(Constants.COMMON_SOAP_SCAN_END);
            returnMap.put(Dict.SUCCESS, Constants.COMMON_SOAP_SCAN_SUCCESS);
            return returnMap;
        } catch (FdevException e) {
            returnMap.put(Dict.ERROR, errorMessageUtil.get(e));
            logger.info("{}", Constants.COMMON_SOAP_SCAN_ERROR + errorMessageUtil.get(e));
            return returnMap;
        }
    }

    /**
     * 解析全部接口报文体
     *
     * @param repositoryList
     * @param interfacePath
     * @return
     */
    public List<SoapRelation> scanAll(List<Map> repositoryList, String interfacePath) {
        List<SoapRelation> soapRelationList = new ArrayList<>();
        for (Map repositoryMap : repositoryList) {
            String fileName = (String) repositoryMap.get(Dict.NAME);
            String xmlPath = interfacePath + pathJoin + fileName;
            File xmlFile = new File(xmlPath);
            if (!xmlFile.exists()) {
                break;
            }
            // 解析xml文件
            SoapRelation soapRelation = CommonUtil.map2Object(SopSoapUtil.analysisInterfaceXml(xmlPath, ErrorConstants.SOAP_CALLING_SCAN_ERROR), SoapRelation.class);
            soapRelation.setTransId("");
            soapRelation.setInterfaceAlias(fileName.substring(0, fileName.length() - 4));
            soapRelation.setServiceCalling(super.getAppServiceId());
            soapRelation.setBranch(super.getBranchName());
            soapRelation.setServiceId(Dict.ESB);
            soapRelation.setRepositoryId((String) repositoryMap.get(Dict.ID));
            soapRelation.setUri("");
            soapRelation.setReqHeader(Lists.newArrayList());
            soapRelation.setRspHeader(Lists.newArrayList());
            soapRelation.setCreateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
            soapRelation.setUpdateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
            soapRelationList.add(soapRelation);
        }
        return soapRelationList;
    }

    /**
     * 解析有变化的接口报文体
     *
     * @param repositoryList
     * @param interfacePath
     * @param oldSoapRelationList
     * @return
     */
    public List<SoapRelation> scanChange(List<Map> repositoryList, String interfacePath, List<SoapRelation> oldSoapRelationList) {
        List<SoapRelation> soapRelationList = new ArrayList<>();
        List<String> notNewIdList = new ArrayList<>();
        for (SoapRelation old : oldSoapRelationList) {
            boolean flag = false;
            String fileName = old.getInterfaceAlias() + Constants.XML_FILE;
            String xmlPath = interfacePath + pathJoin + fileName;
            for (Map repositoryMap : repositoryList) {
                if (fileName.equals(repositoryMap.get(Dict.NAME))) {
                    flag = true;
                    if (!repositoryMap.get(Dict.ID).equals(old.getRepositoryId())) {
                        notNewIdList.add(old.getId());
                        // 解析xml文件
                        Map soapMap = SopSoapUtil.analysisInterfaceXml(xmlPath, ErrorConstants.SOAP_CALLING_SCAN_ERROR);
                        old.setRequest((List<Param>) soapMap.get(Dict.L_REQUEST));
                        old.setResponse((List<Param>) soapMap.get(Dict.L_RESPONSE));
                        old.setRepositoryId((String) repositoryMap.get(Dict.ID));
                        old.setVer(old.getVer() + 1);
                        old.setIsNew(1);
                        old.setId(null);
                        old.setUpdateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
                        soapRelationList.add(old);
                    }
                    repositoryList.remove(repositoryMap);
                    break;
                }
            }
            if (!flag) {
                notNewIdList.add(old.getId());
            }
        }
        if (CollectionUtils.isNotEmpty(repositoryList)) {
            soapRelationList.addAll(scanAll(repositoryList, interfacePath));
        }
        // 将isNew更新为0
        interfaceRelationService.updateSoapRelationIsNewByIds(notNewIdList);
        return soapRelationList;
    }

}
