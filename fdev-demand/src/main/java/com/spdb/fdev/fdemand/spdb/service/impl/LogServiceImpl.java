package com.spdb.fdev.fdemand.spdb.service.impl;

import com.spdb.fdev.common.User;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.base.utils.TimeUtil;
import com.spdb.fdev.fdemand.spdb.dao.ILogDao;
import com.spdb.fdev.fdemand.spdb.entity.IpmpUnit;
import com.spdb.fdev.fdemand.spdb.entity.IpmpUnitEntityLog;
import com.spdb.fdev.fdemand.spdb.entity.IpmpUnitOperateLog;
import com.spdb.fdev.fdemand.spdb.service.ILogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RefreshScope
@Service
public class LogServiceImpl implements ILogService {

    @Autowired
    private ILogDao iLogDao;

    @Override
    public void saveIpmpUnitEntityLog(IpmpUnit newIpmpUnit,IpmpUnit oldIpmpUnit,String updateType) throws Exception {
        IpmpUnitEntityLog ipmpUnitEntityLog = new IpmpUnitEntityLog();
        ipmpUnitEntityLog.setImplUnitNum(newIpmpUnit.getImplUnitNum());//实施单元编号
        ipmpUnitEntityLog.setUpdateType(updateType);// syncNew = 同步新增 syncUpdate = 同步修改 ,fdevUpdate = 页面修改
        ipmpUnitEntityLog.setIpmpUnitInfo(CommonUtils.object2Map(newIpmpUnit));//实施单元内容
        ipmpUnitEntityLog.setUpdateTime(TimeUtil.formatTodayHs());//更新时间
        ipmpUnitEntityLog.setUpdateFieldInfo(getUpdateFieldList(newIpmpUnit,oldIpmpUnit));
        try {
            User user = CommonUtils.getSessionUser();
            ipmpUnitEntityLog.setUpdateUserId(user.getId());//更新人ID
            ipmpUnitEntityLog.setUpdateUserName(user.getUser_name_cn());//更新人姓名
        }catch (Exception e){
            //获取登录信息失败 更新人置空
            ipmpUnitEntityLog.setUpdateUserId("");//更新人ID
            ipmpUnitEntityLog.setUpdateUserName("system");//更新人姓名

        }
        iLogDao.saveIpmpUnitEntityLog(ipmpUnitEntityLog);
    }

    @Override
    public void saveIpmpUnitOperateLog(String implUnitNum,String interfaceName,Map updateMap,Map response) throws Exception {
        IpmpUnitOperateLog ipmpUnitOperateLog = new IpmpUnitOperateLog();
        ipmpUnitOperateLog.setImplUnitNum(implUnitNum);//实施单元编号
        ipmpUnitOperateLog.setUpdateTime(TimeUtil.formatTodayHs());//更新时间
        ipmpUnitOperateLog.setInterfaceName(interfaceName);//IPMP接口名
        ipmpUnitOperateLog.setUpdateMap(updateMap);//更新报文
        ipmpUnitOperateLog.setResultStatus((String) response.get(Dict.STATUS));//0：成功，非0：失败
        ipmpUnitOperateLog.setMessage((String) response.get(Dict.MESSAGE));//返回信息
        ipmpUnitOperateLog.setErrorCode((String) response.get(Dict.ERRORCODE));//错误编码
        ipmpUnitOperateLog.setResponse(response);//返回信息response
        try {
            User user = CommonUtils.getSessionUser();
            ipmpUnitOperateLog.setUpdateUserId(user.getId());//更新人ID
            ipmpUnitOperateLog.setUpdateUserName(user.getUser_name_cn());//更新人姓名
        }catch (Exception e){
            //获取登录信息失败 更新人置空
            ipmpUnitOperateLog.setUpdateUserId("");//更新人ID
            ipmpUnitOperateLog.setUpdateUserName("system");//更新人姓名
        }
        iLogDao.saveIpmpUnitOperateLog(ipmpUnitOperateLog);
    }

    //反射获取两个实体有变动的属性
    public List<String> getUpdateFieldList(Object newObject, Object oldObject) throws Exception {
        List<String> updateFieldInfo = new ArrayList<>();
        //旧为空则为新增
        if(CommonUtils.isNullOrEmpty(oldObject)){
            //新实体
            Class<?> newObjectClazz = newObject.getClass();
            Field[] newFields = newObjectClazz.getDeclaredFields();
            //遍历新实体
            for (Field newField : newFields) {
                newField.setAccessible(true);
                //新属性值不为空
                if(!CommonUtils.isNullOrEmpty(newField.get(newObject))){
                    updateFieldInfo.add(newField.getName());
                }
            }
            return updateFieldInfo;
        }
        //新实体
        Class<?> newObjectClazz = newObject.getClass();
        //旧实体
        Class<?> oldObjectClazz = oldObject.getClass();
        Field[] newFields = newObjectClazz.getDeclaredFields();
        Field[] oldFields = oldObjectClazz.getDeclaredFields();
        //遍历新实体
        for (Field newField : newFields) {
            newField.setAccessible(true);
            String newFieldName = newField.getName();
            //_id id则跳出本次循环
            if("_id".equals(newFieldName) || "id".equals(newFieldName)){
                continue;
            }
            Object newValue = newField.get(newObject);
            //判断是否新增的字段
            boolean isContain = true;
            //遍历旧实体
            for (Field oldField : oldFields) {
                oldField.setAccessible(true);
                String oldFieldName = oldField.getName();
                //判断是否同一个字段
                if(newFieldName.equals(oldFieldName)){
                    isContain = false ;
                    //新属性值不为空
                    if(!CommonUtils.isNullOrEmpty(newValue)){
                        //判断是否相等
                        if(!newValue.equals(oldField.get(oldObject)))
                            updateFieldInfo.add(newFieldName);//有改动的字段
                    }else if(!CommonUtils.isNullOrEmpty(oldField.get(oldObject))){
                        //新属性值不为空 旧属性值为空
                        if(!oldField.get(oldObject).equals(newValue))
                            updateFieldInfo.add(newFieldName);//有改动的字段
                    }
                    break;
                }
            }
            //是新增的字段
            if( isContain ){
                updateFieldInfo.add(newFieldName);
            }
        }
        return updateFieldInfo;
    }
}
