package com.geega.bsc.id.server.zk.loader;

import com.geega.bsc.id.common.exception.DistributedIdException;
import com.geega.bsc.id.common.utils.ResourcesUtil;
import com.geega.bsc.id.server.zk.config.ZkConfigConst;
import com.geega.bsc.id.zk.common.config.ZkConfig;

import java.util.Properties;

/**
 * @author songminghui@naolubrain.com
 * @description
 * @date 2022/7/30 5:28 下午
 */
public class ZkConfigLoader {

    private static ZkConfig zkConfig;

    public static synchronized ZkConfig loadZkConfig() {
        if (zkConfig != null) {
            return zkConfig;
        }
        try {
            zkConfig = new ZkConfig();

            Properties properties = ResourcesUtil.getProperties("/application.properties");
            zkConfig.setConnection(properties.getProperty(ZkConfigConst.ZK_CONNECTION, "127.0.0.1:2181"));
            zkConfig.setNamespace(properties.getProperty(ZkConfigConst.ZK_NAMESPACE, "default"));
            zkConfig.setConnectionTimeoutMs(Integer.valueOf(properties.getProperty(ZkConfigConst.ZK_CONNECTION_TIMEOUT_MS, "10000")));
            zkConfig.setSessionTimeoutMs(Integer.valueOf(properties.getProperty(ZkConfigConst.ZK_SESSION_TIMEOUT_MS, "10000")));
            return zkConfig;
        } catch (Exception e) {
            zkConfig = null;
            throw new DistributedIdException("读取zk配置文件异常", e);
        }
    }
}
