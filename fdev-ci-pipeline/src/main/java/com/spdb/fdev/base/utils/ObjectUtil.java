package com.spdb.fdev.base.utils;

import com.csii.pe.redis.kryo.PooledKryoFactory;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ObjectUtil {

    private static PooledKryoFactory pooledKryoFactory = new PooledKryoFactory();

    /**
     * 将对象转成byte数组
     * @param obj
     * @return
     * @throws Exception
     */
    public static ByteArrayOutputStream object2Bytes(Object obj) throws Exception{
        ByteArrayOutputStream bio = new ByteArrayOutputStream();
        Output output = new Output(bio);
        pooledKryoFactory.getKryo().writeObject(output, obj);
        output.flush();
        return bio;
    }

    /**
     * 将byte数组转成对象
     * @param bytes
     * @return
     * @throws Exception
     */
    public static Object bytes2Object(byte[] bytes, Class obj) throws Exception{
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        return pooledKryoFactory.getKryo().readObject(new Input(in), obj);
    }

}