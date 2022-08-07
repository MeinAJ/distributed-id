package com.geega.bsc.id.client.dispatch;

import com.geega.bsc.id.common.address.NodeAddress;

/**
 * @author songminghui@naolubrain.com
 * @description
 * @date 2022/8/2 12:02 上午
 */
public interface NodeAddressDispatch {

    /**
     * 分配地址
     *
     * @return
     */
    NodeAddress dispatchNodeAddress();

}
