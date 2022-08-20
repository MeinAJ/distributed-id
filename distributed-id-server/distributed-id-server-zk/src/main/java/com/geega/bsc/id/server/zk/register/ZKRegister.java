package com.geega.bsc.id.server.zk.register;

import com.geega.bsc.id.common.exception.DistributedIdException;
import com.geega.bsc.id.common.utils.AddressUtil;
import com.geega.bsc.id.zk.common.address.ServerNode;
import com.geega.bsc.id.zk.common.constant.ZkTreeConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author songminghui@naolubrain.com
 * @description
 * @date 2022/7/30 4:42 下午
 */
@Slf4j
public class ZKRegister {

    private final CuratorFramework zkClient;

    private final String serverIp;

    private final Integer serverPort;

    private final ScheduledExecutorService executorService;

    public ZKRegister(CuratorFramework zkClient, String serverIp, Integer serverPort) {
        this.zkClient = zkClient;
        this.serverIp = serverIp;
        this.serverPort = serverPort;

        //noinspection AlibabaThreadPoolCreation
        this.executorService = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "zk-heartbeat-schedule");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void register() {
        try {
            String nodePath = ZkTreeConstant.ZK_SERVER_ROOT + ZkTreeConstant.ZK_PATH_SEPARATOR + AddressUtil.getAddress(serverIp, serverPort);
            byte[] data = ServerNode.getServerNodeDataBytes(serverIp, serverPort);
            if (zkClient.checkExists().creatingParentsIfNeeded().forPath(nodePath) == null) {
                nodePath = zkClient.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(nodePath, data);
                log.info("向zk注册服务：[{}]", nodePath);
            } else {
                zkClient.setData().forPath(nodePath, data);
                log.info("已注册服务，只设置数据:[{}]", nodePath);
            }
        } catch (Exception e) {
            throw new DistributedIdException("向zk注册服务失败", e);
        }
    }

    public void sendHearBeat() {
        this.executorService.scheduleAtFixedRate(() -> {
            try {
                log.info("上传心跳,{}:{}",serverIp,serverPort);
                zkClient.setData().forPath(ZkTreeConstant.ZK_SERVER_ROOT + ZkTreeConstant.ZK_PATH_SEPARATOR + AddressUtil.getAddress(serverIp, serverPort), ServerNode.getServerNodeDataBytes(serverIp, serverPort));
            } catch (KeeperException.NoNodeException noNodeException) {
                //创建临时节点
                register();
            } catch (Exception e) {
                log.error("定时上传心跳失败", e);
            }
        }, 3, 3, TimeUnit.SECONDS);
    }


}
