package com.geega.bsc.id.client.dispatch;

import com.geega.bsc.id.client.dispatch.NodeAddressDispatch;
import com.geega.bsc.id.client.rule.IRule;
import com.geega.bsc.id.common.address.NodeAddress;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author songminghui@naolubrain.com
 * @description
 * @date 2022/8/5 11:41 下午
 */
@Slf4j
public class DefaultNodeAddressDispatch implements NodeAddressDispatch {

    private List<NodeAddress> nodeAddresses;

    private IRule rule;

    public DefaultNodeAddressDispatch(List<NodeAddress> nodeAddresses, IRule rule) {
        this.rule = rule;
        this.nodeAddresses = nodeAddresses;
    }


    @Override
    public NodeAddress dispatchNodeAddress() {
        NodeAddress nodeAddress = rule.choose(nodeAddresses);
        log.debug("分配地址{}:{}", nodeAddress.getIp(), nodeAddress.getPort());
        return nodeAddress;
    }
}
