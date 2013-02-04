package org.sakaiproject.calendar.impl;

import java.util.ArrayList;
import java.util.Collection;
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
 * Almost a Map that holds the cached calendars.
 * External methods are synchronized because LinkedHashMap is not thread safe.
 * 
 * @author nfernandes
 */
public class SubscriptionCacheMap implements Runnable {
	
	private static final Log m_log = LogFactory
			.getLog(SubscriptionCacheMap.class);

	private final static float DEFAULT_LOAD_FACTOR = 0.75f;

	private int maxCachedEntries;

	private int maxCachedTime;

	private Thread threadCleaner;

	private boolean threadCleanerRunning = false;

	private Object threadCleanerRunningSemaphore = new Object();

	private LinkedHashMap<String, ExternalSubscription> map;
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
		map = new LinkedHashMap<String, ExternalSubscription>(maxCachedEntries, DEFAULT_LOAD_FACTOR, true) {

			protected boolean removeEldestEntry(Entry<String, ExternalSubscription> arg0) {
				return size() > SubscriptionCacheMap.this.maxCachedEntries;
			};
		};
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

	public synchronized ExternalSubscription get(Object arg0) {
		ExternalSubscription e = map.get(arg0);
		return e;
	}

	public synchronized ExternalSubscription put(String key, ExternalSubscription value) {
		if (maxCachedTime > 0 && key != null) {
			cacheTime.put(key, System.currentTimeMillis());
		}
		return map.put(key, value);
	}

	public synchronized void putAll(Map<String,ExternalSubscription> map) {
		if (maxCachedTime > 0 && map != null) {
			for (String key : map.keySet()) {
				cacheTime.put(key, System.currentTimeMillis());
			}
		}
		if (map != null)
			map.putAll(map);
	}

	public synchronized void clear() {
		if (maxCachedTime > 0) {
			cacheTime.clear();
		}
		map.clear();
	}
	public synchronized ExternalSubscription remove(Object key) {
		// Doesn't actually get called when items are removed through size
		// expiry.
		if (maxCachedTime > 0 && key != null) {
			if (cacheTime.containsKey(key))
				cacheTime.remove(key);
		}
		return map.remove(key);
	}

	public synchronized boolean containsKey(String subscriptionUrl) {
		return map.containsKey(subscriptionUrl);
	}

	public synchronized Collection<ExternalSubscription> values() {
		return map.values();
	}

	public void setMaxCachedEntries(int maxCachedEntries) {
		this.maxCachedEntries = maxCachedEntries;
	}

	public void run() {
		try {
			while (threadCleanerRunning) {
				// Find the expired entries (synchronized)
				List<String> toClear = findExpired();
				// Now reload any of them.
				clear(toClear);
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
		} catch (Exception t) {
			m_log.warn("Error while running cache cleaner.", t);
		} finally {
			if (threadCleanerRunning) {
				// thread was stopped by an unknown error: restart
				m_log.info("SmallCacheMap entry cleaner thread was stoped by an unknown error: restarting...");
				startCleanerThread();
			} else
				m_log.debug("Finished SmallCacheMap entry cleaner thread");
		}
	}

	/**
	 * This finds the expired entries in the map, it's synchronised so that we don't
	 * have the map changing while we are using our iterator.
	 * @return A list of expired keys from the map. Will return an empty list if there are none.
	 */
	private synchronized List<String> findExpired() {
		List<String> toClear = new ArrayList<String>();
		for (String key : map.keySet()) {
			// Might be null (bad), autoboxing..
			long cachedFor = System.currentTimeMillis()
					- cacheTime.get(key);
			if (cachedFor > maxCachedTime) {	
				toClear.add(key);
			}
		}
		return toClear;
	}
	
	/**
	 * This updates the expired entries, it isn't synchronized so that it doesn't block the map
	 * for a long time. Although the operations it calls to update the map will aquire a lock (eg get/put).
	 * @param toClear The list of URLs to reload.
	 */
	private void clear(List<String> toClear) {
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