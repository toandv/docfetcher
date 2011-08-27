/*******************************************************************************
 * Copyright (c) 2011 Tran Nam Quang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Tran Nam Quang - initial API and implementation
 *******************************************************************************/

package net.sourceforge.docfetcher.base;

import net.sourceforge.docfetcher.base.annotations.NotNull;
import net.sourceforge.docfetcher.base.annotations.Nullable;

/**
 * @author Tran Nam Quang
 */
public final class DelayedExecutor {
	
	private final long delay;
	private final Object lock = new Object();
	
	// Fields below guarded by lock
	@Nullable private Thread thread;
	@Nullable private Runnable lastRunnable;
	private long lastTimestamp = 0;
	
	public DelayedExecutor(long delay) {
		Util.checkThat(delay >= 0);
		this.delay = delay;
	}
	
	// Discards the previously scheduled runnable if the time passed since the
	// last scheduling is less than the delay
	public void schedule(@NotNull Runnable runnable) {
		Util.checkNotNull(runnable);
		synchronized (lock) {
			lastRunnable = runnable;
			lastTimestamp = System.currentTimeMillis();
			if (thread != null)
				return;
			
			thread = new Thread() {
				public void run() {
					long sleepTime = delay;
					while (true) {
						try {
							Thread.sleep(sleepTime);
						}
						catch (InterruptedException e) {
							break;
						}
						synchronized (lock) {
							long timePassed = System.currentTimeMillis() - lastTimestamp;
							if (timePassed > delay) {
								lastRunnable.run();
								lastRunnable = null;
								thread = null;
								break;
							}
							else {
								sleepTime = delay - timePassed;
							}
						}
					}
				}
			};
			thread.start();
		}
	}

}
