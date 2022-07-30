package com.geega.bsc.id.server.network;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Jun.An3
 * @date 2022/07/18
 */
public class ServerRequestCache {

    private final ArrayBlockingQueue<Request> requestQueue;

    private final ConcurrentHashMap<Integer, LinkedBlockingQueue<Response>> processorResponseQueue;

    public ServerRequestCache() {
        this.requestQueue = new ArrayBlockingQueue<>(16);
        this.processorResponseQueue = new ConcurrentHashMap<>();
    }

    void addResponse(Integer processorId, Response response) {
        LinkedBlockingQueue<Response> responses = processorResponseQueue.get(processorId);
        if (responses == null) {
            synchronized (this) {
                if (!processorResponseQueue.containsKey(processorId)) {
                    processorResponseQueue.put(processorId, new LinkedBlockingQueue<>());
                    LinkedBlockingQueue<Response> responsesQueue = processorResponseQueue.get(processorId);
                    responsesQueue.add(response);
                }
            }
        } else {
            responses.add(response);
        }
    }

    Response getResponse(Integer processorId) {
        Response response = null;
        LinkedBlockingQueue<Response> responses = processorResponseQueue.get(processorId);
        if (responses != null) {
            response = responses.poll();
        }
        return response;
    }

    void addRequest(Request request) {
        requestQueue.offer(request);
    }

    Request getRequest(@SuppressWarnings("SameParameterValue") long timeout) throws InterruptedException {
        return requestQueue.poll(timeout, TimeUnit.MILLISECONDS);
    }

}
