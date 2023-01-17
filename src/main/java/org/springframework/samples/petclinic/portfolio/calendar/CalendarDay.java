package org.springframework.samples.petclinic.portfolio.calendar;

import org.springframework.samples.petclinic.portfolio.collection.PictureFile;

public class CalendarDay {

	private String name;
	private PictureFile thumbnail;
	private int dayOfMonth;

	public CalendarDay(String name, PictureFile thumbnail) {

		this.name = name;
		this.thumbnail = thumbnail;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PictureFile getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(PictureFile thumbnail) {
		this.thumbnail = thumbnail;
	}

	public int getDayOfMonth() {
		return dayOfMonth;
	}

	public void setDayOfMonth(int dayOfMonth) {
		this.dayOfMonth = dayOfMonth;
	}
}
