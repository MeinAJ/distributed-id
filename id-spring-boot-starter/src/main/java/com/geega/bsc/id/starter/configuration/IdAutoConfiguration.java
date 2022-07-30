/*
 * Copyright (c) 2019, ABB and/or its affiliates. All rights reserved.
 * ABB PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.geega.bsc.id.starter.configuration;

import com.geega.bsc.id.client.IdClient;
import com.geega.bsc.id.client.cache.CacheConfig;
import com.geega.bsc.id.starter.properties.CacheProperties;
import com.geega.bsc.id.starter.properties.ZkProperties;
import com.geega.bsc.id.zk.common.config.ZkConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * IdAutoConfiguration
 *
 * @author Jun.An3
 * @date 2022/07/19
 */
@Configuration
@EnableConfigurationProperties(value = {ZkProperties.class, CacheProperties.class})
public class IdAutoConfiguration {

    @Bean
    public IdClient idClient(ZkProperties idProperties, CacheProperties cacheProperties) {
        ZkConfig zkConfig = new ZkConfig();
        zkConfig.setConnection(idProperties.getConnection());
        zkConfig.setNamespace(idProperties.getNamespace());
        zkConfig.setConnectionTimeoutMs(idProperties.getConnectionTimeoutMs());
        zkConfig.setSessionTimeoutMs(idProperties.getSessionTimeoutMs());

        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setCapacity(cacheProperties.getCapacity());
        cacheConfig.setTriggerExpand(cacheProperties.getTriggerExpand());
        return new IdClient(zkConfig, cacheConfig);
    }

}
