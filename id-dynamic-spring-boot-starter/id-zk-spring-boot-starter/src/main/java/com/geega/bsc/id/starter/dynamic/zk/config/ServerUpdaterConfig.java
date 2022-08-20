package com.geega.bsc.id.starter.dynamic.zk.config;

import com.geega.bsc.id.starter.dynamic.common.properties.ServerUpdateProperties;
import com.geega.bsc.id.starter.dynamic.common.updater.ServerUpdater;
import com.geega.bsc.id.starter.dynamic.zk.properties.ZkProperties;
import com.geega.bsc.id.starter.dynamic.zk.updater.ZkServerUpdater;
import com.geega.bsc.id.zk.common.config.ZkConfig;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author songminghui@naolubrain.com
 * @description
 * @date 2022/8/8 12:08 上午
 */
@EnableConfigurationProperties({ServerUpdateProperties.class, ZkProperties.class})
@Configuration
public class ServerUpdaterConfig {

    @Bean
    @ConditionalOnMissingBean
    public ServerUpdater serverUpdater(ServerUpdateProperties serverUpdateProperties, ZkProperties zkProperties) {
        ZkConfig zkConfig = new ZkConfig();
        BeanUtils.copyProperties(zkProperties, zkConfig);
        return new ZkServerUpdater(serverUpdateProperties, zkConfig);
    }
}
