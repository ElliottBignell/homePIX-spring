package org.springframework.samples.homepix;

import java.time.LocalDate;

public class DateRange {

	private final LocalDate startDate;
	private final LocalDate endDate;

	public DateRange(LocalDate startDate, LocalDate endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	// Optionally, you can add utility methods
	public boolean contains(LocalDate date) {
		return (date.isEqual(startDate) || date.isAfter(startDate)) &&
			(date.isEqual(endDate) || date.isBefore(endDate));
	}
}
