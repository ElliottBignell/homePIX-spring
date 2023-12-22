package org.springframework.samples.homepix.portfolio.filtering;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;

public class NameSortStrategy implements PictureFileSortStrategy {

	@Override
	public void applySort(CriteriaQuery<?> query, Root<PictureFile> root, CriteriaBuilder builder, SortDirection sortDirection) {

	}
}
