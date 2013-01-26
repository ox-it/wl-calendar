package org.sakaiproject.calendar.impl;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

import org.sakaiproject.calendar.api.ExternalSubscription;

public class SubscriptionCacheMapTest extends TestCase {

	private SubscriptionCacheMap map;

	protected void setUp() throws Exception {
		super.setUp();
		map = new SubscriptionCacheMap(16, 100);
	}

	protected void tearDown() throws Exception {
		map.stopCleanerThread();
	}

	public void testMap() {
		//map.setSubscriptionExpiredListener(listener)
		ExternalSubscription sub = new BaseExternalSubscription();
		map.put("url", sub);
		assertEquals(1, map.size());
	}

	/**
	 * This test shows how you can end up with an big keyset from a LinkedHashMap when you
	 * don't perform any locking on the Map. You may need multiple core machine to reproduce 
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
					map.put("url",sub);
				}
			}
		};
		deposit.start();
		threads.add(deposit);

		Thread retrieve = new Thread() {
			@Override
			public void run() {
				for (int j = 0; j < limit; j++) {
					map.get("url");
				}
			}
		};
		retrieve.start();
		threads.add(retrieve);
		for (Thread thread : threads) {
			thread.join();
		}
		
		
		// Don't always trust the methods when things are wrong.
		assertEquals(1, map.size());
		assertEquals(1, map.keySet().size());
		// Do our own counting.
		int count = 0;
		for (String key : map.keySet()) {
			count++;
			// This actually loops around and around.
			//if (count % 10000 == 0) System.out.println(count);
			assertTrue("count should never be more than one.", count <= 1);
		}
		


	}
}