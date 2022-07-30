package com.geega.bsc.id.server.network;

import com.geega.bsc.id.common.network.Send;

/**
 * @author Jun.An3
 * @date 2022/07/19
 */
public class Response {

    private final int processorId;

    private final String destination;

    private final Send send;

    public Response(String destination, int processorId, Send send) {
        this.destination = destination;
        this.processorId = processorId;
        this.send = send;
    }

    public String getDestination() {
        return this.destination;
    }

    public Send getSend() {
        return this.send;
    }

    public int getProcessorId() {
        return this.processorId;
    }

}
