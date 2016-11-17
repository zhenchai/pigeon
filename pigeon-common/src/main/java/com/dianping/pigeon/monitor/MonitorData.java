package com.dianping.pigeon.monitor;

import com.dianping.pigeon.util.MapUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author qi.yin
 *         2016/11/08  下午3:11.
 */
public class MonitorData {

    private AtomicLong totalCount = new AtomicLong();

    private AtomicLong totalSuccess = new AtomicLong();

    private AtomicLong totalFailed = new AtomicLong();

    private AtomicLong maxElapsed = new AtomicLong();

    private AtomicLong minElapsed = new AtomicLong(Long.MAX_VALUE);

    private AtomicLong totalElapsed = new AtomicLong();

    private volatile ConcurrentNavigableMap<Long, AtomicLong> elapseds = new ConcurrentSkipListMap<Long, AtomicLong>();

    public MonitorData() {

    }

    public long getTotalCount() {
        return totalCount.get();
    }

    public long getTotalSuccess() {
        return totalSuccess.get();
    }

    public long getTotalFailed() {
        return totalFailed.get();
    }

    public long getMaxElasped() {
        return maxElapsed.get();
    }

    public long getMinElasped() {
        return minElapsed.get();
    }

    public long getAvgElasped() {
        return totalElapsed.get() / (totalSuccess.get() + totalFailed.get());
    }

    public long get95thPercentile() {
        return getPercentile(0.95);
    }

    public long get99thPercentile() {
        return getPercentile(0.99);
    }

    public long get999thPercentile() {
        return getPercentile(0.999);
    }

    public long getPercentile(double delta) {
        List<Long> keys = new ArrayList<Long>(elapseds.keySet());

        long totalCount = totalSuccess.get() + totalFailed.get();
        long tempCount = 0;
        long key = 0;

        for (int i = keys.size() - 1; i >= 0; i++) {

            key = keys.get(i);
            AtomicLong value = elapseds.get(key);

            if (Math.abs((tempCount * 1.0 / totalCount) - (1 - delta)) > 1e-6) {
                break;
            }

            tempCount += value.get();
        }
        return key;
    }

    public void incTotalCount() {
        this.totalCount.incrementAndGet();
    }

    public void incTotalSuccess() {
        this.totalSuccess.incrementAndGet();
    }

    public void incTotalFailed() {
        this.totalFailed.incrementAndGet();
    }

    public void setElapsed(long elapsed) {
        setMaxElapsed(elapsed);
        setMinElapsed(elapsed);
        setTotalElapsed(elapsed);
        setElapseds(elapsed);
    }

    public void setMaxElapsed(long elapsed) {
        for (; ; ) {
            long lastElapsed = maxElapsed.get();

            if (elapsed > lastElapsed) {
                if (maxElapsed.compareAndSet(lastElapsed, elapsed)) {
                    break;
                }
            } else {
                break;
            }
        }
    }

    public void setMinElapsed(long elapsed) {
        for (; ; ) {
            long lastElapsed = minElapsed.get();

            if (elapsed < lastElapsed) {
                if (minElapsed.compareAndSet(lastElapsed, elapsed)) {
                    break;
                }
            } else {
                break;
            }
        }
    }

    public void setTotalElapsed(long elapsed) {
        this.totalElapsed.addAndGet(elapsed);
    }

    public void setElapseds(long elapsed) {
        long duration = computeDuration(elapsed);

        AtomicLong count = MapUtils.getOrCreate(elapseds, duration, AtomicLong.class);

        count.incrementAndGet();
    }


    protected long computeDuration(long duration) {
        if (duration < 20) {
            return duration;
        } else if (duration < 200) {
            return duration - duration % 5;
        } else if (duration < 2000) {
            return duration - duration % 50;
        } else {
            return duration - duration % 500;
        }
    }

}
