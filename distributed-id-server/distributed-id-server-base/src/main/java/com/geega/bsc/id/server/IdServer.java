package com.geega.bsc.id.server;

import com.geega.bsc.id.common.sync.Sync;
import com.geega.bsc.id.server.config.ServerConfig;
import com.geega.bsc.id.server.general.SnowFlake;
import com.geega.bsc.id.server.load.ServerConfigLoader;
import com.geega.bsc.id.server.network.ServerAcceptor;
import com.geega.bsc.id.server.network.ServerRequestCache;
import com.geega.bsc.id.server.network.ServerRequestHandler;
import com.geega.bsc.id.server.spi.MachineIdSpi;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jun.An3
 * @date 2022/07/21
 */
@Slf4j
public class IdServer {

    public static void main(String[] args) throws Exception {
        log.info("info");
        log.debug("debug");
        new IdServer().start();
    }

    public void start() throws InterruptedException {
        //同步组件
        Sync sync = new Sync();

        //读取配置文件
        ServerConfig serverConfig = ServerConfigLoader.loadServerConfig();

        //网络包处理缓冲器
        ServerRequestCache requestChannel = new ServerRequestCache();

        //连接接收器
        ServerAcceptor acceptor = new ServerAcceptor(requestChannel, serverConfig);
        acceptor.start();

        //请求处理器
        ServerRequestHandler requestHandler = new ServerRequestHandler(requestChannel, new SnowFlake(serverConfig.getIdDatacenter(), MachineIdSpi.getMachineId()));
        requestHandler.start();
        log.info("服务启动,{}:{}",serverConfig.getIp(),serverConfig.getPort());
        //启动后做一些事，用于拓展
        doAfterStart();
        //hold住
        sync.waitShutdown();
    }


    protected void doAfterStart() {

    }

}
