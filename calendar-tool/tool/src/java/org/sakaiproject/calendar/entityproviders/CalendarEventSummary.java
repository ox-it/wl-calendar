package org.sakaiproject.calendar.entityproviders;

import lombok.Data;

import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.RecurrenceRule;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.util.CalendarUtil;

@Data
public class CalendarEventSummary {
	private String reference;
	private String siteName;
	private String eventId;
	private String title;
	private String type;
	private String creator;
	private Time firstTime;
	private long duration;
	private String description;
	private RecurrenceRule recurrenceRule;

	/**
	 * This field will only be set if the event is an assignment and can be used to reconstrut the deepLink
	 */
	private String assignmentId;

	/**
	 * Set externally after object creation, signals the site the event came from (not part of CalendarEvent)
	 */
	private String siteId;
	//icon used for specific eventType
	private String eventImage;

	public CalendarEventSummary() {
	}

	public CalendarEventSummary(CalendarEvent event) {
		reference = event.getCalendarReference();
		siteName = event.getSiteName();
		eventId = event.getId();
		title = event.getDisplayName();
		type = event.getType();
		creator = event.getCreator();
		firstTime = event.getRange().firstTime();
		duration = event.getRange().duration();
		recurrenceRule = event.getRecurrenceRule();
		description = event.getDescriptionFormatted();
		assignmentId = event.getField(CalendarUtil.NEW_ASSIGNMENT_DUEDATE_CALENDAR_ASSIGNMENT_ID);
	}

}
