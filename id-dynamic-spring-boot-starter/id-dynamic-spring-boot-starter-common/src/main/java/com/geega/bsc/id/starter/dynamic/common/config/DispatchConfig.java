package com.geega.bsc.id.starter.dynamic.common.config;

import com.geega.bsc.id.client.dispatch.NodeAddressDispatch;
import com.geega.bsc.id.client.rule.IRule;
import com.geega.bsc.id.starter.dynamic.common.dispatch.DynamicNodeAddressDispatch;
import com.geega.bsc.id.starter.dynamic.common.updater.ServerUpdater;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author songminghui@naolubrain.com
 * @description
 * @date 2022/8/8 12:05 上午
 */
@Configuration
public class DispatchConfig {

    @Bean
    @ConditionalOnMissingBean
    public NodeAddressDispatch nodeAddressDispatch(IRule rule, ServerUpdater serverUpdater) {
        return new DynamicNodeAddressDispatch(rule, serverUpdater);
    }
}
