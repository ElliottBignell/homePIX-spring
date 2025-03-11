package org.springframework.samples.homepix.portfolio.locations;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationHierarchyRepository extends CrudRepository<LocationHierarchy, Integer> {

	@Query("SELECT lh FROM LocationHierarchy lh WHERE lh.childLocation.id IN :locationIds")
	List<LocationHierarchy> findHierarchyForLocations(@Param("locationIds") List<Integer> locationIds);

	@Query("SELECT lh FROM LocationHierarchy lh WHERE lh.parentLocation.id = :parentId")
	List<LocationHierarchy> findHierarchyForParent(@Param("parentId") Integer parentId);

	boolean existsByParentLocationAndChildLocation(Location child, Location parent);
}
