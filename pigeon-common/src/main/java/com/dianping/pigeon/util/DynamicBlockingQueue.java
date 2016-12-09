package com.dianping.pigeon.util;

import java.util.concurrent.BlockingQueue;

/**
 * @author qi.yin
 *         2016/12/09  下午10:45.
 */
public interface DynamicBlockingQueue<E> extends BlockingQueue<E> {

    void setCapacity(int capacity);

    int getCapacity();

}
