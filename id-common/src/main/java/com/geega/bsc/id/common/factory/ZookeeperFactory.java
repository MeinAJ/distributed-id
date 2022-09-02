package com.geega.bsc.id.common.factory;

import com.geega.bsc.id.common.config.ZkConfig;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * ZkConfig
 *
 * @author Jun.An3
 * @date 2022/06/30
 */
public class ZookeeperFactory {

    private final CuratorFramework client;

    public ZookeeperFactory() {
        this(
                "default",
                "127.0.0.1:2181",
                10000,
                10000,
                new ExponentialBackoffRetry(1000, 3)
        );
    }

    public ZookeeperFactory(ZkConfig zkConfig) {
        this(
                zkConfig.getNamespace(),
                zkConfig.getConnection(),
                zkConfig.getSessionTimeoutMs(),
                zkConfig.getConnectionTimeoutMs(),
                new ExponentialBackoffRetry(1000, 3)
        );
    }

    private ZookeeperFactory(String namespace,
                             String connection,
                             Integer sessionTimeoutMs,
                             Integer connectionTimeoutMs,
                             RetryPolicy retryPolicy) {
        assert namespace != null;
        assert connection != null;
        assert sessionTimeoutMs != null;
        assert connectionTimeoutMs != null;
        client = CuratorFrameworkFactory.builder()
                .namespace(namespace)
                .connectString(connection)
                .sessionTimeoutMs(sessionTimeoutMs)
                .connectionTimeoutMs(connectionTimeoutMs)
                .retryPolicy(retryPolicy)
                .build();
        client.start();
    }

    public CuratorFramework instance() {
        return this.client;
    }

}
