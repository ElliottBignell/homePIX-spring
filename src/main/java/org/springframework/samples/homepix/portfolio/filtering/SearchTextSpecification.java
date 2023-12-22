package org.springframework.samples.homepix.portfolio.filtering;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;

@AllArgsConstructor
public class SearchTextSpecification implements PictureFileSpecification {
	private String searchText;

	// constructor, getters, setters...

	@Override
	public Predicate toPredicate(Root<PictureFile> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
		// Implement search text filtering logic
		return null;
	}
}
