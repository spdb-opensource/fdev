package com.spdb.fdev.fuser.spdb.Token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.utils.CommonUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
@RefreshScope
public class TokenManger {
    private static final Logger logger = LoggerFactory.getLogger(TokenManger.class);

    @Value("${auth.jwt.token.secret}")
    private String secret;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

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
            String oldToken = (String) redisTemplate.opsForValue().get(getUserByToken(token) + "user.login.token");
            if (!CommonUtils.isNullOrEmpty(oldToken) && token.equals(oldToken)) {
	        	return true;
        	}
            String simulateUserToken = (String)redisTemplate.opsForValue().get(getUserByToken(token)+ "simulateUser.user.login.token");
            if (!CommonUtils.isNullOrEmpty(simulateUserToken) && token.equals(simulateUserToken)) {
            	return true;
            }
        } catch (Exception e) {
            logger.warn("checkToken error", e);
            return false;
        }
        return false;
    }


    /**
     * 根据token获取当前登录用户user_name_en
     */
    public String getUserByToken(String token) throws Exception {
        if (null == token || "".equals(token.trim())) {
            return null;
        }
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
        DecodedJWT jwt = verifier.verify(token);
        Claim userNameEn = jwt.getClaims().get(Dict.USER_NAME_EN);
        if (null == userNameEn || "".equals(userNameEn.asString().trim())) {
            return null;
        }
        String[] args = userNameEn.asString().split("-");
        if (args.length == 2)
            return args[0];
        if (args.length == 3)
            return args[0] + "-" + args[1];
        return "";
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
