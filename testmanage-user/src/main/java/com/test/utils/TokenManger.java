package com.test.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RefreshScope
public class TokenManger {
    @Value("${auth.jwt.token.secret}")
    private String secret;

    public String createToken(String tokenPre) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("alg", "HS256");
        map.put("typ", "JWT");
        String token = JWT.create().withHeader(map).withClaim("serName", tokenPre).sign(Algorithm.HMAC256(secret));
        return token;
    }

    public String getUserByToken(String token) throws Exception {
        if (null == token || "".equals(token.trim())) {
            return null;
        }
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
        DecodedJWT jwt = verifier.verify(token);
        Claim userNameEn = jwt.getClaims().get("userName");
        if (null == userNameEn || "".equals(userNameEn.asString().trim())) {
            return null;
        }
        String[] args = userNameEn.asString().split("-");
        /*if (args.length == 2)
            return args[0];
        if (args.length == 3)
            return args[0] + "-" + args[1];*/
        return "";
    }

}
