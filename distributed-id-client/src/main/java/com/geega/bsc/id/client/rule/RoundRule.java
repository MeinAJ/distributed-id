package com.geega.bsc.id.client.rule;

import com.geega.bsc.id.common.address.NodeAddress;

import java.util.List;

/**
 * @author songminghui@naolubrain.com
 * @description
 * @date 2022/8/5 11:56 下午
 */
public class RoundRule implements IRule {

    private long count = 0L;

    private static final long MAX_COUNT = 1000000L;

    @Override
    public NodeAddress choose(List<NodeAddress> nodeAddresses) {
        if (nodeAddresses == null || nodeAddresses.isEmpty()) {
            return null;
        }
        int number = (int) (count % nodeAddresses.size());
        count++;
        if (count > MAX_COUNT) {
            count = 0;
        }
        return nodeAddresses.get(number);
    }
}
