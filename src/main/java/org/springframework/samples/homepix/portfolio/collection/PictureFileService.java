package org.springframework.samples.homepix.portfolio.collection;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.samples.homepix.portfolio.filtering.DateRangeSpecification;
import org.springframework.samples.homepix.portfolio.filtering.DateSortStrategy;
import org.springframework.samples.homepix.portfolio.filtering.NameSortStrategy;
import org.springframework.samples.homepix.portfolio.filtering.PictureFileSpecification;
import org.springframework.samples.homepix.portfolio.filtering.PictureFileSortStrategy;
import org.springframework.samples.homepix.portfolio.filtering.SearchTextSpecification;
import org.springframework.samples.homepix.portfolio.filtering.SortDirection;
import org.springframework.data.domain.Sort;

import java.util.Date;
import java.util.List;

// Service Layer
public class PictureFileService {

	protected final PictureFileRepository pictureFiles;

	@Autowired
    public PictureFileService(PictureFileRepository pictureFiles) {
        this.pictureFiles = pictureFiles;
    }

    public List<PictureFile> getPhotos(Date startDate, Date endDate, String searchText, SortDirection sortDirection) {

		// Create specifications based on input parameters
		PictureFileSpecification dateRangeSpec = new DateRangeSpecification(startDate, endDate);
		PictureFileSpecification searchTextSpec = new SearchTextSpecification(searchText);

		// Create a composite specification
		Specification<PictureFile> compositeSpec = Specification.where(dateRangeSpec).and(searchTextSpec);

		// Create a sort strategy based on input parameter
		Sort sort = Sort.by(sortDirection == SortDirection.ASC ? Sort.Order.asc("fieldName") : Sort.Order.desc("fieldName"));

		// Apply specifications and sorting strategy to retrieve data
		List<PictureFile> result = pictureFiles.findAll(compositeSpec, sort);

		return result;
	}
}
