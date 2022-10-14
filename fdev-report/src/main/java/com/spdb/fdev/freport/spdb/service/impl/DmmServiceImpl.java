package com.spdb.fdev.freport.spdb.service.impl;

import com.spdb.fdev.freport.base.utils.CommonUtils;
import com.spdb.fdev.freport.base.utils.TimeUtils;
import com.spdb.fdev.freport.spdb.dao.AppDao;
import com.spdb.fdev.freport.spdb.dao.ReportDao;
import com.spdb.fdev.freport.spdb.dao.UserDao;
import com.spdb.fdev.freport.spdb.dto.DmmDto;
import com.spdb.fdev.freport.spdb.entity.report.GitlabMergeRecord;
import com.spdb.fdev.freport.spdb.entity.user.Group;
import com.spdb.fdev.freport.spdb.service.DmmService;
import com.spdb.fdev.freport.spdb.service.GitlabService;
import com.spdb.fdev.freport.spdb.vo.IntegrationRateVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DmmServiceImpl implements DmmService {

    @Value("${fdev.user.group.internet.id}")
    private String internetId;

    @Value("${fdev.user.group.internet.sortNum}")
    private String internetSortNum;

    @Autowired
    private UserDao userDao;

    @Autowired
    private AppDao appDao;

    @Autowired
    private ReportDao reportDao;

    @Autowired
    private GitlabService gitlabService;

    @Override
    public List<IntegrationRateVo> queryIntegrationRate(DmmDto dto) throws Exception {
        List<Group> groupList = userDao.findGroup(new Group() {{
            setStatus("1");//获取所有启用组
            setSortNum("^" + internetSortNum);//获取互联网条线下所有组
        }});
        //数据结构转换
        List<IntegrationRateVo> internetGroupList = groupList.stream().map(item -> {
            {
                IntegrationRateVo vo = new IntegrationRateVo();
                BeanUtils.copyProperties(item, vo);
                return vo;
            }
        }).collect(Collectors.toList());
        //完整父子级结构
        dealChildren(internetGroupList);
        //过滤仅获取“互联网条线”
        internetGroupList = internetGroupList.stream().filter(item -> internetId.equals(item.getParentId())).collect(Collectors.toList());
        Set<String> allGroupIdSet = getAllGroupIdSet(internetGroupList);
//       //I、全量查询 - 数据量过大太慢，时间条件也很难过滤筛选 暂时弃用方案
//      for (String projectId : projectIdSet) {
//          List<MergeRequestDto> mergeRequestDtoList = gitlabService.getProjectMergeRequest(projectId);
//      }
        //II、查询本地库 - 由kafka消息推送维护方案
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dto.getStartDate());
        TimeUtils.setMonthMin(calendar);
        dto.setStartDate(calendar.getTime());
        calendar.setTime(dto.getEndDate());
        TimeUtils.setMonthMax(calendar);
        dto.setEndDate(calendar.getTime());
        List<GitlabMergeRecord> gitlabMergeRecord = reportDao.findGitlabMergeRecord(allGroupIdSet, TimeUtils.FORMAT_TIMESTAMP.format(dto.getStartDate()), TimeUtils.FORMAT_TIMESTAMP.format(dto.getEndDate()));
        Map<String, List<GitlabMergeRecord>> groupMergeMap = new HashMap<>();////定义全局私有变量，便于私有方法递归 groupId-merge
        for (GitlabMergeRecord mergeRecord : gitlabMergeRecord) {//list转map便于数据组装
            List<GitlabMergeRecord> orDefault = groupMergeMap.getOrDefault(mergeRecord.getGroupId(), new ArrayList<>());
            orDefault.add(mergeRecord);
            groupMergeMap.put(mergeRecord.getGroupId(), orDefault);
        }
        //递归组装结果数据
        dealInternetGroupChildrenMerge(internetGroupList, groupMergeMap);
        //只保留一层结构的最终数据结构
        List<IntegrationRateVo> result = new ArrayList<>();
        for (IntegrationRateVo item : internetGroupList) {
            if (!CommonUtils.isNullOrEmpty(item.getChildren())) {
                item.getGitLabMerge().addAll(getChildrenGroupMerge(item.getChildren()));
            }
            result.add(item);
        }
        return result;
    }

    private void dealInternetGroupChildrenMerge(List<IntegrationRateVo> internetGroupList, Map<String, List<GitlabMergeRecord>> groupMergeMap) {
        for (IntegrationRateVo item : internetGroupList) {
            item.setGitLabMerge(new ArrayList<GitlabMergeRecord>() {{
                if (!CommonUtils.isNullOrEmpty(groupMergeMap.get(item.getId()))) {
                    addAll(groupMergeMap.get(item.getId()));
                }
            }});
            if (!CommonUtils.isNullOrEmpty(item.getChildren())) {
                dealInternetGroupChildrenMerge(item.getChildren(), groupMergeMap);
            }
        }
    }

    /**
     * n级结构转两级结构
     *
     * @param resource
     * @return
     */
    private List<GitlabMergeRecord> getChildrenGroupMerge(List<IntegrationRateVo> resource) {
        List<GitlabMergeRecord> result = new ArrayList<>();
        for (IntegrationRateVo item : resource) {
            result.addAll(item.getGitLabMerge());
            if (!CommonUtils.isNullOrEmpty(item.getChildren())) {
                result.addAll(getChildrenGroupMerge(item.getChildren()));
            }
        }
        return result;
    }

    /**
     * 完整父子级结构
     *
     * @param target
     */
    private void dealChildren(List<IntegrationRateVo> target) {
        target.forEach(item -> {
            item.setChildren(new ArrayList<>());//初始化
            target.forEach(resource -> {
                if (item.getId().equals(resource.getParentId())) {
                    item.getChildren().add(resource);
                }
            });
        });
    }

    /**
     * 递归获取全部groupId
     */
    private Set<String> getAllGroupIdSet(List<IntegrationRateVo> resource) {
        return new HashSet<String>() {{
            for (IntegrationRateVo item : resource) {
                add(item.getId());
                if (!CommonUtils.isNullOrEmpty(item.getChildren())) {
                    addAll(getAllGroupIdSet(item.getChildren()));
                }
            }
        }};
    }

}