package com.spdb.fdev.component.runnable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RunnableFactory {

    @Autowired
    private Map<String, BaseRunnable> baseRunableMap;

    public BaseRunnable getRunnable(String scanType) {
        BaseRunnable baseRunnable = baseRunableMap.get(scanType);
        return baseRunnable;
    }

}
