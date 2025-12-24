package org.springframework.samples.homepix.portfolio.locations;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Repository
public interface LocationRelationshipsRepository extends CrudRepository<LocationRelationship, Integer> {

	/**
	 * Retrieve an {@link LocationRelationship} from the data store by id.
	 * @param id the id to search for
	 * @return the {@link LocationRelationship} if found
	 */
	@Query(value = "SELECT * FROM location_relationships WHERE picture_id = :id", nativeQuery = true)
	List<LocationRelationship> findByPictureId(@Param("id") Integer id);

	/**
	 * Retrieve an {@link LocationRelationship} from the data store by location.
	 * @param location_id the id to search for
	 * @return the {@link LocationRelationship} if found
	 */
	@Query(value = "SELECT * FROM location_relationships WHERE location_id = :location_id", nativeQuery = true)
	List<LocationRelationship> findByPictureLocationId(@Param("location_id") Integer location_id);

	/**
	 * Retrieve an {@link LocationRelationship} from the data store by location name.
	 * @param place the id to search for
	 * @return the {@link LocationRelationship} if found
	 */
	@Query("SELECT DISTINCT location FROM Location location WHERE location.location = :place")
	@Transactional(readOnly = true)
	List<LocationRelationship> findByLocation(@Param("place") String place);

	/**
	 * Retrieve a list of {@link LocationRelationship} from the data store by location.
	 * @param locationIds the ids to search for
	 * @return the lis tof {@link LocationRelationship} if found
	 */
	@Query("SELECT lr.location.id, COUNT(lr.location.id) FROM LocationRelationship lr WHERE lr.location.id IN :locationIds GROUP BY lr.location.id")
	List<Object[]> countByLocationIds(@Param("locationIds") List<Integer> locationIds);

	@Query("SELECT lr FROM LocationRelationship lr left join fetch lr.picture left join fetch lr.location WHERE lr.picture.id =:picture_id AND lr.location.id =:location_id")
	@Transactional(readOnly = true)
	Collection<LocationRelationship> findByBothIds(@Param("picture_id") Integer picture_id,
												   @Param("location_id") Integer location_id);

	@Query(
		value = "select distinct location from location_relationships lr join location l on lr.location_id = l.id",
		nativeQuery = true
	)
	List<String> findDistinctLocationNames();
}

