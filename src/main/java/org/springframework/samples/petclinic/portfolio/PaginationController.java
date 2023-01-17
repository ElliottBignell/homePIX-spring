package org.springframework.samples.petclinic.portfolio;

import org.springframework.samples.petclinic.portfolio.calendar.Calendar;

public class PaginationController {

	protected Pagination pagination;
	protected Calendar calendar;

	protected PaginationController() {
		pagination = new Pagination();
		calendar = new Calendar();
	}

	public Pagination getPagination() {
		return pagination;
	}

	public void setPagination(Pagination pagination) {
		this.pagination = pagination;
	}

	public Calendar getCalendar() {
		return calendar;
	}

	public void setCalendar(Calendar calendar) {
		this.calendar = calendar;
	}
}
