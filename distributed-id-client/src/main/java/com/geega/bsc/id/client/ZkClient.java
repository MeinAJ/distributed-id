package com.geega.bsc.id.client;

import com.alibaba.fastjson.JSON;
import com.geega.bsc.id.client.node.NodesInformation;
import com.geega.bsc.id.common.address.NodeAddress;
import com.geega.bsc.id.common.config.ZkConfig;
import com.geega.bsc.id.common.constant.ZkTreeConstant;
import com.geega.bsc.id.common.factory.ZookeeperFactory;
import com.geega.bsc.id.common.utils.SleepUtil;
import com.geega.bsc.id.common.utils.TimeUtil;
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

    @SuppressWarnings("FieldCanBeLocal")
    private final Integer zkInitWaitTimeoutMs = 5000;

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
            final ZookeeperFactory factory = new ZookeeperFactory(this.zkConfig);
            final CuratorFramework zkClient = factory.instance();

            initBaseNode(zkClient);

            if (zkClient.checkExists().forPath(ZkTreeConstant.ZK_SERVER_ROOT) == null) {
                zkClient.create().forPath(ZkTreeConstant.ZK_SERVER_ROOT);
            }
            //获取当前已注册服务
            final List<String> zkNodePaths = zkClient.getChildren().forPath(ZkTreeConstant.ZK_SERVER_ROOT);
            LOGGER.info("当前已注册服务节点：{}", JSON.toJSONString(zkNodePaths));
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
                        LOGGER.info("更新服务节点：{}", event.getData());
                        updateNode(path);
                        break;
                    case CHILD_REMOVED:
                        LOGGER.info("删除服务节点：{}", event.getData());
                        removeNode(path);
                        break;
                    default:
                        break;
                }
            });
            //等待最多默认5s，获取不到ID生成服务，就抛异常
            waitTimeoutThrow();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("初始化zk失败", e);
            throw new RuntimeException("初始化zk失败");
        }
    }

    /**
     * 初始化基础节点
     *
     * @param zkClient
     * @throws Exception
     */
    private void initBaseNode(CuratorFramework zkClient) throws Exception {
        createNodeIfNotExist(zkClient, ZkTreeConstant.ZK_SERVER_ROOT);
        createNodeIfNotExist(zkClient, ZkTreeConstant.ZK_CLIENT_ROOT);
        createNodeIfNotExist(zkClient, ZkTreeConstant.ZK_WORK_ID_ROOT);
    }

    /**
     * 如果没有节点就创建节点
     *
     * @param zkClient
     * @param nodePath
     * @throws Exception
     */
    private void createNodeIfNotExist(CuratorFramework zkClient, String nodePath) throws Exception {
        String[] paths = nodePath.split(ZkTreeConstant.ZK_PATH_SEPARATOR);
        String createPath = "";
        for (String path : paths) {
            if ("".equals(path)) {
                continue;
            }
            createPath += ZkTreeConstant.ZK_PATH_SEPARATOR + path;
            if (zkClient.checkExists().forPath(createPath) == null) {
                zkClient.create().forPath(createPath);
            }
        }
    }

    private void waitTimeoutThrow() {
        boolean success = false;
        long now = TimeUtil.now();
        while (TimeUtil.now() - now <= zkInitWaitTimeoutMs) {
            if (getNodes().size() == 0) {
                SleepUtil.waitMs(1000);
            } else {
                success = true;
                break;
            }
        }
        if (!success) {
            throw new RuntimeException("获取ID生成服务节点失败");
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
