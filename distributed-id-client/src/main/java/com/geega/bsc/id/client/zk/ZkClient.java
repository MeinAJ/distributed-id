package com.geega.bsc.id.client.zk;

import com.geega.bsc.id.client.node.NodesInformation;
import com.geega.bsc.id.common.address.NodeAddress;
import com.geega.bsc.id.common.exception.DistributedIdException;
import com.geega.bsc.id.common.utils.TimeUtil;
import com.geega.bsc.id.zk.common.config.ZkConfig;
import com.geega.bsc.id.zk.common.constant.ZkTreeConstant;
import com.geega.bsc.id.zk.common.factory.CustomZookeeperFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Jun.An3
 * @date 2022/07/11
 */
public class ZkClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZkClient.class);

    private final NodesInformation nodesInformation;

    private final ZkConfig zkConfig;

    public ZkClient(ZkConfig zkConfig) {
        this.zkConfig = zkConfig;
        this.nodesInformation = new NodesInformation();
        this.start();
    }

    public List<NodeAddress> getNodes() {
        return this.nodesInformation.getNodes();
    }

    private void start() {
        try {

            //创建zk客户端
            final CustomZookeeperFactory factory = new CustomZookeeperFactory(this.zkConfig);
            final CuratorFramework zkClient = factory.instance();

            //获取当前已注册服务
            final List<String> zkNodePaths = zkClient.getChildren().forPath(ZkTreeConstant.ZK_SERVER_ROOT);
            for (String zkNodePath : zkNodePaths) {
                updateNode(zkNodePath);
            }

            //监听某个节点
            //true代表初始化时就获取节点的数据并且缓存到本地
            PathChildrenCache nodeCache = new PathChildrenCache(zkClient, ZkTreeConstant.ZK_SERVER_ROOT, true);
            nodeCache.start();

            //监听子节点的数据变化
            nodeCache.getListenable().addListener((curatorFramework, event) -> {
                String path = event.getData().getPath();
                //noinspection unused
                byte[] data = event.getData().getData();
                switch (event.getType()) {
                    case CHILD_ADDED:
                    case CHILD_UPDATED:
                        updateNode(path);
                        break;
                    case CHILD_REMOVED:
                        removeNode(path);
                        break;
                    default:
                        break;
                }
            });
        } catch (Exception e) {
            throw new DistributedIdException("初始化zk失败", e);
        }
    }

    private void updateNode(String zkNodePath) {
        nodesInformation.update(getNodeAddress(zkNodePath));
    }

    private void removeNode(String zkNodePath) {
        nodesInformation.remove(getNodeAddress(zkNodePath));
    }

    private NodeAddress getNodeAddress(String zkNodePath) {
        final String address = zkNodePath.replaceAll(ZkTreeConstant.ZK_SERVER_ROOT + ZkTreeConstant.ZK_PATH_SEPARATOR, "");
        final String[] splits = address.split(":");
        return NodeAddress.builder()
                .ip(splits[0])
                .port(Integer.valueOf(splits[1]))
                .lastUpdateTime(TimeUtil.now())
                .build();
    }

}
