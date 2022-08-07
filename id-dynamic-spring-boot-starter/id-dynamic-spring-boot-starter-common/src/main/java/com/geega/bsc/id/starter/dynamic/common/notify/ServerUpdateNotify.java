package com.geega.bsc.id.starter.dynamic.common.notify;

import com.geega.bsc.id.common.address.NodeAddress;

import java.util.List;

/**
 * @author songminghui@naolubrain.com
 * @description
 * @date 2022/8/7 12:13 上午
 */
public interface ServerUpdateNotify {

    /**
     * 服务更新通知
     * @param nodeAddressList
     */
    void notifyServerUpdate(List<NodeAddress> nodeAddressList);
}
