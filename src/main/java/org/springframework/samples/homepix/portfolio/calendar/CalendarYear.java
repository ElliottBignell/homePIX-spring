package org.springframework.samples.homepix.portfolio.calendar;

import org.springframework.samples.homepix.portfolio.collection.PictureFile;

import java.util.ArrayList;
import java.util.List;

public class CalendarYear {

	private int year;

	private List<PictureFile> material;

	private List<CalendarQuarter> quarters;

	public CalendarYear(int annum) {

		this.year = annum;
		this.material = new ArrayList<>();
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public List<PictureFile> getMaterial() {
		return material;
	}

	public void setMaterial(List<PictureFile> material) {
		this.material = material;
	}

	public List<CalendarQuarter> getQuarters() {
		return quarters;
	}

	public void setQuarters(List<CalendarQuarter> quarters) {
		this.quarters = quarters;
	}

	public PictureFile getThumbnail() { return null; };
}
