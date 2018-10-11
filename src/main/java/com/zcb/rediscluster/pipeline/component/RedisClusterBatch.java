package com.zcb.rediscluster.pipeline.component;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.*;
import redis.clients.util.JedisClusterCRC16;

import java.io.IOException;
import java.util.*;

/**
 * Created by zhuocongbin
 * date 2018/10/11
 */
@Slf4j
public class RedisClusterBatch {
    private String password;
    private JedisCluster jedisCluster;
    private Map<String, JedisPool> nodeMap;
    private TreeMap<Long, String> tree = new TreeMap<>();
    public RedisClusterBatch(JedisCluster jedisCluster, String password) {
        this.jedisCluster = jedisCluster;
        this.password = password;
        init();
    }
    public RedisClusterBatch(String hosts, String password, Integer timeout) {
        this.password = password;
        initJedisCluster(hosts, timeout);
        init();
    }
    public Map<String, String> mget(List<String> keys) throws IOException {
        Map<String, String> result = new HashMap<>();
        Map<Integer, List<String>> keyVal = keyToSlot(keys);
        Iterator<Integer> hostIterator = keyVal.keySet().iterator();
        while (hostIterator.hasNext()) {
            Integer slot = hostIterator.next();
            String hostPot = tree.lowerEntry(Long.valueOf(slot)).getValue();
            JedisPool pool = nodeMap.get(hostPot);
            Pipeline pipelined = pool.getResource().pipelined();
            List<String> keySet = keyVal.get(slot);
            String[] keyArray = new String[keySet.size()];
            for (int i = 0; i < keySet.size(); i++) {
                keyArray[i] = keySet.get(i);
            }
            Response<List<String>> mget = pipelined.mget(keyArray);
            pipelined.close();
            List<String> tempResult = mget.get();
            for (int i = 0; i <keySet.size(); i++) {
                result.put(keySet.get(i), tempResult.get(i));
            }
        }
        return result;
    }
    private void init() {
        nodeMap = jedisCluster.getClusterNodes();
        Iterator<String> iterator = nodeMap.keySet().iterator();
        while (iterator.hasNext()) {
            getSlotHostMap(iterator.next(), tree);
        }
    }
    private void initJedisCluster(String hosts, Integer cTimeout) {
        String[] split = hosts.split(",");
        Set<HostAndPort> hostAndPorts = new HashSet<>();
        List<String> list = Arrays.asList(split);
        JedisPoolConfig jedisPoolConfig = RedisPoolConfigLoader.getJedisPoolConfig();
        list.forEach(post -> {
            String[] split1 = post.split(":");
            HostAndPort hostAndPort = new HostAndPort(split1[0], Integer.valueOf(split1[1]));
            hostAndPorts.add(hostAndPort);
        });
        this.jedisCluster = new JedisCluster(hostAndPorts, cTimeout, 3000, 5, password, jedisPoolConfig);
    }
    private void getSlotHostMap(String host, TreeMap<Long, String> tree) {
        String partes[] = host.split(":");
        HostAndPort hostAndPort = new HostAndPort(partes[0], Integer.parseInt(partes[1]));
        Jedis jedis = new Jedis(hostAndPort.getHost(), hostAndPort.getPort());
        jedis.auth(password);
        List<Object> list = jedis.clusterSlots();
        for (Object object : list) {
            List<Object> list1 = (List<Object>) object;
            List<Object> master = (List<Object>) list1.get(2);
            String hostPort = new String((byte[])master.get(0)) + ":" + master.get(1);
            tree.put((Long)list1.get(0), hostPort);
            tree.put((Long)list1.get(1), hostPort);
        }
        jedis.close();
    }
    private Map<Integer, List<String>> keyToSlot(List<String> keys) {
        Map<Integer, List<String>> keyVal = new HashMap<>();
        keys.forEach(key -> {
            int slot = JedisClusterCRC16.getSlot(key);
            if (keyVal.containsKey(slot)) {
                keyVal.get(slot).add(key);
            }else {
                List<String> temp = new ArrayList<>();
                temp.add(key);
                keyVal.put(slot, temp);
            }
        });
        return keyVal;
    }
}