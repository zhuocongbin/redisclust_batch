package com.zcb.rediscluster.pipeline.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by zhuocongbin
 * date 2018/10/11
 */
@Slf4j
public class PropertiesLoader {
    private static Properties properties;
    static {
        properties = new Properties();
        InputStream resource = PropertiesLoader.class.getClassLoader().getResourceAsStream("application.properties");
        try {
            properties.load(resource);
        } catch (IOException e) {
            log.info("load properties file error");
        }
    }
    private PropertiesLoader(){}
    public static Properties getProperties() {
        return properties;
    }
}
