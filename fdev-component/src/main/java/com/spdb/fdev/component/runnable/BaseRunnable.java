package com.spdb.fdev.component.runnable;

import java.util.Map;

public class BaseRunnable implements Runnable {

    private Map param;


    @Override
    public void run() {

    }

    public Map getParam() {
        return param;
    }

    public void setParam(Map param) {
        this.param = param;
    }
}
