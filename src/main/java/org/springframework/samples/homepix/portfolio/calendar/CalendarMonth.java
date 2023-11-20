package org.springframework.samples.homepix.portfolio.calendar;

import org.springframework.samples.homepix.portfolio.collection.PictureFile;

import java.util.List;

public class CalendarMonth {

	PictureFile thumbnail;

	private String name;

	private int count;

	private int index;

	public List<CalendarWeek> getWeeks() {
		return weeks;
	}

	public void setWeeks(List<CalendarWeek> weeks) {
		this.weeks = weeks;
	}

	private List<CalendarWeek> weeks;

	public CalendarMonth(int index, String name, int count) {

		this.index = index;
		this.name = name;
		this.count = count;
	}

	public String getIndex() {
		return String.valueOf(index + 1);
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
