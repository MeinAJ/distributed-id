package com.geega.bsc.id.client.rule;

import com.geega.bsc.id.common.address.NodeAddress;

import java.util.List;

/**
 * @author songminghui@naolubrain.com
 * @description
 * @date 2022/8/5 11:49 下午
 */
public interface IRule {

    NodeAddress choose(List<NodeAddress> nodeAddresses);
}
