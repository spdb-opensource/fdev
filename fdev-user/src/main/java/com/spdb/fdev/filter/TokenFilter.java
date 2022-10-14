package com.spdb.fdev.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.Util;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.Token.TokenManger;
import com.spdb.fdev.fuser.spdb.entity.user.User;
import com.spdb.fdev.fuser.spdb.service.IInterfaceRegisterService;
import com.spdb.fdev.fuser.spdb.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Resource;
import javax.servlet.FilterConfig;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author xxx
 */
@RefreshScope
public class TokenFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(TokenFilter.class);

    @Value("${no.filter.urls}")
    private String nofilterUrl;

    @Value("${access_token_list:}")
    public List<String> accessTokenList;
    @Value("${system_name_list:}")
    public List<String> systemNameList;

    @Resource
    private TokenManger tokenManger;

    @Resource
    private UserService userService;

    @Autowired
    WebApplicationContext applicationContext;
    @Autowired
    IInterfaceRegisterService interfaceRegisterService;

    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //TODO
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String uri = request.getRequestURI();
        String context = applicationContext.getServletContext().getContextPath();
        String requestMapping = uri.substring(uri.indexOf(context) + context.length());
        String token = request.getHeader(Dict.AUTHORIZATION);
        String source = request.getHeader("source");
        String ldap = request.getHeader("ldap");
        String accessToken = request.getHeader("accessToken");
        String systemName = request.getHeader("systemName");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type,Content-Length,Authorization,Accept,X-Requested-With");
        response.setHeader("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS");
        response.setHeader("Access-Control-MAX-Age", "3600");
        if (request.getMethod().equals("OPTIONS")) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        if (isNoFilter(requestMapping) || "ldap".equals(ldap)|| "back".equals(source) || !getAllUrl().contains(requestMapping) || checkAccessTokenAndSystemName(accessToken, systemName)) {
            chain.doFilter(request, response);
        } else if (!tokenManger.checkToken(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "用户未登录！");
        } else {
            List<Map> users = null;
            try {
                User user = new User();
                String userNameEn = tokenManger.getUserByToken(token);
                user.setUser_name_en(userNameEn);
                users = userService.queryUser(user);
            } catch (Exception e) {
                logger.error("查询用户出错: ", e);
            }
            if (users != null && users.size() > 0) {
                Map userJsonObject = users.get(0);
                //新创建一个comm包的user来使用aop
                com.spdb.fdev.common.User cUser = new com.spdb.fdev.common.User();
                cUser.setId((String) userJsonObject.get("id"));
                cUser.setUser_name_en((String) userJsonObject.get("user_name_en"));
                cUser.setUser_name_cn((String) userJsonObject.get("user_name_cn"));
                cUser.setEmail((String) userJsonObject.get("email"));
                cUser.setPassword((String) userJsonObject.get("password"));
                cUser.setGroup_id((String) userJsonObject.get("group_id"));
                cUser.setRole_id((List<String>) userJsonObject.get("role_id"));
//                cUser.setPermission_id((String) userJsonObject.get("permission_id"));
                cUser.setSvn_user((String) userJsonObject.get("svn_user"));
                cUser.setRedmine_user((String) userJsonObject.get("redmine_user"));
                cUser.setGit_user((String) userJsonObject.get("git_user"));
                cUser.setGit_token((String) userJsonObject.get("git_token"));
                cUser.setTelephone((String) userJsonObject.get("telephone"));
                cUser.setCompany_id((String) userJsonObject.get("company_id"));
                cUser.setStatus((String) userJsonObject.get("status"));
                cUser.setIs_once_login((String) userJsonObject.get("is_once_login"));
                cUser.setLabels((List<String>) userJsonObject.get("labels"));
                //将user放入到session中
                //在UserVerifyUtil中取出的时候为了让group的类型为map而不是com.spdb.fdev.fuser.spdb.entity.user.Group
                Map group = CommonUtils.obj2Map(userJsonObject.get("group"));
                userJsonObject.put("group", group);
                request.getSession().setAttribute("_USER", cUser);
                request.getSession().setAttribute("_FUSER", userJsonObject);
                String userNameEn = (String) userJsonObject.get(Dict.USER_NAME_EN);
                MDC.put("currentUser", userNameEn);

                //权限拦截
                //查询uri是否登记
                Map<String,String> param = new HashMap();
                param.put("interfacePath",uri);
                Set<String> roleIds = interfaceRegisterService.getRolesByInterface(param);
                com.spdb.fdev.common.User sessionUser = Util.getSessionUser();
                List<String> role_ids = sessionUser.getRole_id();
                //已登记且不匹配，则拦截
                if(roleIds.size() > 0 && !isContains(new HashSet<>(role_ids),roleIds) && !"admin".equals(userNameEn)){
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "当前用户权限不足！");
                    return;
                }
            }
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        //TODO
    }

    /**
     * 判断set1中是否包含set2中的元素，包含则返回true
     * @param set1
     * @param set2
     * @return
     */
    private Boolean isContains(Set<String> set1, Set<String> set2){
        for(String str : set2){
            if(set1.contains(str)){
                return true;
            }
        }
        return false;
    }

    public List<String> getAllUrl() {
        RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        //获取url与类和方法的对应信息
        Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
        List<String> urlList = new ArrayList<>();
        for (RequestMappingInfo info : map.keySet()) {
            //获取url的Set集合，一个方法可能对应多个url
            Set<String> patterns = info.getPatternsCondition().getPatterns();
            for (String url : patterns) {
                urlList.add(url);
            }
        }
        return urlList;
    }

    public boolean isNoFilter(String requestMapping) {
        if ("/api/auth/acceptThirdAuth".equals(requestMapping))
            return true;
        String[] urls = nofilterUrl.split(";");
        for (String url : urls) {
            if (url.equals(requestMapping))
                return true;
        }
        return false;
    }

    public boolean checkAccessTokenAndSystemName(String accessToken, String systemName) {
        if (!CommonUtils.isNullOrEmpty(accessToken) && !CommonUtils.isNullOrEmpty(systemName)) {
            int accessTokenSize = this.accessTokenList.size();
            int systemNameSize = this.systemNameList.size();
            if (accessTokenSize != systemNameSize) {
                logger.error("accessToken is not map");
                return false;
            } else {
                for(int i = 0; i < accessTokenSize; ++i) {
                    String accessTokenVal = (String)this.accessTokenList.get(i);
                    String systemNameVal = (String)this.systemNameList.get(i);
                    if (accessToken.equals(accessTokenVal) && systemName.equals(systemNameVal)) {
                        return true;
                    }
                }
                return false;
            }
        } else {
            return false;
        }
    }

}
