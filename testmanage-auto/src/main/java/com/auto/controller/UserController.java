package com.auto.controller;

import com.auto.dict.Dict;
import com.auto.entity.User;
import com.auto.service.IUserService;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.test.testmanagecommon.vaildate.RequestValidate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService iUserService;

    /**
     * 新增
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/addUser")
    @RequestValidate(NotEmptyFields = {Dict.USERNAME})
    public JsonResult addUser(@RequestBody Map<String, String> map) throws Exception {
    	iUserService.addUser(map);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 查询
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryUser")
    public JsonResult queryUser(@RequestBody Map<String, String> map) throws Exception {
        List<User> user = iUserService.queryUser(map);
        Map<String, Object> result = new HashMap<>();
        result.put(Dict.USER, user);
        result.put(Dict.SIZE, user.size());
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 删除
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/deleteUser")
    @RequestValidate(NotEmptyFields = {Dict.USERID})
    public JsonResult deleteUser(@RequestBody Map map) throws Exception {
    	iUserService.deleteUser(map);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 修改
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/updateUser")
    @RequestValidate(NotEmptyFields = {Dict.USERID})
    public JsonResult updateUser(@RequestBody Map map) throws Exception {
    	iUserService.updateUser(map);
        return JsonResultUtil.buildSuccess();
    }
}
