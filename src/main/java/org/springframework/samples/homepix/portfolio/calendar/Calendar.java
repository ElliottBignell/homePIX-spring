/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.homepix.portfolio.calendar;

import java.time.*;
import java.util.*;

import org.springframework.core.style.ToStringCreator;
import org.springframework.samples.homepix.model.BaseEntity;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;

import java.util.Date;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Elliott Bignell
 */
public class Calendar extends BaseEntity {

	java.util.Calendar c = java.util.Calendar.getInstance();

	private String name;

	private int count;

	private int thumbnail_id;

	private List<CalendarYearGroup> items;

	private final String monthNames[] = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov",
			"Dec" };

	public final String dayNames[] = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };

	PictureFileRepository pictureFiles;

	private int valueOfDay(DayOfWeek dow) {

		switch (dow) {
		case MONDAY:
			return 0;
		case TUESDAY:
			return 1;
		case WEDNESDAY:
			return 2;
		case THURSDAY:
			return 3;
		case FRIDAY:
			return 4;
		case SATURDAY:
			return 5;
		case SUNDAY:
			return 6;
		}

		return -1;
	}

	public LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
		return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	public Calendar(PictureFileRepository pictureFiles) {

		this.pictureFiles = pictureFiles;

		items = new ArrayList<>();

		int year = 2002;
		LocalDate date = LocalDate.of(year, 1, 1);

		LocalDateTime now = LocalDateTime.now();
		int finalYear = now.getYear();

		while (year <= finalYear) {

			List<CalendarYear> years = new ArrayList<>();

			for (int annum = 0; annum < 3; annum++) {
				years.add(new CalendarYear(year++));
			}

			CalendarYearGroup group = new CalendarYearGroup(years);
			items.add(group);
		}
	}

	public void populateYear(CalendarYear calendarYear) {

		try {

			c.set(calendarYear.getYear(), 0, 1);
			Date dd = c.getTime();

			List<CalendarQuarter> quarters = new ArrayList<>();

			Map<LocalDate, Long> objectsOnDates = this.pictureFiles.getCountByTakenOn(calendarYear.getYear());

			int monthIndex = 0;

			for (int quarter = 0; quarter < 4; quarter++) {

				List<CalendarMonth> months = new ArrayList<>();

				for (int monthNo = 0; monthNo < 3; monthNo++) {

					int monthOfYeae = quarter * 4 + monthNo;

					CalendarMonth month = new CalendarMonth(monthIndex, this.monthNames[monthIndex++], monthNo + 1);
					months.add(month);
				}

				for (int monthNo = 0; monthNo < 3; monthNo++) {

					int monthOfYear = quarter * 3 + monthNo;
					int dayOfMonth = 1;

					List<CalendarWeek> weeks = new ArrayList<>();

					weekLoop:
					for (int weekNo = 0; weekNo < 6; weekNo++) {

						Date d = c.getTime();
						DayOfWeek dayOfWeek = convertToLocalDateViaInstant(d).getDayOfWeek();
						int day = valueOfDay(DayOfWeek.MONDAY);

						List<CalendarDay> days = new ArrayList<>();

						for (; day < valueOfDay(dayOfWeek); day++) {

							CalendarDay newDay = new CalendarDay(this.dayNames[day], null);
							days.add(newDay);
						}

						for (; day <= valueOfDay(DayOfWeek.SUNDAY); day++) {

							d = c.getTime();

							dayOfWeek = convertToLocalDateViaInstant(d).getDayOfWeek();

							CalendarDay newDay;

							if (dayOfMonth < convertToLocalDateViaInstant(d).lengthOfMonth()) {

								PictureFile thumbnail = null;

								LocalDate date = LocalDate.of(calendarYear.getYear(), monthOfYear + 1, dayOfMonth);

								if (objectsOnDates.containsKey(date)) {

									Long pictureCount = objectsOnDates.get(date);

									if (pictureCount > 0) {

										List<PictureFile> pictures = this.pictureFiles.findByDate(LocalDate.of(
											date.getYear(),
											date.getMonth(),
											date.getDayOfMonth()
										));

										if (!pictures.isEmpty()) {
											thumbnail = pictures.get(0);
										}
									}
								}

								newDay = new CalendarDay(this.dayNames[day], thumbnail);
								newDay.setDayOfMonth(dayOfMonth);
								c.add(java.util.Calendar.DAY_OF_YEAR, 1);
							} else
								newDay = new CalendarDay(this.dayNames[day], null);

							dayOfMonth++;
							days.add(newDay);

							java.util.Calendar cal = java.util.Calendar.getInstance();
							cal.setTime(d);
						}

						addWeek(days, weeks);
					}

					while (weeks.size() < 6) {

						List<CalendarDay> days = new ArrayList<>();

						for (int day = 0; day < 7; day++) {

							CalendarDay newDay = new CalendarDay(this.dayNames[day], null);
							days.add(newDay);
						}

						addWeek(days, weeks);
					}

					months.get(monthNo).setWeeks(weeks);
				}

				CalendarQuarter calendarQuarter = new CalendarQuarter(months);
				quarters.add(calendarQuarter);
			}

			calendarYear.setQuarters(quarters);
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "An error occurred: " + e.getMessage(), e);
		}
	}

	void addWeek(List<CalendarDay> days, List<CalendarWeek> weeks) {

		CalendarWeek week = new CalendarWeek(days);
		week.setDays(days);
		weeks.add(week);
	}

	public String getName() {
		return this.name;
	}

	public void setName(String address) {
		this.name = address;
	}

	public int getCount() {
		return this.count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public List<CalendarYearGroup> getItems() {
		return items;
	}

	public void setItems(List<CalendarYearGroup> items) {
		this.items = items;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("id", this.getId()).append("name", this.getName()).toString();
	}

}
