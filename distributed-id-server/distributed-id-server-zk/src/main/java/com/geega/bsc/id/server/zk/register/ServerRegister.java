package com.geega.bsc.id.server.zk.register;

import com.geega.bsc.id.server.config.ServerConfig;
import com.geega.bsc.id.server.load.ServerConfigLoader;
import com.geega.bsc.id.server.zk.loader.ZkConfigLoader;
import com.geega.bsc.id.zk.common.config.ZkConfig;
import com.geega.bsc.id.zk.common.server.ZKServer;

/**
 * @author songminghui@naolubrain.com
 * @description
 * @date 2022/7/30 6:38 下午
 */
public class ServerRegister {

    public void register() {
        ServerConfig serverConfig = ServerConfigLoader.loadServerConfig();
        ZkConfig zkConfig = ZkConfigLoader.loadZkConfig();
        ZKRegister zkRegister = new ZKRegister(ZKServer.getInstance(zkConfig).getZkClient(), serverConfig.getIp(), serverConfig.getPort());
        zkRegister.register();
        zkRegister.sendHearBeat();
    }
}
