package com.geega.bsc.id.starter.dynamic.zk.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author songminghui@naolubrain.com
 * @description
 * @date 2022/8/8 12:13 上午
 */
@Data
@ConfigurationProperties(prefix = "id.server.zk")
public class ZkProperties {

    private String namespace;

    private String connection;

    private Integer sessionTimeoutMs;

    private Integer connectionTimeoutMs;
}
