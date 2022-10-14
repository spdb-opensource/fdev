package com.spdb.fdev.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.spdb.fdev.common.util.FdevUserCacheUtil;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.dict.Constants;
import com.spdb.fdev.common.dict.Dict;
import com.spdb.fdev.common.dict.ErrorConstants;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.Util;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author xxx
 */
public class TokenFilter implements Filter {
    private static Logger logger = LoggerFactory.getLogger(TokenFilter.class);

    @Value("${userManager.url:}")
    public String url;

    @Value("${userManager.user.url:}")
    public String userUrl;

    @Value("${no.filter.urls:}")
    private String nofilterUrl = "";

    @Value("${access_token_list:}")
    public List<String> accessTokenList;

    @Value("${system_name_list:}")
    public List<String> systemNameList;

    @Resource(name = "getRestTemplate")
    private RestTemplate restTemplate;

    @Value("${auth.jwt.token.secret}")
    private String secret;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    WebApplicationContext applicationContext;


    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String token = request.getHeader(Dict.TOKEN_AUTHORIZATION_KEY);
        String source = request.getHeader(Dict.BACKEND_REQUEST_FLAG_KEY);
        String accessToken = request.getHeader(Dict.ACCESSTOKEN);
        String systemName = request.getHeader(Dict.SYSTEMNAME);
        String Uri = request.getRequestURI();
        String context = applicationContext.getServletContext().getContextPath();
        String requestMapping = Uri.substring(Uri.indexOf(context) + context.length());
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type,Content-Length,Authorization,Accept,X-Requested-With");
        response.setHeader("Access-Control-Allow-Methods", "POST,GET,OPTIONS");
        response.setHeader("Access-Control-MAX-Age", "3600");
        if (request.getMethod().equals("OPTIONS")) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        } else {
            if (!this.isNoFilter(requestMapping) && !Constants.BACKEND_REQUEST_FLAG_VALUE.equals(source)) {
                if (this.checkAccessTokenAndSystemName(accessToken, systemName)) {
                    MDC.put(Dict.MDC_USERNAME_EN, request.getHeader(Dict.HTTPHEADER_USERNAME_EN));
                    chain.doFilter(request, response);
                    return;
                }
                
                if(!url.contains("/fusermanage")){
                	   if (this.checkToken(token, request)) {
                           if(!checkPermission(Uri,request,response,chain)){
                               response.sendError(HttpServletResponse.SC_FORBIDDEN, "当前用户权限不足！");
                               return;
                           }
                           chain.doFilter(request, response);
                           return;
                       }else {
                           logger.error("user is not logined, token: " + token);
                       }
                } else {
                       if (this.checkTokenNew(token, request)) {
                           chain.doFilter(request, response);
                           return;
                       } else {
                            logger.error("user is not logined, token: " + token);
                       }
                }
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "会话已失效！");
                return;
            } else {
                MDC.put(Dict.MDC_USERNAME_EN, request.getHeader(Dict.HTTPHEADER_USERNAME_EN));
                chain.doFilter(request, response);
            }
        }
    }

    /**
     * 校验接口权限
     * @param Uri
     * @param request
     * @param response
     * @param chain
     * @throws IOException
     * @throws ServletException
     */
    private boolean checkPermission(String Uri,HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws IOException, ServletException {
        //查询uri是否登记
        Map<String,String> param = new HashMap();
        param.put("interfacePath",Uri);
        String[] apiUrls = url.split("api");
        Map responseMap = restTemplate.postForObject(apiUrls[0] + "api/interfaceRegister/getRolesByInterface", param, Map.class);
        if(responseMap != null && "AAAAAAA".equals(responseMap.get(Dict.CODE)) && responseMap.get("data") != null){
            User sessionUser = null;
            sessionUser = Util.getSessionUser();
            List<String> role_ids = sessionUser.getRole_id();
            List<String> roleIds = (List<String>)responseMap.get("data");
            //已登记且不匹配，则拦截
            if(roleIds.size() > 0 && !isContains(new HashSet<>(role_ids),new HashSet<>(roleIds))){
                return false;
            }
        }
        return true;
    }

    private boolean checkToken(String token, HttpServletRequest request) {
        try {
            String responseEntity = restTemplate.postForObject(url, token, String.class);
            JSONObject dataMap = JSONObject.fromObject(responseEntity);
            if ("AAAAAAA".equals(dataMap.get(Dict.CODE))) {
                JSONArray jsonArray = JSONArray.fromObject(dataMap.get(Dict.DATA));
                JSONObject userJsonObject = JSONObject.fromObject(jsonArray.get(0));
                userJsonObject.remove("group");
                userJsonObject.remove("role");
                userJsonObject.remove("company");
                userJsonObject.remove("permission");
                userJsonObject.remove("user_label");
                userJsonObject.remove("_id");
                ObjectMapper objectMapper = new ObjectMapper();
                User user = objectMapper.readValue(userJsonObject.toString(), User.class);
                Map fuser = objectMapper.readValue(JSONObject.fromObject(jsonArray.get(1)).toString(), Map.class);
                request.getSession().setAttribute(Dict._USER, user);
                request.getSession().setAttribute(Dict._FUSER, fuser);
                Object redisUser = redisTemplate.opsForValue().get(Dict._USER + token);
                if (Util.isNullOrEmpty(redisUser)) {
                    redisTemplate.opsForValue().set(Dict._USER + token, user, 7, TimeUnit.DAYS);
                    redisTemplate.opsForValue().set(Dict._FUSER + token, fuser, 7, TimeUnit.DAYS);
                }
                MDC.put(Dict.MDC_USERNAME_EN, user.getUser_name_en());
                return true;
            }
        } catch (Exception e) {
            logger.error("checkToken error", e);
        }
        return false;
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

    @Override
    public void destroy() {

    }

    private boolean checkTokenNew(String token, HttpServletRequest request) {
        try {
            if (!checkToken(token)) {
                throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"token错误"});
            }
            // 解析登录凭证，获取用户ID，并查询用户信息返回
            String userNameEn = Util.getUserByToken(token);
            
            if(Util.isNullOrEmpty(userNameEn)) {
            	return false;
            }
            
            // 判断redis 是否有值
            Object redisUser = redisTemplate.opsForValue().get(Dict._USER + token);
            Object redisFUser = redisTemplate.opsForValue().get(Dict._FUSER + token);
            
            if(Util.isNullOrEmpty(redisUser) || Util.isNullOrEmpty(redisFUser)) {
                Map<String, String> send = new HashMap<>();
                send.put(Dict.USER_NAME_EN, userNameEn);
                String responseEntity = restTemplate.postForObject(userUrl + "/user/query", send, String.class);
                
                JSONObject dataMap = JSONObject.fromObject(responseEntity);
                if ("AAAAAAA".equals(dataMap.get(Dict.CODE))) {
                	 JSONArray jsonArray = JSONArray.fromObject(dataMap.get(Dict.DATA));
                     JSONObject userJsonObject = JSONObject.fromObject(jsonArray.get(0));
                     
                     userJsonObject.remove("group");
                     userJsonObject.remove("role");
                     userJsonObject.remove("company");
                     userJsonObject.remove("permission");
                     userJsonObject.remove("user_label");
                     userJsonObject.remove("_id");
                     userJsonObject.remove("create_date");
                     userJsonObject.remove("leave_date");
                     userJsonObject.remove("area_id");
                     userJsonObject.remove("function_id");
                     userJsonObject.remove("rank_id");
                     userJsonObject.remove("education");
                     userJsonObject.remove("start_time");
                     userJsonObject.remove("remark");
                     userJsonObject.remove("area");
                     userJsonObject.remove("function");
                     userJsonObject.remove("rank");
                     userJsonObject.remove("kf_approval");
                     userJsonObject.remove("net_move");
                     userJsonObject.remove("is_spdb");
                     userJsonObject.remove("vm_ip");
                     userJsonObject.remove("vm_name");
                     userJsonObject.remove("vm_user_name");
                     
                     userJsonObject.remove("phone_type");
                     userJsonObject.remove("phone_mac");
                     userJsonObject.remove("device_no");
                     userJsonObject.remove("is_spdb_mac");
                     userJsonObject.remove("ftms_level");
                     userJsonObject.remove("mantis_token");
                     
                     userJsonObject.remove("orgId");
                     userJsonObject.remove("orgnization");
                     userJsonObject.remove("createTime");
                     userJsonObject.remove("updateTime");
                     
                     ObjectMapper objectMapper = new ObjectMapper();
                     User user = objectMapper.readValue(userJsonObject.toString(), User.class);
                     Map  fuser = objectMapper.readValue(JSONObject.fromObject(jsonArray.get(0)).toString(), Map.class);
                   	 redisTemplate.opsForValue().set(Dict._USER + token, user, 7, TimeUnit.DAYS);
                   	 redisTemplate.opsForValue().set(Dict._FUSER + token, fuser, 7, TimeUnit.DAYS);
                     MDC.put(Dict.MDC_USERNAME_EN, user.getUser_name_en());
                     return true;
                }else {
                	 return false;
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("checkToken error", e);
        }
        return false;
    }

    private boolean isNoFilter(String requestMapping) {
        if (requestMapping.contains("websocket") || requestMapping.contains("swagger")
                || requestMapping.contains("api-docs") || requestMapping.contains(".png")
                || requestMapping.contains("/rest/url")) {
            return true;
        }
        String[] urls = nofilterUrl.split(";");
        for (String url : urls) {
            if (url.equals(requestMapping) || (!"".equals(url) && requestMapping.contains(url)))
                return true;
        }
        return false;
    }

    private boolean checkAccessTokenAndSystemName(String accessToken, String systemName) {
        if (Util.isNullOrEmpty(accessToken) || Util.isNullOrEmpty(systemName))
            return false;
        int accessTokenSize = accessTokenList.size();
        int systemNameSize = systemNameList.size();
        if (accessTokenSize != systemNameSize) {
            logger.error("accessToken is not map");
            return false;
        }
        for (int i = 0; i < accessTokenSize; i++) {
            String accessTokenVal = accessTokenList.get(i);
            String systemNameVal = systemNameList.get(i);
            if (accessToken.equals(accessTokenVal) && systemName.equals(systemNameVal))
                return true;
        }
        return false;
    }

    /**
     * 创建token
     *
     * @throws UnsupportedEncodingException
     * @throws JWTCreationException
     * @throws IllegalArgumentException
     **/
    public String createToken(String userNameEn) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("alg", "HS256");
        map.put("typ", "JWT");
        String token = JWT.create().withHeader(map)
                .withClaim(Dict.USER_NAME_EN, userNameEn)
                .sign(Algorithm.HMAC256(secret));
        return token;
    }

    /**
     * 核查token
     **/
    public boolean checkToken(String token) {
        if (null == token || "".equals(token.trim())) {
            return false;
        }
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
            DecodedJWT jwt = verifier.verify(token);
            Claim userNameEn = jwt.getClaims().get(Dict.USER_NAME_EN);
            if (null == userNameEn || "".equals(userNameEn.asString().trim())) {
                return false;
            }
            String oldToken = (String) redisTemplate.opsForValue().get(Util.getUserByToken(token) + "user.login.token");
            if (!Util.isNullOrEmpty(oldToken) && token.equals(oldToken)) {
                return true;
            }
            String simulateUserToken = (String)redisTemplate.opsForValue().get(Util.getUserByToken(token)+ "simulateUser.user.login.token");
            if (!Util.isNullOrEmpty(simulateUserToken) && token.equals(simulateUserToken)) {
                return true;
            }
        } catch (Exception e) {
            logger.warn("checkToken error", e);
            return false;
        }
        return false;
    }


    /**
     * base64加密token
     **/
    public String encode(String token) {
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encode = encoder.encode(token.getBytes());
        String encodeToken = new String(encode);
        return encodeToken;
    }

    /**
     * base64解密token
     **/
    public String decode(String token) {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] redecode = decoder.decode(token);
        String decodeToken = new String(redecode);
        return decodeToken;
    }

}
