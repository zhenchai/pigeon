package com.dianping.pigeon.remoting.common.monitor.trace;

import com.dianping.pigeon.util.MapUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author qi.yin
 *         2016/11/17  下午1:32.
 */
public class AbstractTraceData {

    private AtomicLong totalCount = new AtomicLong();

    private AtomicLong totalSuccess = new AtomicLong();

    private AtomicLong totalFailed = new AtomicLong();

    private AtomicLong maxElapsed = new AtomicLong();

    private AtomicLong minElapsed = new AtomicLong(Long.MAX_VALUE);

    private long avgElapsed;

    private long elapsed95th;

    private long elapsed99th;

    private long elapsed999th;

    private transient AtomicLong totalElapsed = new AtomicLong();

    private transient ConcurrentNavigableMap<Long, AtomicLong> elapseds = new ConcurrentSkipListMap<Long, AtomicLong>();

    private byte serialize;

    private int timeout;

    public AbstractTraceData() {

    }

    public void setSerialize(byte serialize) {
        this.serialize = serialize;
    }

    public byte getSerialize() {
        return serialize;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
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

    public long getTotalElapsed() {
        return totalElapsed.get();
    }

    public long getMaxElapsed() {
        return maxElapsed.get();
    }

    public long getMinElapsed() {
        return minElapsed.get();
    }

    public long getAvgElapsed() {
        long count = totalSuccess.get() + totalFailed.get();

        if (count == 0L) {
            avgElapsed = 0L;
        } else {
            avgElapsed = totalElapsed.get() / (totalSuccess.get() + totalFailed.get());
        }

        return avgElapsed;
    }

    @JsonIgnore
    public long getAvgElapsed_() {
        return avgElapsed;
    }

    public long getElapsed95th() {
        elapsed95th = getPercentile(0.95);
        return elapsed95th;
    }

    @JsonIgnore
    public long getElapsed95th_() {
        return elapsed95th;
    }

    public long getElapsed99th() {
        elapsed99th = getPercentile(0.99);
        return elapsed99th;
    }

    @JsonIgnore
    public long getElapsed99th_() {
        return elapsed99th;
    }

    public long getElapsed999th() {
        elapsed999th = getPercentile(0.999);
        return elapsed999th;
    }

    @JsonIgnore
    public long getElapsed999th_() {
        return elapsed999th;
    }

    public void setElapsed95th(long elapsed95th) {
        this.elapsed95th = elapsed95th;
    }

    public void setAvgElapsed(long avgElapsed) {
        this.avgElapsed = avgElapsed;
    }

    public void setElapsed99th(long elapsed99th) {
        this.elapsed99th = elapsed99th;
    }

    public void setElapsed999th(long elapsed999th) {
        this.elapsed999th = elapsed999th;
    }

    @JsonIgnore
    public long getPercentile(double delta) {
        List<Long> keys = new ArrayList<Long>(elapseds.keySet());

        long totalCount = totalSuccess.get() + totalFailed.get();
        long tempCount = 0L;
        long key = 0L;
        if (totalCount == 0L) {
            return 0;
        }
        for (int i = 0; i < elapseds.size(); i++) {

            key = keys.get(i);

            AtomicLong value = elapseds.get(key);

            tempCount += value.get();

            double difference = tempCount * 1.0 / totalCount - delta;

            if (Math.abs(difference) < 1e-5 || difference > 0) {
                break;
            }

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

    @JsonIgnore
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

    @JsonIgnore
    public void setElapseds(long elapsed) {
        long duration = computeDuration(elapsed);

        AtomicLong count = MapUtils.getOrCreate(elapseds, duration, AtomicLong.class);

        count.incrementAndGet();
    }

    @JsonIgnore
    public ConcurrentNavigableMap<Long, AtomicLong> getElapseds() {
        return elapseds;
    }

    @JsonIgnore
    public void setElapseds(ConcurrentNavigableMap<Long, AtomicLong> elapseds) {
        this.elapseds = elapseds;
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
