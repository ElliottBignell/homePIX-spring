package org.springframework.samples.homepix.portfolio.calendar;

import lombok.Getter;
import lombok.Setter;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;

@Getter
@Setter
public class YearThumbnailMapEntry {

	private Integer year;
	private PictureFile thumbnail;

	public YearThumbnailMapEntry(Integer year, PictureFile thumbnail) {
		this.year = year;
		this.thumbnail = thumbnail;
	}

	// Getters
}
