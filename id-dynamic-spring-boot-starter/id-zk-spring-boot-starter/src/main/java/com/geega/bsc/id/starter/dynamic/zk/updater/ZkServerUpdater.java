package com.geega.bsc.id.starter.dynamic.zk.updater;

import com.geega.bsc.id.common.address.NodeAddress;
import com.geega.bsc.id.starter.dynamic.common.notify.ServerUpdateNotify;
import com.geega.bsc.id.starter.dynamic.common.properties.ServerUpdateProperties;
import com.geega.bsc.id.starter.dynamic.common.updater.BaseAbstractServerUpdater;
import com.geega.bsc.id.zk.common.config.ZkConfig;
import com.geega.bsc.id.zk.common.constant.ZkTreeConstant;
import com.geega.bsc.id.zk.common.factory.CustomZookeeperFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;

import java.util.ArrayList;
import java.util.List;

/**
 * @author songminghui@naolubrain.com
 * @description
 * @date 2022/8/7 11:52 下午
 */
@Slf4j
public class ZkServerUpdater extends BaseAbstractServerUpdater {

    private ZkConfig zkConfig;

    private final CuratorFramework client;

    public ZkServerUpdater(ServerUpdateProperties serverUpdateProperties, ZkConfig zkConfig) {
        super(serverUpdateProperties);
        this.zkConfig = zkConfig;
        client = new CustomZookeeperFactory(zkConfig).instance();
    }


    @Override
    public List<NodeAddress> pollNodeAddress() {
        try {
            List<NodeAddress> nodeAddresses = new ArrayList<>();
            final List<String> zkNodePaths = client.getChildren().forPath(ZkTreeConstant.ZK_SERVER_ROOT);
            for (String zkNodePath : zkNodePaths) {
                nodeAddresses.add(createNodeAddress(zkNodePath));
            }
            return nodeAddresses;
        } catch (Exception e) {
            log.error("zk拉取服务失败",e);
            return null;
        }
    }

    @Override
    protected void startMonitorSever(ServerUpdateNotify notify) {

    }

    private NodeAddress createNodeAddress(String zkNodePath) {
        String address = zkNodePath.replaceAll(ZkTreeConstant.ZK_SERVER_ROOT + ZkTreeConstant.ZK_PATH_SEPARATOR, "");
        String[] addresses = address.split(":");
        return NodeAddress.builder()
                .ip(addresses[0])
                .port(Integer.valueOf(addresses[1]))
                .lastUpdateTime(System.currentTimeMillis())
                .build();

    }
}
