package org.sakaiproject.calendar.impl;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import org.sakaiproject.calendar.api.ExternalSubscription;

public class SubscriptionCacheMapTest extends TestCase {

	private SubscriptionCacheMap subscriptionCacheMap;

	protected void setUp() throws Exception {
		super.setUp();
		subscriptionCacheMap = new SubscriptionCacheMap(16, 100);
	}

	protected void tearDown() throws Exception {
		subscriptionCacheMap.stopCleanerThread();
	}

	public void testMap() {
		//subscriptionCacheMap.setSubscriptionExpiredListener(listener)
		ExternalSubscription sub = new BaseExternalSubscription();
		subscriptionCacheMap.put("url", sub);
		assertEquals(1, subscriptionCacheMap.values().size());
	}

	/**
	 * This test shows how you can end up with an big keyset from a LinkedHashMap when you
	 * don't perform any locking on the subscriptionCacheMap. You may need multiple core machine to reproduce 
	 * it.
	 */
	public void testMapMultipleThreads() throws InterruptedException {
		Collection<Thread> threads = new ArrayList<Thread>();
		final BaseExternalSubscription sub = new BaseExternalSubscription();

		final int limit = 10000;

		// What really breaks the map is that we are using LinkedHashMap like a LRU cache.
		// Then having concurrent put/gets to it.
		Thread deposit = new Thread() {
			@Override
			public void run() {
				for (int j = 0; j < limit; j++) {
					subscriptionCacheMap.put("url",sub);
				}
			}
		};
		deposit.start();
		threads.add(deposit);

		Thread retrieve = new Thread() {
			@Override
			public void run() {
				for (int j = 0; j < limit; j++) {
					subscriptionCacheMap.get("url");
				}
			}
		};
		retrieve.start();
		threads.add(retrieve);
		for (Thread thread : threads) {
			thread.join();
		}
		
		
		// Don't always trust the methods when things are wrong.
		assertEquals(1, subscriptionCacheMap.values().size());
		// Do our own counting.
		int count = 0;
		for (Object value : subscriptionCacheMap.values()) {
			count++;
			// This actually loops around and around.
			//if (count % 10000 == 0) System.out.println(count);
			assertTrue("count should never be more than one.", count <= 1);
		}
		


	}
}