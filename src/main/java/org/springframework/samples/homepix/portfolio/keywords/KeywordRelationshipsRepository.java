package org.springframework.samples.homepix.portfolio.keywords;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

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

	@Query("SELECT kw FROM KeywordRelationships kw WHERE kw.pictureFile.id IN :pictureIds")
	@Transactional(readOnly = true)
	Collection<KeywordRelationships> findByPictureIds(@Param("pictureIds") List<Integer> pictureIds);

	@Query("SELECT kw FROM KeywordRelationships kw left join fetch kw.pictureFile left join fetch kw.keyword WHERE kw.pictureFile.id =:picture_id AND kw.keyword.id =:keyword_id")
	@Transactional(readOnly = true)
	Collection<KeywordRelationships> findByBothIds(@Param("picture_id") Integer picture_id,
												   @Param("keyword_id") Integer keyword_id);

	@Query(value = """
		SELECT
		    k.id,
		    k.word,
		    COUNT(kr.keyword_id) AS usage_count
		FROM keyword k
		LEFT JOIN keyword_relationships_new kr ON k.id = kr.keyword_id
		GROUP BY k.id, k.word
		ORDER BY usage_count DESC, k.word
        """,
		nativeQuery = true)
	List<Object[]> findByNameContainingOrderByUsageDesc();
}
