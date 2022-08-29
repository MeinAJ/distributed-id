package com.geega.bsc.id.client.network;

import com.alibaba.fastjson.JSON;
import com.geega.bsc.id.client.IdClient;
import com.geega.bsc.id.client.zk.ZkClient;
import com.geega.bsc.id.common.address.ServerNode;
import com.geega.bsc.id.common.exception.DistributedIdException;
import com.geega.bsc.id.common.network.DistributedIdChannel;
import com.geega.bsc.id.common.network.IdGeneratorTransportLayer;
import com.geega.bsc.id.common.network.NetworkReceive;
import com.geega.bsc.id.common.utils.AddressUtil;
import com.geega.bsc.id.common.utils.ByteBufferUtil;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Jun.An3
 * @date 2022/07/25
 */
@Slf4j
public class IdProcessor {

    private final ZkClient zkClient;

    private final String id;

    private final IdClient generator;

    private DistributedIdChannel distributedIdChannel;

    private Selector selector;

    private final List<NetworkReceive> completedReceives;

    /**
     * 0:正在初始化，1：建立好连接 2：断开连接
     */
    private volatile int connectionState = 0;

    private final Deque<NetworkReceive> stagedReceives;

    private final ExecutorService executorService;

    private SocketChannel channel;

    public IdProcessor(ZkClient zkClient, String id, IdClient generator, ServerNode nodeAddress) {
        this.zkClient = zkClient;
        this.id = id;
        this.generator = generator;
        this.completedReceives = new ArrayList<>();
        this.stagedReceives = new ArrayDeque<>();
        this.init(nodeAddress.getIp(), nodeAddress.getPort());
        //noinspection AlibabaThreadPoolCreation
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "Sender-Schedule");
            thread.setDaemon(true);
            return thread;
        });
        this.executorService.execute(new Sender());
    }

    private void init(String ip, int port) {
        try {
            this.channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.socket().setTcpNoDelay(true);
            channel.socket().setKeepAlive(true);
            channel.socket().setSendBufferSize(1024);
            channel.connect(new InetSocketAddress(ip, port));
            this.selector = Selector.open();

            //注册到selector，监听事件为连接事件
            SelectionKey selectionKey = channel.register(this.selector, SelectionKey.OP_CONNECT);

            distributedIdChannel = buildChannel(String.valueOf(id), selectionKey, 1024 * 1024);
            selectionKey.attach(distributedIdChannel);

            //等待连接上
            //noinspection LoopStatementThatDoesntLoop
            while (true) {
                this.selector.select();
                Iterator<SelectionKey> keysIterator = this.selector.selectedKeys().iterator();
                while (keysIterator.hasNext()) {
                    SelectionKey key = keysIterator.next();
                    keysIterator.remove();
                    if (key.isConnectable()) {
                        //监听连接事件
                        if (channel.isConnectionPending()) {
                            if (channel.finishConnect()) {
                                //设置可读事件，意思是从服务端有消息来时，提醒我;同时移除OP_CONNECT事件
                                connectionState = 1;
                                //关闭建立事件，打开读事件
                                removeInterestOps(key, SelectionKey.OP_CONNECT);
                                addInterestOps(key, SelectionKey.OP_READ);
                                //注册客户端到zk
                                zkClient.register(channel);
                                log.info("创建连接:[{}]", AddressUtil.getConnectionId(channel));
                                break;
                            } else {
                                key.cancel();
                            }
                        } else {
                            key.cancel();
                        }
                    }
                }
                break;
            }
        } catch (Exception e) {
            throw new DistributedIdException("建立连接异常", e);
        } finally {
            if (connectionState != 1) {
                connectionState = 2;
                close();
            }
        }
    }

    public SocketChannel getSocketChannel() {
        return this.channel;
    }

    class Sender implements Runnable {

        @Override
        public void run() {
            while (connectionState == 1) {
                try {
                    //监听操作系统是否有事件，事件来时，操作系统回调函数，让你阻塞状态唤醒
                    selector.select();
                    Iterator<SelectionKey> keysIterator = selector.selectedKeys().iterator();
                    while (keysIterator.hasNext()) {
                        SelectionKey key = keysIterator.next();
                        keysIterator.remove();
                        //读取数据
                        if (key.isReadable() && !hasStagedReceive()) {
                            NetworkReceive networkReceive;
                            while ((networkReceive = distributedIdChannel.read()) != null) {
                                addToStagedReceives(networkReceive);
                            }
                        } else if (key.isWritable()) {
                            //写数据
                            distributedIdChannel.write();
                        }
                        if (!key.isValid()) {
                            //修改状态
                            connectionState = 2;
                        }
                    }
                    stagedToCompletedReceives();
                    handleCompletedReceives();
                } catch (IOException e) {
                    connectionState = 2;
                } catch (Exception e) {
                    log.error("读写错误", e);
                } finally {
                    if (connectionState == 2) {
                        //关闭连接和释放资源
                        close(distributedIdChannel);
                    }
                }
            }
        }

    }

    boolean isValid() {
        return connectionState == 1;
    }

    void close() {
        try {
            close(distributedIdChannel);
        } catch (Exception ignored) {
            //do nothing
        } finally {
            log.warn("关闭连接：{}", distributedIdChannel.socketDescription());
        }
    }

    private void close(DistributedIdChannel channel) {
        try {
            channel.close();
            this.stagedReceives.clear();
            this.executorService.shutdown();
            this.completedReceives.clear();
            this.selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addToStagedReceives(NetworkReceive receive) {
        stagedReceives.add(receive);
    }

    private boolean hasStagedReceive() {
        return stagedReceives.size() > 0;
    }

    public void poll(int num) {
        //放入请求数据，如果上一次请求还没发送，就不用再次发送
        distributedIdChannel.setSend(ByteBufferUtil.getSend(id, num));
        //不管怎样，唤醒selector
        selector.wakeup();
    }

    private void handleCompletedReceives() {
        if (!this.completedReceives.isEmpty()) {
            Iterator<NetworkReceive> iterator = completedReceives.iterator();
            while (iterator.hasNext()) {
                ByteBuffer payload = iterator.next().payload();
                iterator.remove();
                String idsJsonString = ByteBufferUtil.byteBufferToString(payload);
                if (idsJsonString != null && idsJsonString.length() > 0) {
                    List<Long> ids = JSON.parseArray(idsJsonString, Long.class);
                    generator.cache(ids);
                }
            }
        }
    }

    private void stagedToCompletedReceives() {
        if (!this.stagedReceives.isEmpty()) {
            Iterator<NetworkReceive> iterator = this.stagedReceives.iterator();
            if (distributedIdChannel.isReadEvent()) {
                while (iterator.hasNext()) {
                    this.completedReceives.add(iterator.next());
                    iterator.remove();
                }
            }
        }
    }

    private DistributedIdChannel buildChannel(String id, SelectionKey key, @SuppressWarnings("SameParameterValue") int maxReceiveSize) throws DistributedIdException {
        DistributedIdChannel channel;
        try {
            IdGeneratorTransportLayer transportLayer = new IdGeneratorTransportLayer(key);
            channel = new DistributedIdChannel(id, transportLayer, maxReceiveSize);
        } catch (Exception e) {
            throw new DistributedIdException(e);
        }
        return channel;
    }

    private void addInterestOps(SelectionKey key, @SuppressWarnings("SameParameterValue") int ops) {
        key.interestOps(key.interestOps() | ops);

    }

    private void removeInterestOps(SelectionKey key, @SuppressWarnings("SameParameterValue") int ops) {
        key.interestOps(key.interestOps() & ~ops);
    }

}
