/*
 * Copyright 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.result;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.codename1.l10n.DateFormat;
import com.codename1.l10n.ParseException;
import com.codename1.util.regex.RE;

/**
 * Represents a parsed result that encodes a calendar event at a certain time,
 * optionally with attendees and a location.
 *
 * @author Sean Owen
 * 
 *         TODO Check if CN1 {@link com.codename1.l10n.SimpleDateFormat} can
 *         handle the parsing directly
 */
public final class CalendarParsedResult extends ParsedResult {

	private static final RE RFC2445_DURATION = new RE(
			"^P(?:(\\d+)W)?(?:(\\d+)D)?(?:T(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?)?$");
	private static final long[] RFC2445_DURATION_FIELD_UNITS = { 7 * 24 * 60 * 60 * 1000L, // 1 week
			24 * 60 * 60 * 1000L, // 1 day
			60 * 60 * 1000L, // 1 hour
			60 * 1000L, // 1 minute
			1000L, // 1 second
	};

	private static final RE DATE_TIME = new RE("^[0-9]{8}(T[0-9]{6}Z?)?$");

	private final String summary;
	private final long start;
	private final boolean startAllDay;
	private final long end;
	private final boolean endAllDay;
	private final String location;
	private final String organizer;
	private final String[] attendees;
	private final String description;
	private final double latitude;
	private final double longitude;

	public CalendarParsedResult(String summary, String startString, String endString, String durationString,
			String location, String organizer, String[] attendees, String description, double latitude,
			double longitude) {
		super(ParsedResultType.CALENDAR);
		this.summary = summary;

		try {
			this.start = parseDate(startString);
		} catch (ParseException pe) {
			throw new IllegalArgumentException(pe.toString());
		}

		if (endString == null) {
			long durationMS = parseDurationMS(durationString);
			end = durationMS < 0L ? -1L : start + durationMS;
		} else {
			try {
				this.end = parseDate(endString);
			} catch (ParseException pe) {
				throw new IllegalArgumentException(pe.toString());
			}
		}

		this.startAllDay = startString.length() == 8;
		this.endAllDay = endString != null && endString.length() == 8;

		this.location = location;
		this.organizer = organizer;
		this.attendees = attendees;
		this.description = description;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public String getSummary() {
		return summary;
	}

	/**
	 * @return start time
	 * @deprecated use {@link #getStartTimestamp()}
	 */
	@Deprecated
	public Date getStart() {
		return new Date(start);
	}

	/**
	 * @return start time
	 * @see #getEndTimestamp()
	 */
	public long getStartTimestamp() {
		return start;
	}

	/**
	 * @return true if start time was specified as a whole day
	 */
	public boolean isStartAllDay() {
		return startAllDay;
	}

	/**
	 * @return event end {@link Date}, or {@code null} if event has no duration
	 * @deprecated use {@link #getEndTimestamp()}
	 */
	@Deprecated
	public Date getEnd() {
		return end < 0L ? null : new Date(end);
	}

	/**
	 * @return event end {@link Date}, or -1 if event has no duration
	 * @see #getStartTimestamp()
	 */
	public long getEndTimestamp() {
		return end;
	}

	/**
	 * @return true if end time was specified as a whole day
	 */
	public boolean isEndAllDay() {
		return endAllDay;
	}

	public String getLocation() {
		return location;
	}

	public String getOrganizer() {
		return organizer;
	}

	public String[] getAttendees() {
		return attendees;
	}

	public String getDescription() {
		return description;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	@Override
	public String getDisplayResult() {
		StringBuilder result = new StringBuilder(100);
		maybeAppend(summary, result);
		maybeAppend(format(startAllDay, start), result);
		maybeAppend(format(endAllDay, end), result);
		maybeAppend(location, result);
		maybeAppend(organizer, result);
		maybeAppend(attendees, result);
		maybeAppend(description, result);
		return result.toString();
	}

	/**
	 * Parses a string as a date. RFC 2445 allows the start and end fields to be of
	 * type DATE (e.g. 20081021) or DATE-TIME (e.g. 20081021T123000 for local time,
	 * or 20081021T123000Z for UTC).
	 *
	 * @param when The string to parse
	 * @throws ParseException if not able to parse as a date
	 */
	private static long parseDate(String when) throws ParseException {
		if (!DATE_TIME.match(when)) {
			throw new ParseException(when, 0);
		}
		return parseDateImpl(when);
	}

	private static String format(boolean allDay, long date) {
		if (date < 0L) {
			return null;
		}
		DateFormat format = allDay ? DateFormat.getDateInstance(DateFormat.MEDIUM)
				: DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
		return format.format(date);
	}

	private static long parseDurationMS(CharSequence durationString) {
		if (durationString == null) {
			return -1L;
		}
		// Matcher m = RFC2445_DURATION.matcher(durationString);
		if (!RFC2445_DURATION.match(durationString.toString())) {
			return -1L;
		}
		long durationMS = 0L;
		for (int i = 0; i < RFC2445_DURATION_FIELD_UNITS.length; i++) {
			String fieldValue = RFC2445_DURATION.getParen(i + 1);
			if (fieldValue != null) {
				durationMS += RFC2445_DURATION_FIELD_UNITS[i] * Integer.parseInt(fieldValue);
			}
		}
		return durationMS;
	}

	/**
	 * A rolled out method to parse RFC 2445 time string. RFC 2445 allows the start
	 * and end fields to be of type DATE (e.g. 20081021) or DATE-TIME (e.g.
	 * 20081021T123000 for local time, or 20081021T123000Z for UTC).
	 * 
	 * This method is specialized for parsing a String in the format of <br>
	 * 1. yyyyMMdd 				-- DATE<br>
	 * 2. yyyyMMdd'T'hhmmss 	-- DATE-TIME, Local time<br>
	 * 3. yyyyMMdd'T'hhmmss'Z' 	-- DATE-TIME, UTC time<br>
	 * Other string formats cannot be parsed. For general use, use
	 * {@link com.codename1.l10n.SimpleDateFormat}
	 * 
	 * License: {@link http://www.apache.org/licenses/LICENSE-2.0 }
	 * 
	 * @param s
	 * @return 
	 * @throws ParseException if not able to parse as a date
	 * 
	 * @author Roy Wang
	 */
	private static long parseDateImpl(String s) throws ParseException {
		Calendar c = Calendar.getInstance();
		int yyyy, MM, dd;
		yyyy = Integer.parseInt(s.substring(0, 4));
		MM = Integer.parseInt(s.substring(4, 6)) - 1;
		dd = Integer.parseInt(s.substring(6, 8)) - 1;

		if (s.length() == 8) { // Show only year/month/day
			// For dates without a time, for purposes of interacting with Android, the
			// resulting timestamp
			// needs to be midnight of that day in GMT. See:
			// http://code.google.com/p/android/issues/detail?id=8330
			c.setTimeZone(TimeZone.getTimeZone("GMT"));
			c.set(Calendar.YEAR, yyyy);
			c.set(Calendar.MONTH, MM);
			c.set(Calendar.DAY_OF_MONTH, dd);
			c.set(Calendar.HOUR, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			c.getTime();
			return c.getTime().getTime();
		} else {
			if (s.length() == 15) {
				c.setTimeZone(TimeZone.getDefault());
			} else if (s.length() == 16 && s.charAt(15) == 'Z') {
				c.setTimeZone(TimeZone.getTimeZone("UTC"));
			} else {
				throw new ParseException(s, 0);
			}
			int hh, mm, ss;
			hh = Integer.parseInt(s.substring(9, 11));
			mm = Integer.parseInt(s.substring(11, 13));
			ss = Integer.parseInt(s.substring(13, 15));
			c.set(Calendar.YEAR, yyyy);
			c.set(Calendar.MONTH, MM);
			c.set(Calendar.DAY_OF_MONTH, dd);
			c.set(Calendar.HOUR_OF_DAY, hh);
			c.set(Calendar.MINUTE, mm);
			c.set(Calendar.SECOND, ss);
			c.set(Calendar.MILLISECOND, 0);
			return c.getTime().getTime();
		}
	}

}
