/*
 * Copyright (c) 2019, ABB and/or its affiliates. All rights reserved.
 * ABB PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.geega.bsc.id.starter.configuration;

import com.geega.bsc.id.client.IdClient;
import com.geega.bsc.id.client.cache.CacheConfig;
import com.geega.bsc.id.client.dispatch.DefaultNodeAddressDispatch;
import com.geega.bsc.id.client.dispatch.NodeAddressDispatch;
import com.geega.bsc.id.client.rule.IRule;
import com.geega.bsc.id.client.rule.RandomRule;
import com.geega.bsc.id.common.address.NodeAddress;
import com.geega.bsc.id.starter.properties.CacheProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * IdAutoConfiguration
 *
 * @author Jun.An3
 * @date 2022/07/19
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(value = {CacheProperties.class})
public class IdAutoConfiguration {

    @Value("${node.address:}")
    private String nodeAddress;

    @Bean
    public IdClient idClient(CacheProperties cacheProperties, NodeAddressDispatch nodeAddressDispatch) {
        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setCapacity(cacheProperties.getCapacity());
        cacheConfig.setTriggerExpand(cacheProperties.getTriggerExpand());
        return new IdClient(nodeAddressDispatch, cacheConfig);
    }

    @Bean
    @ConditionalOnMissingBean(NodeAddressDispatch.class)
    public NodeAddressDispatch idProcessDispatchAddress(IRule rule) {
        log.debug("node address:{}", nodeAddress);
        if ("".equals(nodeAddress.trim())) {
            throw new IllegalArgumentException("node.address不能为空");
        }
        String[] nodeAddresses = nodeAddress.split(",");
        List<NodeAddress> nodeAddressList = new ArrayList<>();
        for (String node : nodeAddresses) {
            if (node.contains(":")) {
                String[] ipPort = node.split(":");
                NodeAddress address = NodeAddress.builder().ip(ipPort[0]).port(Integer.valueOf(ipPort[1])).build();
                nodeAddressList.add(address);
            } else {
                NodeAddress address = NodeAddress.builder().ip(node).port(80).build();
                nodeAddressList.add(address);
            }
        }
        return new DefaultNodeAddressDispatch(nodeAddressList, rule);
    }

    @Bean
    @ConditionalOnMissingBean(IRule.class)
    public IRule rule() {
        return new RandomRule();
    }
}
