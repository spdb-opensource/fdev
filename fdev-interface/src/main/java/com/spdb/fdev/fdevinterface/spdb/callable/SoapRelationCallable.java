package com.spdb.fdev.fdevinterface.spdb.callable;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevinterface.base.dict.Constants;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.base.utils.CommonUtil;
import com.spdb.fdev.fdevinterface.base.utils.FileUtil;
import com.spdb.fdev.fdevinterface.base.utils.SopSoapUtil;
import com.spdb.fdev.fdevinterface.base.utils.TimeUtils;
import com.spdb.fdev.fdevinterface.spdb.entity.EsbRelation;
import com.spdb.fdev.fdevinterface.spdb.entity.SoapRelation;
import com.spdb.fdev.fdevinterface.spdb.entity.SopRelation;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Soap接口调用方
 */
@Component(Dict.SOAP_RELATION_CALLABLE)
public class SoapRelationCallable extends BaseScanCallable {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //规范路径
    @Value(value = "${path.soap.client.map}")
    private String soapMapPath;
    @Value(value = "${path.soap.client.interface}")
    private String soapInterface;
    @Value(value = "${path.soap.client.header}")
    private String soapHeader;

    //兼容路径
    @Value(value = "${path.soap.client.map.one}")
    private String soapMapOne;
    @Value(value = "${path.soap.client.interface.one}")
    private String soapInterfaceOne;
    @Value(value = "${path.soap.client.header.one}")
    private String soapHeaderOne;
    @Value(value = "${path.soap.client.map.two}")
    private String soapMapTwo;
    @Value(value = "${path.soap.client.interface.two}")
    private String soapInterfaceTwo;
    @Value(value = "${path.soap.client.header.two}")
    private String soapHeaderTwo;

    @Override
    public Map call() {
        logger.info(Constants.SOAP_CALLING_SCAN_START);
        Map returnMap = new HashMap();
        try {
            // 获取报文头路径
            String headerPath = getSoapHeaderPath(super.getSrcPathList());
            // 获取别名文件路径
            String mappingXmlPath = getMappingXmlPath(super.getSrcPathList());
            // 获取Soap接口调用方interface路径
            String interfacePath = getInterfacePath(super.getSrcPathList());
            if (StringUtils.isEmpty(headerPath) || StringUtils.isEmpty(mappingXmlPath) || StringUtils.isEmpty(interfacePath)) {
                return returnMap;
            }
            // 获取Soap别名信息及其对应的Repository ID，Header Repository ID
            List<SoapRelation> mappingAndRepoIdList = getSoapMappingAndRepoId(mappingXmlPath, headerPath, interfacePath);
            List<SoapRelation> soapRelationList;
            // 获取数据库数据
            List<SoapRelation> oldSoapRelationList = interfaceRelationService.getSoapRelationList(super.getAppServiceId(), super.getBranchName());
            if (CollectionUtils.isEmpty(oldSoapRelationList)) {
                // 根据解析出的sop接口别名，去扫描对应xml文件
                soapRelationList = scanAll(mappingAndRepoIdList, interfacePath, headerPath);
            } else {
                // 只扫描有变化的xml文件
                soapRelationList = scanChange(mappingAndRepoIdList, oldSoapRelationList, interfacePath, headerPath);
            }
            relatedEsbData(soapRelationList);
            interfaceRelationService.saveSoapRelationList(soapRelationList);
            returnMap.put(Dict.SUCCESS, Constants.SOAP_CALLING_SCAN_SUCCESS);
            logger.info(Constants.SOAP_CALLING_SCAN_END);
            return returnMap;
        } catch (FdevException e) {
            returnMap.put(Dict.ERROR, errorMessageUtil.get(e));
            logger.error("{}", errorMessageUtil.get(e));
            return returnMap;
        }
    }

    /**
     * 获取Soap接口调用方报文头的文件路径
     *
     * @param srcPath
     */
    public String getSoapHeaderPath(List<String> srcPath) {
        String headerPath = FileUtil.getFilePath(srcPath, mainResources, soapHeader);
        if (!StringUtils.isEmpty(headerPath)) {
            return headerPath;
        }
        headerPath = FileUtil.getFilePath(srcPath, mainResources, soapHeaderOne);
        if (!StringUtils.isEmpty(headerPath)) {
            return headerPath;
        }
        return FileUtil.getFilePath(srcPath, mainResources, soapHeaderTwo);
    }

    /**
     * 获取Soap接口调用方的client_mapping.xml文件路径
     *
     * @param srcPath
     */
    public String getMappingXmlPath(List<String> srcPath) {
        String mappingXmlPath = null;
        for (String src : srcPath) {
            // 规范路径：src/main/resources/config/packet/webservice/client/client_mapping.xml
            mappingXmlPath = src + mainResources + soapMapPath;
            File soapMapFile = new File(mappingXmlPath);
            if (soapMapFile.exists()) {
                return mappingXmlPath;
            }
            // 兼容路径1：src\main\resources\config\packet\webservice
            String soapMapPathOne = src + mainResources + soapMapOne;
            File soapMapPathFileOne = new File(soapMapPathOne);
            if (soapMapPathFileOne.exists()) {
                // 兼容文件名1：src\main\resources\config\packet\webservice\wsclient_mapping.xml
                mappingXmlPath = soapMapPathOne + pathJoin + Constants.SOAP_MAP_ONE;
                File xmlOneFile = new File(mappingXmlPath);
                if (xmlOneFile.exists()) {
                    return mappingXmlPath;
                }
                // 兼容文件名2：src\main\resources\config\packet\webservice\client_mapping.xml
                mappingXmlPath = soapMapPathOne + pathJoin + Constants.SOAP_MAP_TWO;
                File xmlTwoFile = new File(mappingXmlPath);
                if (xmlTwoFile.exists()) {
                    return mappingXmlPath;
                }
            }

            // 兼容路径2：src\main\resources\packets\webservice
            String soapMapPathTwo = src + mainResources + soapMapTwo;
            File soapMapPathFileTwo = new File(soapMapPathTwo);
            if (soapMapPathFileTwo.exists()) {
                // 兼容文件名1：src\main\resources\packets\webservice\wsclient_mapping.xml
                mappingXmlPath = soapMapPathTwo + pathJoin + Constants.SOAP_MAP_ONE;
                File xmlOneFile = new File(mappingXmlPath);
                if (xmlOneFile.exists()) {
                    return mappingXmlPath;
                }
                // 兼容文件名2：src\main\resources\packets\webservice\client_mapping.xml
                mappingXmlPath = soapMapPathTwo + pathJoin + Constants.SOAP_MAP_TWO;
                File xmlTwoFile = new File(mappingXmlPath);
                if (xmlTwoFile.exists()) {
                    return mappingXmlPath;
                }
            }
            mappingXmlPath = null;
        }
        return mappingXmlPath;
    }

    /**
     * 获取Soap接口调用方的interface文件夹路径
     *
     * @param srcPath
     * @return
     */
    public String getInterfacePath(List<String> srcPath) {
        String interfacePath = null;
        for (String src : srcPath) {
            // 规范路径：src\main\resources\config\packet\webservice\client\interface
            interfacePath = src + mainResources + soapInterface;
            File file = new File(interfacePath);
            if (file.exists() && file.isDirectory()) {
                return interfacePath;
            }
            // 兼容路径1：src/main/resources/config/packet/webservice/interface
            interfacePath = src + mainResources + soapInterfaceOne;
            File fileOne = new File(interfacePath);
            if (fileOne.exists() && fileOne.isDirectory()) {
                return interfacePath;
            }
            // 兼容路径2：src/main/resources/packets/webservice/interface
            interfacePath = src + mainResources + soapInterfaceTwo;
            File fileTwo = new File(interfacePath);
            if (fileTwo.exists() && fileTwo.isDirectory()) {
                return interfacePath;
            }
            interfacePath = null;
        }
        return interfacePath;
    }

    /**
     * 获取Soap别名信息及其对应的Repository ID，Header Repository ID
     *
     * @param mappingXmlPath
     * @param interfacePath
     * @return
     */
    public List<SoapRelation> getSoapMappingAndRepoId(String mappingXmlPath, String headerPath, String interfacePath) {
        // 拼接GitLab上头文件所在文件夹路径
        String headerAfterResourcesPath = headerPath.split(Dict.RESOURCES)[1];
        String headerBeforePath = headerAfterResourcesPath.split("/SoapHeader")[0];
        String soapHeaderRepoPath = gitSrcMainResources + headerBeforePath;
        // 拼接GitLab上interface文件夹路径
        String interfaceAfterResourcesPath = interfacePath.split(Dict.RESOURCES)[1];
        String soapInterfaceRepoPath = gitSrcMainResources + interfaceAfterResourcesPath;
        // 如果interfacePathArray的长度为3，说明src文件夹外面还有一层以appServiceId(应用英文名)命名的文件夹
        if (interfacePath.contains(super.getAppServiceId())) {
            soapHeaderRepoPath = super.getAppServiceId() + pathJoin + soapHeaderRepoPath;
            soapInterfaceRepoPath = super.getAppServiceId() + pathJoin + soapInterfaceRepoPath;
        }
        List<Map> headerFilesRepoList = gitLabService.getProjectsRepository(super.getProjectId(), super.getAppServiceId(), super.getBranchName(), soapHeaderRepoPath);
        Map headerMap = new HashMap();
        for (Map map : headerFilesRepoList) {
            if (map.get(Dict.NAME).equals(Constants.SOAP_HEADER)) {
                headerMap = map;
                break;
            }
        }
        if (headerMap.size() == 0) {
            throw new FdevException(ErrorConstants.SOAP_CALLING_SCAN_ERROR, new String[]{"获取头文件的Repository ID失败！"});
        }
        // 获取GitLab上interface文件夹下所有子文件的Repository ID
        List<Map> interfaceRepositoryList = gitLabService.getProjectsRepository(super.getProjectId(), super.getAppServiceId(), super.getBranchName(), soapInterfaceRepoPath);
        // 解析别名文件(每次都要解析，因为可能存在client_mapping.xml没变，但新增了对应xml文件的情况)
        List<SoapRelation> mappingList = CommonUtil.mapList2ObjectList(SopSoapUtil.analysisMappingXmlFile(mappingXmlPath, ErrorConstants.SOAP_CALLING_SCAN_ERROR), SoapRelation.class);
        // 此处为了兼容msper-开头的应用，不取交集，因为msper-开头的应用，其详情可能存在msper-web-common-service公共组件里
        Iterator<SoapRelation> soapIterator = mappingList.iterator();
        while (soapIterator.hasNext()) {
            SoapRelation soapRelation = soapIterator.next();
            for (Map interfaceRepositoryMap : interfaceRepositoryList) {
                // 初始化为空字符串，否则后续需处理空指针异常
                soapRelation.setRepositoryId("");
                soapRelation.setHeaderRepositoryId("");
                if (interfaceRepositoryMap.get(Dict.NAME).equals(soapRelation.getInterfaceAlias() + Constants.XML_FILE)) {
                    // 设置Repository ID
                    soapRelation.setRepositoryId((String) interfaceRepositoryMap.get(Dict.ID));
                    soapRelation.setHeaderRepositoryId((String) headerMap.get(Dict.ID));
                    break;
                }
            }
        }
        return mappingList;
    }

    /**
     * 根据解析出的soap接口别名，去扫描对应xml文件
     *
     * @param mappingAndRepoIdList
     * @param interfacePath
     * @param headerPath
     * @return
     */

    public List<SoapRelation> scanAll(List<SoapRelation> mappingAndRepoIdList, String interfacePath, String headerPath) {
        List<SoapRelation> soapRelationList = new ArrayList<>();
        SoapRelation header = CommonUtil.map2Object(SopSoapUtil.analysisHeaderFile(headerPath, ErrorConstants.SOAP_CALLING_SCAN_ERROR), SoapRelation.class);
        for (SoapRelation soapMappingRepo : mappingAndRepoIdList) {
            String xmlPath = interfacePath + pathJoin + soapMappingRepo.getInterfaceAlias() + Constants.XML_FILE;
            SoapRelation soapRelation = getSoapRelation(soapMappingRepo, xmlPath, header);
            soapRelationList.add(soapRelation);
        }
        return soapRelationList;
    }

    /**
     * 只扫描有变化的xml文件
     *
     * @param mappingAndRepoIdList
     * @param oldSoapRelationList
     * @param interfacePath
     * @param headerPath
     * @return
     */
    public List<SoapRelation> scanChange(List<SoapRelation> mappingAndRepoIdList, List<SoapRelation> oldSoapRelationList, String interfacePath, String headerPath) {
        List<SoapRelation> soapRelationList = new ArrayList<>();
        List<String> notNewIdList = new ArrayList<>();
        SoapRelation header = CommonUtil.map2Object(SopSoapUtil.analysisHeaderFile(headerPath, ErrorConstants.SOAP_CALLING_SCAN_ERROR), SoapRelation.class);
        for (SoapRelation soapMappingRepo : mappingAndRepoIdList) {
            String interfaceAlias = soapMappingRepo.getInterfaceAlias();
            String xmlPath = interfacePath + pathJoin + interfaceAlias + Constants.XML_FILE;
            String repositoryId = soapMappingRepo.getRepositoryId();
            // 标记是否存在对应的老数据
            boolean flag = false;
            for (SoapRelation oldSoap : oldSoapRelationList) {
                if (interfaceAlias.equals(oldSoap.getInterfaceAlias())) {
                    flag = true;
                    if (soapMappingRepo.getHeaderRepositoryId().equals(oldSoap.getHeaderRepositoryId())) {
                        // 如果本次扫描的Repository ID与数据库里面的不一致，则需要扫描xml文件
                        if (repositoryId.equals(oldSoap.getRepositoryId())) {
                            // 判断mapping文件里<prop key="">中的key是否改变
                            if (soapMappingRepo.getUri() != null && !soapMappingRepo.getUri().equals(oldSoap.getUri())) {
                                notNewIdList.add(oldSoap.getId());
                                oldSoap.setVer(oldSoap.getVer() + 1);
                                oldSoap.setId(null);
                                oldSoap.setUri(soapMappingRepo.getUri());
                                oldSoap.setTransId(soapMappingRepo.getTransId());
                                oldSoap.setCreateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
                                oldSoap.setUpdateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
                                soapRelationList.add(oldSoap);
                            }
                        } else {
                            notNewIdList.add(oldSoap.getId());
                            oldSoap.setVer(oldSoap.getVer() + 1);
                            oldSoap.setId(null);
                            // 更新接口消息体
                            updateOldSoap(oldSoap, xmlPath, repositoryId);
                            oldSoap.setCreateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
                            oldSoap.setUpdateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
                            soapRelationList.add(oldSoap);
                        }
                    } else {
                        notNewIdList.add(oldSoap.getId());
                        oldSoap.setId(null);
                        oldSoap.setUri(soapMappingRepo.getUri());
                        oldSoap.setTransId(soapMappingRepo.getTransId());
                        // 如果本次扫描的Header Repository ID与数据库里面的不一致，则需要更新消息头信息
                        oldSoap.setReqHeader(header.getReqHeader());
                        oldSoap.setRspHeader(header.getRspHeader());
                        oldSoap.setHeaderRepositoryId(soapMappingRepo.getHeaderRepositoryId());
                        oldSoap.setVer(oldSoap.getVer() + 1);
                        oldSoap.setCreateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
                        oldSoap.setUpdateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
                        if (!repositoryId.equals(oldSoap.getRepositoryId())) {
                            // 更新接口消息体
                            updateOldSoap(oldSoap, xmlPath, repositoryId);
                        }
                        soapRelationList.add(oldSoap);
                    }
                    // 删除后，剩下此次扫描相对上次扫描删除的接口
                    oldSoapRelationList.remove(oldSoap);
                    break;
                }
            }
            if (!flag) {
                // 获取接口请求体
                SoapRelation soapRelation = getSoapRelation(soapMappingRepo, xmlPath, header);
                soapRelationList.add(soapRelation);
            }
        }
        if (CollectionUtils.isNotEmpty(oldSoapRelationList)) {
            List<String> idList = oldSoapRelationList.stream().map(SoapRelation::getId).collect(Collectors.toList());
            notNewIdList.addAll(idList);
        }
        interfaceRelationService.updateSoapRelationIsNewByIds(notNewIdList);
        return soapRelationList;
    }


    /**
     * 获取接口请求体
     *
     * @param soapMappingRepo
     * @param xmlPath
     * @return
     */
    public SoapRelation getSoapRelation(SoapRelation soapMappingRepo, String xmlPath, SoapRelation header) {
        // 获取接口请求体
        SoapRelation soapRelation = CommonUtil.map2Object(SopSoapUtil.analysisInterfaceXml(xmlPath, ErrorConstants.SOAP_CALLING_SCAN_ERROR), SoapRelation.class);
        soapRelation.setRepositoryId(soapMappingRepo.getRepositoryId());
        soapRelation.setHeaderRepositoryId(soapMappingRepo.getHeaderRepositoryId());
        soapRelation.setTransId(soapMappingRepo.getTransId());
        soapRelation.setInterfaceAlias(soapMappingRepo.getInterfaceAlias());
        soapRelation.setUri(soapMappingRepo.getUri());
        soapRelation.setServiceCalling(super.getAppServiceId());
        soapRelation.setBranch(super.getBranchName());
        soapRelation.setServiceId(Dict.ESB);
        soapRelation.setReqHeader(header.getReqHeader());
        soapRelation.setRspHeader(header.getRspHeader());
        // 设置请求类型
        soapRelation.setRequestType(Dict.HTTP_XML);
        // 设置请求协议
        soapRelation.setRequestProtocol(Dict.SOAP);
        // 设置接口类型
        soapRelation.setInterfaceType(Dict.SOAP);
        soapRelation.setCreateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
        soapRelation.setUpdateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
        return soapRelation;
    }

    /**
     * 更新接口消息体
     *
     * @param oldSoap
     * @param xmlPath
     * @param repositoryId
     */
    public void updateOldSoap(SoapRelation oldSoap, String xmlPath, String repositoryId) {
        SopRelation newSop = CommonUtil.map2Object(SopSoapUtil.analysisInterfaceXml(xmlPath, ErrorConstants.SOAP_CALLING_SCAN_ERROR), SopRelation.class);
        oldSoap.setRequest(newSop.getRequest());
        oldSoap.setResponse(newSop.getResponse());
        // 设置Repository ID
        oldSoap.setRepositoryId(repositoryId);
        oldSoap.setIsNew(1);
    }

    /**
     * 关联ESB数据
     *
     * @param soapRelationList
     */
    private void relatedEsbData(List<SoapRelation> soapRelationList) {
        List<String> transIds = soapRelationList.stream().map(SoapRelation::getTransId).collect(Collectors.toList());
        List<EsbRelation> esbRelationList = esbRelationDao.getEsbRelationByConsumerMsgType(transIds);
        for (SoapRelation soapRelation : soapRelationList) {
            for (EsbRelation esbRelation : esbRelationList) {
                if (soapRelation.getTransId().equals(esbRelation.getServiceAndOperationId())) {
                    soapRelation.setInterfaceName(esbRelation.getTranName());
                    soapRelation.setServiceId(esbRelation.getProviderSysIdAndName());
                    soapRelation.setEsbTransId(esbRelation.getTranId());
                    soapRelation.setEsbServiceId(esbRelation.getServiceId());
                    soapRelation.setEsbOperationId(esbRelation.getOperationId());
                    break;
                }
            }
        }
    }

}
