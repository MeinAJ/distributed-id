package com.geega.bsc.id.zk.common.config;

import lombok.Data;

/**
 * @author Jun.An3
 * @date 2022/07/20
 */
@Data
public class ZkConfig {

    String namespace;

    String connection;

    Integer sessionTimeoutMs;

    Integer connectionTimeoutMs;

}
