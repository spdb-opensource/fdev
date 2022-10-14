package com.spdb.fdev.fdemand.spdb.service;

import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import org.springframework.web.multipart.MultipartFile;

import com.spdb.fdev.common.User;
import com.spdb.fdev.fdemand.spdb.entity.DemandDoc;

import java.util.List;
import java.util.Map;

public interface IDocService {

    Map queryDemandDocPagination(Map<String, Object> param);

    /**
     * 上传需求文档doc
     * 批量上传
     *
     */
    void uploadFile(String demand_id, String doc_type, String doc_link, String user_group_id, String user_group_cn, MultipartFile[] file, String demand_kind) throws Exception;

    /**
     * @param user
     * @param demand_id
     * @param doc_type
     * @param doc_link
     * @param user_group_id
     * @param user_group_cn
     * @throws Exception
     */
    void updateDemandDocLink(User user, String demand_id, String doc_type, String doc_link, String user_group_id, String user_group_cn, String demand_kind) throws Exception;

    /**
     * 更新需求文档doc
     * 批量更新
     *
     */
    void updateDemandDoc(User user, String demand_id, String doc_type, List<String> listPathAll, String user_group_id, String user_group_cn, MultipartFile[] file, String demand_kind) throws Exception;

    /**
     * 删除需求doc
     * 批量删除
     *
     * @param params
     */
    void deleteDemandDoc(Map params) throws Exception;

    /**
     * 存储需求doc
     */
    DemandDoc save(DemandDoc demandDoc) throws Exception;

    boolean uploadFiletoMinio(String moduleName, String path, MultipartFile multipartFile, User user);

    void fileToGitlab(DemandBaseInfo demand);
}