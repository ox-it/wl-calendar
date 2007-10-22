-----------------------------------------------------------------------------
-- CALENDAR_CALENDAR
-----------------------------------------------------------------------------

CREATE TABLE CALENDAR_CALENDAR
(
    CALENDAR_ID VARCHAR2 (99) NOT NULL,
	NEXT_ID INT,
    XML LONG
);

CREATE UNIQUE INDEX CALENDAR_CALENDAR_INDEX ON CALENDAR_CALENDAR
(
	CALENDAR_ID
);

-----------------------------------------------------------------------------
-- CALENDAR_EVENT
-----------------------------------------------------------------------------

CREATE TABLE CALENDAR_EVENT
(
    CALENDAR_ID VARCHAR2 (99) NOT NULL,
	EVENT_ID VARCHAR2 (36) NOT NULL,
	EVENT_START DATE NOT NULL,
	EVENT_END DATE NOT NULL,
	RANGE_START INTEGER NOT NULL,
	RANGE_END INTEGER NOT NULL,
    XML LONG
);

CREATE INDEX CALENDAR_EVENT_INDEX ON CALENDAR_EVENT
(
	CALENDAR_ID
);


CREATE INDEX CALENDAR_EVENT_RSTART ON CALENDAR_EVENT
(
	RANGE_START
);

CREATE INDEX CALENDAR_EVENT_REND ON CALENDAR_EVENT
(
	RANGE_END
);
