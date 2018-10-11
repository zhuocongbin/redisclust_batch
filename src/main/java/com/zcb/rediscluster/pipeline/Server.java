package com.zcb.rediscluster.pipeline;

import com.zcb.rediscluster.pipeline.component.RedisClusterBatch;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by zhuocongbin
 * date 2018/10/11
 */
@Slf4j
public class Server {
    public static void main(String[] args) {
        /*RedisClusterBatch redisClusterBatch = new RedisClusterBatch(RedisClusterUtils.getJedisCluster(), "onenet_redis123");*/
        RedisClusterBatch redisClusterBatch = new RedisClusterBatch("xxx.xx.x.xxx:6379,xxx.xx.x.xxx:6379,xxx.xx.x.xxx:6379", "xxx", 3000);
        Map<String, String> batch = null;
        try {
            batch = redisClusterBatch.mget(Arrays.asList("zhan", "buzhidao", "dao"));
        } catch (IOException e) {
            log.info(e.getMessage());
        }
        Iterator<String> iterator = batch.keySet().iterator();
        String temp;
        while (iterator.hasNext()) {
            temp = iterator.next();
            System.out.println(temp + " -> " + batch.get(temp));
        }
    }
}
