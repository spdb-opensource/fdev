package com.spdb.fdev.fdemand.spdb.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.fdemand.base.dict.Constants;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.dict.IpmpUnitEnum;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.spdb.dao.IIpmpUnitDao;
import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import com.spdb.fdev.fdemand.spdb.entity.DictEntity;
import com.spdb.fdev.fdemand.spdb.entity.IpmpUnit;
import com.spdb.fdev.fdemand.spdb.entity.XTestIpmpUnit;
import com.spdb.fdev.fdemand.spdb.service.IIpmpUnitService;
import com.spdb.fdev.fdemand.spdb.service.ITestIpmpUnitService;
import com.spdb.fdev.fdemand.spdb.service.impl.IpmpUnitServiceImpl;
import com.sun.org.apache.xpath.internal.operations.Bool;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import sun.nio.cs.FastCharsetProvider;

import java.util.*;
import java.util.regex.Pattern;

@RefreshScope
@Repository
public class IpmpUnitDaoImpl implements IIpmpUnitDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${fdev.ipmp.state:评估中,待实施,开发中,业务测试中,业务测试完成,已投产}")
    private List<String> ipmpSortRule;

    @Autowired
    private IpmpUnitServiceImpl ipmpUnitService;

    @Override
    public void saveIpmpUnit(List<IpmpUnit> ipmpUnit) throws Exception {
        mongoTemplate.insert(ipmpUnit,IpmpUnit.class);
    }

    @Override
    public Map<String, Object> queryIpmpUnitByDemandId(Map<String, Object> params) throws Exception {
        String informationNum = (String)params.get(Dict.INFORMATIONNUM);//需求编号
        Integer size = (Integer)params.get(Dict.SIZE);//每页条数，不传默认查全部
        Integer index = (Integer)params.get(Dict.INDEX);//页码
        String implContent = (String)params.get(Dict.IMPLCONTENT);//实施单元内容
        String implUnitNum = (String)params.get(Dict.IMPLUNITNUM);//实施单元编号
        String prjNum = (String)params.get(Dict.PRJNUM);//项目编号
        String implLeader = (String)params.get(Dict.IMPLLEADER);//牵头人域账号
        Query query = new Query();
        Criteria criteria = Criteria.where(Dict.INFORMATIONNUM).is(informationNum);
        if(!CommonUtils.isNullOrEmpty(implContent)){
            Pattern pattern = Pattern.compile("^.*" + implContent + ".*$");
            criteria.and(Dict.IMPLCONTENT).regex(pattern);
        }
        if(!CommonUtils.isNullOrEmpty(implUnitNum)){
            Pattern pattern = Pattern.compile("^.*" + implUnitNum + ".*$");
            criteria.and(Dict.IMPLUNITNUM).regex(pattern);
        }
        if(!CommonUtils.isNullOrEmpty(prjNum)){
            Pattern pattern = Pattern.compile("^.*" + prjNum + ".*$");
            criteria.and(Dict.PRJNUM).regex(pattern);
        }
        if(!CommonUtils.isNullOrEmpty(implLeader)){
            Pattern pattern = Pattern.compile("^.*" + implLeader + ".*$");
            criteria.and(Dict.IMPLLEADER).regex(pattern);
        }

        criteria.and(Dict.LEADERFLAG).in(Constants.ONE,Constants.TWO);//实施牵头人均是fdev：1；实施牵头人部分是fdev：2；实施牵头人均不是fdev：3
        query.addCriteria(criteria);
        query.fields().exclude(Dict.OBJECTID);
        Long count = mongoTemplate.count(query, IpmpUnit.class);
        //按牵头人标识排序 1 2 3
        query.with(new Sort(Sort.Direction.ASC, Dict.LEADERFLAG));
        List<IpmpUnit> data = mongoTemplate.find(query, IpmpUnit.class);
        List<IpmpUnit> sortData = new ArrayList<>();
        List<IpmpUnit> sortDataEnd = new ArrayList<>();

        if(!CommonUtils.isNullOrEmpty(data)){
            //排序 将不能编辑的往下
            for( IpmpUnit ipmpUnit : data ){
                if(ipmpSortRule.contains(ipmpUnit.getImplStatusName()))
                    sortData.add(ipmpUnit);
                else sortDataEnd.add(ipmpUnit);
            }
            sortData.addAll(sortDataEnd);
            //分页
            if (!CommonUtils.isNullOrEmpty(size)&&!CommonUtils.isNullOrEmpty(index)
                    && size != 0 && index != 0) {
                Integer start = Math.min((index - 1) * size, sortData.size());
                Integer end = Math.min(start + size, sortData.size());
                sortData = sortData.subList(start,end);
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.COUNT,count);
        map.put(Dict.DATA,sortData);
        return map;
    }

    @Override
    public Map<String, Object> queryIpmpUnitAllByDemandNum(Map<String, Object> params) throws Exception {
        String informationNum = (String)params.get(Dict.INFORMATIONNUM);//需求编号
        Integer size = (Integer)params.get(Dict.SIZE);//每页条数，不传默认查全部
        Integer index = (Integer)params.get(Dict.INDEX);//页码
        String implContent = (String)params.get(Dict.IMPLCONTENT);//实施单元内容
        String implUnitNum = (String)params.get(Dict.IMPLUNITNUM);//实施单元编号
        String prjNum = (String)params.get(Dict.PRJNUM);//项目编号
        String implLeader = (String)params.get(Dict.IMPLLEADER);//牵头人域账号
        Query query = new Query();
        Criteria criteria = Criteria.where(Dict.INFORMATIONNUM).is(informationNum);
        if(!CommonUtils.isNullOrEmpty(implContent)){
            Pattern pattern = Pattern.compile("^.*" + implContent + ".*$");
            criteria.and(Dict.IMPLCONTENT).regex(pattern);
        }
        if(!CommonUtils.isNullOrEmpty(implUnitNum)){
            Pattern pattern = Pattern.compile("^.*" + implUnitNum + ".*$");
            criteria.and(Dict.IMPLUNITNUM).regex(pattern);
        }
        if(!CommonUtils.isNullOrEmpty(prjNum)){
            Pattern pattern = Pattern.compile("^.*" + prjNum + ".*$");
            criteria.and(Dict.PRJNUM).regex(pattern);
        }
        if(!CommonUtils.isNullOrEmpty(implLeader)){
            Pattern pattern = Pattern.compile("^.*" + implLeader + ".*$");
            criteria.and(Dict.IMPLLEADER).regex(pattern);
        }

        query.addCriteria(criteria);
        query.fields().exclude(Dict.OBJECTID);
        Long count = mongoTemplate.count(query, IpmpUnit.class);
        //按牵头人标识排序 1 2 3
        query.with(new Sort(Sort.Direction.ASC, Dict.LEADERFLAG));
        List<IpmpUnit> data = mongoTemplate.find(query, IpmpUnit.class);
        List<IpmpUnit> sortData = new ArrayList<>();
        List<IpmpUnit> sortDataEnd = new ArrayList<>();

        if(!CommonUtils.isNullOrEmpty(data)){
            //排序 将不能编辑的往下
            for( IpmpUnit ipmpUnit : data ){
                if(ipmpSortRule.contains(ipmpUnit.getImplStatusName()))
                    sortData.add(ipmpUnit);
                else sortDataEnd.add(ipmpUnit);
            }
            sortData.addAll(sortDataEnd);
            //分页
            if (!CommonUtils.isNullOrEmpty(size)&&!CommonUtils.isNullOrEmpty(index)
                    && size != 0 && index != 0) {
                Integer start = Math.min((index - 1) * size, sortData.size());
                Integer end = Math.min(start + size, sortData.size());
                sortData = sortData.subList(start,end);
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.COUNT,count);
        map.put(Dict.DATA,sortData);
        return map;
    }

    @Override
    public Map<String, Object> queryAllTechIpmpUnit(Map<String, Object> params) throws Exception {
        Integer size = (Integer)params.get(Dict.SIZE);//每页条数，不传默认查全部
        Integer index = (Integer)params.get(Dict.INDEX);//页码
        String implContent = (String)params.get(Dict.IMPLCONTENT);//实施单元内容
        String implUnitNum = (String)params.get(Dict.IMPLUNITNUM);//实施单元编号
        String prjNum = (String)params.get(Dict.PRJNUM);//项目编号
        String implLeader = (String)params.get(Dict.IMPLLEADER);//牵头人域账号
        Query query = new Query();
        Criteria criteria =  Criteria.where(Dict.IMPLUNITNUM).regex(Pattern.compile("^.*科技.*$"));
        if(!CommonUtils.isNullOrEmpty(implContent)){
            Pattern pattern = Pattern.compile("^.*" + implContent + ".*$");
            criteria.and(Dict.IMPLCONTENT).regex(pattern);
        }
        if(!CommonUtils.isNullOrEmpty(implUnitNum)){
            Pattern pattern = Pattern.compile("^.*" + implUnitNum + ".*$");
            criteria.and(Dict.IMPLUNITNUM).regex(pattern);
        }
        if(!CommonUtils.isNullOrEmpty(prjNum)){
            Pattern pattern = Pattern.compile("^.*" + prjNum + ".*$");
            criteria.and(Dict.PRJNUM).regex(pattern);
        }
        if(!CommonUtils.isNullOrEmpty(implLeader)){
            Pattern pattern = Pattern.compile("^.*" + implLeader + ".*$");
            criteria.and(Dict.IMPLLEADER).regex(pattern);
        }
        criteria.and(Dict.IMPLLEADER).ne(null);
        query.addCriteria(criteria);
        query.fields().exclude(Dict.OBJECTID);
        Long count = mongoTemplate.count(query, IpmpUnit.class);
        //按牵头人标识排序 1 2 3
        query.with(new Sort(Sort.Direction.ASC, Dict.LEADERFLAG));

        List<IpmpUnit> data = mongoTemplate.find(query, IpmpUnit.class);
        List<IpmpUnit> sortData = new ArrayList<>();
        List<IpmpUnit> sortDataEnd = new ArrayList<>();

        if(!CommonUtils.isNullOrEmpty(data)){
            //排序 将不能编辑的往下
            for( IpmpUnit ipmpUnit : data ){
                if(ipmpSortRule.contains(ipmpUnit.getImplStatusName()))
                    sortData.add(ipmpUnit);
                else sortDataEnd.add(ipmpUnit);
            }
            sortData.addAll(sortDataEnd);
            //分页
            if (!CommonUtils.isNullOrEmpty(size)&&!CommonUtils.isNullOrEmpty(index)
                    && size != 0 && index != 0) {
                Integer start = Math.min((index - 1) * size, sortData.size());
                Integer end = Math.min(start + size, sortData.size());
                sortData = sortData.subList(start,end);
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put(Dict.COUNT,count);
        map.put(Dict.DATA,sortData);
        return map;
    }

    @Override
    public Map<String, Object> queryIpmpUnitByNums(Map<String, Object> params) throws Exception {
        Set<String> implUnitNumList = (Set<String>)params.get(Dict.IMPLUNITNUMLIST);//实施单元编号列表
        Integer size = (Integer)params.get(Dict.SIZE);//每页条数，不传默认查全部
        Integer index = (Integer)params.get(Dict.INDEX);//页码
        String implContent = (String)params.get(Dict.IMPLCONTENT);//实施单元内容
        String implUnitNum = (String)params.get(Dict.IMPLUNITNUM);//实施单元编号
        String prjNum = (String)params.get(Dict.PRJNUM);//项目编号
        String implLeader = (String)params.get(Dict.IMPLLEADER);//牵头人域账号
        Query query = new Query();
        Criteria criteria = Criteria.where(Dict.IMPLUNITNUM).in(implUnitNumList);
        if(!CommonUtils.isNullOrEmpty(implContent)){
            Pattern pattern = Pattern.compile("^.*" + implContent + ".*$");
            criteria.and(Dict.IMPLCONTENT).regex(pattern);
        }
        if(!CommonUtils.isNullOrEmpty(implUnitNum)){
            Pattern pattern = Pattern.compile("^.*" + implUnitNum + ".*$");
            criteria.and(Dict.IMPLUNITNUM).regex(pattern);
        }
        if(!CommonUtils.isNullOrEmpty(prjNum)){
            Pattern pattern = Pattern.compile("^.*" + prjNum + ".*$");
            criteria.and(Dict.PRJNUM).regex(pattern);
        }
        if(!CommonUtils.isNullOrEmpty(implLeader)){
            Pattern pattern = Pattern.compile("^.*" + implLeader + ".*$");
            criteria.and(Dict.IMPLLEADER).regex(pattern);
        }

        query.addCriteria(criteria);
        query.fields().exclude(Dict.OBJECTID);
        Long count = mongoTemplate.count(query, IpmpUnit.class);
        //按牵头人标识排序 1 2 3
        query.with(new Sort(Sort.Direction.ASC, Dict.LEADERFLAG));
        List<IpmpUnit> data = mongoTemplate.find(query, IpmpUnit.class);
        List<IpmpUnit> sortData = new ArrayList<>();
        List<IpmpUnit> sortDataEnd = new ArrayList<>();

        if(!CommonUtils.isNullOrEmpty(data)){
            for( IpmpUnit ipmpUnit : data ){
                if(ipmpSortRule.contains(ipmpUnit.getImplStatusName()))
                    sortData.add(ipmpUnit);
                else sortDataEnd.add(ipmpUnit);
            }
            sortData.addAll(sortDataEnd);
            //分页
            if (!CommonUtils.isNullOrEmpty(size)&&!CommonUtils.isNullOrEmpty(index)
                    && size != 0 && index != 0) {
                Integer start = Math.min((index - 1) * size, sortData.size());
                Integer end = Math.min(start + size, sortData.size());
                sortData = sortData.subList(start,end);
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.COUNT,count);
        map.put(Dict.DATA,sortData);
        return map;
    }

    @Override
    public IpmpUnit queryIpmpUnitById(String implUnitNum) throws Exception {
        Query query = new Query(Criteria.where(Dict.IMPLUNITNUM).is(implUnitNum));//实施单元编号
        IpmpUnit ipmpUnit = mongoTemplate.findOne(query, IpmpUnit.class);
        return ipmpUnit;
    }

    @Override
    public String updateIpmpUnit(IpmpUnit ipmpUnit) throws Exception {
        Query query = Query.query(Criteria.where(Dict.IMPLUNITNUM).is(ipmpUnit.getImplUnitNum()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(ipmpUnit);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Dict.IMPLUNITNUM, ipmpUnit.getImplUnitNum());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if (Dict.OBJECTID.equals(key)) {
                continue;
            }
            if(Dict.OA_CONTACT_NO.equals(key)){
                if(CommonUtils.isNullOrEmpty(value))
                    continue;
                else
                    update.set(key, value);
            }
            update.set(key, value);
        }
        this.mongoTemplate.findAndModify(query, update, IpmpUnit.class);
        return ipmpUnit.getImplUnitNum();
    }

    @Override
    public List<IpmpUnit> queryIpmpUnitByDemandId(String informationNum) throws Exception {
        Query query = new Query();
        Criteria criteria = Criteria.where(Dict.INFORMATIONNUM).is(informationNum);
        criteria.and(Dict.LEADERFLAG).in(Constants.ONE,Constants.TWO);//实施牵头人均是fdev：1；实施牵头人部分是fdev：2；实施牵头人均不是fdev：3
        //去掉暂存，暂缓，已撤销
        criteria.and(Dict.IMPLSTATUSNAME).nin(IpmpUnitEnum.IpmpUnitStatusEnum.DEFER.getName()
                ,IpmpUnitEnum.IpmpUnitStatusEnum.TEMPORARY_STORAGE.getName(),IpmpUnitEnum.IpmpUnitStatusEnum.IS_CANCELED.getName());
        query.addCriteria(criteria);
        return mongoTemplate.find(query, IpmpUnit.class);
    }

    @Override
    public IpmpUnit queryIpmpUnitByNo(String unitNo) {
        Query query = new Query(Criteria.where(Dict.ID).is(unitNo));//实施单元id
        IpmpUnit ipmpUnit = mongoTemplate.findOne(query, IpmpUnit.class);
        return ipmpUnit;
    }

    @Override
    public DictEntity queryTestImplDept(String testImplDept) {
        Query query = new Query();
        Criteria criteria = Criteria.where(Dict.TYPE).is("demandPlatform");
        criteria.and(Dict.CODE).is(testImplDept);
        DictEntity dictEntity = mongoTemplate.findOne(query, DictEntity.class);
        return dictEntity;
    }

    @Override
    public void removeIpmpUnitByDemandNum(String informationNum) {
        Query query = new Query(Criteria.where(Dict.INFORMATIONNUM).is(informationNum));
        mongoTemplate.remove(query, IpmpUnit.class);
    }

    @Override
    public Map<String, Object> queryIpmpUnitList(String demandKey, String implUnitType, String keyword, List<String>
            groupIds, String prjNum, String implLeader, Integer size, Integer index, String applyStage,List<String> implStatusNameList) {

        Query query = new Query();
        Criteria criteria = new Criteria();

        if(!CommonUtils.isNullOrEmpty(demandKey)){
            Pattern pattern = Pattern.compile("^.*" + demandKey + ".*$");
            criteria.orOperator(Criteria.where(Dict.INFORMATIONNUM).regex(pattern),
                    Criteria.where(Dict.INFORMATIONTITLE).regex(pattern));
        }
        if(Dict.BUSINESS.equals(implUnitType)){
            criteria.and(Dict.IMPLUNITNUM).not().regex(Pattern.compile("^.*科技.*$"));
        }else if(Dict.TECH.equals(implUnitType)){
            criteria.and(Dict.IMPLUNITNUM).regex(Pattern.compile("^.*科技.*$"));
        }
        if(!CommonUtils.isNullOrEmpty(keyword)){
            Pattern pattern = Pattern.compile("^.*" + keyword + ".*$");
            criteria.orOperator(Criteria.where(Dict.IMPLUNITNUM).regex(pattern),
                    Criteria.where(Dict.IMPLCONTENT).regex(pattern));
        }
        if(!CommonUtils.isNullOrEmpty(groupIds)){
            criteria.and(Dict.LEADERGROUP).in(groupIds);
        }
        if(!CommonUtils.isNullOrEmpty(prjNum)){
            Pattern pattern = Pattern.compile("^.*" + prjNum + ".*$");
            criteria.and(Dict.PRJNUM).regex(pattern);
        }
        if(!CommonUtils.isNullOrEmpty(implLeader)){
            Pattern pattern = Pattern.compile("^.*" + implLeader + ".*$");
            criteria.and(Dict.IMPLLEADER).regex(pattern);
        }
        if(!CommonUtils.isNullOrEmpty(implStatusNameList)){
            criteria.and(Dict.IMPLSTATUSNAME).in(implStatusNameList);
        }
        criteria.and(Dict.LEADERFLAG).in(Constants.ONE,Constants.TWO);//实施牵头人均是fdev：1；实施牵头人部分是fdev：2；实施牵头人均不是fdev：3
        query.addCriteria(criteria);
        query.fields().exclude(Dict.OBJECTID);
        Long count = mongoTemplate.count(query, IpmpUnit.class);
        //按牵头人标识排序 1 2 3
        query.with(new Sort(Sort.Direction.ASC, Dict.LEADERFLAG));
        List<IpmpUnit> data = mongoTemplate.find(query, IpmpUnit.class);
        List<IpmpUnit> sortData = new ArrayList<>();
        List<IpmpUnit> sortDataEnd = new ArrayList<>();

        if(!CommonUtils.isNullOrEmpty(data)){
            if(!CommonUtils.isNullOrEmpty(applyStage) && !"all".equals(applyStage)){
                List<IpmpUnit> temp = new ArrayList<>();
                for(IpmpUnit ipmpUnit : data){
                    temp.add(ipmpUnit);
                    //新增搜索条件调整排期阶段：”全部“、”未申请“、”申请中“、”已完成“
                    if("noApply".equals(applyStage)){
                        //未申请：所有计划调整日期为空
                        if(!ipmpUnitService.isNoApply(ipmpUnit)){
                            temp.remove(ipmpUnit);
                            count--;
                        }
                    }
                    if("applying".equals(applyStage)){
                        //申请中：存在不为空的计划调整日期 且 不等于对应的计划日期 且 对应的阶段没有确认延期
                        if(!ipmpUnitService.isApplying(ipmpUnit)){
                            temp.remove(ipmpUnit);
                            count--;
                        }
                    }
                    if("applied".equals(applyStage)){
                        //已完成：存在不为空的计划调整日期 且 （均等于对应的计划日期 或 对应的阶段确认延期）
                        if(!ipmpUnitService.isApplied(ipmpUnit)){
                            temp.remove(ipmpUnit);
                            count--;
                        }
                    }
                }
                data = temp;
            }

            //排序 将不能编辑的往下
            for( IpmpUnit ipmpUnit : data ){
                if(ipmpSortRule.contains(ipmpUnit.getImplStatusName())){
                    sortData.add(ipmpUnit);
                } else {
                    sortDataEnd.add(ipmpUnit);
                }
            }
            sortData.addAll(sortDataEnd);
            //分页
            if (!CommonUtils.isNullOrEmpty(size)&&!CommonUtils.isNullOrEmpty(index)
                    && size != 0 && index != 0) {
                Integer start = Math.min((index - 1) * size, sortData.size());
                Integer end = Math.min(start + size, sortData.size());
                sortData = sortData.subList(start,end);
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.COUNT,count);
        map.put(Dict.DATA,sortData);
        return map;
    }



    @Override
    public List<XTestIpmpUnit> queryXTestIpmpUnit() throws Exception {
        Criteria criteria = new Criteria();
        // 获取每条实施单元最新的记录
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group(Dict.IMPLUNITNUM,Dict.STATUS).max(Dict.SYNCTIME).as(Dict.SYNCTIME),
                Aggregation.sort(new Sort(Sort.Direction.DESC, Dict.SYNCTIME))
        );
        List<XTestIpmpUnit> xTestIpmpUnitList = mongoTemplate.aggregate(aggregation, Dict.XTEST_IPMP_UNIT,
                                                    XTestIpmpUnit.class).getMappedResults();

        return xTestIpmpUnitList;
    }


    @Override
    public void saveXTestIpmpUnit(List<XTestIpmpUnit> xTestIpmpUnitList) throws Exception {

        mongoTemplate.insert(xTestIpmpUnitList,XTestIpmpUnit.class);
    }

    @Override
    public List<IpmpUnit> queryAllIpmpUnit() {
        return mongoTemplate.find(new Query(Criteria.where(Dict.LEADERFLAG).in(Constants.ONE,Constants.TWO)),IpmpUnit.class);
    }

    @Override
    public List<IpmpUnit> queryByImplUnitNum(String implUnitNum) {
        Pattern pattern = Pattern.compile("^.*" + implUnitNum + ".*$");
        Query query = new Query().addCriteria(Criteria.where(Dict.IMPLUNITNUM).regex(pattern)).addCriteria(Criteria.where(Dict.LEADERFLAG).in(Constants.ONE, Constants.TWO));//实施牵头人均是fdev：1；实施牵头人部分是fdev：2；实施牵头人均不是fdev：3
        return mongoTemplate.find(query, IpmpUnit.class);
    }
}
