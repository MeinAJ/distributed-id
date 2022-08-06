package com.geega.bsc.id.client.rule;

import com.geega.bsc.id.common.address.NodeAddress;

import java.util.List;
import java.util.Random;

/**
 * @author songminghui@naolubrain.com
 * @description
 * @date 2022/8/5 11:53 下午
 */
public class RandomRule implements IRule {
    @Override
    public NodeAddress choose(List<NodeAddress> nodeAddresses) {
        if (nodeAddresses == null || nodeAddresses.isEmpty()) {
            return null;
        }
        int number = new Random().nextInt(nodeAddresses.size());
        return nodeAddresses.get(number);
    }
}
