package org.springframework.samples.homepix.portfolio.filtering;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;

import java.util.Date;

@AllArgsConstructor
public class DateRangeSpecification implements PictureFileSpecification {
	private Date startDate;
	private Date endDate;

	// constructor, getters, setters...

	@Override
	public Predicate toPredicate(Root<PictureFile> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
		// Implement date range filtering logic
		return null;
	}
}
