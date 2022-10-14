package com.spdb.fdev.fuser.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fuser.base.dict.Constants;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.dict.ErrorConstants;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.base.utils.DES3;
import com.spdb.fdev.fuser.base.utils.Validator;
import com.spdb.fdev.fuser.spdb.Token.TokenManger;
import com.spdb.fdev.fuser.spdb.entity.user.*;
import com.spdb.fdev.fuser.spdb.service.*;
import com.spdb.fdev.transport.RestTransport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.sf.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Api(tags = "用户接口")
@RequestMapping("/api/user")
@RestController
@RefreshScope
public class UserController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

    @Value("${server.login.path}")
    private String loginpath;

    @Value("${is.adduser.sendmail}")
    private boolean isSendMail;

    @Value("${user.isjob.list}")
    private String joblist;

    @Resource
    private RestTransport restTransport;
    @Resource
    private UserService userService;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Resource
    private CompanyService companyService;

    @Resource
    private GroupService groupService;

    @Resource
    private RoleService roleService;

    @Resource
    private LabelService labelService;

    @Resource
    private MailService mailService;

    @Resource
    private TokenManger tokenManger;

    @Autowired
    private FtmsUserService ftmsUserService;

    @Autowired
    GitlabService gitlabService;

    @Autowired
    ApprovalService approvalService;

    @Autowired
    UserVerifyUtil userVerifyUtil;

    @Resource
    private DES3 des3;

    /**
     * 新增用户
     *
     * @param user
     * @param request
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "新增用户")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult addUser(@RequestBody @ApiParam(name = "新增用户参数", value = "") User user, HttpServletRequest request)
            throws Exception {
        String nameCn = user.getUser_name_cn();
        String email = user.getEmail();
        String password = user.getPassword();
        String groupId = user.getGroup_id();
        List<String> roleId = user.getRole_id();
        String companyId = user.getCompany_id();
        List<String> labels = user.getLabels();
        String redmine = user.getRedmine_user();
        String svn = user.getSvn_user();
        //String gitlab = user.getGit_user();
        String gitUserId = user.getGit_user_id();
        String tel = user.getTelephone();
        Boolean Kf_approval = user.getKf_approval();
        Boolean net_move = user.getNet_move();
        Boolean is_spdb = user.getIs_spdb();
        String vm_ip = user.getVm_ip();
        String vm_name = user.getVm_name();
        String vm_user_name = user.getVm_user_name();
        String phone_type = user.getPhone_type();
        String phone_mac = user.getPhone_mac();
        String status = user.getStatus();
        String area_id = user.getArea_id();
        String function_id = user.getFunction_id();
        String rank_id = user.getRank_id();
        String education = user.getEducation();
        String start_time = user.getStart_time();
        String remark = user.getRemark();
        String createDate = user.getCreate_date();
        String is_party_member = user.getIs_party_member();
        String section = user.getSection();
        String work_num = user.getWork_num();
        //用户开通kf是否为行内测试机
        String is_spdb_mac = CommonUtils.isNullOrEmpty(user.getIs_spdb_mac()) ? "" : user.getIs_spdb_mac();
        //校验邮箱
        if (CommonUtils.isNullOrEmpty(email) || !Validator.isEmail(email)) {
            logger.error("wrong email format");
            throw new FdevException(ErrorConstants.USR_EMAILERROR);
        }
        // 根据邮箱自动截取用户英文名称 并查询英文名称的用户是否已经存在
        String nameEn = email.substring(0, email.indexOf("@"));
        User user1 = new User();
        user1.setUser_name_en(nameEn);
        List<Map> u = userService.queryUser(user1);
        //判断用户名是否存在
        if (!CommonUtils.isNullOrEmpty(u)) {
            logger.error("user already exist");
            throw new FdevException(ErrorConstants.USR_EXISTED);
        }
        User user0 = new User("", nameEn, nameCn, email, des3.encrypt("xxx"), groupId, roleId,
                svn, redmine, gitUserId, "", "", tel, companyId, status, "3", labels,
                createDate, "", area_id, function_id, rank_id, education, start_time, remark,is_party_member,section,work_num,"","");
        //判断是否KF白名单开通
        if (!CommonUtils.isNullOrEmpty(Kf_approval) && Kf_approval) {
            user0.setKf_approval(Kf_approval);
            //判断手机型号、mac是否为空
            if (CommonUtils.isNullOrEmpty(phone_type))
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"手机型号格式不对!"});
            if (CommonUtils.isNullOrEmpty(phone_mac))
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"手机mac地址格式不对!"});
            user0.setPhone_type(phone_type);
            user0.setPhone_mac(phone_mac);
        } else {
            user0.setKf_approval(false);
            user0.setPhone_mac("");
            user0.setPhone_type("");
        }
        //判断是否网段迁移
        if (!CommonUtils.isNullOrEmpty(net_move) && net_move) {
            user0.setNet_move(true);
            //判断是否为厂商 是否为行内
            if (!is_spdb) {
                //厂商，只有厂商的时候是会需要vm_name这个字段
                if (CommonUtils.isNullOrEmpty(vm_name)) {
                    throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"虚拟机名称格式不对!"});
                }
                user0.setVm_name(vm_name);
                user0.setIs_spdb(false);
            } else {
                //行内
                user0.setIs_spdb(true);
                user0.setVm_name("");
            }
            //无论是否为厂商还是行内，都会有这下面的几个字段
            String regex = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)($|(?!\\.$)\\.)){4}$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(vm_ip);
            if (CommonUtils.isNullOrEmpty(vm_ip) || !matcher.find()) { //ip正则判断
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"虚拟机ip地址/VDI虚机ip地址格式不对"});
            }
            user0.setVm_ip(vm_ip);
            if (CommonUtils.isNullOrEmpty(vm_user_name)) {  //报错信息
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"虚拟机用户名或域用户为空!"});
            }
            user0.setVm_user_name(vm_user_name);
        } else {
            user0.setVm_name("");
            user0.setNet_move(false);
            user0.setIs_spdb(user.getIs_spdb());
            user0.setVm_ip("");
            user0.setVm_user_name("");
        }
        user0.setIs_kfApproval(false);
        user0.setIs_vmApproval(false);
        user0.setIs_spdb_mac(is_spdb_mac);
        // 判断是否有属性为NULL
        if (CommonUtils.checkObjFieldIsNull(user0)) {
            logger.error("user param has null value");
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"用户属性"});
        }
        // 判断，公司，小组，角色，标签，权限身份是否存在
        if (!checkEntity(companyId, groupId, roleId, labels)) {
            logger.error("company, group, permission, role or label not exist");
            throw new FdevException(ErrorConstants.USR_CHECK_ENTITY_ERROR, new String[]{"公司,小组,权限,角色,标签有不存在项"});
        }
        //判断手机号码格式
        if (!Validator.isPhone(tel)) {
            logger.error("phone number format error");
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"手机号码格式不对!"});
        }
        if (!CommonUtils.isNullOrEmpty(gitUserId)) {
            //判断gitlab用户正确性
            String git_user = userService.checkGitUserById(gitUserId);
            user0.setGit_user_id(gitUserId);
            user0.setGit_user(git_user);
            //gitlab 给用户添加fdev-resouce项目的reporter权限
            Map<String, Object> map = new HashMap<>();
            map.put(Dict.GIT_USER_ID, gitUserId);
            Object object = userService.addMember(map);
            if (CommonUtils.isNullOrEmpty(object)) {
                logger.error("fail to add fdev-resource reporter");
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"用户添加fdev-resouce项目的reporter权限失败"});
            }
        }
        //判断access token正确性
        if (!CommonUtils.isNullOrEmpty(user.getGit_token())) {
            String gitlabToken = user.getGit_token();
            Map<String, Object> map = new HashMap<>();
            user0.setGit_token(gitlabToken);
            map.put(Dict.TOKEN, gitlabToken);
            map.put(Dict.REST_CODE, "checkGitlabToken");
            Boolean check = (Boolean) restTransport.submit(map);
            if (check == false) {
                logger.error("gitlab token wrong");
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"access token有误,保证与gitlab账号一一对应！获取"
                        + "地址为: http://xxx/profile/personal_access_tokens"});
            }
        }
        String applicant_id = null;
        List<Map> loginUserInfos = new ArrayList<>();
        // 厂商用户新增需要行内人员
        if (!email.endsWith("spdb.com.cn")) {
            //获取当前登录的用户信息
            String userNameEn = tokenManger.getUserByToken(request.getHeader(Dict.AUTHORIZATION));
            User loginUser = new User();
            loginUser.setUser_name_en(CommonUtils.isNullOrEmpty(userNameEn)?" ":userNameEn);
            loginUserInfos = userService.queryUser(loginUser);
            Role role = new Role();
            List<Role> queryRoles = roleService.queryRole(role);
            //如果当前登陆用户存在，说明是他人新增用户
            if (!CommonUtils.isNullOrEmpty(loginUserInfos)) {
                Map loginUserInfo = loginUserInfos.get(0);
                //新增权限判断
                if (!havePower(loginUserInfo, user, Dict.NAME)) {
                    logger.error("authurity not correct");
                    throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"新增权限不足"});
                }
                //获取当前登录用户的id
                applicant_id = (String) loginUserInfo.get(Dict.ID);
                String name_en = (String) loginUserInfo.get(Dict.NAMEEN);
                if (!roleService.checkRole(name_en,Constants.SUPER_MANAGER)) {
                    //判断修改用户角色是否为环境配置管理员 或 基础架构管理员
                    List<String> roles = user.getRole_id();
                    if (CommonUtils.isNullOrEmpty(roles)) {
                        throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"role_id", "用户角色为空"});
                    }
                    for (Role queryRole : queryRoles) {
                        if (Constants.ENV_ADMIN.equals(queryRole.getName()) || Constants.FRAMEWORK_ADMIN.equals(queryRole.getName())) {
                            String id = queryRole.getId();
                            if (roles.contains(id)) {
                                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"只有admin账号和超级管理员可以指派用户角色为环境配置管理员或基础架构管理员"});
                            }
                        }
                    }
                }
            } else {
                List<Role> r = queryRoles.stream().filter(
                        e -> Constants.MANUROLE.contains(e.getName())).collect(Collectors.toList());
                for (String id : roleId) {
                    if (!r.stream().map(Role::getId).collect(Collectors.toList()).contains(id)) {
                        logger.error("manu role not allowed");
                        throw new FdevException(ErrorConstants.ROLE_ERROR,
                                new String[]{"厂商用户新增补录只能选择开发人员或者测试人员角色"});
                    }
                }
            }

        }
        //新增用户
        if (email.endsWith("spdb.com.cn") && !CommonUtils.isNullOrEmpty(password)
                && userService.authenticate(user0.getUser_name_en(), password)) {
            user0.setIs_once_login("3");// 如果是行内用户新增补录并且通过了权限验证,不进新手引导
            user0.setPassword(des3.encrypt(password));
            isSendMail = false;//行内用户新增补录不发邮件
            userService.addUser(user0);//新增
        } else if (!email.endsWith("spdb.com.cn") && !CommonUtils.isNullOrEmpty(password)
                && userService.authenticateManu(user0.getUser_name_en(), password)) {
            // 如果是厂商用户新增补录并且通过了权限验证,进新手引导
            user0.setPassword(des3.encrypt(password));
            isSendMail = false;//厂商用户新增补录不发邮件
            userService.addUser(user0);
        } else {
            //厂商用户他人帮建，需要发邮件提示，进新手引导
            userService.addUser(user0);
        }
        // 标签热度+1
        for (String labelId : labels) {
            List<Label> mylabel = labelService.queryLabel(new Label(labelId, null, null, null));
            if (CommonUtils.isNullOrEmpty(mylabel)) {
                continue;
            }
            Label label = mylabel.get(0);
            label.setCount(label.getCount() + 1);
            labelService.updateLabel(label);
        }
        if (isSendMail) {
            HashMap model = new HashMap();
            model.put("user_name", nameEn);
            model.put("loginpath", loginpath);
            model.put("login_user_name", nameEn);
            mailService.sendEmail(Constants.EMAIL_ONECELOGIN, Constants.TEMPLATE_ONECELOGIN, model, email);
        }
        User user2 = new User();
        user2.setUser_name_en(nameEn);
        Map returnUser = userService.queryUser(user2).get(0);
        //获得使用人id
        String user_id = (String) returnUser.get(Dict.ID);
        if (user0.getKf_approval()) {
            //新增一条手机kf网络白名单开通 status:未审批，该开通只需要填手机信号、手机mac地址
            NetApproval addApproval = new NetApproval();
            addApproval.setType(Dict.KF_APPROVAL);
            addApproval.setId(CommonUtils.createId());
            addApproval.setApplicant_id(applicant_id);
            addApproval.setUser_id(user_id);
            addApproval.setPhone_type(phone_type);
            addApproval.setPhone_mac(phone_mac);
            addApproval.setStatus(Dict.WAIT_APPROVE);
            addApproval.setCreate_time(CommonUtils.formatDate2(new Date(), CommonUtils.INPUT_DATE));
            this.approvalService.addApprovalByUser(addApproval);
        }
        if (user0.getNet_move()) {
            //新增一份虚拟机网段迁移申请  status:为审批
            NetApproval addApproval = new NetApproval();
            addApproval.setVm_ip(user0.getVm_ip());
            addApproval.setVm_user_name(user0.getVm_user_name());
            //判断是否为行内
            if (user0.getIs_spdb()) {
                addApproval.setType(Dict.VDI_APPROVAL);
            } else {
                addApproval.setVm_name(user0.getVm_name());
                addApproval.setType(Dict.VM_APPROVAL);
            }
            addApproval.setId(CommonUtils.createId());
            addApproval.setApplicant_id(applicant_id);
            addApproval.setUser_id(user_id);
            addApproval.setStatus(Dict.WAIT_APPROVE);
            addApproval.setCreate_time(CommonUtils.formatDate2(new Date(), CommonUtils.INPUT_DATE));
            this.approvalService.addApprovalByUser(addApproval);
        }
        return JsonResultUtil.buildSuccess(returnUser);
    }

    @ApiOperation(value = "修改用户")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult updateUser(
            @RequestBody @ApiParam(name = "修改用户参数", value = "示例：{" + "\r\n  " + ",\"user_name_en\": \"test\" "
                    + "\r\n " + ",\"gitlab_user\": \"csii\" " + "\r\n   " + ",\"role_id\": \"5c41796ca3178a3eb4b52002\" "
                    + "\r\n " + ",\"permission_id\": \"5c417a21a3178a3eb4b52002\" " + "\r\n "
                    + ",\"labels\": [\"5c47cb4436ba06b8416e5c41\"]" + "\r\n " + ",\"telephone\": \"100xxxxxxxx\""
                    + "\r\n " + ",\"status\": \"0\"" + "\r\n" + "}") User user,
            HttpServletRequest request) throws Exception {

        if (CommonUtils.isNullOrEmpty(user.getUser_name_en())) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"用户英文名为空"});
        }
        //判断修改用户是否存在
        User user2 = new User();
        user2.setUser_name_en(user.getUser_name_en());
        List<Map> u = userService.queryUser(user2);
        if (CommonUtils.isNullOrEmpty(u)) {
            throw new FdevException(ErrorConstants.USR_NOT_EXIST);
        }
        //修改用户的 用户id
        String user_id = (String) u.get(0).get(Dict.ID);
        //获取当前登录的用户信息
        String token = request.getHeader(Dict.AUTHORIZATION);
        String userNameEn1 = tokenManger.getUserByToken(token);
        User user1 = new User();
        user1.setUser_name_en(userNameEn1);
        List<Map> res = userService.queryUser(user1);
        if (CommonUtils.isNullOrEmpty(res)) {
            throw new FdevException(ErrorConstants.USR_NOT_EXIST);
        }
        if (!havePower(res.get(0), user, Dict.OLD)) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"修改权限不足"});
        }
        //获取当前登录用户的id
        String applicant_id = (String) res.get(0).get(Dict.ID);
        String userNameEn = (String) res.get(0).get(Dict.USER_NAME_EN);
        Group group = (Group) res.get(0).get(Dict.GROUP);
        if (CommonUtils.isNullOrEmpty(group)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"组不存在"});
        }
        //判断手机号码格式
        if (!Validator.isPhone(user.getTelephone())) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"手机号码格式不对"});
        }

        //判断修改用户角色是否为环境配置管理员 或 基础架构管理员
        List<String> roles = user.getRole_id();
        for (String roleId : roles) {
            List<String> oldRoleIds = (List<String>) u.get(0).get(Dict.ROLE_ID);
            if (!oldRoleIds.contains(roleId)) {
                Role role = new Role();
                role.setId(roleId);
                List<Role> queryRoles = roleService.queryRole(role);
                if (Constants.ENV_ADMIN.equals(queryRoles.get(0).getName()) || Constants.FRAMEWORK_ADMIN.equals(queryRoles.get(0).getName())) {
                    if (!roleService.checkRole(null,Constants.SUPER_MANAGER)) {
                        throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"只有超级管理员权限可以指派用户角色为环境配置管理员或基础架构管理员"});
                    }
                }
            }
        }
        //判断修改信息的是否为本人且不是测试中心、业务部门、规划部门的小组成员
        if (user.getUser_name_en().equals(userNameEn1)) {
            //判断gitlab用户正确性
            if (!CommonUtils.isNullOrEmpty(user.getGit_user_id())) {
                String git_user = userService.checkGitUserById(user.getGit_user_id());
                user.setGit_user(git_user);
                Map<String, Object> map = new HashMap<>();
                map.put(Dict.GIT_USER_ID, user.getGit_user_id());
                Object object = userService.addMember(map);
                if (CommonUtils.isNullOrEmpty(object)) {
                    throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"用户添加fdev-resouce项目的reporter权限失败"});
                }
            } else {
                user.setGit_user_id((String) u.get(0).get(Dict.GIT_USER_ID));
            }
            //判断access token正确性
            if (!CommonUtils.isNullOrEmpty(user.getGit_token())) {
                String gitlabToken = user.getGit_token();
                Map<String, Object> map = new HashMap<>();
                map.put(Dict.TOKEN, gitlabToken);
                map.put(Dict.REST_CODE, "checkGitlabToken");
                Boolean check = (Boolean) restTransport.submit(map);
                if (check == false) {
                    throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"access token有误,保证与gitlab账号一一对应！获取"
                            + "地址为: http://xxx/profile/personal_access_tokens"});
                }
            } else {
                user.setGit_token((String) u.get(0).get(Dict.GIT_TOKEN));
            }
        }

        Boolean KFflag = false;         //用来表示kf网络的字段
        Boolean VMflag = false;         //用来标识VM迁移网络的字段

        String regex = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)($|(?!\\.$)\\.)){4}$";
        Pattern pattern = Pattern.compile(regex);

        //判断kf网络是否申请开通
        if (!CommonUtils.isNullOrEmpty(user.getIs_kfApproval())) {
            if (user.getIs_kfApproval()) {
                if (!CommonUtils.isNullOrEmpty(user.getPhone_type().trim()) &&
                        !CommonUtils.isNullOrEmpty(user.getPhone_mac().trim())) {
                    KFflag = true;
                } else {
                    throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"手机类型或手机mac地址为空"});
                }
            }
        } else
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"是否提交kf网络申请字段为空"});

        //判断虚机迁移是否申请开通
        if (!CommonUtils.isNullOrEmpty(user.getIs_vmApproval())) {
            if (user.getIs_vmApproval()) {
                if (CommonUtils.isNullOrEmpty(user.getVm_ip().trim()))
                    throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"虚机迁移虚机ip字段为空"});

                if (CommonUtils.isNullOrEmpty(user.getVm_user_name().trim()))
                    throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"虚机用户名字段为空"});

                Matcher matcher = pattern.matcher(user.getVm_ip());
                if (CommonUtils.isNullOrEmpty(user.getIs_spdb()))
                    throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"是否为行内字段为空"});
                //行内人员
                if (user.getIs_spdb()) {
                    if (matcher.find()) {
                        VMflag = true;
                    } else
                        throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"行内虚机迁移虚机ip不合法"});
                } else { //厂商
                    if (matcher.find() && !CommonUtils.isNullOrEmpty(user.getVm_name().trim())) {
                        VMflag = true;
                    } else
                        throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"厂商虚机迁移虚机ip不合法或虚机名为空"});
                }
            }
        } else
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"是否提交虚机迁移申请字段为空"});
        if (!CommonUtils.isNullOrEmpty(user.getVm_ip().trim())) {
            Matcher m = pattern.matcher(user.getVm_ip().trim());
            if (!m.find()) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"虚机ip不合法"});
            }
        }

        //是否为行内测试机
        String is_spdb_mac = user.getIs_spdb_mac();
        //若库里面没有值，则给空
        if (CommonUtils.isNullOrEmpty(is_spdb_mac)) {
            is_spdb_mac = "";
        }

        Boolean leaveFlag = false;           //表示是否离职了

        //修改为离职状态
        if (CommonUtils.isNullOrEmpty(user.getLeave_date()) && user.getStatus().equals("1") && res.get(0).get(Dict.STATUS).equals("0")) {
            String leavedate = CommonUtils.formatDate("yyyy/MM/dd");
            user.setLeave_date(leavedate);
            //若当前用户涉及KF网络白名单，则在点击提交的时候，新增一条手机KF网络白名单关闭申请
            leaveFlag = true;
        }
        //当变成离职时，新增一条手机kf网络白名单关闭申请，仅对非行内测试机进行关闭申请，行内测试机不关闭
        if (leaveFlag) {
            //将对应用户的gitlab用户从ebank组删除
            String git_user_id = (String) u.get(0).get(Dict.GIT_USER_ID);
            String git_token = (String) u.get(0).get(Dict.GIT_TOKEN);
            if (!CommonUtils.isNullOrEmpty(git_user_id) && !CommonUtils.isNullOrEmpty(git_token)) {
                Object result = null;
                Boolean existEbank = true;
                try {
                    //查询ebank组中是否存在有该用户
                    result = this.gitlabService.queryGitlabUserOfGroup(git_user_id, git_token);
                } catch (Exception e) {
                    logger.error("该用户gitlab用户不存在ebank组内！");
                    existEbank = false;
                }
                if (!CommonUtils.isNullOrEmpty(result) && existEbank) {
                    //如果去gitlab查找用户在ebank组中存在有
                    //便执行从ebank组移除
                    try {
                        this.gitlabService.removeGitlabUser(git_user_id, git_token);
                    } catch (Exception e) {
                        logger.error("将该用户从ebank组删除出错！");
                    }
                }
            }
            NetApproval openWaitApp = new NetApproval();
            //查询开通 待审核的记录
            openWaitApp.setType(Dict.KF_APPROVAL);
            openWaitApp.setStatus(Dict.WAIT_APPROVE);
            openWaitApp.setUser_id(user_id);
            //kf开通 待审核的记录
            List<Map> openApps = this.approvalService.queryApproval(openWaitApp);

            //若不为空，将该条记录状态改为“拒绝”
            if (!CommonUtils.isNullOrEmpty(openApps)) {
                String openAppId = (String) openApps.get(0).get("id");
                NetApproval updateApproval = new NetApproval();
                updateApproval.setId(openAppId);
                updateApproval.setStatus(Dict.REFUSED);
                //更新
                this.approvalService.updateApprovalStatus(updateApproval);
            }
            if (!is_spdb_mac.equals("1")) {
                //若使用的是行内测试机（即is_spdb_mac为1），则不做关闭处理
                NetApproval openPassApp = new NetApproval();
                openPassApp.setType(Dict.KF_APPROVAL);
                openPassApp.setStatus(Dict.PASSED);
                openPassApp.setUser_id(user_id);
                //kf开通 已开通的记录
                List<Map> openPassApps = this.approvalService.queryApproval(openPassApp);
                if (!CommonUtils.isNullOrEmpty(openPassApps)) {
                    //变为关闭 待审核
                    String openPassId = (String) openPassApps.get(0).get("id");
                    NetApproval closeApproval = new NetApproval();
                    closeApproval.setId(openPassId);
                    closeApproval.setStatus(Dict.WAIT_APPROVE);
                    closeApproval.setType(Dict.KF_OFF_APPROVAL);
                    closeApproval.setCreate_time(CommonUtils.formatDate2(new Date(), CommonUtils.INPUT_DATE));
                    this.approvalService.updateApprovalStatus(closeApproval);
                }
            } else
                logger.info("该用户" + user_id + "使用的是kf为行内测试机，离职无须关闭");
        } else {
            //修改用户的时候，对kf、虚机迁移网络的开通
            if (KFflag) {
                //kf
                /**
                 * kf 开通 状态只会最多有一条（待审核/已通过）
                 */
                NetApproval kfOpenWaitApp = new NetApproval();
                kfOpenWaitApp.setType(Dict.KF_APPROVAL);
                kfOpenWaitApp.setStatus(Dict.WAIT_APPROVE);
                kfOpenWaitApp.setUser_id(user_id);
                //查询kf开通 待审核的记录
                List<Map> openWaitApps = this.approvalService.queryApproval(kfOpenWaitApp);

                NetApproval kfOpenPassApp = new NetApproval();
                kfOpenPassApp.setUser_id(user_id);
                kfOpenPassApp.setStatus(Dict.PASSED);
                kfOpenPassApp.setType(Dict.KF_APPROVAL);
                //查询kf开通 已开通的记录
                List<Map> openPassApps = this.approvalService.queryApproval(kfOpenPassApp);

                //存在有kf开通 待审核，对其进行修改
                if (!CommonUtils.isNullOrEmpty(openWaitApps)) {
                    //取出kf相关信息（phone_type、phone_mac）
                    if (!(openWaitApps.get(0).get(Dict.PHONE_TYPE).equals(user.getPhone_type()) &&
                            openWaitApps.get(0).get(Dict.PHONE_MAC).equals(user.getPhone_mac()))) {
                        String openWaitId = (String) openWaitApps.get(0).get("id");
                        //取phone_type、phone_mac
                        NetApproval updateApp = new NetApproval();
                        updateApp.setId(openWaitId);
                        updateApp.setPhone_type(user.getPhone_type());
                        updateApp.setPhone_mac(user.getPhone_mac());
                        updateApp.setApplicant_id(applicant_id);
                        updateApp.setCreate_time(CommonUtils.formatDate2(new Date(), CommonUtils.INPUT_DATE));
                        this.approvalService.updateApprovalStatus(updateApp);
                    }
                } else if (!CommonUtils.isNullOrEmpty(openPassApps)) {
                    //若有查到相关的kf开通 已开通的记录
                    //取出kf相关信息（phone_type、phone_mac）
                    if (!(openPassApps.get(0).get(Dict.PHONE_TYPE).equals(user.getPhone_type()) &&
                            openPassApps.get(0).get(Dict.PHONE_MAC).equals(user.getPhone_mac()))) {
                        //当kf相关信息不一致时，将该条记录变为关闭 待审核，新增一条 开通 待审核
                        String openPassId = (String) openPassApps.get(0).get("id");
                        NetApproval updateApp = new NetApproval();
                        updateApp.setId(openPassId);
                        updateApp.setType(Dict.KF_OFF_APPROVAL);
                        updateApp.setStatus(Dict.WAIT_APPROVE);
                        updateApp.setApplicant_id(applicant_id);
                        updateApp.setCreate_time(CommonUtils.formatDate2(new Date(), CommonUtils.INPUT_DATE));
                        this.approvalService.updateApprovalStatus(updateApp);
                        //新增一条kf开通 待审核记录
                        NetApproval addNewApp = new NetApproval();
                        addNewApp.setId(CommonUtils.createId());
                        addNewApp.setType(Dict.KF_APPROVAL);
                        addNewApp.setApplicant_id(applicant_id);
                        addNewApp.setUser_id(user_id);
                        addNewApp.setPhone_mac(user.getPhone_mac());
                        addNewApp.setPhone_type(user.getPhone_type());
                        addNewApp.setStatus(Dict.WAIT_APPROVE);
                        addNewApp.setCreate_time(CommonUtils.formatDate2(new Date(), CommonUtils.INPUT_DATE));
                        this.approvalService.addApprovalByUser(addNewApp);
                    }
                } else {
                    //都不是的时候，即没有记录的时候，新增一条
                    //新增一条kf开通 待审核记录
                    NetApproval addNewApp = new NetApproval();
                    addNewApp.setId(CommonUtils.createId());
                    addNewApp.setType(Dict.KF_APPROVAL);
                    addNewApp.setApplicant_id(applicant_id);
                    addNewApp.setUser_id(user_id);
                    addNewApp.setPhone_mac(user.getPhone_mac());
                    addNewApp.setPhone_type(user.getPhone_type());
                    addNewApp.setStatus(Dict.WAIT_APPROVE);
                    addNewApp.setCreate_time(CommonUtils.formatDate2(new Date(), CommonUtils.INPUT_DATE));
                    this.approvalService.addApprovalByUser(addNewApp);
                }
            }
            if (VMflag) { //虚机网络迁移
                /**
                 * 开通 已开通，网络迁移不止有一条
                 */
                if (user.getIs_spdb()) {
                    //是否是行内
                    //查询行内的VDI 开通 待审核
                    NetApproval spdbOpenWaitApp = new NetApproval();
                    spdbOpenWaitApp.setUser_id(user_id);
                    spdbOpenWaitApp.setType(Dict.VDI_APPROVAL);
                    spdbOpenWaitApp.setStatus(Dict.WAIT_APPROVE);
                    List<Map> spdbOpenWaitApps = this.approvalService.queryApproval(spdbOpenWaitApp);
                    //查询行内的VDI 开通  已开通，按当前提交的信息去查
                    NetApproval spdbOpenPassApp = new NetApproval();
                    spdbOpenPassApp.setUser_id(user_id);
                    spdbOpenPassApp.setType(Dict.VDI_APPROVAL);
                    spdbOpenPassApp.setStatus(Dict.PASSED);
                    spdbOpenPassApp.setVm_user_name(user.getVm_user_name());
                    spdbOpenPassApp.setVm_ip(user.getVm_ip());
                    List<Map> spdbOpenPassApps = this.approvalService.queryApproval(spdbOpenPassApp);

                    if (!CommonUtils.isNullOrEmpty(spdbOpenWaitApps)) {
                        //若存在VDI开通 待审核，对比迁移的信息（vm_ip、vm_name_name），不一致便修改，一致便不处理
                        if (!(spdbOpenWaitApps.get(0).get(Dict.VM_IP).equals(user.getVm_ip()) &&
                                spdbOpenWaitApps.get(0).get(Dict.VM_USER_NAME).equals(user.getVm_user_name()))) {
                            String spdvOpenWaitId = (String) spdbOpenWaitApps.get(0).get("id");
                            NetApproval updateApp = new NetApproval();
                            updateApp.setId(spdvOpenWaitId);
                            updateApp.setApplicant_id(applicant_id);
                            updateApp.setVm_ip(user.getVm_ip());
                            updateApp.setVm_user_name(user.getVm_user_name());
                            updateApp.setCreate_time(CommonUtils.formatDate2(new Date(), CommonUtils.INPUT_DATE));
                            this.approvalService.updateApprovalStatus(updateApp);
                        }

                    } else if (!CommonUtils.isNullOrEmpty(spdbOpenPassApps)) {
                        //若存在有VDI开通 已通过（按修改的参数），不做处理
                    } else {
                        //若都没有，则新增一个VDI开通 待审核
                        NetApproval addApp = new NetApproval();
                        addApp.setId(CommonUtils.createId());
                        addApp.setStatus(Dict.WAIT_APPROVE);
                        addApp.setType(Dict.VDI_APPROVAL);
                        addApp.setApplicant_id(applicant_id);
                        addApp.setUser_id(user_id);
                        addApp.setVm_ip(user.getVm_ip());
                        addApp.setVm_user_name(user.getVm_user_name());
                        addApp.setCreate_time(CommonUtils.formatDate2(new Date(), CommonUtils.INPUT_DATE));
                        this.approvalService.addApprovalByUser(addApp);
                    }
                } else {
                    //查询厂商的VM 开通 待审核
                    NetApproval noSpdbOpenWaitApp = new NetApproval();
                    noSpdbOpenWaitApp.setUser_id(user_id);
                    noSpdbOpenWaitApp.setType(Dict.VM_APPROVAL);
                    noSpdbOpenWaitApp.setStatus(Dict.WAIT_APPROVE);
                    List<Map> noSpdbOpenWaitApps = this.approvalService.queryApproval(noSpdbOpenWaitApp);

                    //查询厂商的VM 开通  已开通，按修改的信息查询
                    NetApproval noSpdbOpenPassApp = new NetApproval();
                    noSpdbOpenPassApp.setUser_id(user_id);
                    noSpdbOpenPassApp.setType(Dict.VM_APPROVAL);
                    noSpdbOpenPassApp.setStatus(Dict.PASSED);
                    noSpdbOpenPassApp.setVm_ip(user.getVm_ip());
                    noSpdbOpenPassApp.setVm_name(user.getVm_name());
                    noSpdbOpenPassApp.setVm_user_name(user.getVm_user_name());
                    List<Map> noSpdbOpenPassApps = this.approvalService.queryApproval(noSpdbOpenPassApp);

                    if (!CommonUtils.isNullOrEmpty(noSpdbOpenWaitApps)) {
                        //若存在VM开通 带审核，便修改
                        if (!(noSpdbOpenWaitApps.get(0).get(Dict.VM_IP).equals(user.getVm_ip()) &&
                                noSpdbOpenWaitApps.get(0).get(Dict.VM_USER_NAME).equals(user.getVm_user_name()) &&
                                noSpdbOpenWaitApps.get(0).get(Dict.VM_NAME).equals(user.getVm_name()))) {
                            String noSpdbOpenWaitId = (String) noSpdbOpenWaitApps.get(0).get("id");
                            NetApproval updateApp = new NetApproval();
                            updateApp.setId(noSpdbOpenWaitId);
                            updateApp.setApplicant_id(applicant_id);
                            updateApp.setVm_ip(user.getVm_ip());
                            updateApp.setVm_user_name(user.getVm_user_name());
                            updateApp.setVm_name(user.getVm_name());
                            updateApp.setCreate_time(CommonUtils.formatDate2(new Date(), CommonUtils.INPUT_DATE));
                            this.approvalService.updateApprovalStatus(updateApp);
                        }
                    } else if (!CommonUtils.isNullOrEmpty(noSpdbOpenPassApps)) {
                        //若存在VM开通 已开通（按VM开通参数查找）,便不做处理
                    } else {
                        //都不存在（包括），直接新增一条
                        NetApproval addApp = new NetApproval();
                        addApp.setId(CommonUtils.createId());
                        addApp.setStatus(Dict.WAIT_APPROVE);
                        addApp.setType(Dict.VM_APPROVAL);
                        addApp.setApplicant_id(applicant_id);
                        addApp.setUser_id(user_id);
                        addApp.setVm_ip(user.getVm_ip());
                        addApp.setVm_user_name(user.getVm_user_name());
                        addApp.setVm_name(user.getVm_name());
                        addApp.setCreate_time(CommonUtils.formatDate2(new Date(), CommonUtils.INPUT_DATE));
                        this.approvalService.addApprovalByUser(addApp);
                    }
                }
            }
        }
        //更新用户
        userService.updateUser(user);
        //清除缓存
        userService.removeCache(user.getUser_name_en());
        userService.removeCache(user.getId());
        //如修改了用户的角色信息，则将被修改用户下线，以刷新菜单
        if(!CommonUtils.isNullOrEmpty(u.get(0).get(Dict.ROLE_ID)) && !u.get(0).get(Dict.ROLE_ID).equals(user.getRole_id())){
            redisTemplate.opsForValue().set(user.getUser_name_en() + "user.login.token",null);
            logger.info("角色菜单更改，将用户"+user.getUser_name_en()+"强制下线！");
        }
        //用户离职信息同步到ftms玉衡测试管理平台
        if (!CommonUtils.isNullOrEmpty(user.getStatus())) {
            ftmsUserService.isLeave(user.getUser_name_en(), user.getStatus());
        }
        //组装小组等信息
        User user3 = new User();
        user3.setUser_name_en(user.getUser_name_en());
        List<Map> list = userService.queryUser(user3);
        if (CommonUtils.isNullOrEmpty(list)) {
            throw new FdevException(ErrorConstants.USR_NOT_EXIST);
        }
        return JsonResultUtil.buildSuccess(list.get(0));
    }

    /*更新用户的玉衡角色、用户测试等级、mantis密钥*/
    @ApiOperation(value = "更新用户的玉衡角色、用户测试等级、mantis密钥")
    @RequestMapping(value = "/updateUserFtms", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult updateUserFtms(@RequestBody @ApiParam(name = "需要修改的用户数据") Map<String, Object> requestParam) throws Exception {
        Map user = userService.updateUserFtms(requestParam);
        return JsonResultUtil.buildSuccess(user);
    }

    /* 修改用户第一次登录的状态 */
    @ApiOperation(value = "修改用户第一次登录的状态,0为首次登录进入密码修改状态,1为不是首次登录状态,2为首次登录进入新手指引页面状态")
    @RequestMapping(value = "/updateUserIsOnceLogin", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult updateUserIsOnceLogin(HttpServletRequest request) throws Exception {
        //权限修改用户信息
        String token = request.getHeader(Dict.AUTHORIZATION);
        String userNameEn = tokenManger.getUserByToken(token);
        User user1 = new User();
        user1.setUser_name_en(userNameEn);
        List<Map> res = userService.queryUser(user1);
        if (CommonUtils.isNullOrEmpty(res)) {
            throw new FdevException(ErrorConstants.USR_NOT_EXIST);
        }
        //更新用户登录状态
        if (!"3".equals(res.get(0).get(Dict.IS_ONCE_LOGIN))) {
            user1.setIs_once_login("3");//不是首次登陆进入新手指引页面状态
            user1.setUser_name_en((String) res.get(0).get(Dict.USER_NAME_EN));
            User user2 = userService.updateUser(user1);
            res = userService.queryUser(user2);
        }
        return JsonResultUtil.buildSuccess(res);
    }

    /* 按条件查询用户,组合条件:“用户名” “英文简称” “小组” “公司”，随意组合 */
    @ApiOperation(value = "查询用户(关联小组、公司、角色等详细信息）")
    @RequestMapping(value = "/query", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryUserByTerm(@RequestBody @ApiParam(name = "查询用户参数", value = "示例：可输入任意参数 {}") User user)
            throws Exception {
        List<Map> users = userService.queryUser(user);
        return JsonResultUtil.buildSuccess(resetFieldOfUsers(users, "password"));
    }

    private List<Map> resetFieldOfUsers(List<Map> users, String field) throws Exception {
        for (Map user : users) {
            user.put(field, null);
        }
        return users;
    }

    /* 按条件查询用户,组合条件:“用户名” “英文简称” “小组” “公司”，“用户状态（0为在职）”随意组合 */
    @ApiOperation(value = "查询用(不关联小组、公司、角色等详细信息，只获取用户表数据）")
    @RequestMapping(value = "/queryUserCoreData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryUserCoreData(@RequestBody @ApiParam(name = "查询用户参数", value = "示例：可输入任意参数 {}") User user)
            throws Exception {
        List<Map> users = userService.queryUserCoreData(user);
        Map allGroupName = groupService.queryAllGroupName();
        for(Map u:users){
            u.put("groupName",allGroupName.get(u.get(Dict.GROUP_ID)));
        }
        return JsonResultUtil.buildSuccess(users);
    }

    /* 根据ids或usernameEns集合查询多个用户的一级数据 */
    @ApiOperation(value = "根据ids或usernameEns集合查询多个用户的一级数据")
    @RequestMapping(value = "/queryByUserCoreData", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryUserByUserList
    (@RequestBody @ApiParam(name = "查询用户参数", value = "示例：可输入多个用户ID ") Map<String, Object> requestParam)
            throws Exception {
        List<String> paramList = new ArrayList<String>();
        if (requestParam.containsKey("ids")) {
            paramList = JSONArray.fromObject(requestParam.get("ids"));
        } else if (requestParam.containsKey("usernameEns")) {
            paramList = JSONArray.fromObject(requestParam.get("usernameEns"));
        } else {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"key解析错误"});
        }
        HashMap map = new HashMap<>();
        if (CommonUtils.isNullOrEmpty(paramList)) {
            return JsonResultUtil.buildSuccess(null);
        }
        for (String param : paramList) {
            if (CommonUtils.isNullOrEmpty(param)) {
                map.put(param, null);
                continue;
            }
            User user = new User();
            if (requestParam.containsKey("ids")) {
                user.setId(param);
            } else if (requestParam.containsKey("usernameEns")) {
                user.setUser_name_en(param);
            } else {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"key解析错误"});
            }
            //查询用户核心数据
            List<Map> users = userService.queryUserCoreData(user);
            if (CommonUtils.isNullOrEmpty(users)) {
                map.put(param, null);
                continue;
            }
            if (requestParam.containsKey("ids")) {
                map.put(users.get(0).get(Dict.ID), users.get(0));
            } else if (requestParam.containsKey("usernameEns")) {
                map.put(users.get(0).get(Dict.USER_NAME_EN), users.get(0));
            } else {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"key解析错误"});
            }
        }
        ;
        return JsonResultUtil.buildSuccess(map);

    }

    @ApiOperation(value = "删除用户")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult deleteUserByNameEn(
            @RequestBody @ApiParam(name = "删除用户参数", value = "示例：可输入任意参数 [{ \"user_name_en\": \"test\" }]") List<User> userList,
            HttpServletRequest request) throws Exception {
//        //获取当前登录用户
        String userNameEn2 = tokenManger.getUserByToken(request.getHeader(Dict.AUTHORIZATION));
        User user2 = new User();
        user2.setUser_name_en(userNameEn2);
        List<Map> res = userService.queryUser(user2);
        if (CommonUtils.isNullOrEmpty(res)) {
            throw new FdevException(ErrorConstants.USR_NOT_EXIST);
        }
        String userNameEn = (String) res.get(0).get(Dict.USER_NAME_EN);
        if (!roleService.checkRole(null,Constants.SUPER_MANAGER)) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"不是admin用户或超级管理员, 无权进行此操作!"});
        }
        long result = 0;
        for (User user : userList) {
            if (CommonUtils.isNullOrEmpty(user.getUser_name_en())) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"用户名为空"});
            }
            User user4 = new User();
            user4.setUser_name_en(user.getUser_name_en());
            List<Map> u = userService.queryUser(user4);
            if (CommonUtils.isNullOrEmpty(u)) {
                throw new FdevException(ErrorConstants.USR_NOT_EXIST);
            }
            Map requ = u.get(0);
            if (!user.getUser_name_en().equals(requ.get(Dict.USER_NAME_EN))) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"用户名为空"});
            }
            if (user.getUser_name_en().equals(userNameEn)) {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"不能删除自己"});
            }
            if (Dict.ADMIN.equals(user.getUser_name_en())) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"不能删除超管"});
            }
            if ((roleService.checkRole(null, Constants.GROUP_MANAGER) || roleService.checkRole(null, Constants.TEAM_LEADER))
                    && !roleService.checkRole(null,Constants.SUPER_MANAGER)) {
                if (roleService.checkRole((String)requ.get(Dict.USER_NAME_EN), Constants.GROUP_MANAGER) || roleService.checkRole(null, Constants.TEAM_LEADER)) {
                    throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"不能删除小组管理员和团队负责人"});
                }
            }
            if (!havePower(res.get(0), user, Dict.OLD)) {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"删除没有权限"});
            }
            result += userService.delUserByNameEn(user.getUser_name_en());
            //清除缓存
            userService.removeCache(user.getId());
            userService.removeCache(user.getUser_name_en());
        }
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * @param loginUser 当前登录人员token</br>
     * @param user      操作的用户数据, user中需要传 userNameEn ，groupID，</br>
     * @return true 有权限，false权限不足</br>
     * @throws Exception
     * @ 获取当前登陆用户，判断该用户是否有越权行为 @ 小组管理员能对本组人员和子组人员进行操作； @ 普通人员只能对自己修改；
     */
    private boolean havePower(Map loginUser, User user, String method) throws Exception {
        if (!CommonUtils.isNullOrEmpty(loginUser)) {
            String groupId = (String) loginUser.get(Dict.GROUP_ID);
            String userNameEn = (String) loginUser.get(Dict.USER_NAME_EN);// 当前登录用户名称
            //查询预修改用户的之前组
            User oldUser = new User();
            oldUser.setUser_name_en(user.getUser_name_en());
            List<Map> result = userService.queryUser(oldUser);
            //当前组和子组
            List<String> arrayList = new ArrayList<>();
            Group group = new Group();
            group.setId(groupId);
            List<Group> groupAndChildGroups = groupService.queryChildGroupById(group);
            for (Group Child : groupAndChildGroups) {
                if (!arrayList.contains((String) Child.getId())) {
                    arrayList.add((String) Child.getId());
                }
            }
            if(roleService.checkRole(null,Constants.SUPER_MANAGER) || roleService.checkRole(null,Constants.USER_MANAGER)){
                return true;
            }else if(roleService.checkRole(null,Constants.GROUP_MANAGER) || roleService.checkRole(null,Constants.TEAM_LEADER)){
                if (Dict.OLD.equals(method))
                    return arrayList.contains((String) result.get(0).get(Dict.GROUP_ID)) && (!roleService.checkRole(user.getUser_name_en(),Constants.SUPER_MANAGER));
                return arrayList.contains(user.getGroup_id()) && (!roleService.checkRole(user.getUser_name_en(),Constants.SUPER_MANAGER));
            }else {
                return userNameEn.equals(user.getUser_name_en()) && groupId.equals(user.getGroup_id());
            }
        }
        return false;
    }

    /**
     * @ 判断，公司，小组，角色，标签是否存在</br>
     * @ 全都存在返回 true,否则返回false
     */
    private boolean checkEntity(String companyId, String groupId, List<String> roleIds,
                                List<String> labels) throws Exception {
        Company com = new Company();
        com.setId(companyId);
        if (CommonUtils.isNullOrEmpty(companyService.getCompany(com)))
            return false;
        Group group = new Group();
        group.setId(groupId);
        if (CommonUtils.isNullOrEmpty(groupService.queryGroup(group)))
            return false;
        Role role = new Role();
        for (String roleId : roleIds) {
            role.setId(roleId);
            if (CommonUtils.isNullOrEmpty(roleService.queryRole(role)))
                return false;
        }
        Label label = null;
        for (Iterator<String> its = labels.iterator(); its.hasNext(); ) {
            label = new Label(its.next(), null, null, null);
            if (CommonUtils.isNullOrEmpty(labelService.queryLabel(label)))
                return false;
        }
        return true;
    }

    @ApiOperation(value = "获取当前用户")
    @RequestMapping(value = "/currentUser", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult currentUser(ServletRequest servletRequest) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String token = request.getHeader(Dict.AUTHORIZATION);
        if (CommonUtils.isNullOrEmpty(token)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"token为空"});
        }
        String userNameEn = tokenManger.getUserByToken(token);
        User user = new User();
        user.setUser_name_en(userNameEn);
        List<Map> users = userService.queryUser(user);
        if (CommonUtils.isNullOrEmpty(users)) {
            throw new FdevException(ErrorConstants.USR_NOT_EXIST);
        }
        return JsonResultUtil.buildSuccess(users.get(0));
    }

    @ApiOperation(value = "模糊查询")
    @RequestMapping(value = "/getUserByName", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult getUserByName(
            @RequestBody Map request)
            throws Exception {
        List<Map> users;
        String nameCn = (String) request.get(Dict.USER_NAME_CN);
        if (CommonUtils.isNullOrEmpty(nameCn))
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"姓名不能为空"});
        users = userService.getUserByName(nameCn);
        return JsonResultUtil.buildSuccess(users);
    }

    @ApiOperation(value = "将库中的git_user同步更新为 gitlab中的username ")
    @RequestMapping(value = "/refreshGitUser", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult refershGitlabUsername() {
        logger.info("定时更新git_user,当前时间:{}", CommonUtils.formatDate2(new Date(), "yyyy-MM-dd HH:mm:ss"));
        User user = new User();
        user.setStatus("0");
        List<Map> userList = new ArrayList<>();
        try {
            userList = userService.queryUser(user);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Map map : userList) {
            String git_user_id = (String) map.get(Dict.GIT_USER_ID);
            String user_name_en = (String) map.get(Dict.USER_NAME_EN);
            String user_name_cn = (String) map.get(Dict.USER_NAME_CN);
            String git_user = (String) map.get(Dict.GIT_USER);
            try {
                String gitlabUsername = gitlabService.queryGitlabUsername(git_user_id);
                if (CommonUtils.isNullOrEmpty(git_user) || !git_user.equals(gitlabUsername)) {
                    //同步用户gitlabel帐号
                    userService.updateGitUser(user_name_en, gitlabUsername);
                }
            } catch (Exception e) {
                logger.error("同步gitlab中的username失败,gitlabel用户id:{},gitlabel帐号:{},用户名:{},用户中文名称:{}", git_user_id, git_user, user_name_en, user_name_cn);
            }
        }

        return JsonResultUtil.buildSuccess();
    }


    @ApiOperation(value = "从配置文件中指定的6个用户中获取在职人员信息")
    @RequestMapping(value = "/getJobUser", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult getJobUser() {
        String[] userlist = joblist.split("\\|");
        List<User> users = new ArrayList<>();
        for (String name_en : userlist) {
            User user = new User();
            try {
                user = userService.getIsJobUser(name_en, "0");
            } catch (Exception e) {
                logger.error("配置中的用户名查询异常,用户名:{}", name_en);
            }
            users.add(user);
        }

        return JsonResultUtil.buildSuccess(users);
    }

    @ApiOperation(value = "查询浦发行内所属地域")
    @RequestMapping(value = "/queryArea", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryArea(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String area_id = requestParam.get(Dict.AREA_ID);
        List areaList = userService.queryArea(area_id);
        return JsonResultUtil.buildSuccess(areaList);
    }

    @ApiOperation(value = "查询人员职能")
    @RequestMapping(value = "/queryfunction", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryfunction(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String function_id = requestParam.get(Dict.FUNCTION_ID);
        List functionList = userService.queryfunction(function_id);
        return JsonResultUtil.buildSuccess(functionList);
    }

    @ApiOperation(value = "查询职级")
    @RequestMapping(value = "/queryrank", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult queryrank(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String rank_id = requestParam.get(Dict.RANK_ID);
        List rankList = userService.queryrank(rank_id);
        return JsonResultUtil.buildSuccess(rankList);
    }

    @ApiOperation(value = "项目组资源统计表查询")
    @RequestMapping(value = "/queryUserStatis", method = RequestMethod.POST)
    public JsonResult queryUserStatis(@RequestBody Map requestParam) throws Exception {
        List<String> ids = (List<String>) requestParam.get(Dict.IDS);
        boolean includeChild = (boolean) requestParam.get("isIncludeChildren");
        Map result = userService.queryUserStatis(ids, includeChild);
        return JsonResultUtil.buildSuccess(result);
    }

    @ApiOperation(value = "根据小组id查询人员数量")
    @RequestMapping(value = "/queryTaskNumByGroup", method = RequestMethod.POST)
    public JsonResult queryTaskNumByGroup(@RequestBody @ApiParam Map<String, Object> request) throws Exception {
        List<String> ids = (List<String>) request.get("ids");
        boolean isParent = (boolean) request.get("isParent");
        Company com = new Company();
        com.setStatus("1");
        List<Company> companies = companyService.getCompany(com);
        Map<String, Object> group_object = new HashMap<>();
        group_object.put("companies", companies);
        if (ids.size() == 0) {
            group_object.put("groups", "");
            return JsonResultUtil.buildSuccess(group_object);
        }

        User user = new User();
        user.setStatus("0");
        List<Map> listmap = userService.queryUserCoreData(user);
        List<String> userids = new ArrayList<>();
        for (Map map : listmap) {
            userids.add(map.get(Dict.ID).toString());
        }
        //获取在职人员的开发任务数量
        Map<String, Object> developList = userService.queryTaskNum(userids, Arrays.asList("developer"));
        //获取在职人员的总任务数量
        Map<String, Object> developAllList = userService.queryTaskNum(userids, Arrays.asList("master",
                "spdb_master", "tester", "developer", "creator"));

        List<Map<String, Object>> group_list = new ArrayList<>();
        for (String groupId : ids) {
            Map<String, Object> group_map = new HashMap<>();
            Group groupDetail = groupService.queryDetailById(groupId);
            // 小组名称
            group_map.put(Dict.NAME, groupDetail.getName());
            Set<String> child_group_ids = new HashSet<>();
            child_group_ids.add(groupId);
            if (isParent) {
                Group group = new Group();
                group.setId(groupId);
                List<Group> child_group = groupService.queryChildGroupById(group);
                for (Group g : child_group) {
                    child_group_ids.add(g.getId());
                }
            }
            Map<String, String> companyDetail = new HashMap<>();
            for (Company company : companies) {
                List<User> list = userService.queryUserByCompanyGroup("0", company.getId(), child_group_ids);
                int develop = 0;
                int develop_all = 0;
                for (User user1 : list) {
                    for (Map.Entry entry : developList.entrySet()) {
                        if (user1.getId().equals(entry.getKey())) {
                            Map<String, Object> developDetail2 = (Map<String, Object>) developList.get(user1.getId());
                            Integer total = (Integer) developDetail2.get("total");
                            if (total == 0) {
                                develop++;
                            }
                        }
                    }

                    for (Map.Entry entry : developAllList.entrySet()) {
                        if (user1.getId().equals(entry.getKey())) {
                            Map<String, Object> developDetail = (Map<String, Object>) developAllList.get(user1.getId());
                            Integer total = (Integer) developDetail.get("total");
                            if (total == 0) {
                                develop_all++;
                            }
                        }
                    }
                }
                companyDetail.put(company.getName(), list.size() + "/" + develop + "/" + develop_all);
                group_map.put(Dict.COMPANY, companyDetail);
            }
            group_list.add(group_map);
        }
        group_object.put("groups", group_list);
        return JsonResultUtil.buildSuccess(group_object);
    }

    /**
     * 用户列表查询
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "用户列表查询（条件筛选、分页）")
    @RequestValidate(NotEmptyFields = {Dict.PAGE, Dict.PER_PAGE})
    @RequestMapping(value = "/queryUser", method = RequestMethod.POST)
    public JsonResult queryUser(@RequestBody Map<String, Object> requestParam) throws Exception {
        List<String> search = (List<String>) requestParam.get(Dict.SEARCH);
        String companyId = (String) requestParam.get(Dict.COMPANY_ID);
        String groupId = (String) requestParam.get(Dict.GROUP_ID);
        String status = (String) requestParam.get(Dict.STATUS);
        int page = (int) requestParam.get(Dict.PAGE);
        int per_page = (int) requestParam.get(Dict.PER_PAGE);
        String labelId = (String) requestParam.get(Dict.LABELID);
        String is_party_member = (String)requestParam.get(Dict.IS_PARTY_MEMBER);
        String  area_id = (String)requestParam.get(Dict.AREA_ID);
        String function_id = (String)requestParam.get(Dict.FUNCTION_ID);
        String section = (String)requestParam.get(Dict.SECTION);
        Map<String, Object> Userlist = userService.queryUserBySearch(search, companyId, groupId, status, page, per_page, labelId,is_party_member,area_id,function_id,section);
        return JsonResultUtil.buildSuccess(Userlist);
    }

    @ApiOperation(value = "Ldap身份认证")
    @RequestValidate(NotEmptyFields = {Dict.USERNAME, Dict.PASSWORD})
    @RequestMapping(value = "/test", method = RequestMethod.POST)
    public JsonResult queryTest(@RequestBody Map<String, Object> requestParam) throws Exception {
        String username = (String) requestParam.get("username");
        String password = (String) requestParam.get("password");
        return JsonResultUtil.buildSuccess(userService.authenticate(username, password));
    }

    @ApiOperation(value = "根据指定组查询组下的（包含子组）总人数和组内行内行外开发和测试人员数量（未使用）")
    @RequestMapping(value = "/queryUserNumByGroup", method = RequestMethod.POST)
    public JsonResult queryUserNumByGroup(@RequestBody Map<String, Object> requestParam) throws Exception {
        List<String> groupIds = (List<String>) requestParam.get("groupIds");
        List<Map> result = userService.queryUserNumByGroup(groupIds);
        return JsonResultUtil.buildSuccess(result);
    }

    @ApiOperation(value = "根据用户id来查询获取用户的email")
    @RequestMapping(value = "/getEmail", method = RequestMethod.POST)
    public JsonResult getEmail(@RequestBody Map<String, Object> requestParam) throws Exception {
        List<String> ids = (List<String>) requestParam.get(Dict.IDS);
        List<Map> result = userService.queryEmailByUserIds(ids);
        return JsonResultUtil.buildSuccess(result);
    }

    @ApiOperation(value = "根据用户名更新gitToken")
    @RequestMapping(value = "/updateGitToken", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.USER_NAME_EN, Dict.GIT_TOKEN})
    public JsonResult updateGitToken(@RequestBody Map requestMap) throws Exception {
        String userNameEn = (String)requestMap.get(Dict.USER_NAME_EN);
        String gitToken = (String)requestMap.get(Dict.GIT_TOKEN);
        //判断access token正确性
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.TOKEN, gitToken);
        map.put(Dict.REST_CODE, "checkGitlabToken");
        Boolean check = (Boolean) restTransport.submit(map);
        if (check == false) {
            logger.error("gitlab token wrong");
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"access token有误,保证与gitlab账号一一对应！获取"
                    + "地址为: http://xxx/profile/personal_access_tokens"});
        }
        return JsonResultUtil.buildSuccess(userService.updateGitToken(userNameEn, gitToken));
    }

    @ApiOperation(value = "批量调整人员组别，可调整的人员列表")
    @PostMapping(value = "/canAddUserList")
    @ResponseBody
    public JsonResult canAddUserList() throws Exception {
        return JsonResultUtil.buildSuccess(userService.canAddUserList());
    }

    @ApiOperation(value = "根据ids批量获取用户信息(list格式返回)")
    @PostMapping(value = "/getUsersInfoByIds")
    @ResponseBody
    public JsonResult getUsersInfoByIds(@RequestBody Map requestMap){
        return JsonResultUtil.buildSuccess(userService.getUsersInfoByIds(requestMap));
    }

    @ApiOperation(value = "查询所有用户信息（包括角色）")
    @PostMapping(value = "/getAllUserAndRole")
    @ResponseBody
    public JsonResult getAllUserAndRole(@RequestBody Map requestMap){
        return JsonResultUtil.buildSuccess(userService.getAllUserAndRole(requestMap));
    }
}
