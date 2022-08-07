package com.geega.bsc.id.starter.dynamic.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author songminghui@naolubrain.com
 * @description
 * @date 2022/8/6 7:07 下午
 */
@Data
@ConfigurationProperties(prefix = "id.server.updater")
public class ServerUpdateProperties {

    private long pollDuration = 5;

}
