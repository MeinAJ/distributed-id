package com.geega.bsc.id.server.zk;

import com.geega.bsc.id.server.IdServer;
import com.geega.bsc.id.server.zk.register.ServerRegister;

/**
 * @author songminghui@naolubrain.com
 * @description
 * @date 2022/7/30 5:27 下午
 */
public class ZkIdServer extends IdServer {

    public static void main(String[] args) throws InterruptedException {
        new ZkIdServer().start();
    }

    @Override
    protected void doAfterStart() {
        new ServerRegister().register();
        super.doAfterStart();
    }
}
