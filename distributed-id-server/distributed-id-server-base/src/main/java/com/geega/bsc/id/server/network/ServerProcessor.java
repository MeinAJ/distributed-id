package com.geega.bsc.id.server.network;

import com.geega.bsc.id.common.exception.DistributedIdException;
import com.geega.bsc.id.common.network.DistributedIdChannel;
import com.geega.bsc.id.common.network.IdGeneratorTransportLayer;
import com.geega.bsc.id.common.network.NetworkReceive;
import com.geega.bsc.id.common.network.Send;
import com.geega.bsc.id.common.utils.AddressUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Jun.An3
 * @date 2022/07/18
 */
public class ServerProcessor extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerProcessor.class);

    private final ConcurrentHashMap<String, DistributedIdChannel> channels;

    private final List<NetworkReceive> completedReceives;

    private final Map<DistributedIdChannel, Deque<NetworkReceive>> stagedReceives;

    private final List<Send> completedSends;

    private final ConcurrentLinkedQueue<SocketChannel> newConnections;

    private final ConcurrentLinkedQueue<DistributedIdChannel> waitCloseConnections;

    private final ServerRequestCache requestChannel;

    private Selector selector;

    private final int processorId;

    public void addChannel(SocketChannel channel) {
        this.newConnections.add(channel);
        selector.wakeup();
    }

    public ServerProcessor(int processorId, ServerRequestCache requestChannel) {
        this.processorId = processorId;
        this.requestChannel = requestChannel;
        this.channels = new ConcurrentHashMap<>();
        this.newConnections = new ConcurrentLinkedQueue<>();
        this.waitCloseConnections = new ConcurrentLinkedQueue<>();
        this.completedReceives = new ArrayList<>();
        this.stagedReceives = new HashMap<>();
        this.completedSends = new ArrayList<>();
        this.init();
    }

    private void init() {
        try {
            this.selector = Selector.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                //从队列中拉取数据出来,完成selector注册
                configureNewConnections();
                //处理数据,将数据发送出去,就是写
                processNewResponses();
                //尝试拉取事件
                poll();
                addToCompletedReceives();
                //对于读取到的请求,进行处理,然后处理完后,可能会把结果放入newResponses中,等待被发送出去
                processCompletedReceives();
                //处理发送数据成功
                processCompletedSends();
                //处理待关闭连接
                processDisconnected();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void processCompletedReceives() {
        if (!completedReceives.isEmpty()) {
            Iterator<NetworkReceive> iterator = completedReceives.iterator();
            while (iterator.hasNext()) {
                NetworkReceive completedReceive = iterator.next();
                iterator.remove();
                DistributedIdChannel distributedIdChannel = channels.get(completedReceive.source());
                Request request = new Request(selector, completedReceive.payload(), distributedIdChannel.id(), processorId);
                requestChannel.addRequest(request);
                distributedIdChannel.mute();
            }
        }
    }

    private void poll() {
        try {
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {

                SelectionKey selectionKey = iterator.next();
                iterator.remove();

                DistributedIdChannel distributedIdChannel = (DistributedIdChannel) selectionKey.attachment();
                try {
                    if (selectionKey.isConnectable()) {
                        if (!distributedIdChannel.finishConnect()) {
                            continue;
                        }
                    }

                    if (selectionKey.isReadable() && !hasStagedReceive(distributedIdChannel)) {
                        NetworkReceive networkReceive;
                        while ((networkReceive = distributedIdChannel.read()) != null) {
                            addToStagedReceives(distributedIdChannel, networkReceive);
                        }
                    }
                    if (selectionKey.isWritable()) {
                        Send send = distributedIdChannel.write();
                        if (send != null) {
                            this.completedSends.add(send);
                        }
                    }
                    if (!selectionKey.isValid()) {
                        this.waitCloseConnections.offer(distributedIdChannel);
                    }
                } catch (IOException exception) {
                    this.waitCloseConnections.offer(distributedIdChannel);
                    LOGGER.error("IO异常，连接：{}", distributedIdChannel.socketDescription());
                } catch (Exception e) {
                    LOGGER.error("未知异常", e);
                }
            }
        } catch (Exception e) {
            LOGGER.error("未知异常", e);
        }
    }

    private void processNewResponses() {
        Response curr = requestChannel.getResponse(processorId);
        while (curr != null) {
            try {
                sendResponse(curr);
            } finally {
                curr = requestChannel.getResponse(processorId);
            }
        }
    }

    private void sendResponse(Response curr) {
        DistributedIdChannel channel = channels.get(curr.getDestination());
        channel.setSend(curr.getSend());
    }

    private void configureNewConnections() {
        while (!newConnections.isEmpty()) {
            SocketChannel channel = newConnections.poll();
            try {
                if (channel != null) {
                    String connectionId = AddressUtil.getConnectionId(channel);
                    LOGGER.info("创建连接：[{}]", connectionId);
                    SelectionKey selectionKey = channel.register(selector, SelectionKey.OP_READ);
                    DistributedIdChannel distributedIdChannel = buildChannel(connectionId, selectionKey, 1024);
                    selectionKey.attach(distributedIdChannel);
                    this.channels.put(connectionId, distributedIdChannel);
                }
            } catch (Exception e) {
                LOGGER.error("创建连接失败", e);
            }
        }
    }

    private DistributedIdChannel buildChannel(String id, SelectionKey key, @SuppressWarnings("SameParameterValue") int maxReceiveSize) throws DistributedIdException {
        DistributedIdChannel channel;
        try {
            IdGeneratorTransportLayer transportLayer = new IdGeneratorTransportLayer(key);
            channel = new DistributedIdChannel(id, transportLayer, maxReceiveSize);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DistributedIdException(e);
        }
        return channel;
    }

    private void processCompletedSends() {
        for (Send send : completedSends) {
            String destination = send.destination();
            DistributedIdChannel distributedIdChannel = channels.get(destination);
            distributedIdChannel.unmute();
        }
    }

    /**
     * 关闭异常连接
     */
    private void processDisconnected() {
        while (!waitCloseConnections.isEmpty()) {
            DistributedIdChannel distributedIdChannel = waitCloseConnections.poll();
            try {
                if (distributedIdChannel != null) {
                    LOGGER.warn("关闭异常连接:{}", distributedIdChannel.socketDescription());
                    distributedIdChannel.close();
                }
            } catch (Exception e) {
                LOGGER.error("关闭连接异常", e);
            }
        }
    }

    private void addToStagedReceives(DistributedIdChannel channel, NetworkReceive receive) {
        if (!stagedReceives.containsKey(channel)) {
            stagedReceives.put(channel, new ArrayDeque<>());
        }
        Deque<NetworkReceive> deque = stagedReceives.get(channel);
        deque.add(receive);
    }

    private boolean hasStagedReceive(DistributedIdChannel channel) {
        return stagedReceives.containsKey(channel);
    }

    private void addToCompletedReceives() {
        if (!this.stagedReceives.isEmpty()) {
            Iterator<Map.Entry<DistributedIdChannel, Deque<NetworkReceive>>> iter = this.stagedReceives.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<DistributedIdChannel, Deque<NetworkReceive>> entry = iter.next();
                DistributedIdChannel channel = entry.getKey();
                if (!channel.isMute()) {
                    Deque<NetworkReceive> deque = entry.getValue();
                    NetworkReceive networkReceive = deque.poll();
                    this.completedReceives.add(networkReceive);
                    if (deque.isEmpty()) {
                        iter.remove();
                    }
                }
            }
        }
    }

}
