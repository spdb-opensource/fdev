package com.spdb.fdev.fdemand.spdb.dao;

import com.spdb.fdev.fdemand.spdb.entity.DictEntity;
import com.spdb.fdev.fdemand.spdb.entity.IpmpUnit;
import com.spdb.fdev.fdemand.spdb.entity.XTestIpmpUnit;

import java.util.List;
import java.util.Map;

public interface IIpmpUnitDao {
    /**
     * 查看实施单元列表 包含 牵头人标识 1 2
     *
     * @param params
     * @return
     * @throws Exception
     */
    Map<String, Object> queryIpmpUnitByDemandId(Map<String, Object> params) throws Exception ;
    /**
     * 查看实施单元列表 包含 牵头人标识 1 2 3
     *
     * @param params
     * @return
     * @throws Exception
     */
    Map<String, Object> queryIpmpUnitAllByDemandNum(Map<String, Object> params) throws Exception;
    /**
     * 查看科技关联实施单元列表 包含 牵头人标识 1 2 3
     *
     * @param params
     * @return
     * @throws Exception
     */
    Map<String, Object> queryIpmpUnitByNums(Map<String, Object> params) throws Exception;
    /**
     * 查看科技实施单元列表 包含 牵头人标识 1 2 3
     *
     * @param params
     * @return
     * @throws Exception
     */
    Map<String, Object> queryAllTechIpmpUnit(Map<String, Object> params) throws Exception;
    /**
     * 查看实施单元详情
     *
     * @param ipmpUnitNo
     * @return
     * @throws Exception
     */
    IpmpUnit queryIpmpUnitById(String ipmpUnitNo) throws Exception ;
    /**
     * 修改实施单元
     *
     * @param ipmpUnit
     * @return
     * @throws Exception
     */
    String updateIpmpUnit(IpmpUnit ipmpUnit) throws Exception ;
    /**
     * 查看实施单元
     *
     * @param informationNum
     * @return
     * @throws Exception
     */
    List<IpmpUnit> queryIpmpUnitByDemandId(String informationNum) throws Exception;
    /**
     * 新增实施单元
     *
     * @param ipmpUnit
     * @return
     * @throws Exception
     */
    void saveIpmpUnit(List<IpmpUnit> ipmpUnit) throws Exception;

    /**
     * 根据实施单元id查询信息
     * @param unitNo
     * @return
     */
    IpmpUnit queryIpmpUnitByNo(String unitNo);
    /**
     * 根据需求编号删除实施单元
     * @return
     */
    void removeIpmpUnitByDemandNum(String informationNum);

    Map<String, Object> queryIpmpUnitList(String demandKey, String implUnitType, String keyword, List<String> groupIds, String prjNum, String implLeader, Integer size, Integer index, String applyStage,List<String> implStatusNameList);

    DictEntity queryTestImplDept(String testImplDept) ;


    List<XTestIpmpUnit> queryXTestIpmpUnit() throws Exception;

    void saveXTestIpmpUnit(List<XTestIpmpUnit> xTestIpmpUnitList) throws Exception;

    List<IpmpUnit> queryAllIpmpUnit();

    List<IpmpUnit> queryByImplUnitNum(String implUnitNum);
}
