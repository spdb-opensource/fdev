package com.fdev.database.spdb.dao.Impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fdev.database.dict.Dict;
import com.fdev.database.spdb.dao.AppDao;
import com.fdev.database.spdb.entity.App;
import net.sf.json.JSONObject;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


@Repository
public class AppDaoImpl implements AppDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public void save(App app) {
        mongoTemplate.save(app);
    }

    @Override
    public List<App> queryAppName(App app) throws Exception {
        ObjectMapper mapper=new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = app ==null?"{}":mapper.writeValueAsString(app);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Criteria c = new Criteria();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if(Dict.OBJECTID.equals(key))continue;
            if (Dict.SPDB_MANAGERS.equals(key)) {  //根据应用负责人中用户英文名进行查询
                c.and(key).elemMatch(Criteria.where(Dict.USER_NAME_EN).is(app.getSpdb_managers().get(0).get(Dict.USER_NAME_EN)));
            } else {
                c.and(key).is(value);
            }
        }
        Query query = Query.query(c);
        return mongoTemplate.find(query, App.class);
    }

    @Override
    public List<App> queryAppByUser(String user_name_en) {
        Query query = Query.query(Criteria.where(Dict.SPDB_MANAGERS).elemMatch(Criteria.where(Dict.USER_NAME_EN).is(user_name_en)));
        return mongoTemplate.find(query, App.class);
    }

    @Override
    public void updateManager(String appId, List<Map> spdb_managers) {
        Query query =Query.query(Criteria.where(Dict.ID).is(appId));
        Update update = Update.update(Dict.SPDB_MANAGERS, spdb_managers);
        mongoTemplate.findAndModify(query, update, App.class);
    }

}
