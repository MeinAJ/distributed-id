package com.geega.bsc.id.starter.dynamic.common.updater;

import com.geega.bsc.id.starter.dynamic.common.notify.ServerUpdateNotify;

/**
 * @author songminghui@naolubrain.com
 * @description
 * @date 2022/8/6 6:24 下午
 */
public interface ServerUpdater {

    /**
     * 更新服务
     *
     * @return
     */
    void start(ServerUpdateNotify notify);
}
