package com.dianping.pigeon.threadpool;

import java.util.concurrent.BlockingQueue;

/**
 * Created by chenchongze on 16/12/9.
 */
public interface ResizableBlockingQueue<E> extends BlockingQueue<E> {

    int getCapacity();

    void setCapacity(int capacity);
}
