package org.springframework.samples.homepix.portfolio.collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.samples.homepix.portfolio.filtering.DateRangeSpecification;
import org.springframework.samples.homepix.portfolio.filtering.PictureFileSpecification;
import org.springframework.samples.homepix.portfolio.filtering.SearchTextSpecification;
import org.springframework.samples.homepix.portfolio.filtering.SortDirection;
import org.springframework.data.domain.Sort;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.stereotype.Service;

// Service Layer
@Service
public class PictureFileService {

	protected final PictureFileRepository pictureFileRepository;

	@Autowired
	private KeywordRelationshipsRepository keywordRelationshipRepository;

	@Autowired
    public PictureFileService(PictureFileRepository pictureFiles) {
        this.pictureFileRepository = pictureFiles;
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
		List<PictureFile> result = pictureFileRepository.findAll(compositeSpec, sort);

		return result;
	}

	public List<double[]> getNearbyPositions(double refLatitude, double refLongitude, int radius) {

		// Call the repository to find nearby pictures
		List<Object[]> results = pictureFileRepository.findPositionsWithinRadiusWithoutCrowding(refLatitude, refLongitude, radius);

		// Transform the result into a list of coordinates (latitude, longitude)
		List<double[]> nearbyCoordinates = new ArrayList<>();
		for (Object[] result : results) {
			double latitude = ((Number) result[1]).doubleValue();
			double longitude = ((Number) result[2]).doubleValue();
			nearbyCoordinates.add(new double[]{latitude, longitude});
		}

		return nearbyCoordinates;
	}

	public List<PictureFile> getNearbyPictures(double refLatitude, double refLongitude, int radius) {

		// Call the repository to find nearby pictures
		List<PictureFile> results = pictureFileRepository.findPicturesWithinRadiusWithoutCrowding(refLatitude, refLongitude, radius);

		return results;
	}
}
