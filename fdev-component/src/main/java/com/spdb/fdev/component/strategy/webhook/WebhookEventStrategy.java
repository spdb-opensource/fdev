package com.spdb.fdev.component.strategy.webhook;

import java.util.Map;

/**
 * @Author: c-jiangjk
 * @Date: 2021/2/10 10:08
 */
public interface WebhookEventStrategy {
    void eventExecutor(Map<String, Object> parse) throws Exception;
}
