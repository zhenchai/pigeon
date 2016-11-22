package com.dianping.pigeon.util;

import java.util.concurrent.TimeUnit;

public class TimeUtils {

	public static long currentTimeMillis() {
		return MillisecondClock.CLOCK.currentTimeMillis();
	}

	static class MillisecondClock {
		private long rate = 0;// 频率
		private volatile long now = 0;// 当前时间

		private MillisecondClock(long rate) {
			this.rate = rate;
			this.now = System.currentTimeMillis();
			start();
		}

		private void start() {
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						try {
							TimeUnit.MICROSECONDS.sleep(rate);
						} catch (InterruptedException e) {
						}
						now = System.currentTimeMillis();
					}
				}
			}, "Pigeon-MillisecondClock");
			t.setDaemon(true);
			t.start();
		}

		public long currentTimeMillis() {
			return now;
		}

		public static final MillisecondClock CLOCK = new MillisecondClock(500);
	}
}
