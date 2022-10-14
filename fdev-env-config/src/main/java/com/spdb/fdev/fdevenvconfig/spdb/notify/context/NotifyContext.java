package com.spdb.fdev.fdevenvconfig.spdb.notify.context;

import com.spdb.fdev.fdevenvconfig.spdb.notify.INotifyEventStrategy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * create by Idea
 *
 * @Author aric
 * @Date 2020/1/6 14:13
 * @Version 1.0
 */
@Service
public class NotifyContext {

    private Map<String, INotifyEventStrategy> notifyEventStrategyMap = new HashMap<>();

    public NotifyContext(Map<String, INotifyEventStrategy> notifyEventStrategyMap) {
        this.notifyEventStrategyMap.clear();
        notifyEventStrategyMap.forEach((k, v) -> this.notifyEventStrategyMap.put(k, v));
    }

    public void getResource(String event, Map<String, Object> parse) {
        INotifyEventStrategy notifyEventStrategy = this.notifyEventStrategyMap.get(event);
        if (notifyEventStrategy == null) {
            return;
        }
        notifyEventStrategy.doNotify(parse);
    }
}
