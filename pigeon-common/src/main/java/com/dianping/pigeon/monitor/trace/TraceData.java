package com.dianping.pigeon.monitor.trace;

import com.dianping.pigeon.util.MapUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author qi.yin
 *         2016/11/17  下午2:04.
 */
public class TraceData {

    private AtomicLong totalCount = new AtomicLong();

    private AtomicLong totalSuccess = new AtomicLong();

    private AtomicLong totalFailed = new AtomicLong();

    private AtomicLong maxElapsed = new AtomicLong();

    private AtomicLong minElapsed = new AtomicLong(Long.MAX_VALUE);

    private long avgElasped;

    private long elasped95th;

    private long elasped99th;

    private long elasped999th;

    private transient AtomicLong totalElapsed = new AtomicLong();

    private transient ConcurrentNavigableMap<Long, AtomicLong> elapseds = new ConcurrentSkipListMap<Long, AtomicLong>();

    public TraceData() {

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

    public long getElasped95th() {
        return getPercentile(0.95);
    }

    public long getElasped99th() {
        return getPercentile(0.99);
    }

    public long getElasped999th() {
        return getPercentile(0.999);
    }

    public void setElasped95th(long elasped95th) {
        this.elasped95th = elasped95th;
    }

    public void setAvgElasped(long avgElasped) {
        this.avgElasped = avgElasped;
    }

    public void setElasped99th(long elasped99th) {
        this.elasped99th = elasped99th;
    }

    public void setElasped999th(long elasped999th) {
        this.elasped999th = elasped999th;
    }

    public long getPercentile(double delta) {
        List<Long> keys = new ArrayList<Long>(elapseds.keySet());

        long totalCount = totalSuccess.get() + totalFailed.get();
        long tempCount = 0;
        long key = 0;

        for (int i = keys.size() - 1; i >= 0; i++) {

            key = keys.get(i);
            AtomicLong value = elapseds.get(key);

            if (Math.abs((tempCount * 1.0 / totalCount) - (1 - delta)) < 1e-6) {
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
