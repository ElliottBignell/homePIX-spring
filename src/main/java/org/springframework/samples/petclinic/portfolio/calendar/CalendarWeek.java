package org.springframework.samples.petclinic.portfolio.calendar;

import java.util.List;

public class CalendarWeek {

	private List<CalendarDay> days;

	public CalendarWeek(List<CalendarDay> days) {
		this.days = days;
	}

	public List<CalendarDay> getDays() {
		return days;
	}

	public void setDays(List<CalendarDay> days) {
		this.days = days;
	}

}
