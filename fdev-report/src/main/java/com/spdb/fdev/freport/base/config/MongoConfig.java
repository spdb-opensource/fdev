package com.spdb.fdev.freport.base.config;

import com.mongodb.MongoClientURI;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "spring.data.mongodb")
@Data
public class MongoConfig implements InitializingBean {

    private Map<String, String> config;

    private Map<String, MongoTemplate> mongoTemplateMap;

    @Override
    public void afterPropertiesSet() {
        mongoTemplateMap = new HashMap<>();
        for (Map.Entry<String, String> set : config.entrySet()) {
            MongoClientURI mongoClientURI = new MongoClientURI(set.getValue());
            mongoTemplateMap.put(set.getKey(), new MongoTemplate(new SimpleMongoDbFactory(mongoClientURI)));
        }
    }
    public MongoTemplate getMongoTemplate(String dsId) {
        return mongoTemplateMap.get(dsId);
    }
}
