package org.springframework.samples.homepix.portfolio.calendar;

import java.util.List;

public class CalendarYearGroup {

	private List<CalendarYear> years;

	public CalendarYearGroup(List<CalendarYear> years) {
		this.years = years;
	}

	public List<CalendarYear> getYears() {
		return years;
	}

	public void setYears(List<CalendarYear> years) {
		this.years = years;
	}

}
