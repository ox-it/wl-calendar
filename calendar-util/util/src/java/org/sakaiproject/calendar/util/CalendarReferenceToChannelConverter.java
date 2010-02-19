package org.sakaiproject.calendar.util;

import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.util.MergedListEntryProviderFixedListWrapper;

/**
 * Used by callback to convert channel references to channels.
 */
public class CalendarReferenceToChannelConverter implements MergedListEntryProviderFixedListWrapper.ReferenceToChannelConverter
{
	private CalendarService calendarService;
	
	public CalendarReferenceToChannelConverter(CalendarService calendarService) {
		this.calendarService = calendarService;
	}
	  public Object getChannel(String channelReference)
	  {
			try
			{
				 return calendarService.getCalendar(channelReference); 
			}
			catch (IdUnusedException e)
			{
				 return null;
			}
			catch (PermissionException e)
			{
				 return null;
			}
	  }
 }