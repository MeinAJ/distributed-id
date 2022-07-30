package com.geega.bsc.id.zk.common.server;


import com.geega.bsc.id.zk.common.config.ZkConfig;
import com.geega.bsc.id.zk.common.factory.CustomZookeeperFactory;
import org.apache.curator.framework.CuratorFramework;

/**
 * @author songminghui@naolubrain.com
 * @description
 * @date 2022/7/30 4:43 下午
 */
public class ZKServer {

    private final ZkConfig zkConfig;

    private final CuratorFramework zkClient;

    private static ZKServer zkServer;

    private ZKServer(ZkConfig zkConfig) {
        assert zkConfig != null;
        this.zkClient = new CustomZookeeperFactory(zkConfig).instance();
        this.zkConfig = zkConfig;
    }


    public static ZKServer getInstance(ZkConfig zkConfig) {
        if (zkServer == null) {
            synchronized (ZKServer.class) {
                if (zkServer == null) {
                    zkServer = new ZKServer(zkConfig);
                }
            }
        }
        return zkServer;
    }

    public ZkConfig getZkConfig() {
        return zkConfig;
    }

    public CuratorFramework getZkClient() {
        return zkClient;
    }
}
