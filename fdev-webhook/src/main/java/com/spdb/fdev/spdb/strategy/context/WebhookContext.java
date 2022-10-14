package com.spdb.fdev.spdb.strategy.context;

import com.spdb.fdev.spdb.strategy.WebhookEventStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WebhookContext {

    @Autowired
    private Map<String, WebhookEventStrategy> strategyMap = new HashMap<>();

    public WebhookContext(Map<String, WebhookEventStrategy> strategyMap) {
        this.strategyMap.clear();
        strategyMap.forEach((k, v) -> this.strategyMap.put(k, v));
    }

    public Object getResource(String event, Map<String, Object> parse) {
        WebhookEventStrategy webhookEventStrategy = strategyMap.get(event);
        if (webhookEventStrategy == null) {
            return null;
        }
        return webhookEventStrategy.eventExecutor(parse);
    }

}
