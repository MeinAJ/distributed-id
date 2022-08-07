package com.geega.bsc.id.starter.dynamic.common.dispatch;

import com.geega.bsc.id.client.dispatch.NodeAddressDispatch;
import com.geega.bsc.id.client.rule.IRule;
import com.geega.bsc.id.common.address.NodeAddress;
import com.geega.bsc.id.starter.dynamic.common.updater.ServerUpdater;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author songminghui@naolubrain.com
 * @description
 * @date 2022/8/6 6:58 下午
 */
public class DynamicNodeAddressDispatch implements NodeAddressDispatch {

    protected List<NodeAddress> nodeAddresses;

    private IRule rule;

    private ReadWriteLock readWriteLock;


    public DynamicNodeAddressDispatch( IRule rule, ServerUpdater serverUpdater) {
        this.rule = rule;
        this.nodeAddresses = new ArrayList<>();
        readWriteLock = new ReentrantReadWriteLock();
        serverUpdater.start(this::doUpdateServer);
    }

    @Override
    public NodeAddress dispatchNodeAddress() {
        Lock lock = readWriteLock.readLock();
        lock.lock();
        try {
            return rule.choose(nodeAddresses);
        } finally {
            lock.unlock();
        }
    }

    protected void doUpdateServer(List<NodeAddress> nodeAddressList) {
        Lock lock = readWriteLock.writeLock();
        lock.lock();
        nodeAddresses = new ArrayList<>(nodeAddressList);
        lock.unlock();
    }
}
