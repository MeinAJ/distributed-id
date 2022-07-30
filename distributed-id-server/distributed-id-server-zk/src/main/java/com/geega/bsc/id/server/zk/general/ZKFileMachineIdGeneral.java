package com.geega.bsc.id.server.zk.general;

import cn.hutool.core.io.FileUtil;
import com.geega.bsc.id.common.exception.DistributedIdException;
import com.geega.bsc.id.server.config.ServerConfig;
import com.geega.bsc.id.server.general.BaseMachineIdGeneral;
import com.geega.bsc.id.server.load.ServerConfigLoader;
import com.geega.bsc.id.server.zk.config.MachineIdFileConfig;
import com.geega.bsc.id.server.zk.loader.MachineIdFileConfigLoader;
import com.geega.bsc.id.server.zk.loader.ZkConfigLoader;
import com.geega.bsc.id.zk.common.address.ServerNode;
import com.geega.bsc.id.zk.common.config.ZkConfig;
import com.geega.bsc.id.zk.common.constant.ZkTreeConstant;
import com.geega.bsc.id.zk.common.server.ZKServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;

/**
 * @author songminghui@naolubrain.com
 * @description
 * @date 2022/7/30 6:08 下午
 */
@Slf4j
public class ZKFileMachineIdGeneral extends BaseMachineIdGeneral {

    private final MachineIdFileConfig config;

    public ZKFileMachineIdGeneral() {
        this.config = MachineIdFileConfigLoader.loadMachineIdFileConfig();
    }

    @Override
    protected Long generalId() {
        try {
            Long cacheWorkId = readWorkId();
            if (cacheWorkId != null && cacheWorkId != -1) {
                log.info("获取本地workId：[{}]", cacheWorkId);
                return cacheWorkId;
            } else {
                ZkConfig zkConfig = ZkConfigLoader.loadZkConfig();
                ServerConfig serverConfig = ServerConfigLoader.loadServerConfig();
                CuratorFramework zkClient = ZKServer.getInstance(zkConfig).getZkClient();
                final String nodePath = zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(ZkTreeConstant.ZK_WORK_ID_ROOT + ZkTreeConstant.ZK_PATH_SEPARATOR + "workid-", ServerNode.getServerNodeDataBytes(serverConfig.getIp(), serverConfig.getPort()));
                Long workId = parseWorkId(nodePath);
                saveWorkId(workId);
                log.info("创建Zk-workId：[{}]", workId);
                return workId;
            }
        } catch (Exception e) {
            throw new DistributedIdException("创建自增workId失败", e);
        }
    }

    public synchronized Long readWorkId() {
        String filePath = getWorkIdFilePath();
        try {
            BufferedReader buffer = new BufferedReader(new FileReader(filePath));
            return Long.valueOf(buffer.readLine());
        } catch (Exception e) {
            //do nothing
            log.warn("无法从【{}】读取workId", filePath);
        }
        return -1L;
    }

    public synchronized void saveWorkId(Long workId) {
        FileUtil.mkParentDirs(getWorkIdFilePath());
        saveWorkIdToFile(workId);
    }

    private void saveWorkIdToFile(Long workId) {
        PrintStream stream;
        try {
            //写入的文件path
            stream = new PrintStream(getWorkIdFilePath());
            //写入的字符串
            stream.print(workId);
            log.info("写入本地文件，workId：{}", workId);
        } catch (Exception e) {
            throw new DistributedIdException("无法保存workId至文件中，workId：" + workId);
        }
    }

    private Long parseWorkId(String nodePath) {
        String sequentialId = nodePath.replaceAll(ZkTreeConstant.ZK_WORK_ID_ROOT + ZkTreeConstant.ZK_PATH_SEPARATOR + "workid-", "");
        return Long.valueOf(sequentialId);
    }

    private String getWorkIdFilePath() {
        return config.getRoot() + File.separator + config.getName();
    }
}
