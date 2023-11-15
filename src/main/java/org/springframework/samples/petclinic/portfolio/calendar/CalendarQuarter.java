package org.springframework.samples.petclinic.portfolio.calendar;

import java.util.List;

public class CalendarQuarter {

	private List<CalendarMonth> months;

	public CalendarQuarter(List<CalendarMonth> months) {
		this.months = months;
	}

	public List<CalendarMonth> getMonths() {
		return months;
	}

	public void setMonths(List<CalendarMonth> months) {
		this.months = months;
	}

}
