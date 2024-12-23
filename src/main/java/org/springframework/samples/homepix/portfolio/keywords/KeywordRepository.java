package org.springframework.samples.homepix.portfolio.keywords;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.homepix.portfolio.folder.Folder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Repository
public interface KeywordRepository extends CrudRepository<Keyword, Integer> {

	/**
	 * Retrieve {@link Keyword}s from the data store by content, returning all keywords
	 * whose content<i>matches</i> with the given content.
	 * @param content Value to search for
	 * @return a PictureCollection of matching {@link Folder}s (or an empty
	 * PictureCollection if none found)
	 */

	@Query("SELECT DISTINCT keyword FROM Keyword keyword WHERE keyword.word = :word")
	@Transactional(readOnly = true)
	Collection<Keyword> findByContent(@Param("word") String word);
}
