package com.spdb.fdev.spdb.strategy;

import java.util.Map;

public interface WebhookEventStrategy {

    Object eventExecutor(Map<String, Object> parse);

}
