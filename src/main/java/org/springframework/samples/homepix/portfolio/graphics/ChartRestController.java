package org.springframework.samples.homepix.portfolio.graphics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.portfolio.folder.Folder;
import org.springframework.samples.homepix.portfolio.folder.FolderRepository;
import org.springframework.samples.homepix.portfolio.locations.*;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Elliott Bignell
 */
@RestController
@RequestMapping("/api/chart-data")
public class ChartRestController {

	@Autowired
	FolderRepository folderRepository;

	@Autowired
	LocationRepository locationRepository;

	@Autowired
	LocationHierarchyRepository locationHierarchyRepository;

	@Autowired
	LocationRelationshipsRepository locationRelationshipsRepository;

	@GetMapping("/country")
	public Map<String, Object> getChartByCountry() {
		return getChart(0);
	}

	@GetMapping("/location/{name}")
	public Map<String, Object> getChartByLocation(@PathVariable("name") String name) {

		List<Location> where = locationRepository.findByName(name);

		Integer locationID = !where.isEmpty() ? where.iterator().next().getId() : 0;

		return getChart(locationID);
	}

	public Map<String, Object> getChart(Integer locationID) {

		List<Location> locations = locationHierarchyRepository.findHierarchyForParent(locationID).stream()
			.map(LocationHierarchy::getChildLocation)
			.filter(location -> location.getId() != 0)
			.collect(Collectors.toList());

		// Transform the data into chart-friendly JSON
		List<String> labels = locations.stream()
			.map(Location::getLocation)
			.collect(Collectors.toList());

		List<Integer> counts = locations.stream()
			.map(location -> getLocationCounts(locations).getOrDefault(location.getId(), 0))
			.collect(Collectors.toList());

		List<String> links = locations.stream()
			.map(Location::getLocation)
			.collect(Collectors.toList());

		return Map.of(
			"labels", labels,
			"data", counts,
			"links", links
		);
	}

	public Map<Integer, Integer> getLocationCounts(List<Location> locations) {

		List<Integer> locationIds = locations.stream()
			.map(Location::getId)
			.filter(location -> location != 0)
			.collect(Collectors.toList());

		// Fetch count data from database
		List<Object[]> countResults = locationRelationshipsRepository.countByLocationIds(locationIds);

		// Convert results into a Map (locationId -> count)
		Map<Integer, Integer> locationCountMap = countResults.stream()
			.collect(Collectors.toMap(
				row -> (Integer) row[0],  // Location ID
				row -> ((Number) row[1]).intValue()  // Count
			));

		// Ensure every location in the list gets a value (default 0 if missing)
		return locationIds.stream()
			.collect(Collectors.toMap(
				id -> id,
				id -> locationCountMap.getOrDefault(id, 0)
			));
	}
}
