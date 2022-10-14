package com.spdb.fdev.fdevfootprint.spdb.service.impl;

import com.spdb.fdev.fdevfootprint.spdb.entity.SwitchModel;
import com.spdb.fdev.fdevfootprint.spdb.service.SwitchFlagQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@Service
public class SwitchFlagQueryServiceImpl implements SwitchFlagQueryService, InitializingBean {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    private Map<String, SwitchModel> switchModelMap = new HashMap();

    @Override
    public void afterPropertiesSet() throws Exception {
        Yaml yaml = new Yaml();
        try(InputStream switchModelInputStream = new ClassPathResource("switch_model.yml").getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(switchModelInputStream, Charset.forName("UTF-8"))) {
            Map<String, Map> modelMap = yaml.load(inputStreamReader);
            for (String channel : modelMap.keySet()) {
                SwitchModel switchModel = new SwitchModel((String) modelMap.get(channel).get("channelNo"), (String) modelMap.get(channel).get("channelName"), (String) modelMap.get(channel).get("status"), (String) modelMap.get(channel).get("autoPercent"));
                switchModelMap.put((String) modelMap.get(channel).get("channelNo"), switchModel);
            }
        } catch (Exception e) {
            logger.error("handle switch_model.yml error", e);
        }
    }

    @Override
    public SwitchModel getSwitchFlag(String channel) {
        return switchModelMap.get(channel);
    }

}
