package com.spdb.fdev.base.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolUtils {

    private static final int availableProcessors = Runtime.getRuntime().availableProcessors();//  CPU核数

    public static ExecutorService getDefaultThreadPool(int k){
        int core = availableProcessors * (1 + k);
        return Executors.newFixedThreadPool(core);//  I/O密集 = CPU * [1 + (I/O耗时/CPU耗时)]
    }

}
