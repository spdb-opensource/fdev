package com.mantis.controller;

import com.mantis.dict.Dict;
import com.mantis.service.IMantisUserService;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.util.JsonResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class MantisUserController {
    @Autowired
    private IMantisUserService mantisUserService;

    /**
     * 查询在职人员姓名（中英文+组id，可在入参加组id筛选）
     * @param map
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/regeisterMantisUser")
    public JsonResult regeisterMantisUser(@RequestBody Map<String, String> map) throws Exception {
        String userName = map.get(Dict.USER_NAME);
        String password = map.get(Dict.PASSWORD);
        String userNameCn = map.get(Dict.USER_NAME_CN);
        String email = map.get(Dict.EMAIL);
        mantisUserService.regeisterMantisUser(userName, password, userNameCn, email);
        return JsonResultUtil.buildSuccess(null);
    }


    /**
     * 将所有mantis用户的生产问题权限取消
     * @param map
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/cancelAllUserProjectLevel")
    public JsonResult cancelAllUserProjectLevel(@RequestBody Map<String, String> map) throws Exception {
        mantisUserService.cancelAllUserProjectLevel();
        return JsonResultUtil.buildSuccess(null);
    }
}
