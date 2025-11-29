package org.springframework.samples.homepix.portfolio.locations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LocationService {

	@Autowired
	LocationRepository locationRepository;

	@Autowired
	LocationHierarchyRepository locationHierarchyRepository;

	@Autowired
	LocationRelationshipsRepository locationRelationshipsRepository;

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

	public void addLocationToPicture(PictureFile picture, String word) {

		Collection<Location> existing = locationRepository.findByPlaceName(word);
		Location newLocation;

		if (existing.isEmpty()) {

			newLocation = new Location();
			newLocation.setLocation(word);
			this.locationRepository.save(newLocation);
		}
		else
			newLocation = existing.iterator().next();

		Collection<LocationRelationship> relations = this.locationRelationshipsRepository.findByBothIds(picture.getId(), newLocation.getId());

		if (relations.isEmpty()) {

			LocationRelationship relation = new LocationRelationship();
			relation.setPicture(picture);
			relation.setLocation(newLocation);
			this.locationRelationshipsRepository.save(relation);
		}
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

	@Cacheable("allLocations")
	public Iterable<LocationRelationship> findAll() {
		return this.locationRelationshipsRepository.findAll();
	}

	@CacheEvict(value = { "allLocations" }, allEntries = true)
	@Scheduled(cron = "0 0 3 * * *") // every day at 3 AM
	public void resetCache() {
		// This will clear the "folders" cache.
		// Optionally re-fetch or do nothing here;
		// next call to getSortedFolders() will reload.
	}
}
