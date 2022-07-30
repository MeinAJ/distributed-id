package com.geega.bsc.id.client;

import com.geega.bsc.id.client.cache.CacheConfig;
import com.geega.bsc.id.client.network.IdProcessorDispatch;
import com.geega.bsc.id.client.zk.ZkClient;
import com.geega.bsc.id.zk.common.config.ZkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ID生成器
 *
 * @author Jun.An3
 * @date 2022/07/18
 */
public class IdClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdClient.class);

    private final int capacity;

    private final int trigger;

    private final int expandNum;

    private final LinkedBlockingQueue<Long> idQueue;

    private final ExecutorService executorService;

    private final IdProcessorDispatch processorDispatch;

    private final AtomicBoolean isExpanding = new AtomicBoolean(false);

    public IdClient(ZkConfig zkConfig, CacheConfig cacheConfig) {
        this.capacity = cacheConfig.getCapacity();
        this.trigger = cacheConfig.getTriggerExpand();
        this.expandNum = capacity - trigger;
        assert expandNum > 0;
        this.idQueue = new LinkedBlockingQueue<>(this.capacity);
        this.processorDispatch = new IdProcessorDispatch(new ZkClient(zkConfig), this);
        //noinspection AlibabaThreadPoolCreation
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "Get-Id-Schedule");
            thread.setDaemon(true);
            return thread;
        });
        //如果等待5s无法获取数据，直接抛异常
        this.preloadCache();
    }

    private void preloadCache() {
        executeOnceSync(capacity);
    }

    public Long id(long ms) {
        try {
            return idQueue.poll(ms, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            //do nothing
        } finally {
            trigger();
        }
        return null;
    }

    /**
     * 获取ID
     */
    public Long id() {
        return id(0);
    }

    private void trigger() {
        try {
            //这里存在一个问题，当某次拉取不成功时，isExpanding很可能一直为true，为了解决这个问题
            //当idQueue.size()==0，要么说明刚好使用完缓存，要么说明isExpanding.get()一直等于true
            //正常使用情况下，idQueue.size()==0的概率是很低的，所以采取如下措施：
            //当idQueue.size()==0时，直接将isExpanding设置为false
            if (idQueue.size() == 0) {
                isExpanding.set(false);
            }
            if (idQueue.size() < trigger && !isExpanding.get()) {
                if (isExpanding.compareAndSet(false, true)) {
                    executeOnceAsync(expandNum);
                }
            }
        } catch (Exception ignored) {
            //do nothing
        }
    }

    private void executeOnceAsync(@SuppressWarnings("SameParameterValue") int num) {
        this.executorService.execute(() -> executeOnceSync(num));
    }

    private void executeOnceSync(int num) {
        try {
            this.processorDispatch.dispatch().poll(num);
        } catch (Exception ignored) {
            //do nothing
        }
    }

    /**
     * 缓存ID
     */
    public void cache(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            for (Long id : ids) {
                idQueue.offer(id);
            }
        }
        isExpanding.set(false);
        LOGGER.info("拉取ID数据：[{}]", ids);
    }

}
