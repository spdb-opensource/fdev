package com.spdb.fdev.base.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import de.javakaffee.kryoserializers.KryoReflectionFactorySupport;
import de.javakaffee.kryoserializers.SubListSerializers;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public abstract class KryoFactory {
    private static final KryoFactory factory = new PooledKryoFactory();
    private final Set<Class> registrations = new LinkedHashSet();
    private boolean registrationRequired;
    private volatile boolean kryoCreated;

    protected KryoFactory() {
    }

    public static KryoFactory getDefaultFactory() {
        return factory;
    }

    public void registerClass(Class clazz) {
        if (this.kryoCreated) {
            throw new IllegalStateException("Can't register class after creating kryo instance");
        } else {
            this.registrations.add(clazz);
        }
    }

    protected Kryo createKryo() {
        if (!this.kryoCreated) {
            this.kryoCreated = true;
        }

        Kryo kryo = new KryoReflectionFactorySupport();
        kryo.setRegistrationRequired(false);
        kryo.setMaxDepth(20);
        kryo.register(Locale.class, new JavaSerializer());

        try {
            kryo.register(Class.forName("java.util.ArrayList$SubList"), SubListSerializers.createFor(Class.forName("java.util.ArrayList$SubList")));
        } catch (ClassNotFoundException var3) {
            var3.printStackTrace();
        }

        return kryo;
    }

    public void returnKryo(Kryo kryo) {
    }

    public void setRegistrationRequired(boolean registrationRequired) {
        this.registrationRequired = registrationRequired;
    }

    public void close() {
    }

    public abstract Kryo getKryo();
}