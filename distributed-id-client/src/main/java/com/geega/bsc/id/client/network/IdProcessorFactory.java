package com.geega.bsc.id.client.network;

import com.geega.bsc.id.client.dispatch.NodeAddressDispatch;
import com.geega.bsc.id.common.address.NodeAddress;
import com.geega.bsc.id.common.exception.DistributedIdException;
import com.geega.bsc.id.common.utils.AddressUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 处理器工厂
 *
 * @author Jun.An3
 * @date 2022/07/18
 */
@Slf4j
public class IdProcessorFactory {

    private volatile IdProcessor currentProcessor;

    private final AtomicInteger id = new AtomicInteger(1);

    private NodeAddressDispatch nodeAddressDispatch;

    //最多一个连接取数据的次数
    private final static int MAX_TIMES_FETCH_PER_NODE = 10;

    private final AtomicInteger nodeFetchTimes = new AtomicInteger(0);

    public IdProcessorFactory(NodeAddressDispatch address) {
        this.nodeAddressDispatch = address;
    }

    public IdProcessor create() {
        if (currentProcessor != null && currentProcessor.isValid() && nodeFetchTimes.getAndIncrement() <= MAX_TIMES_FETCH_PER_NODE) {
            log.info("使用连接：[{}]", AddressUtil.getConnectionId(currentProcessor.getSocketChannel()));
            return currentProcessor;
        }
        closeCurrentProcess();
        innerCreate();
        return currentProcessor;
    }

    private void innerCreate() {
        if (currentProcessor == null || !currentProcessor.isValid()) {
            synchronized (this) {
                if (currentProcessor == null || !currentProcessor.isValid()) {
                    NodeAddress nodeAddress = nodeAddressDispatch.dispatchNodeAddress();
                    if (nodeAddress == null) {
                        throw new DistributedIdException("无可用服务");
                    }
                    currentProcessor = new IdProcessor(String.valueOf(id.getAndIncrement()), nodeAddress);
                    nodeFetchTimes.set(0);
                }
            }
        }
    }

    private void closeCurrentProcess() {
        synchronized (this) {
            if (this.currentProcessor != null) {
                this.currentProcessor.close();
                //help gc
                this.currentProcessor = null;
            }
        }
    }


}
