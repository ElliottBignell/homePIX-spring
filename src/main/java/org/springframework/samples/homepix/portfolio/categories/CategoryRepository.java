package org.springframework.samples.homepix.portfolio.categories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.homepix.portfolio.folder.Folder;
import org.springframework.samples.homepix.portfolio.locations.Location;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Repository
public interface CategoryRepository extends CrudRepository<Category, Integer> {
	/**
	 * Retrieve {@link category}s from the data store by content, returning all keywords
	 * whose content<i>matches</i> with the given content.
	 * @param place Value to search for
	 * @return a PictureCollection of matching {@link Folder}s (or an empty
	 * PictureCollection if none found)
	 */

	@Query("SELECT DISTINCT category FROM Category category WHERE category.category = :category")
	@Transactional(readOnly = true)
	Collection<Category> findByContent(@Param("category") String place);
}
