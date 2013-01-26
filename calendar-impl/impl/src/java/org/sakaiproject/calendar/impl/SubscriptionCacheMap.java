package org.sakaiproject.calendar.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.calendar.api.ExternalSubscription;
import org.sakaiproject.calendar.impl.BaseExternalCalendarSubscriptionService.SubscriptionExpiredListener;

/**
 * Hash table and linked list implementation of the Map interface,
 * access-ordered. Older entries will be removed if map exceeds the maximum
 * capacity specified.
 * 
 * @author nfernandes
 */
public class SubscriptionCacheMap extends
		LinkedHashMap<String, ExternalSubscription> implements Runnable {
	
	private static final Log m_log = LogFactory
			.getLog(SubscriptionCacheMap.class);

	private static final long serialVersionUID = 1L;

	private final static float DEFAULT_LOAD_FACTOR = 0.75f;

	private int maxCachedEntries;

	private int maxCachedTime;

	private Thread threadCleaner;

	private boolean threadCleanerRunning = false;

	private Object threadCleanerRunningSemaphore = new Object();

	private Map<String, Long> cacheTime;

	private SubscriptionExpiredListener listener;
	private Object listenerLock = new Object();

	public SubscriptionCacheMap() {
		// This is wrong as these are in minutes and the other constructur takes
		// ms.
		this(
				BaseExternalCalendarSubscriptionService.DEFAULT_MAX_USER_CACHED_ENTRIES,
				BaseExternalCalendarSubscriptionService.DEFAULT_MAX_USER_CACHED_TIME);
	}

	/**
	 * LinkedHashMap implementation that removes least accessed entry and
	 * (optionally) removes entries with more that maxCachedTime.
	 * 
	 * @param maxCachedEntries
	 *            Maximum number of entries to keep cached.
	 * @param maxCachedTime
	 *            If > 0, entries will be removed after being 'maxCachedTime' in
	 *            cache.
	 */
	public SubscriptionCacheMap(int maxCachedEntries, int maxCachedTime) {
		super(maxCachedEntries, DEFAULT_LOAD_FACTOR, true);
		this.maxCachedEntries = maxCachedEntries;
		this.maxCachedTime = maxCachedTime;
		if (maxCachedTime > 0) {
			cacheTime = new ConcurrentHashMap<String, Long>();
			startCleanerThread();
		}
	}

	public void setSubscriptionExpiredListener(
			SubscriptionExpiredListener listener) {
		synchronized (listenerLock) {
			this.listener = listener;
		}
	}

	public void removeSubscriptionExpiredListener() {
		synchronized (listenerLock) {
			this.listener = null;
		}
	}

	@Override
	public ExternalSubscription get(Object arg0) {
		ExternalSubscription e = super.get(arg0);
		return e;
	}

	@Override
	public ExternalSubscription put(String key, ExternalSubscription value) {
		if (maxCachedTime > 0 && key != null) {
			cacheTime.put(key, System.currentTimeMillis());
		}
		return super.put(key, value);
	}

	@Override
	public void putAll(Map<? extends String, ? extends ExternalSubscription> map) {
		if (maxCachedTime > 0 && map != null) {
			for (String key : map.keySet()) {
				cacheTime.put(key, System.currentTimeMillis());
			}
		}
		if (map != null)
			super.putAll(map);
	}

	@Override
	public void clear() {
		if (maxCachedTime > 0) {
			cacheTime.clear();
		}
		super.clear();
	}

	@Override
	public ExternalSubscription remove(Object key) {
		// Doesn't actually get called when items are removed through size
		// expiry.
		if (maxCachedTime > 0 && key != null) {
			if (cacheTime.containsKey(key))
				cacheTime.remove(key);
		}
		return super.remove(key);
	}

	public void setMaxCachedEntries(int maxCachedEntries) {
		this.maxCachedEntries = maxCachedEntries;
	}

	@Override
	protected boolean removeEldestEntry(Entry<String, ExternalSubscription> arg0) {
		return size() > maxCachedEntries;
	}

	public void run() {
		try {
			while (threadCleanerRunning) {
				// clean expired entries
				List<String> toClear = new ArrayList<String>();
				for (String key : this.keySet()) {
					// Might be null (bad), autoboxing..
					long cachedFor = System.currentTimeMillis()
							- cacheTime.get(key);
					if (cachedFor > maxCachedTime) {
						toClear.add(key);
					}
				}
				// cleaning is not object removal but, Calendar removal from
				// value (ExternalSubscription)
				for (String url : toClear) {
					synchronized (listenerLock) {
						ExternalSubscription e = this.get(url);
						if (e != null) {
							e.setCalendar(null);
							this.put(url, e); // This refreshes the timeout of
												// the cache entry, but doesn't
												// change insertion order.
							m_log.debug("Cleared calendar for expired Calendar Subscription: "
									+ url);
							if (listener != null) {
								listener.subscriptionExpired(url, e);
							}
						}
					}
				}

				// sleep if no work to do
				if (!threadCleanerRunning)
					break;
				try {
					synchronized (threadCleanerRunningSemaphore) {
						threadCleanerRunningSemaphore.wait(maxCachedTime);
					}
				} catch (InterruptedException e) {
					m_log.warn(
							"Failed to sleep SmallCacheMap entry cleaner thread",
							e);
				}
			}
		} catch (Throwable t) {
			m_log.debug("Failed to execute SmallCacheMap entry cleaner thread",
					t);
		} finally {
			if (threadCleanerRunning) {
				// thread was stopped by an unknown error: restart
				m_log.debug("SmallCacheMap entry cleaner thread was stoped by an unknown error: restarting...");
				startCleanerThread();
			} else
				m_log.debug("Finished SmallCacheMap entry cleaner thread");
		}
	}

	/** Start the update thread */
	private void startCleanerThread() {
		threadCleanerRunning = true;
		threadCleaner = null;
		threadCleaner = new Thread(this, this.getClass().getName());
		threadCleaner.start();
	}

	/** Stop the update thread */
	void stopCleanerThread() {
		threadCleanerRunning = false;
		synchronized (threadCleanerRunningSemaphore) {
			threadCleanerRunningSemaphore.notifyAll();
		}
	}
}