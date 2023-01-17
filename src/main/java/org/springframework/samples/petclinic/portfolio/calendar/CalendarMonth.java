package org.springframework.samples.petclinic.portfolio.calendar;

import org.springframework.samples.petclinic.portfolio.collection.PictureFile;

import java.util.List;

public class CalendarMonth {

	PictureFile thumbnail;

	private String name;
	private int count;

	public List<CalendarWeek> getWeeks() {
		return weeks;
	}

	public void setWeeks(List<CalendarWeek> weeks) {
		this.weeks = weeks;
	}

	private List< CalendarWeek > weeks;

	public CalendarMonth(String name, int count) {

		this.name = name;
		this.count = count;
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

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
