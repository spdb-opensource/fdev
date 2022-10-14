package com.spdb.fdev.fuser.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.nonull.NoNull;
import com.spdb.fdev.fuser.spdb.entity.user.InterfaceRegister;
import com.spdb.fdev.fuser.spdb.service.IInterfaceRegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;


/**
 * @Author liux81
 * @DATE 2021/12/7
 */
@RestController
@RequestMapping("/api/interfaceRegister")
public class InterfaceRegisterController {
    @Autowired
    IInterfaceRegisterService interfaceRegisterService;

    @PostMapping("/add")
    @NoNull(require = {"interfacePath","roleIds","functionDesc"},parameter = InterfaceRegister.class)
    public JsonResult add(@RequestBody InterfaceRegister interfaceRegister) throws Exception {
        interfaceRegisterService.add(interfaceRegister);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping("/update")
    @NoNull(require = {Dict.ID,"interfacePath","roleIds","functionDesc"},parameter = InterfaceRegister.class)
    public JsonResult update(@RequestBody InterfaceRegister interfaceRegister) throws Exception {
        interfaceRegisterService.update(interfaceRegister);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping("/delete")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult delete(@RequestBody Map param) throws Exception {
        interfaceRegisterService.delete(param);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping("/query")
    public JsonResult query(@RequestBody Map param) throws Exception {
        return JsonResultUtil.buildSuccess(interfaceRegisterService.query(param));
    }

    @PostMapping("/export")
    public JsonResult export(@RequestBody Map param, HttpServletResponse resp) throws Exception {
        interfaceRegisterService.export(param,resp);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping("/getRolesByInterface")
    public JsonResult getRolesByInterface(@RequestBody Map param){
        return JsonResultUtil.buildSuccess(interfaceRegisterService.getRolesByInterface(param));
    }


}
