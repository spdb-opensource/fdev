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
import com.spdb.fdev.fdevinterface.spdb.entity.SoapApi;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Soap接口提供方
 */
@Component(Dict.SOAP_CALLABLE)
public class SoapCallable extends BaseScanCallable {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    // 路径相关
    @Value(value = "${path.soap.server.map}")
    private String soapMapPath;
    @Value(value = "${path.soap.server.interface}")
    private String soapInterface;
    @Value(value = "${path.soap.server.header}")
    private String soapHeader;

    @Override
    public Map call() {
        logger.info(Constants.SOAP_PROVIDER_SCAN_START);
        Map returnMap = new HashMap();
        try {
            // 获取报文头路径
            String headerPath = FileUtil.getFilePath(super.getSrcPathList(), mainResources, soapHeader);
            // 获取别名文件路径
            String mappingXmlPath = FileUtil.getFilePath(super.getSrcPathList(), mainResources, soapMapPath);
            // 获取Soap接口调用方interface路径
            String interfacePath = FileUtil.getFilePath(super.getSrcPathList(), mainResources, soapInterface);
            if (StringUtils.isEmpty(headerPath) || StringUtils.isEmpty(mappingXmlPath) || StringUtils.isEmpty(interfacePath)) {
                return returnMap;
            }
            // 获取Soap别名信息及其对应的Repository ID，Header Repository ID
            List<SoapApi> mappingAndRepoIdList = getSoapMappingAndRepoId(mappingXmlPath, headerPath, interfacePath);
            List<SoapApi> soapApiList;
            // 获取数据库数据
            List<SoapApi> oldSoapApiList = interfaceService.getSoapApiList(super.getAppServiceId(), super.getBranchName());
            if (CollectionUtils.isEmpty(oldSoapApiList)) {
                // 根据解析出的soap接口别名，去扫描对应xml文件
                soapApiList = scanAll(mappingAndRepoIdList, interfacePath, headerPath);
            } else {
                // 只扫描有变化的xml文件
                soapApiList = scanChange(mappingAndRepoIdList, oldSoapApiList, interfacePath, headerPath);
            }
            relatedEsbData(soapApiList);
            interfaceService.saveSoapApiList(soapApiList);
            returnMap.put(Dict.SUCCESS, Constants.SOAP_PROVIDER_SCAN_SUCCESS);
            logger.info(Constants.SOAP_PROVIDER_SCAN_END);
            return returnMap;
        } catch (FdevException e) {
            returnMap.put(Dict.ERROR, errorMessageUtil.get(e));
            logger.error("{}", errorMessageUtil.get(e));
            return returnMap;
        }
    }

    /**
     * 获取Soap别名信息及其对应的Repository ID，Header Repository ID
     *
     * @param mappingXmlPath
     * @param interfacePath
     * @return
     */
    private List<SoapApi> getSoapMappingAndRepoId(String mappingXmlPath, String headerPath, String interfacePath) {
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
            throw new FdevException(ErrorConstants.SOAP_PROVIDER_SCAN_ERROR, new String[]{"获取头文件的Repository ID失败！"});
        }
        // 获取GitLab上interface文件夹下所有子文件的Repository ID
        List<Map> interfaceRepositoryList = gitLabService.getProjectsRepository(super.getProjectId(), super.getAppServiceId(), super.getBranchName(), soapInterfaceRepoPath);
        // 解析别名文件(每次都要解析，因为可能存在client_mapping.xml没变，但新增了对应xml文件的情况)
        List<SoapApi> mappingList = CommonUtil.mapList2ObjectList(SopSoapUtil.analysisMappingXmlFile(mappingXmlPath, Constants.SOAP_PROVIDER_SCAN_ERROR), SoapApi.class);
        // 组装client_mapping.xml中配置过的xml文件的RepoSitory ID，减少后期处理逻辑的循环次数
        Iterator<SoapApi> soapIterator = mappingList.iterator();
        while (soapIterator.hasNext()) {
            SoapApi soapApi = soapIterator.next();
            // 标记是否存在对应xml文件
            boolean flag = false;
            for (Map interfaceRepositoryMap : interfaceRepositoryList) {
                if (interfaceRepositoryMap.get(Dict.NAME).equals(soapApi.getInterfaceAlias() + Constants.XML_FILE)) {
                    flag = true;
                    // 设置Repository ID
                    soapApi.setRepositoryId((String) interfaceRepositoryMap.get(Dict.ID));
                    soapApi.setHeaderRepositoryId((String) headerMap.get(Dict.ID));
                    break;
                }
            }
            if (!flag) {
                soapIterator.remove();
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

    private List<SoapApi> scanAll(List<SoapApi> mappingAndRepoIdList, String interfacePath, String headerPath) {
        List<SoapApi> soapApiList = new ArrayList<>();
        SoapApi header = CommonUtil.map2Object(SopSoapUtil.analysisHeaderFile(headerPath, Constants.SOAP_PROVIDER_SCAN_ERROR), SoapApi.class);
        for (SoapApi soapMappingRepo : mappingAndRepoIdList) {
            String xmlPath = interfacePath + pathJoin + soapMappingRepo.getInterfaceAlias() + Constants.XML_FILE;
            SoapApi soapApi = getSoapApi(soapMappingRepo, xmlPath, header);
            soapApiList.add(soapApi);
        }
        return soapApiList;
    }

    /**
     * 只扫描有变化的xml文件
     *
     * @param mappingAndRepoIdList
     * @param oldSoapApiList
     * @param interfacePath
     * @param headerPath
     * @return
     */
    private List<SoapApi> scanChange(List<SoapApi> mappingAndRepoIdList, List<SoapApi> oldSoapApiList, String interfacePath, String headerPath) {
        List<SoapApi> soapApiList = new ArrayList<>();
        List<String> notNewIdList = new ArrayList<>();
        SoapApi header = CommonUtil.map2Object(SopSoapUtil.analysisHeaderFile(headerPath, Constants.SOAP_PROVIDER_SCAN_ERROR), SoapApi.class);
        for (SoapApi soapMappingRepo : mappingAndRepoIdList) {
            String interfaceAlias = soapMappingRepo.getInterfaceAlias();
            String xmlPath = interfacePath + pathJoin + interfaceAlias + Constants.XML_FILE;
            String repositoryId = soapMappingRepo.getRepositoryId();
            // 标记是否存在对应的老数据
            boolean flag = false;
            for (SoapApi oldSoap : oldSoapApiList) {
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
                                soapApiList.add(oldSoap);
                            }
                        } else {
                            notNewIdList.add(oldSoap.getId());
                            oldSoap.setVer(oldSoap.getVer() + 1);
                            oldSoap.setId(null);
                            // 更新接口消息体
                            updateOldSoap(oldSoap, xmlPath, repositoryId);
                            oldSoap.setCreateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
                            oldSoap.setUpdateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
                            soapApiList.add(oldSoap);
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
                        soapApiList.add(oldSoap);
                    }
                    // 删除后，剩下此次扫描相对上次扫描删除的接口
                    oldSoapApiList.remove(oldSoap);
                    break;
                }
            }
            if (!flag) {
                // 获取接口请求体
                SoapApi soapApi = getSoapApi(soapMappingRepo, xmlPath, header);
                soapApiList.add(soapApi);
            }
        }
        if (CollectionUtils.isNotEmpty(oldSoapApiList)) {
            List<String> idList = oldSoapApiList.stream().map(SoapApi::getId).collect(Collectors.toList());
            notNewIdList.addAll(idList);
        }
        interfaceService.updateSoapApiIsNewByIds(notNewIdList);
        return soapApiList;
    }


    /**
     * 获取接口请求体
     *
     * @param soapMappingRepo
     * @param xmlPath
     * @return
     */
    private SoapApi getSoapApi(SoapApi soapMappingRepo, String xmlPath, SoapApi header) {
        // 获取接口请求体
        SoapApi soapApi = CommonUtil.map2Object(SopSoapUtil.analysisInterfaceXml(xmlPath, ErrorConstants.SOAP_PROVIDER_SCAN_ERROR), SoapApi.class);
        soapApi.setRepositoryId(soapMappingRepo.getRepositoryId());
        soapApi.setHeaderRepositoryId(soapMappingRepo.getHeaderRepositoryId());
        soapApi.setTransId(soapMappingRepo.getTransId());
        soapApi.setInterfaceAlias(soapMappingRepo.getInterfaceAlias());
        soapApi.setUri(soapMappingRepo.getUri());
        soapApi.setServiceId(super.getAppServiceId());
        soapApi.setBranch(super.getBranchName());
        soapApi.setReqHeader(header.getReqHeader());
        soapApi.setRspHeader(header.getRspHeader());
        // 设置请求类型
        soapApi.setRequestType(Dict.HTTP_XML);
        // 设置请求协议
        soapApi.setRequestProtocol(Dict.SOAP);
        // 设置接口类型
        soapApi.setInterfaceType(Dict.SOAP);
        soapApi.setCreateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
        soapApi.setUpdateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
        return soapApi;
    }

    /**
     * 更新接口消息体
     *
     * @param oldSoap
     * @param xmlPath
     * @param repositoryId
     */
    private void updateOldSoap(SoapApi oldSoap, String xmlPath, String repositoryId) {
        SoapApi newSoap = CommonUtil.map2Object(SopSoapUtil.analysisInterfaceXml(xmlPath, ErrorConstants.SOAP_PROVIDER_SCAN_ERROR), SoapApi.class);
        oldSoap.setRequest(newSoap.getRequest());
        oldSoap.setResponse(newSoap.getResponse());
        // 设置Repository ID
        oldSoap.setRepositoryId(repositoryId);
    }

    /**
     * 关联ESB数据
     *
     * @param soapApiList
     */
    private void relatedEsbData(List<SoapApi> soapApiList) {
        List<String> transIds = soapApiList.stream().map(SoapApi::getTransId).collect(Collectors.toList());
        List<EsbRelation> esbRelationList = esbRelationDao.getEsbRelationByProviderMsgType(transIds);
        for (SoapApi soapApi : soapApiList) {
            for (EsbRelation esbRelation : esbRelationList) {
                if (soapApi.getTransId().equals(esbRelation.getServiceAndOperationId())) {
                    soapApi.setInterfaceName(esbRelation.getTranName());
                    break;
                }
            }
        }
    }

}
