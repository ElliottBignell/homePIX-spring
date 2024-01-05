package org.springframework.samples.homepix.portfolio.keywords;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationships;
import org.springframework.samples.homepix.portfolio.keywords.Keywords;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Repository
public interface KeywordRelationshipsRepository extends CrudRepository<KeywordRelationships, Integer> {

	/**
	 * Retrieve an {@link KeywordRelationships} from the data store by id.
	 * @param id the id to search for
	 * @return the {@link KeywordRelationships} if found
	 */
	@Query("SELECT kw FROM KeywordRelationships kw left join fetch kw.pictureFile WHERE kw.pictureFile.id =:picture_id")
	@Transactional(readOnly = true)
	Collection<KeywordRelationships> findByPictureId(@Param("picture_id") Integer picture_id);
}
