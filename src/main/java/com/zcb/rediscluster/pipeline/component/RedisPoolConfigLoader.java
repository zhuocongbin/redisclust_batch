package com.zcb.rediscluster.pipeline.component;

import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by zhuocongbin
 * date 2018/10/11
 */
public class RedisPoolConfigLoader {
    private static JedisPoolConfig jedisPoolConfig;
    static {
        jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(10);
        jedisPoolConfig.setMaxWaitMillis(3000);
        jedisPoolConfig.setTestOnBorrow(true);
    }
    private RedisPoolConfigLoader(){}
    public static JedisPoolConfig getJedisPoolConfig() {
        return jedisPoolConfig;
    }
}
