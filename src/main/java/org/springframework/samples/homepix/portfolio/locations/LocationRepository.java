package org.springframework.samples.homepix.portfolio.locations;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.homepix.portfolio.folder.Folder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends CrudRepository<Location, Integer> {
	/**
	 * Retrieve {@link location}s from the data store by content, returning all locations
	 * whose content<i>matches</i> with the given content.
	 * @param place Value to search for
	 * @return a PictureCollection of matching {@link Folder}s (or an empty
	 * PictureCollection if none found)
	 */

	@Query("SELECT DISTINCT location FROM Location location WHERE LOWER(location.location) = LOWER(:place)")
	@Transactional(readOnly = true)
	Collection<Location> findByPlaceName(@Param("place") String place);

	/**
	 * Retrieve an {@link Location} from the data store by name.
	 * @param name the id to search for
	 * @return the {@link Location} if found
	 */
	@Query(value = "SELECT * FROM location WHERE location = :name", nativeQuery = true)
	List<Location> findByName(@Param("name") String name);

	public Optional<Location> findByLocation(String location);
}
