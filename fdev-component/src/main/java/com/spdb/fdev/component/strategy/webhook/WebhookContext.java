package com.spdb.fdev.component.strategy.webhook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: c-jiangjk
 * @Date: 2021/2/10 10:07
 */
@Service
public class WebhookContext {

    @Autowired
    private Map<String, WebhookEventStrategy> strategyMap = new HashMap<>();

    public WebhookContext(Map<String, WebhookEventStrategy> strategyMap) {
        this.strategyMap.clear();
        strategyMap.forEach((k, v) -> this.strategyMap.put(k, v));
    }

    public void executor(String event, Map<String, Object> parse) throws Exception {
        WebhookEventStrategy webhookEventStrategy = strategyMap.get(event);
        if (webhookEventStrategy == null) {
            return;
        }
        webhookEventStrategy.eventExecutor(parse);
    }

}
