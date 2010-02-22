/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.calendar.util;

import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.util.MergedListEntryProviderBase;

public class EntryProvider extends MergedListEntryProviderBase
{
	private CalendarService calendarService;
	
	public EntryProvider(CalendarService calendarService)
	{
		this.calendarService = calendarService;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.util.MergedListEntryProviderBase#makeReference(java.lang.String)
	 */
	public Object makeObjectFromSiteId(String id)
	{
		String calendarReference = calendarService.calendarReference(id, SiteService.MAIN_CONTAINER);
		Object calendar = null;
		
		if ( calendarReference != null )
		{
			 try
				 {
				  calendar = calendarService.getCalendar(calendarReference);
				 }
				 catch (IdUnusedException e)
				 {
					  // The channel isn't there.
				 }
				 catch (PermissionException e)
				 {
					  // We can't see the channel
				 }				 
		}
		
		return calendar;
	}

	/* (non-Javadoc)
	 * @see org.chefproject.actions.MergedEntryList.EntryProvider#allowGet(java.lang.Object)
	 */
	public boolean allowGet(String ref)
	{
		return calendarService.allowGetCalendar(ref);
	}
	
	/* (non-Javadoc)
	 * @see org.chefproject.actions.MergedEntryList.EntryProvider#getContext(java.lang.Object)
	 */
	public String getContext(Object obj)
	{
		if ( obj == null )
		{
			 return "";
		}

		Calendar calendar = (Calendar)obj;
		return calendar.getContext();
	}
	
	/* (non-Javadoc)
	 * @see org.chefproject.actions.MergedEntryList.EntryProvider#getReference(java.lang.Object)
	 */
	public String getReference(Object obj)
	{
		if ( obj == null )
		 {
			 return "";
		 }
		
		 Calendar calendar = (Calendar)obj;
		return calendar.getReference();
	}
	
	/* (non-Javadoc)
	 * @see org.chefproject.actions.MergedEntryList.EntryProvider#getProperties(java.lang.Object)
	 */
	public ResourceProperties getProperties(Object obj)
	{
		if ( obj == null )
		 {
			 return null;
		 }

		Calendar calendar = (Calendar)obj;
		return calendar.getProperties();
	}
}