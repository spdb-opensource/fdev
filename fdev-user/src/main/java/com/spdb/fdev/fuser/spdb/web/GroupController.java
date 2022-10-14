package com.spdb.fdev.fuser.spdb.web;

import java.io.DataInput;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import javax.annotation.Resource;

import com.spdb.fdev.cache.RedisCache;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fuser.base.dict.Constants;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.nonull.NoNull;
import com.spdb.fdev.fuser.spdb.dao.GroupDao;
import com.spdb.fdev.fuser.spdb.dao.UserDao;
import com.spdb.fdev.fuser.spdb.entity.user.User;
import com.spdb.fdev.fuser.spdb.service.RoleService;
import com.spdb.fdev.fuser.spdb.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fuser.base.dict.ErrorConstants;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.entity.user.Group;
import com.spdb.fdev.fuser.spdb.service.GroupService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags = "小组接口")
@RequestMapping("/api/group")
@RestController
public class GroupController{

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

    @Resource
    private GroupService groupService;

    @Resource
    private UserService userService;

    @Resource
    private RoleService roleService;

    @Autowired
    private UserVerifyUtil userVerifyUtil;

    @Resource
    private RedisCache redisCache;

    @Resource
    private GroupDao groupDao;

    @Resource
    private UserDao userDao;

    /* 查询小组和全称 */
    @ApiOperation(value = "查询小组")
    @RequestMapping(value = "/query", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult query(@RequestBody @ApiParam(name = "group", value = "例如:{\"name\":\"公共组\"}") Group group) throws Exception {
        List<Group> grouplist = new ArrayList<>();
        if (CommonUtils.checkObjFieldIsAllNull(group)) {
            //所有小组信息（包含小组拼接名称）进行排序查询，并进行redis缓存处理
            grouplist = groupService.queryAllGroup();
        } else {
            //根据条件查询小组信息（包含小组拼接名称）
            grouplist = groupService.queryByGroup(group);
        }
        return JsonResultUtil.buildSuccess(grouplist);
    }

    /*根据组ID查询当前组及子组的全部人员 */
    @ApiOperation(value = "查询当前组及子组的全部人员")
    @RequestMapping(value = "/queryByGroupId", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryByGroupId(@RequestBody @ApiParam(name = "group", value = "例如:{\"id\":\"4cf6c56cd31za1126ce303l5\"}") Group group)
            throws Exception {
        if (CommonUtils.isNullOrEmpty(group.getId())) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"小组id为空"});
        }
        List<Map> list = groupService.queryByGroupId(group);
        return JsonResultUtil.buildSuccess(list);
    }

    /*根据组ID查询当前组及父组 */
    @ApiOperation(value = "查询当前组及父组")
    @RequestMapping(value = "/queryParentGroupById", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryParentGroupById(@RequestBody @ApiParam(name = "group", value = "例如:{\"id\":\"4cf6c56cd31za1126ce303l5\"}") Group group)
            throws Exception {
        if (CommonUtils.isNullOrEmpty(group.getId())) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"小组id为空"});
        }
        List<Group> list = groupService.queryParentGroupById(group);
        return JsonResultUtil.buildSuccess(list);
    }

    /*根据组ID查询当前组及子组 */
    @ApiOperation(value = "查询当前组及子组")
    @RequestMapping(value = "/queryChildGroupById", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryChildGroupById(@RequestBody @ApiParam(name = "group", value = "例如:{\"id\":\"4cf6c56cd31za1126ce303l5\"}") Group group)
            throws Exception {
        if (CommonUtils.isNullOrEmpty(group.getId())) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"小组id为空"});
        }
        List<Group> list = groupService.queryChildGroupById(group);
        if (CommonUtils.isNullOrEmpty(list)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"小组id不存在"});
        }
        return JsonResultUtil.buildSuccess(list);
    }

    /* 新增 */
    @ApiOperation(value = "新增小组")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult addGroup(
            @RequestBody @ApiParam(name = "group", value = "例如:{\"name\":\"公共组\",\"name_en\":\"commonGroup\",\"parent_id\":\"5c78cb36d3e2a18ea42c6bc8\"}") Group group
    ) throws Exception {
        if (StringUtils.isEmpty(group.getName())) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"小组名为空"});
        }
        if(!roleService.checkRole(null, Constants.SUPER_MANAGER) && !roleService.checkRole(null,Constants.GROUP_MANAGER)
        && !roleService.checkRole(null, Constants.TEAM_LEADER) && !roleService.checkRole(null, Constants.USER_MANAGER)){
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }
        //新增判断，小组管理员/团队管理员只能增加，删除、更新本组及子组下的小组
        isGroupAdmin(group.getParent_id());
        // 判断中文名是否已存在
        Group newGroup2 = new Group();
        newGroup2.setName(group.getName());
//        newGroup2.setStatus("1");
        List<Group> groupNames = groupService.queryGroup(newGroup2);
        if(!CommonUtils.isNullOrEmpty(groupNames)){
            throw new FdevException(ErrorConstants.USR_GROUP_EXEIT, new String[]{"小组名称不可重复"});
        }
        //计算新增组的级别
        Group parentGroup=null;
        group.setLevel(1);
        if(!CommonUtils.isNullOrEmpty(group.getParent_id())){
            parentGroup = groupService.queryDetailById(group.getParent_id());
        }
        if(!CommonUtils.isNullOrEmpty(parentGroup)){
            group.setLevel(parentGroup.getLevel() + 1);
        }
        return addNode(group);
    }

    /* 修改小组 */
    @ApiOperation(value = "修改小组")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult updateGroup(
            @RequestBody @ApiParam(name = "group", value = "例如:{ \"id\":\"5c4182ffa3178a4a841e6c55\",\"name\":\"A组\",\"name_en\":\"AGroup\"}") Group group)
            throws Exception {
        if (CommonUtils.isNullOrEmpty(group.getId()) || CommonUtils.isNullOrEmpty(group.getName())) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"组id或名字为空"});
        }

        if(!roleService.checkRole(null, Constants.SUPER_MANAGER) && !roleService.checkRole(null,Constants.GROUP_MANAGER)
                && !roleService.checkRole(null, Constants.TEAM_LEADER) && !roleService.checkRole(null, Constants.USER_MANAGER)){
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }
        //新增判断，小组管理员/团队负责人只能增加，删除、更新本组及子组下的小组
        isGroupAdmin(group.getId());
        Group group1 = new Group();
        group1.setId(group.getId());
        List<Group> groupNames = groupService.queryGroup(group1);
        if (CommonUtils.isNullOrEmpty(groupNames)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"该小组不存在"});
        }
        // 判断中文名未做修改
        if (groupNames.get(0).getName().equals(group.getName())) {
            return JsonResultUtil.buildSuccess();
        }
        // 判断中文名是否已存在
        Group newGroup2 = new Group();
        newGroup2.setName(group.getName());
//        newGroup2.setStatus("1");
        List<Group> newGroups = groupService.queryGroup(newGroup2);
        if (CommonUtils.isNullOrEmpty(newGroups)) {
            Group updateGroup = groupService.updateGroup(group);
            updateGroup.setCurrent_count(group.getCurrent_count());
            updateGroup.setCount(group.getCount());
            redisCache.removeCache("fuser.allgroup");
            redisCache.removeCache("fuser.alluser");
            redisCache.removeCache("fuser.allcoreuser");
            return JsonResultUtil.buildSuccess(updateGroup);
        }else {
            throw new FdevException(ErrorConstants.USR_GROUP_EXEIT, new String[]{"小组名称不可重复"});
        }
    }

    /* 删除小组 */
    @ApiOperation(value = "删除小组")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult deleteGroup(
            @RequestBody @ApiParam(name = "groupList", value = "例如:[{ \"id\":\"5c4182ffa3178a4a841e6c55\"}]") List<Group> groupList
    )
            throws Exception {
//        List<Group> list = null;
        for (Group group : groupList) {
            if (CommonUtils.isNullOrEmpty(group.getId())) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"组id为空"});
            }
            if(!roleService.checkRole(null, Constants.SUPER_MANAGER) && !roleService.checkRole(null,Constants.GROUP_MANAGER)
                    && !roleService.checkRole(null, Constants.TEAM_LEADER) && !roleService.checkRole(null, Constants.USER_MANAGER)){
                throw new FdevException(ErrorConstants.ROLE_ERROR);
            }
            //新增判断，小组管理员只能增加，删除、更新本组及子组下的小组
            isGroupAdmin(group.getId());

            Group newGroup = new Group();
            newGroup.setId(group.getId());
            List<Group> glist = groupService.queryGroup(newGroup);
            Group newGroup2 = new Group();
            newGroup2.setParent_id(group.getId());
            newGroup2.setStatus("1");
            List<Group> glist2 = groupService.queryGroup(newGroup2);
            // 删除节点，有子节点不能删
            if (CommonUtils.isNullOrEmpty(glist) || !CommonUtils.isNullOrEmpty(glist2)) {
                throw new FdevException(ErrorConstants.USR_DELETE_ERROR);
            }
            //判断组下有没有人
            User user = new User();
            user.setGroup_id(glist.get(0).getId());
            user.setStatus("0");
            long userCount = userDao.getUserCount(user);
            if (userCount == 0) {
                // 删除
                groupService.deleteGroup(group);
                groupService.removeCache(group.getId());
                groupService.removeCache("fuser.allgroup");
            } else {
                throw new FdevException(ErrorConstants.USR_INUSE_ERROR);
            }
        }
//        Group query = new Group();
//        list = groupService.queryGroup(query);
        return JsonResultUtil.buildSuccess();
    }

    /*
     * 新增节点方法
     */
    private JsonResult addNode(Group group) throws Exception {
        if (StringUtils.isEmpty(group.getParent_id())) {
            // ParentId==null,新增根节点
            //维护sortnum字段
            List<Group> groups = groupDao.queryFirstGroupMap(true);
            group.setSortNum(groups.size() + 1 + "");
            Group addGroup = groupService.addGroup(group);
            groupService.removeCache("fuser.allgroup");
            return JsonResultUtil.buildSuccess(addGroup);
        } else {
            // 未找到ParentId
            Group newGroup = new Group();
            newGroup.setId(group.getParent_id());
            List<Group> glist = groupService.queryGroup(newGroup);
            if (CommonUtils.isNullOrEmpty(glist)) {
                throw new FdevException(ErrorConstants.USR_NO_PARENTID);
            }
            List<Group> childs = groupDao.queryChild(group.getId(),true);
            group.setSortNum(glist.get(0).getSortNum() + "-" + (childs.size() + 1));
            // 新增子节点
            Group addGroup = groupService.addGroup(group);
            groupService.removeCache("fuser.allgroup");
            return JsonResultUtil.buildSuccess(addGroup);
        }
    }

    /* 查询当前组的开发人数 */
    @ApiOperation(value = "查询当前组的开发人数")
    @RequestMapping(value = "/queryDevResource", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryDevResource(@RequestBody Map requestParam) throws Exception {
        List<String> groupIds = (List<String>) requestParam.get("groupIds");
        if (CommonUtils.isNullOrEmpty(groupIds)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{" groupIds "});
        }
        List<Group> list = groupService.queryDevResource(groupIds);
        return JsonResultUtil.buildSuccess(list);
    }

    @ApiOperation(value = "根据条件获取在职小组（包含小组拼接名称）的信息")
    @RequestMapping(value = "/reBuildGroupName", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult reBuildGroupName(@RequestBody Group group) throws Exception {
        group.setStatus("1");
        List<Group> grouplist = groupService.queryByGroup(group);
        return JsonResultUtil.buildSuccess(grouplist);
    }

    /* 查询小组和人数 */
    @ApiOperation(value = "查询小组")
    @RequestMapping(value = "/queryGroup", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryGroup(@RequestBody @ApiParam(name = "group", value = "例如:{\"name\":\"公共组\"}") Group group)
            throws Exception {
        List<Group> list = null;
        list = groupService.queryGroup(group);
        return JsonResultUtil.buildSuccess(list);
    }
    /*
     * 判断操作组是否是当前用户所在的组及子组
     */
    private void isGroupAdmin(String id) throws Exception {
        if((roleService.checkRole(null,Constants.GROUP_MANAGER) || roleService.checkRole(null,Constants.TEAM_LEADER))
                && (!roleService.checkRole(null,Constants.SUPER_MANAGER) && !roleService.checkRole(null,Constants.USER_MANAGER))){
            Group pram = new Group();
            pram.setId(CommonUtils.getSessionUser().getGroup_id());
            List<Group> Prams = groupService.queryChildGroupById(pram);
            if (CommonUtils.isNullOrEmpty(Prams)) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"小组id不存在"});
            }
            boolean errorFlag = true;
            for (Group gp :Prams) {
                if(gp.getId().equals(id)){
                    errorFlag = false;
                    break;
                }
            }
            if(errorFlag){
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"小组管理员、团队负责人只能增加，删除、更新本组及子组下的小组"});
            }
        }
    }

    @ApiOperation(value = "根据多个小组id查询小组信息")
    @RequestMapping(value = "/queryGroupByIds", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryGroupByIds(@RequestBody Map Param) {
        List<String> groupIds = (List<String>) Param.get("groupIds");
        boolean allFlag = false;
        if(!CommonUtils.isNullOrEmpty(Param.get("allFlag"))){
            allFlag = (boolean)Param.get("allFlag");
        }
        return JsonResultUtil.buildSuccess(groupService.queryGroupByIds(groupIds,allFlag));
    }


    @ApiOperation(value = "查询小组fullName")
    @RequestMapping(value = "/queryGroupFullName", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryGroupFullName(@RequestBody Map Param) {
        List<String> groupIds = (List<String>) Param.get("ids");
        return JsonResultUtil.buildSuccess(groupService.queryGroupFullNameByIds(groupIds));
    }

    @ApiOperation(value = "根据小组id查询小组中文名")
    @RequestMapping(value = "/queryGroupNameById", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryGroupNameById(@RequestBody Map Param) throws Exception {
        String groupId = (String) Param.get("groupId");
        return JsonResultUtil.buildSuccess(groupService.queryGroupNameById(groupId));
    }

    @ApiOperation(value = "查询所有小组，按层级关系返回")
    @RequestMapping(value = "/queryAllGroup", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryAllGroupAndChild(){
        return JsonResultUtil.buildSuccess(groupService.queryAllGroupAndChild());
    }

    @ApiOperation(value = "根据组id分页查询组下的人（分包含和不包含子组）")
    @PostMapping(value = "/queryUserByGroupId")
    @ResponseBody
    @RequestValidate(NotEmptyFields = {"groupId", "showChild"})
    public JsonResult queryUserByGroupId(@RequestBody Map param) throws Exception {
        String groupId = (String)param.get("groupId");
        boolean showChild = (boolean)param.get("showChild");
        int pageSize = 0;
        int index = 0;
        if(!CommonUtils.isNullOrEmpty(param.get("pageSize"))){
            pageSize = (int)param.get("pageSize");
        }
        if(!CommonUtils.isNullOrEmpty(param.get("index"))){
            index = (int)param.get("index");
        }
        return JsonResultUtil.buildSuccess(groupService.queryUserByGroupId(groupId,showChild,pageSize,index));
    }

    @ApiOperation(value = "批量新增组员/调整人员的组信息")
    @PostMapping(value = "/addGroupUsers")
    @ResponseBody
    @RequestValidate(NotEmptyFields = {"userIds","groupId"})
    public JsonResult addGroupUsers(@RequestBody Map param){
        List<String> userIds = (List<String>)param.get("userIds");
        String groupId = (String)param.get("groupId");
        groupService.addGroupUsers(userIds, groupId);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping("/getThreeLevelGroup")
    @RequestValidate(NotEmptyFields = {"id"})
    public JsonResult getThreeLevelGroup(@RequestBody Group group) throws Exception {
        return JsonResultUtil.buildSuccess(groupService.getThreeLevelGroup(group.getId()));
    }

    /**
     * 根据组ids查询当前组及子组
     * @param param
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "批量查询当前组及子组")
    @RequestMapping(value = "/queryChildGroupByIds", method = RequestMethod.POST)
    public JsonResult queryChildGroupByIds(@RequestBody Map<String, List<String>> param) throws Exception {
        List<String> ids = param.get(Dict.IDS);
        if (CommonUtils.isNullOrEmpty(ids)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"小组id为空"});
        }
        return JsonResultUtil.buildSuccess(groupService.queryChildGroupByIds(ids));
    }
}
