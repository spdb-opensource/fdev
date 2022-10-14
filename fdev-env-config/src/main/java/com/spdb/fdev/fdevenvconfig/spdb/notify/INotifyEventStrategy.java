package com.spdb.fdev.fdevenvconfig.spdb.notify;

import org.springframework.scheduling.annotation.Async;

import java.util.Map;

/**
 * 通知接口
 *
 * @author szy
 */
public interface INotifyEventStrategy {
    /**
     * 通知执行
     *
     * @param parse
     * @return
     */
    @Async
    void doNotify(Map<String, Object> parse);
}
