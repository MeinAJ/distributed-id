package com.geega.bsc.id.server.network;

import java.nio.ByteBuffer;
import java.nio.channels.Selector;

/**
 * @author Jun.An3
 * @date 2022/07/18
 */
public class Request {

    private final int processorId;

    private final String connectionId;

    private final ByteBuffer data;

    private final Selector selector;

    public Request(Selector selector, ByteBuffer data, String connectionId, int processorId) {
        this.selector = selector;
        this.data = data;
        this.connectionId = connectionId;
        this.processorId = processorId;
    }

    public String getConnectionId() {
        return this.connectionId;
    }

    public int getProcessorId() {
        return this.processorId;
    }

    public ByteBuffer getData() {
        return this.data;
    }

    public Selector getSelector() {
        return selector;
    }

}
