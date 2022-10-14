package com.spdb.fdev.base.utils;

import com.esotericsoftware.kryo.Kryo;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PooledKryoFactory extends KryoFactory {
    private final Queue<Kryo> pool = new ConcurrentLinkedQueue();
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public PooledKryoFactory() {
    }

    public void returnKryo(Kryo kryo) {
        this.pool.offer(kryo);
    }

    public void close() {
        this.pool.clear();
    }

    public Kryo getKryo() {
        Kryo kryo = (Kryo)this.pool.poll();
        if (kryo == null) {
            kryo = this.createKryo();
        }

        this.logger.debug("pooled kryo queue size: " + this.pool.size());
        return kryo;
    }
}
