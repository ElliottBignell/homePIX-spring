package org.springframework.samples.homepix.portfolio.calendar;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * Repository class for <code>YearThumbnail</code> domain objects All method names are compliant
 * with Spring Data naming conventions so this interface can easily be extended for Spring
 * Data. See:
 * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation
 *
 * @author Elliott Bignell
 */
@Repository
public interface YearThumbnailRepository extends CrudRepository<YearThumbnail, Long> {

	/**
	 * Retrieve {@link YearThumbnail}s from the data store by year, returning all albums
	 * whose last name <i>starts</i> with the given name.
	 * @param year Value to search for
	 * @return a YearThumbnailCollection of matching {@link YearThumbnail}s (or an empty
	 * YearThumbnailCollection if none found)
	 */
	@Query("SELECT DISTINCT yearThumbnail FROM YearThumbnail yearThumbnail WHERE yearThumbnail.year = :year")
	@Transactional(readOnly = true)
	Collection<YearThumbnail> findByYear(@Param("year") Integer year);

	@Query("SELECT new org.springframework.samples.homepix.portfolio.calendar.YearThumbnailMapEntry(y.year, pf) FROM YearThumbnail y JOIN PictureFile pf ON y.thumbnail = pf.id")
	List<YearThumbnailMapEntry> findYearThumbnailMapEntries();
}
