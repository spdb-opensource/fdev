package com.test.controller;

import com.test.dict.Dict;
import com.test.service.UserService;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.rediscluster.RedisUtils;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.test.testmanagecommon.vaildate.RequestValidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private RedisUtils redisUtils;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @RequestValidate(NotEmptyFields = {Dict.USER_ID , Dict.ROLE_ID })
    @PostMapping(value = "/update")
    public JsonResult updateUser(@RequestBody Map<String,Object> requestMap) throws Exception {
    	String user_id = (String) requestMap.get(Dict.USER_ID);
        List<String> roleIds = (List<String>) requestMap.get(Dict.ROLE_ID);
        String level = (String) requestMap.get(Dict.LEVEL);
        return JsonResultUtil.buildSuccess(userService.update(user_id, level, roleIds));
    }

    @PostMapping(value = "/saveMantisToken")
    public JsonResult saveMantisToken(@RequestBody Map<String,Object> map) throws Exception {
    	String mantis_token = (String) map.get(Dict.MANTIS_TOKEN);
        String user_id = (String) map.get(Dict.ID);
        userService.saveMantisToken(mantis_token);
        return JsonResultUtil.buildSuccess(null);
    }

    @PostMapping(value = "/query")
    public JsonResult query(@RequestBody Map<String,Object> map) throws Exception {
        List<String> search = (List<String>) map.get(Dict.SEARCH);
        String groupId = (String) map.get(Dict.GROUPID);
        Integer currentPage = (Integer) map.get(Dict.CURRENTPAGE);
        Integer pageSize = (Integer) map.get(Dict.PAGESIZE);
        return JsonResultUtil.buildSuccess(userService.query(search, groupId, currentPage, pageSize));
    }

    @PostMapping(value = "/exportUser")
    public void exportUser(@RequestBody Map<String,Object> map, HttpServletResponse resp) throws Exception {
        List<String> search = (List<String>) map.get(Dict.SEARCH);
        String groupId = (String) map.get(Dict.GROUPID);
        userService.exportUser(search, groupId, resp);
    }



    @PostMapping(value = "/fdevSyncUser")
	public void fdevSyncUser()throws Exception {
		userService.fdevSyncUser();
	}

    /**
     * 分配人员
     * @param requestMap
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/distributionUpdate")
    @Transactional
    public JsonResult distributionUpdate(@RequestBody Map<String,Object> requestMap) throws Exception {
    	String group_id = (String) requestMap.get(Dict.GROUP_ID);
        String manager = (String) requestMap.get(Dict.WORKMANAGER);
    	List<String> leaders = (List<String>) requestMap.get(Dict.WORKLEADER);
    	List<String> securityLeader = (List<String>) requestMap.get(Dict.SECURITYLEADER);
        String uatContact = String.valueOf(requestMap.getOrDefault(Dict.UATCONTACT, ""));
    	Integer i = userService.updateWorkConfig(group_id, manager, leaders, uatContact, securityLeader);
    	if(i<1) {
    		throw new FtmsException("更新失败！");
    	}
		return JsonResultUtil.buildSuccess("true");
    }

    /**
     * 查询已分配
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryAlreadyAllocated")
    @RequestValidate(NotEmptyFields = {})
    public JsonResult queryAlreadyAllocated(@RequestBody Map map) throws Exception {
        List<Map<String, Object>> result = userService.queryAlreadyAllocated();
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 通过token缓存玉衡用户redis信息并返回用户信息给前端
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/syncLoginFtms")
    @RequestValidate(NotEmptyFields = {Dict.TOKEN})
    public JsonResult syncLoginFtms(@RequestBody Map map) throws Exception {
        String token = (String) map.get(Dict.TOKEN);
        return JsonResultUtil.buildSuccess(userService.syncLoginFtms(token));
    }
}
