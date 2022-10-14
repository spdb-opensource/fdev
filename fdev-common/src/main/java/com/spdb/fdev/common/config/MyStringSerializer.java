package com.spdb.fdev.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.nio.charset.Charset;

@Component
public class MyStringSerializer implements RedisSerializer<String> {

    @Value("${spring.profiles.active}")
    private String active;

    private final Charset charset;

    public MyStringSerializer() {
        this(Charset.forName("UTF-8"));
    }

    public MyStringSerializer(Charset charset) {
        Assert.notNull(charset, "Charset must not be null!");
        this.charset = charset;
    }

    @Override
    public String deserialize(byte[] bytes) {
        String saveKey = new String(bytes, charset);
        String prefix = active;
        if(prefix.contains("new")){
            prefix = prefix.replace("-new", "");
        }
        String keyPrefix = prefix + ".";
        int indexOf = saveKey.indexOf(keyPrefix);
        if (indexOf > 0) {
            saveKey = saveKey.substring(indexOf);
        }
        return (saveKey.getBytes() == null ? null : saveKey);
    }

    @Override
    public byte[] serialize(String string) {
        String prefix = active;
        if(prefix.contains("new")){
            prefix = prefix.replace("-new", "");
        }
        String keyPrefix = prefix + ".";
        String key = keyPrefix + string;
        return (key == null ? null : key.getBytes(charset));
    }
}
