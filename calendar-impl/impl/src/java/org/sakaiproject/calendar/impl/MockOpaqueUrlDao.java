package org.sakaiproject.calendar.impl;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import org.sakaiproject.calendar.api.OpaqueUrl;
import org.sakaiproject.calendar.api.OpaqueUrlDao;

/**
 * @author alexis
 */
public class MockOpaqueUrlDao implements OpaqueUrlDao {
	// <List{userUUID, calendarRef}, opaqueUUID>
	private HashMap<List<String>, String> myMap = new HashMap<List<String>, String>();
	// "Inverse" of the above: <opaqueUUID, List{userUUID, calendarRef}>
	private HashMap<String, List<String>> myMap2 = new HashMap<String, List<String>>();
	
	public OpaqueUrl newOpaqueUrl(String userUUID, String calendarRef)
	{
		List<String> list = new Vector<String>();
		list.add(userUUID);
		list.add(calendarRef);
		String opaqueUUID = UUID.randomUUID().toString();
		myMap.put(list, opaqueUUID);
		myMap2.put(opaqueUUID, list);
		return new MockOpaqueUrl(userUUID, calendarRef, opaqueUUID);
	}
	
	public OpaqueUrl getOpaqueUrl(String userUUID, String calendarRef)
	{
		List<String> list = new Vector<String>();
		list.add(userUUID);
		list.add(calendarRef);
		String opaqueUUID = myMap.get(list);
		return (opaqueUUID != null) 
			? new MockOpaqueUrl(list.get(0), list.get(1), opaqueUUID) : null;
	}
	
	public OpaqueUrl getOpaqueUrl(String opaqueUUID)
	{
		List<String> list = myMap2.get(opaqueUUID);
		return (list != null) 
			? new MockOpaqueUrl(list.get(0), list.get(1), opaqueUUID) : null; 
	}
	
	public void deleteOpaqueUrl(String userUUID, String calendarRef)
	{
		List<String> list = new Vector<String>();
		list.add(userUUID);
		list.add(calendarRef);
		myMap2.remove(myMap.remove(list));
	}
	
	// Bean contract methods:
	public void init() {}
	public void destroy() {}
}
