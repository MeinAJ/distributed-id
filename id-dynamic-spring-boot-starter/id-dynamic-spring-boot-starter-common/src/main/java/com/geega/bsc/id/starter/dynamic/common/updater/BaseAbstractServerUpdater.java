package com.geega.bsc.id.starter.dynamic.common.updater;

import com.geega.bsc.id.common.address.NodeAddress;
import com.geega.bsc.id.starter.dynamic.common.notify.ServerUpdateNotify;
import com.geega.bsc.id.starter.dynamic.common.properties.ServerUpdateProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author songminghui@naolubrain.com
 * @description
 * @date 2022/8/7 12:14 上午
 */
@Slf4j
public abstract class BaseAbstractServerUpdater implements ServerUpdater {

    protected ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    protected ServerUpdateProperties serverUpdateProperties;

    protected boolean isStart = false;

    public BaseAbstractServerUpdater(ServerUpdateProperties serverUpdateProperties) {
        this.serverUpdateProperties = serverUpdateProperties;
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("PollingNodeAddressUpdater");
                return thread;
            }
        });

    }

    @Override
    public void start(ServerUpdateNotify notify) {
        if (isStart) {
            log.warn("server updater 已启动,请勿重复启动");
            return;
        }
        isStart = true;
        scheduledThreadPoolExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                List<NodeAddress> nodeAddressList = pollNodeAddress();
                if (nodeAddressList != null) {
                    notify.notifyServerUpdate(nodeAddressList);
                }
            }
        }, serverUpdateProperties.getPollDuration(), serverUpdateProperties.getPollDuration(), TimeUnit.SECONDS);
        startMonitorSever(notify);
    }

    /**
     * 监听服务
     *
     * @param notify
     */
    protected void startMonitorSever(ServerUpdateNotify notify) {

    }

    /**
     * 拉取服务
     *
     * @return
     */
    public abstract List<NodeAddress> pollNodeAddress();
}
