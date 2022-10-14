package com.spdb.fdev.fuser.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fuser.base.dict.Constants;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.dict.ErrorConstants;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.base.utils.DES3;
import com.spdb.fdev.fuser.base.utils.MD5;
import com.spdb.fdev.fuser.base.utils.Validator;
import com.spdb.fdev.fuser.spdb.Token.TokenManger;
import com.spdb.fdev.fuser.spdb.dao.LdapUserDao;
import com.spdb.fdev.fuser.spdb.entity.user.OAuth;
import com.spdb.fdev.fuser.spdb.entity.user.User;
import com.spdb.fdev.fuser.spdb.service.GroupService;
import com.spdb.fdev.fuser.spdb.service.MailService;
import com.spdb.fdev.fuser.spdb.service.OAuthService;
import com.spdb.fdev.fuser.spdb.service.UserService;
import com.spdb.fdev.transport.RestTransport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
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
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Api(tags = "认证接口")
@RestController
@RequestMapping("/api/auth")
@RefreshScope
public class AuthController {
    private Logger logger = LoggerFactory.getLogger(this.getClass()); // 控制台日志打印
    @Resource
    private UserService userService;

    @Resource
    private GroupService groupService;

    @Value("${server.login.path}")
    private String path;

    @Value("${server.oauth.md5.key}")
    private String oauth2Key;

    @Value("${server.oauth.path}")
    private String oauthUrl;

    @Value("${send.email.verfityCode}")
    private String vCode;

    @Resource
    private RestTransport restTransport;
    @Resource
    private TokenManger tokenManger;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private OAuthService oAuthService;
    @Resource
    private MailService mailService;
    @Resource
    private LdapUserDao ldapUserDao;
    @Resource
    private DES3 des3;

    // 用户认证
    @ApiOperation(value = "用户认证")
    @RequestMapping(value = "/checkUser", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult checkUser(@RequestBody String authorization) throws Exception {
        if (!tokenManger.checkToken(authorization)) {
            // return CommonUtils.ERROR();
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"token错误"});
        }
        // 解析登录凭证，获取用户ID，并查询用户信息返回
        String userNameEn = tokenManger.getUserByToken(authorization);
        User user = new User();
        user.setUser_name_en(userNameEn);
        List<Map> users = userService.queryUser(user);
        Map userInfo = new HashMap();
        userInfo.putAll(users.get(0));
        Map map = users.get(0);
        Iterator iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            if (Dict.GIT_USER_ID.equals(key) || Dict.CREATE_DATE.equals(key) || Dict.LEAVE_DATE.equals(key) || Dict.AREA_ID.equals(key)
                    || Dict.FUNCTION_ID.equals(key) || Dict.RANK_ID.equals(key) || Dict.EDUCATION.equals(key) || Dict.START_TIME.equals(key)
                    || Dict.REMARK.equals(key) || Dict.REDMINE_USER.equals(key) || Dict.SVN_USER.equals(key) || Dict.AREA.equals(key)
                    || Dict.FUNCTION.equals(key) || Dict.RANK.equals(key) || Dict.KF_APPROVAL.equals(key) || Dict.NET_MOVE.equals(key)
                    || Dict.IS_SPDB.equals(key) || Dict.VM_IP.equals(key) || Dict.VM_NAME.equals(key) || Dict.VM_USER_NAME.equals(key)
                    || Dict.PHONE_TYPE.equals(key) || Dict.PHONE_MAC.equals(key) || Dict.DEVICE_NO.equals(key) || Dict.IS_SPDB_MAC.equals(key)
                    || Dict.FTMS_LEVEL.equals(key) || Dict.MANTIS_TOKEN.equals(key) || Dict.CREATETIME.equals(key) || Dict.UPDATETIME.equals(key)
                    ||Dict.IS_PARTY_MEMBER.equals(key) || Dict.SECTION.equals(key) || Dict.WORK_NUM.equals(key) || Dict.IS_MANAGE_GROUP.equals(key) || Dict.SECTIONINFO.equals(key)) {
                iterator.remove();
            }
        }
        users.add(userInfo);
        return JsonResultUtil.buildSuccess(users);
    }

    /**
     * 登入
     *
     * @param requestMap
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "登入")
    @ApiImplicitParam(name = "user", value = "例如:{\"user_name_en\":\"xxx\",\"password\":\"xxx\"}")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public JsonResult login(@RequestBody Map requestMap) throws Exception {
        //获取用户名密码
        String userNameEn = (String) requestMap.get(Dict.USER_NAME_EN);
        String passWd = (String) requestMap.get(Dict.PASSWORD);
        // 参数非空校验
        if (CommonUtils.isNullOrEmpty(userNameEn) || CommonUtils.isNullOrEmpty(passWd)) {
            logger.error("account or password is empty");
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"帐号或密码为空"});
        }
        // 查询用户是否存在，不区分大小写
        User user = new User();
        user.setUser_name_en(userNameEn);
        List<Map> list = userService.queryUserIgnoreCase(user);
        //如果是admin登陆，则不校验ldap，仅验密
        if (Dict.ADMIN.equals(userNameEn) && !CommonUtils.isNullOrEmpty(list)) {
            return JsonResultUtil.buildSuccess(adminLogin(list, passWd));
        }
        boolean spdbres = userService.authenticate(userNameEn, passWd);// 行内用户ldap认证
        boolean res = userService.authenticateManu(userNameEn, passWd);// 厂商用户ldap认证
        if (!res && !spdbres) {
            //行内和厂商的ldap认证均未通过
            logger.error("ldap authenticate fail");
            throw new FdevException(ErrorConstants.USR_INCORRECT_USERNAME_PASSWORD, new String[]{"请使用虚拟机账号密码登入"});
        }
        // 首次登陆需跳转信息补录页面
        if (CommonUtils.isNullOrEmpty(list)) {
            Map<String, Object> firstLogin = new HashMap<>();
            firstLogin.put(Dict.USERNAME, userNameEn);
            firstLogin.put("pass_wd", passWd);
            firstLogin.put(Dict.IS_ONCE_LOGIN, Constants.IS_ONCE_LOGIN);//此处返回4供前端判断跳转页面
            firstLogin.put(Dict.TOKEN, getTokenAndSetRedis(userNameEn));
            firstLogin.put(Dict.IS_SPDB, spdbres);
            firstLogin.put(Dict.STATUS, "0");
            return JsonResultUtil.buildSuccess(firstLogin);
        }
        //fdev存在用户信息
        Map result = list.get(0);
        userNameEn = (String)result.get(Dict.USER_NAME_EN);
        // 若用户离职，则报错
        if (Dict.DIMISSION.equals(result.get(Dict.STATUS))) {
            logger.error("user already leave");
            throw new FdevException(ErrorConstants.USR_LOGIN_DIMISSION_ERROR);
        }
        // 获取老密码，如果老密码和新密码不一致，那么更新fdev用户密码
        String password = (String) result.get(Dict.PASSWORD);
        if (!password.equals(des3.encrypt(passWd)) && !password.equals(MD5.encoder(userNameEn, passWd))) {
            userService.updatePassword(userNameEn, des3.encrypt(passWd));
        }
        result.put(Dict.TOKEN, getTokenAndSetRedis(userNameEn));
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * admin登陆
     *
     * @param list
     * @param passWd
     * @return
     * @throws Exception
     */
    private Map adminLogin(List<Map> list, String passWd) throws Exception {
        Map result = list.get(0);
        String userNameEn = (String) result.get(Dict.USER_NAME_EN);
        String password = (String) result.get(Dict.PASSWORD);
        //fdev用户验密
        if (!password.equals(des3.encrypt(passWd)) && !password.equals(MD5.encoder(userNameEn, passWd))) {
            logger.error("password not correct");
            throw new FdevException(ErrorConstants.USR_PASSWORD_ERROR);
        }
        result.put(Dict.TOKEN, getTokenAndSetRedis(userNameEn));
        return result;
    }

    /**
     * 根据用户名生成token并存入redis并返回token
     *
     * @param userNameEn
     * @return
     * @throws Exception
     */
    private String getTokenAndSetRedis(String userNameEn) throws Exception {
        String tokenPre = userNameEn + "-" + System.currentTimeMillis();
        String token = tokenManger.createToken(tokenPre);
        redisTemplate.opsForValue().set(userNameEn + "user.login.token", token, 7, TimeUnit.DAYS);
        return token;
    }


    //忘记密码,给用户邮箱发送验证码
    @ApiOperation(value = "发送验证码")
    @ApiImplicitParam(name = "object", value = "例如:{\"emile\":\"xxx\"}")
    @RequestMapping(value = "/getVerifyCode", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult getVerifyCode(@RequestBody Map map) throws Exception {
        String email = (String) map.get(Dict.EMAIL);
        if (CommonUtils.isNullOrEmpty(email)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"邮箱为空"});
        }
        User user = new User();
        user.setEmail(email);
        List<Map> maps = userService.queryUser(user);
        if (CommonUtils.isNullOrEmpty(maps)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"邮箱不存在"});
        }
        if ("0".equals((String) maps.get(0).get(Dict.IS_ONCE_LOGIN))) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"未完成首次登入"});
        }
        //生成验证码
        String str = (String) redisTemplate.opsForValue().get(vCode + email);
        if (str == null) {
            getVerfityToSendEmail(email, maps);
        } else {
            // 当已存在验证码，用户发接口重新获取，将之前的删掉，重新生成。
            redisTemplate.delete(vCode + email);
            getVerfityToSendEmail(email, maps);
        }
        return JsonResultUtil.buildSuccess(null);
    }

    public void getVerfityToSendEmail(String email, List<Map> maps) throws Exception {
        String verfityCode = CommonUtils.uuid();
        redisTemplate.opsForValue().set(vCode + email, verfityCode, 3, TimeUnit.MINUTES);
        HashMap model = new HashMap();
        model.put(Dict.USER_NAME, maps.get(0).get(Dict.USER_NAME_EN));
        model.put(Dict.CODE, verfityCode);
        mailService.sendEmail(Constants.EMAIL_FORGETPW, Constants.TEMPLATE_FORGETPW, model, email);
    }

    //忘记密码,修改密码
    @ApiOperation(value = "忘记密码")
    @ApiImplicitParam(name = "object", value = "例如:{\"password\":\"xxx\",\"emile\":\"xxx\",\"code\":\"adcv\"}")
    @RequestMapping(value = "/forgetPassWord", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult forgetPassWord(@RequestBody Map map) throws Exception {
        if (CommonUtils.checkObjFieldIsNull(map)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"有空填项"});
        }
        String email = (String) map.get(Dict.EMAIL);
        String code = (String) map.get(Dict.CODE);
        String newPassWord = (String) map.get(Dict.PASSWORD);
        if (!Validator.isPassWord(newPassWord)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.PASSWORD, "数字，字母，符号至少任意两者组合，不少于8位数"});
        }
        //验证码校验
        String verfityCode = (String) redisTemplate.opsForValue().get(vCode + email);
        if (verfityCode == null) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"邮箱错误或验证码失效"});
        }
        if (!verfityCode.equalsIgnoreCase(code)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"验证码错误"});
        }
        //验证成功后删除验证码
        redisTemplate.delete(vCode + email);
        User user = new User();
        user.setEmail(email);
        List<Map> maps = userService.queryUser(user);
        if (CommonUtils.isNullOrEmpty(maps)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"邮箱不存在"});
        }
        user.setUser_name_en((String) maps.get(0).get(Dict.USER_NAME_EN));
        user.setUser_name_cn((String) maps.get(0).get(Dict.USER_NAME_CN));
        user.setPassword(des3.encrypt(newPassWord));
        User newUser = userService.updateUser(user);
        // 修改密码后删除旧token
        redisTemplate.delete(newUser.getUser_name_en() + "user.login.token");
        return JsonResultUtil.buildSuccess(null);

    }

    // 修改密码
    @ApiOperation(value = "修改密码")
    @ApiImplicitParam(name = "object", value = "例如:{\"password\":\"xxx\",\"gitLable\":\"a1aEQCizUD5VDYXWpojh\"}")
    @RequestMapping(value = "/onceLogin", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult onceLogin(ServletRequest servletRequest, @RequestBody Map object) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String token = request.getHeader(Dict.AUTHORIZATION);
        String newPassWord = (String) object.get(Dict.PASSWORD);
        String gitLable = (String) object.get(Dict.GIT_TOKEN);
        if (CommonUtils.isNullOrEmpty(newPassWord))
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"密码不能为空"});
        if (!Validator.isPassWord(newPassWord)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.PASSWORD, "数字，字母，符号至少任意两者组合，不少于8位数"});
        }
        if (null == token || "".equals(token)) {
            throw new FdevException("用户未登录");
        }
        // 解析登录凭证，获取用户ID，查询用户信息
        String userNameEn1 = tokenManger.getUserByToken(token);
        User reqUser = new User();
        reqUser.setUser_name_en(userNameEn1);
        List<Map> users = userService.queryUser(reqUser);
        if (CommonUtils.isNullOrEmpty(users)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{Dict.PASSWORD, "用户不存在"});
        }
        List<String> groupids = groupService.queryGroupByNames(Constants.TESTCENTRE, Constants.BUSINESSDEPT, Constants.PLANDEPT);
        if (!groupids.contains(users.get(0).get("group_id")) && CommonUtils.isNullOrEmpty(gitLable))
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"gitLable不能为空"});

        Map getUser = users.get(0);
        String userNameEn = (String) getUser.get(Dict.USER_NAME_EN);
        String oldPassWord = (String) getUser.get(Dict.PASSWORD);
        String userNameCn = (String) getUser.get(Dict.USER_NAME_CN);
        String email = (String) getUser.get(Dict.EMAIL);
        String group_id = (String) getUser.get(Dict.GROUP_ID);
        if (oldPassWord.equals(des3.encrypt(newPassWord))) {
            throw new FdevException(ErrorConstants.USR_PASSWORD_ERROR, new String[]{Dict.PASSWORD, "密码相同"});
        }
        //用户属于测试中心、业务部门、规划部门 小组时，修改密码不需要补填gitlab token
        if (!groupids.contains(users.get(0).get("group_id"))) {
            //判断access token正确性
            Map<String, Object> param = new HashMap<>();
            param.put(Dict.TOKEN, gitLable);
            param.put(Dict.REST_CODE, "checkGitlabToken");
            Boolean data = (Boolean) restTransport.submit(param);
            if (data == false) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"access token错误"});
            }
        }
        User updateUser = new User();
        updateUser.setUser_name_en(userNameEn);
        updateUser.setPassword(des3.encrypt(newPassWord));
        updateUser.setUser_name_cn(userNameCn);
        updateUser.setEmail(email);
        updateUser.setGit_token(gitLable);
        updateUser.setIs_once_login("3");//首次登陆进入新手指引页面状态
        User newUser = userService.updateUser(updateUser);
        // 首次登录，修改密码后生成新token
        String tokenPre = newUser.getUser_name_en() + "-" + System.currentTimeMillis();
        String newToken = tokenManger.createToken(tokenPre);
        redisTemplate.opsForValue().set(newUser.getUser_name_en() + "user.login.token", newToken, 7, TimeUnit.DAYS);
        List<Map> json = userService.queryUser(reqUser);
        Map jsonObject = json.get(0);
        jsonObject.put(Dict.TOKEN, newToken);
        return JsonResultUtil.buildSuccess(jsonObject);
    }


    // 退出
    @ApiOperation(value = "退出")
    @ApiImplicitParam(name = "user", value = "例如:{\"id\":\"5c6b94ffa3178a32e0e3fcf1\"}")
    @RequestMapping(value = "/exit", method = RequestMethod.POST)
    public JsonResult exit(@RequestBody User user) throws Exception {
        Boolean exit = userService.exit(user);
        if (exit) {
            return JsonResultUtil.buildSuccess();
        }
        throw new FdevException(ErrorConstants.SERVER_ERROR);
    }

    // 查询oAuth
    @ApiOperation(value = "查询oAuth")
    @ApiImplicitParam(name = "oAuth", value = "例如:{\"name\":\"svn\",\"host\":\"xxx/svn\"}")
    @RequestMapping(value = "/queryOAuth", method = RequestMethod.POST)
    public JsonResult queryOAuth(@RequestBody OAuth oAuth) throws Exception {
        List<OAuth> oAuths = oAuthService.query(oAuth);
        return JsonResultUtil.buildSuccess(oAuths);
    }

    // 接受第三方认证请求
    @ApiOperation(value = "接受第三方认证请求,返回我方前端地址")
    @RequestMapping(value = "/acceptThirdAuth", method = RequestMethod.POST)
    public JsonResult thirdAuth(@RequestBody Map requestParam, HttpServletRequest request,
                                HttpServletResponse response) throws Exception {
        String name = (String) requestParam.get(Dict.NAME);
        String host = (String) requestParam.get(Dict.HOST);
        if (CommonUtils.isNullOrEmpty(name) || CommonUtils.isNullOrEmpty(host)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"第三方名字或网址为空"});
        }
        OAuth add = oAuthService.add(new OAuth(null, name, host));
        String redirect = oauthUrl + "?OAuthId=" + add.getId();
        return JsonResultUtil.buildSuccess(redirect);
    }

    @ApiOperation(value = "第三方认证已登录用户通过登录凭证生成认证码")
    @RequestMapping(value = "/createAuthCode", method = RequestMethod.POST)
    public JsonResult accessToken(@RequestBody Map jsonObject, HttpServletRequest request,
                                  HttpServletResponse response) throws Exception {
        String code = null;
        String token = request.getHeader(Dict.AUTHORIZATION);
        String oAuthId = (String) jsonObject.get(Dict.ID);
        if (CommonUtils.isNullOrEmpty(oAuthId)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"第三方id为空"});
        }
        OAuth oAuth = new OAuth();
        oAuth.setId(oAuthId);
        List<OAuth> oAths = oAuthService.query(oAuth);
        if (CommonUtils.isNullOrEmpty(oAuth)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"第三方不存在"});
        }
        String userNameEn = tokenManger.getUserByToken(token);
        code = MD5.encoder(oauth2Key, tokenManger.createToken(userNameEn));
        redisTemplate.opsForValue().set(code, token, 600L, TimeUnit.SECONDS);
        Map result = new HashMap();
        result.put("redirectUrl", oAths.get(0).getHost() + "?token=" + code);
        return JsonResultUtil.buildSuccess(result);
    }

    @ApiOperation(value = "第三方认证使用认证码换取登录凭证")
    @RequestMapping(value = "/otherAuthGetToken", method = RequestMethod.POST)
    public JsonResult otherAuthGetToken(@RequestBody Map requestParam, HttpServletRequest request) {
        String code = (String) requestParam.get(Dict.TOKEN);
        if (CommonUtils.isNullOrEmpty(code))
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"token为空"});
        String token = (String) redisTemplate.opsForValue().get(code);
        if (CommonUtils.isNullOrEmpty(token)) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL);
        }
        return JsonResultUtil.buildSuccess(token);

    }

    // 第三方认证
    @ApiOperation(value = "第三方认证获取用户信息")
    @RequestMapping(value = "/otherAuthGetUserInfo", method = RequestMethod.POST)
    public JsonResult oauthGetUserInfo(@RequestBody Map requestParam, HttpServletRequest request)
            throws Exception {
        // 请求数据：token
        String token = (String) requestParam.get(Dict.AUTHORIZATION);
        String userNameEn = tokenManger.getUserByToken(token);
        User user = new User();
        user.setUser_name_en(userNameEn);
        List<Map> result = userService.queryUser(user);
        // 返回数据：用户信息
        return JsonResultUtil.buildSuccess(result);
    }

    @ApiOperation(value = "admin模拟用户")
    @RequestMapping(value = "/simulateUser", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult simulateUser(@RequestBody Map<String, String> requestParam, HttpServletRequest request) throws Exception {
        //当前登录用户
        String userNameEn = tokenManger.getUserByToken(request.getHeader(Dict.AUTHORIZATION));
        User user = new User();
        user.setUser_name_en(userNameEn);
        List<Map> res = userService.queryUser(user);
        String name_en = (String) (res.get(0).get(Dict.USER_NAME_EN));
        //不是超管权限就报错
        if (!Dict.ADMIN.equals(name_en)) {
            throw new FdevException(ErrorConstants.USR_SIMULATEUSER_ERROR, new String[]{" 当前用户无权限进行此操作! "});
        }
        //模拟的用户
        String user_name_en = requestParam.get(Dict.USER_NAME_EN);
        User user2 = new User();
        user2.setUser_name_en(user_name_en);
        List<Map> u = userService.queryUser(user2);
        if (CommonUtils.isNullOrEmpty(u)) {
            throw new FdevException(ErrorConstants.USR_NOT_EXIST);
        }
        if (!"3".equals((String) u.get(0).get(Dict.IS_ONCE_LOGIN))) {
            throw new FdevException(ErrorConstants.USR_SIMULATEUSER_ERROR, new String[]{" 用户未完成首次登入! "});
        }
        String tokenPre = u.get(0).get(Dict.USER_NAME_EN) + "-" + System.currentTimeMillis();
        String token = tokenManger.createToken(tokenPre);
        redisTemplate.opsForValue().set(((String) u.get(0).get(Dict.USER_NAME_EN)) + "simulateUser.user.login.token", token, 7, TimeUnit.DAYS);
        u.get(0).put(Dict.TOKEN, token);
        return JsonResultUtil.buildSuccess(u.get(0));
    }


    @ApiOperation(value = "修改用户密码")
    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult updatePassword(@RequestBody Map<String, String> requestParam
            , HttpServletRequest request) throws Exception {
        //当前登录用户
        String userNameEn = tokenManger.getUserByToken(request.getHeader(Dict.AUTHORIZATION));
        User user = new User();
        user.setUser_name_en(userNameEn);
        List<Map> res = userService.queryUser(user);
        if (CommonUtils.isNullOrEmpty(res)) {
            throw new FdevException(ErrorConstants.USR_NOT_EXIST);
        }
        String newPassWord = (String) requestParam.get(Dict.PASSWORD);
        if (!Validator.isPassWord(newPassWord)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.PASSWORD, "数字，字母，符号至少任意两者组合，不少于8位数"});
        }
        String oldPassWord = (String) res.get(0).get(Dict.PASSWORD);
        if (oldPassWord.equals(des3.encrypt(newPassWord)) || oldPassWord.equals(MD5.encoder(userNameEn, newPassWord))) {
            throw new FdevException(ErrorConstants.USR_PASSWORD_ERROR, new String[]{"密码相同"});
        }
        User updateUser = new User();
        updateUser.setUser_name_en(userNameEn);
        updateUser.setUser_name_cn((String) res.get(0).get(Dict.USER_NAME_CN));
        updateUser.setEmail((String) res.get(0).get(Dict.EMAIL));
        updateUser.setPassword(des3.encrypt(newPassWord));
        updateUser.setIs_once_login("3");
        User newUser = userService.updateUser(updateUser);
        redisTemplate.delete(newUser.getUser_name_en() + "user.login.token");
        return JsonResultUtil.buildSuccess(newUser);
    }

    @ApiOperation(value = "批量更新人员离职情况")
    @RequestMapping(value = "/updateAllLeaveUser", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult updateAllLeaveUser(){
        userService.updateAllLeaveUser();
        return JsonResultUtil.buildSuccess();
    }
}
