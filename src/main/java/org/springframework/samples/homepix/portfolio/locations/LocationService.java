package org.springframework.samples.homepix.portfolio.locations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LocationService {

	@Autowired
	LocationHierarchyRepository locationHierarchyRepository;

	public List<Location> sortLocationsByHierarchy(List<Location> locationList) {

		if (locationList == null || locationList.isEmpty()) {
			return locationList;
		}

		// Fetch parent-child relationships from the database
		List<Integer> locationIds = locationList.stream().map(Location::getId).collect(Collectors.toList());
		List<LocationHierarchy> hierarchies = locationHierarchyRepository.findHierarchyForLocations(locationIds);

		// Create a mapping: child -> parent
		Map<Integer, Integer> childToParentMap = new HashMap<>();
		for (LocationHierarchy lh : hierarchies) {
			childToParentMap.put(lh.getChildLocation().getId(), lh.getParentLocation().getId());
		}

		// Sort locations based on hierarchy
		return locationList.stream()
			.sorted(Comparator.comparingInt(location -> getHierarchyDepth(location.getId(), childToParentMap)))
			.collect(Collectors.toList());
	}

	/**
	 * Recursively determines the depth of a location in the hierarchy.
	 * Higher depth means it's a more specific location.
	 */
	private int getHierarchyDepth(Integer locationId, Map<Integer, Integer> childToParentMap) {
		int depth = 0;
		while (childToParentMap.containsKey(locationId)) {
			locationId = childToParentMap.get(locationId);
			depth++;
		}
		return depth;
	}
}
