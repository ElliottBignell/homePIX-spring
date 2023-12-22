package org.springframework.samples.homepix.portfolio.filtering;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;

public interface PictureFileSpecification extends Specification<PictureFile> {
	Predicate toPredicate(Root<PictureFile> root, CriteriaQuery<?> query, CriteriaBuilder builder);
}
