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
import com.spdb.fdev.fdevinterface.spdb.entity.SopRelation;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Sop接口调用方
 */
@Component(Dict.SOP_RELATION_CALLABLE)
public class SopRelationCallable extends BaseScanCallable {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value(value = "${path.sop.client.alias}")
    private String sopAlias;
    @Value(value = "${path.sop.client.interface}")
    private String sopInterface;

    @Override
    public Map call() {
        logger.info(Constants.SOP_CALLING_SCAN_START);
        Map returnMap = new HashMap();
        try {
            // 获取别名文件sop_alias.properties路径
            String sopAliasPath = FileUtil.getFilePath(super.getSrcPathList(), mainResources, sopAlias);
            // 获取Sop接口调用方interface文件夹路径
            String interfacePath = FileUtil.getFilePath(super.getSrcPathList(), mainResources, sopInterface);
            if (StringUtils.isEmpty(sopAliasPath) || StringUtils.isEmpty(interfacePath)) {
                return returnMap;
            }
            // 获取Sop别名信息及其对应的Repository ID
            List<SopRelation> sopAliasAndRepoIdList = getSopAliasAndRepoId(sopAliasPath, interfacePath);
            List sopRelationList;
            // 获取数据库数据
            List<SopRelation> oldSopRelationList = interfaceRelationService.getSopRelationList(super.getAppServiceId(), super.getBranchName());
            if (CollectionUtils.isEmpty(oldSopRelationList)) {
                // 根据解析出的sop接口别名，去扫描对应xml文件
                sopRelationList = scanAll(sopAliasAndRepoIdList, interfacePath);
            } else {
                // 只扫描有变化的xml文件
                sopRelationList = scanChange(sopAliasAndRepoIdList, oldSopRelationList, interfacePath);
            }
            relatedEsbData(sopRelationList);
            interfaceRelationService.saveSopRelationList(sopRelationList);
            logger.info(Constants.SOP_CALLING_SCAN_END);
            returnMap.put(Dict.SUCCESS, Constants.SOP_CALLING_SCAN_SUCCESS);
            return returnMap;
        } catch (FdevException e) {
            returnMap.put(Dict.ERROR, errorMessageUtil.get(e));
            logger.error("{}", errorMessageUtil.get(e));
            return returnMap;
        }
    }

    /**
     * 获取Sop别名信息及其对应的Repository ID
     *
     * @param sopAliasPath
     * @param interfacePath
     * @return
     */
    public List<SopRelation> getSopAliasAndRepoId(String sopAliasPath, String interfacePath) {
        // GitLab上interface文件夹路径
        String sopInterfaceRepoPath = gitSrcMainResources + sopInterface;
        // 如果interfacePathArray的长度为3，说明src文件夹外面还有一层以appServiceId(应用英文名)命名的文件夹
        if (interfacePath.contains(super.getAppServiceId())) {
            sopInterfaceRepoPath = super.getAppServiceId() + pathJoin + sopInterfaceRepoPath;
        }
        // 获取GitLab上interface文件夹下所有子文件的Repository ID
        List<Map> interfaceRepositoryList = gitLabService.getProjectsRepository(super.getProjectId(), super.getAppServiceId(), super.getBranchName(), sopInterfaceRepoPath);
        // 解析别名文件(每次都要解析，因为可能存在sop_alias.properties没变，但新增了对应xml文件的情况)
        List<SopRelation> sopAliasRelationList = analysisAliasFile(sopAliasPath);
        // 组装sop_alias.properties中配置过的xml文件的RepoSitory ID，减少后期处理逻辑的循环次数
        Iterator<SopRelation> sopAliasIterator = sopAliasRelationList.iterator();
        while (sopAliasIterator.hasNext()) {
            SopRelation sopAliasRelation = sopAliasIterator.next();
            // 标记是否存在对应xml文件
            boolean flag = false;
            for (Map interfaceRepositoryMap : interfaceRepositoryList) {
                if (interfaceRepositoryMap.get(Dict.NAME).equals(sopAliasRelation.getInterfaceAlias() + Constants.XML_FILE)) {
                    flag = true;
                    // 设置Repository ID
                    sopAliasRelation.setRepositoryId((String) interfaceRepositoryMap.get(Dict.ID));
                    break;
                }
            }
            if (!flag) {
                sopAliasIterator.remove();
            }
        }
        return sopAliasRelationList;
    }

    /**
     * 解析Sop接口调用方别名的文件sop_alias.properties
     *
     * @param sopRelationAliasPath
     * @return
     */
    public List<SopRelation> analysisAliasFile(String sopRelationAliasPath) {
        List<SopRelation> sopRelationList = new ArrayList<>();
        // 去重
        Set<String> hashSet = new HashSet<>();
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(sopRelationAliasPath), "UTF-8"))) {
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                if (!str.contains("#") && str.indexOf('=') > -1) {
                    hashSet.add(str);
                }
            }
        } catch (Exception e) {
            logger.error("解析配置Sop接口别名的文件出错，{}！", e.getMessage());
            throw new FdevException(ErrorConstants.SOP_CALLING_SCAN_ERROR, new String[]{"解析配置Sop接口别名的文件出错！"});
        }
        for (String str : hashSet) {
            String alias = str.split("=")[0].trim();
            String transId = str.split("=")[1].trim().substring(1, 5);
            SopRelation sopRelation = new SopRelation();
            sopRelation.setInterfaceAlias(alias);
            sopRelation.setTransId(transId);
            sopRelationList.add(sopRelation);
        }
        return sopRelationList;
    }

    /**
     * 根据解析出的sop接口别名，去扫描对应xml文件
     *
     * @param sopAliasAndRepoIdList
     * @return
     */
    public List<SopRelation> scanAll(List<SopRelation> sopAliasAndRepoIdList, String interfacePath) {
        List<SopRelation> sopRelationList = new ArrayList<>();
        for (SopRelation sopAliasRepo : sopAliasAndRepoIdList) {
            String xmlPath = interfacePath + pathJoin + sopAliasRepo.getInterfaceAlias() + Constants.XML_FILE;
            // 获取接口请求体
            SopRelation sopRelation = getSopRelation(sopAliasRepo, xmlPath);
            sopRelationList.add(sopRelation);
        }
        return sopRelationList;
    }

    /**
     * 只扫描有变化的xml文件
     *
     * @param sopAliasAndRepoIdList
     * @param oldSopRelationList
     * @return
     */
    public List<SopRelation> scanChange(List<SopRelation> sopAliasAndRepoIdList, List<SopRelation> oldSopRelationList, String interfacePath) {
        List<SopRelation> sopRelationList = new ArrayList<>();
        List<String> notNewIdList = new ArrayList<>();
        for (SopRelation sopAliasRepo : sopAliasAndRepoIdList) {
            String interfaceAlias = sopAliasRepo.getInterfaceAlias();
            String xmlPath = interfacePath + pathJoin + interfaceAlias + Constants.XML_FILE;
            // 标记是否存在对应的老数据
            boolean flag = false;
            for (SopRelation oldSop : oldSopRelationList) {
                // 如果本次扫描的Repository ID与数据库里面的不一致，则需要扫描xml文件
                if (interfaceAlias.equals(oldSop.getInterfaceAlias())) {
                    flag = true;
                    if (!sopAliasRepo.getRepositoryId().equals(oldSop.getRepositoryId())) {
                        notNewIdList.add(oldSop.getId());
                        SopRelation newSop = CommonUtil.map2Object(SopSoapUtil.analysisInterfaceXml(xmlPath, ErrorConstants.SOP_CALLING_SCAN_ERROR), SopRelation.class);
                        oldSop.setRequest(newSop.getRequest());
                        oldSop.setResponse(newSop.getResponse());
                        // 设置Repository ID
                        oldSop.setRepositoryId(sopAliasRepo.getRepositoryId());
                        oldSop.setVer(oldSop.getVer() + 1);
                        oldSop.setIsNew(1);
                        oldSop.setId(null);
                        oldSop.setUpdateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
                        sopRelationList.add(oldSop);
                    }
                    // 删除后，剩下此次扫描相对上次扫描删除的接口
                    oldSopRelationList.remove(oldSop);
                    break;
                }
            }
            if (!flag) {
                // 获取接口请求体
                SopRelation sopRelation = getSopRelation(sopAliasRepo, xmlPath);
                sopRelationList.add(sopRelation);
            }
        }
        if (CollectionUtils.isNotEmpty(oldSopRelationList)) {
            List<String> idList = oldSopRelationList.stream().map(SopRelation::getId).collect(Collectors.toList());
            notNewIdList.addAll(idList);
        }
        interfaceRelationService.updateSopRelationIsNewByIds(notNewIdList);
        return sopRelationList;
    }

    /**
     * 获取接口请求体
     *
     * @param sopAliasRepo
     * @param xmlPath
     * @return
     */
    public SopRelation getSopRelation(SopRelation sopAliasRepo, String xmlPath) {
        SopRelation sopRelation = CommonUtil.map2Object(SopSoapUtil.analysisInterfaceXml(xmlPath, ErrorConstants.SOP_CALLING_SCAN_ERROR), SopRelation.class);
        sopRelation.setRepositoryId(sopAliasRepo.getRepositoryId());
        sopRelation.setTransId(sopAliasRepo.getTransId());
        sopRelation.setInterfaceAlias(sopAliasRepo.getInterfaceAlias());
        sopRelation.setServiceCalling(super.getAppServiceId());
        sopRelation.setBranch(super.getBranchName());
        sopRelation.setServiceId(Dict.ESB);
        // 设置请求类型
        sopRelation.setRequestType("-");
        // 设置请求协议
        sopRelation.setRequestProtocol(Dict.TCP);
        // 设置接口类型
        sopRelation.setInterfaceType(Dict.SOP);
        sopRelation.setCreateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
        sopRelation.setUpdateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
        return sopRelation;
    }

    /**
     * 关联ESB数据
     *
     * @param sopRelationList
     */
    private void relatedEsbData(List<SopRelation> sopRelationList) {
        List<String> transIds = sopRelationList.stream().map(SopRelation::getTransId).collect(Collectors.toList());
        List<EsbRelation> esbRelationList = esbRelationDao.getEsbRelationByTranId(transIds);
        for (SopRelation sopRelation : sopRelationList) {
            for (EsbRelation esbRelation : esbRelationList) {
                if (sopRelation.getTransId().equals(esbRelation.getTranId())) {
                    sopRelation.setInterfaceName(esbRelation.getTranName());
                    sopRelation.setServiceId(esbRelation.getProviderSysIdAndName());
                    sopRelation.setEsbTransId(esbRelation.getTranId());
                    sopRelation.setEsbServiceId(esbRelation.getServiceId());
                    sopRelation.setEsbOperationId(esbRelation.getOperationId());
                    break;
                }
            }
        }
    }

}
