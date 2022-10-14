package com.spdb.fdev.fuser.spdb.service.Impl;

import com.spdb.fdev.cache.RedisCache;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.common.annoation.RemoveCachedProperty;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fuser.base.dict.Constants;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.dict.ErrorConstants;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.dao.IInterfaceRegisterDao;
import com.spdb.fdev.fuser.spdb.entity.user.InterfaceRegister;
import com.spdb.fdev.fuser.spdb.service.IInterfaceRegisterService;
import com.spdb.fdev.fuser.spdb.service.RoleService;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @Author liux81
 * @DATE 2021/12/7
 */
@Service
public class InterfaceRegisterServiceImpl implements IInterfaceRegisterService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IInterfaceRegisterDao interfaceRegisterDao;

    @Autowired
    RoleService roleService;

    @Resource
    private RedisCache redisCache;

    @RemoveCachedProperty(redisKeyExpression = "fuser.interface.{interfaceRegister.interfacePath}")
    @Override
    public void add(InterfaceRegister interfaceRegister) throws Exception {
        interfaceRegister.setInterfacePath(interfaceRegister.getInterfacePath().trim());
        if(checkInterfaceExist(interfaceRegister.getInterfacePath().trim())){
            throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{interfaceRegister.getInterfacePath(),"接口路径已存在！"});
        }
        User user = CommonUtils.getSessionUser();
        interfaceRegister.setStatus("using");
        interfaceRegister.setCreateUser(user.getId());
        interfaceRegister.setCreateTime(CommonUtils.formatDate2(new Date(),CommonUtils.STANDARDDATEPATTERN));
        interfaceRegisterDao.add(interfaceRegister);
    }

    @RemoveCachedProperty(redisKeyExpression = "fuser.interface.{interfaceRegister.interfacePath}")
    @Override
    public void update(InterfaceRegister interfaceRegister) throws Exception {
        interfaceRegister.setInterfacePath(interfaceRegister.getInterfacePath().trim());
        String id = interfaceRegister.getId();
        InterfaceRegister oldInterface = interfaceRegisterDao.queryById(id);
        if(!oldInterface.getInterfacePath().equals(interfaceRegister.getInterfacePath().trim())){
            if(checkInterfaceExist(interfaceRegister.getInterfacePath())){
                throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{interfaceRegister.getInterfacePath(),"接口路径已存在！"});
            }
        }
        User user = CommonUtils.getSessionUser();
        interfaceRegister.setStatus("using");
        interfaceRegister.setUpdateUser(user.getId());
        interfaceRegister.setUpdateTime(CommonUtils.formatDate2(new Date(),CommonUtils.STANDARDDATEPATTERN));
        interfaceRegisterDao.update(interfaceRegister);
    }

    /**
     * 判断接口路径是否已存在，true表示存在，false不存在.
     * @param interfacePath
     * @return
     */
    public boolean checkInterfaceExist(String interfacePath){
        if(CommonUtils.isNullOrEmpty(interfaceRegisterDao.queryByInterface(interfacePath.trim()))){
            return false;
        }
        return true;
    }

    @Override
    public void delete(Map param) throws Exception {
        String id = (String) param.get(Dict.ID);
        InterfaceRegister interfaceRegister = interfaceRegisterDao.queryById(id);
        interfaceRegisterDao.delete(id);
        //删除之后，清除缓存，避免因redis缓存造成的多余拦截
        redisCache.removeCache("fuser.interface." + interfaceRegister.getInterfacePath());
    }

    @Override
    public Map<String, Object> query(Map param) throws Exception {
        Map<String, Object> resultMap = interfaceRegisterDao.query(param);
        //封装新增按钮
        resultMap.put("addButton",getAddButton());
        //封装编辑、删除按钮，0可点击，1当前用户权限不足，2不展示
        List<InterfaceRegister> records = (List<InterfaceRegister>)resultMap.get("records");
        boolean checkRole = roleService.checkRole(null, Constants.SUPER_MANAGER);
        for(InterfaceRegister interfaceRegister : records){
            if("using".equals(interfaceRegister.getStatus())){
                if(!checkRole) {
                    interfaceRegister.setUpdateButton(1);
                    interfaceRegister.setDeleteButton(1);
                    interfaceRegister.setRecoverButton(2);
                }else {
                    interfaceRegister.setUpdateButton(0);
                    interfaceRegister.setDeleteButton(0);
                    interfaceRegister.setRecoverButton(2);
                }
            }else {
                if(!checkRole) {
                    interfaceRegister.setUpdateButton(2);
                    interfaceRegister.setDeleteButton(2);
                    interfaceRegister.setRecoverButton(1);
                }else {
                    interfaceRegister.setUpdateButton(2);
                    interfaceRegister.setDeleteButton(2);
                    interfaceRegister.setRecoverButton(0);
                }
            }
        }
        resultMap.put("records",records);
        return resultMap;
    }

    @LazyInitProperty(redisKeyExpression = "fuser.interface.{param.interfacePath}")
    @Override
    public Set<String> getRolesByInterface(Map param) {
        String interfacePath = (String) param.get("interfacePath");
        InterfaceRegister interfaceRegister = interfaceRegisterDao.queryByInterface(interfacePath.trim());
        if(!CommonUtils.isNullOrEmpty(interfaceRegister)){
            return interfaceRegister.getRoleIds();
        }
        return new HashSet<String>();
    }

    @Override
    public void export(Map param, HttpServletResponse resp) throws Exception {
        Map map = this.query(param);
        List<InterfaceRegister> interfaceRegisters = (List<InterfaceRegister>)map.get("records");
        // 初始化workbook
        InputStream inputStream = null;
        XSSFWorkbook workbook = null;
        XSSFSheet sheet = null;
        //引入模板
        try {
            ClassPathResource classPathResource=new ClassPathResource("interface.xlsx");
            inputStream=classPathResource.getInputStream();
            workbook = new XSSFWorkbook(inputStream);
            sheet = workbook.getSheetAt(0);
            logger.info("-------load model OrderExprotList success-----");
        } catch (Exception e1) {
            logger.error("---export---" + e1);
            throw new FdevException("接口登记列表导出失败，请联系fdev管理员");
        }
        int i=1;//行数
        for (InterfaceRegister interfaceRegister : interfaceRegisters) {
            int j=0;//列数
            sheet.createRow(i);
            sheet.getRow(i).createCell(j++).setCellValue(interfaceRegister.getInterfacePath());//接口路径
            Set<Map<String, Object>> roles = interfaceRegister.getRoles();
            String roleNames = "";
            for(Map role : roles){
                roleNames += role.get(Dict.NAME) + " ";
            }
            sheet.getRow(i).createCell(j++).setCellValue(roleNames.trim());//角色
            sheet.getRow(i).createCell(j++).setCellValue(interfaceRegister.getFunctionDesc());//功能说明
            if("using".equals(interfaceRegister.getStatus())){
                sheet.getRow(i).createCell(j++).setCellValue("使用中");//状态
            }else {
                sheet.getRow(i).createCell(j++).setCellValue("已废弃");//状态
            }
            i++;
        }
        try {
            resp.reset(); resp.setContentType("application/octet-stream");
            resp.setCharacterEncoding("UTF-8");
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            resp.setHeader("Content-Disposition", "attachment;filename=" +
                    "interface"+ ".xlsx");
            workbook.write(resp.getOutputStream()); } catch (IOException e) {
            logger.error("导出接口登记列表时，数据读写错误" + e.getMessage());
        }
    }

    /**
     * 新增按钮,0可点击，1当前用户权限不足
     */
    private Integer getAddButton() throws Exception {
        if(roleService.checkRole(null, Constants.SUPER_MANAGER)){
            return 0;
        }
        return 1;
    }
}
