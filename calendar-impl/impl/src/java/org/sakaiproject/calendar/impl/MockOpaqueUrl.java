package org.sakaiproject.calendar.impl;

import org.sakaiproject.calendar.api.OpaqueUrl;

public class MockOpaqueUrl implements OpaqueUrl {
	String userUUID;
	String calendarRef;
	String opaqueUUID;
	public MockOpaqueUrl(String userUUID, String calendarRef, String opaqueUUID)
	{
		this.userUUID = userUUID;
		this.calendarRef = calendarRef;
		this.opaqueUUID = opaqueUUID;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.calendar.util.OpaqueUrl#getUserUUID()
	 */
	public String getUserUUID()
	{
		return userUUID;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.calendar.util.OpaqueUrl#getCalendarRef()
	 */
	public String getCalendarRef()
	{
		return calendarRef;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.calendar.util.OpaqueUrl#getOpaqueUUID()
	 */
	public String getOpaqueUUID()
	{
		return opaqueUUID;
	}
}
