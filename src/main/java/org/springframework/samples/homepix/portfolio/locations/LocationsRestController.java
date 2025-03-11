package org.springframework.samples.homepix.portfolio.locations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureRangeService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import java.util.Arrays;

@RestController
@RequestMapping("/api/locations")
//@Secured("ROLE_ADMIN")
public class LocationsRestController {

	@Autowired
	private LocationService locationService;

	@Autowired
	private LocationRepository locationRepository;

	@Autowired
	private LocationHierarchyRepository locationHierarchyRepository;

	@Autowired
	PictureRangeService pictureRangeService;

	// Get all pictures and return as JSON
	@GetMapping("/")
	public List<Location> getAllKeywords() {
		return StreamSupport.stream(locationRepository.findAll().spliterator(), false) // Convert Iterable to Stream
			.collect(Collectors.toList());
	}

	@PostMapping("/hierarchy/add")
	@ResponseBody
	public ResponseEntity<List<String>> addHierarchy(@RequestBody Map<String, Object> updates) {

		String places = (String) updates.get("places");        // Step 1: Convert String array to List<Location> using Streams
		List<String> response = new ArrayList<>();
		String[] words = places.split(",");

		        // Step 1: Convert String array to List<Location>, ensuring no duplicates
        List<Location> locations = Arrays.stream(words)
			.map(this::findOrCreateLocation) // Ensure location exists
			.collect(Collectors.toList());

        // Step 2: Iterate over adjacent pairs and add hierarchy entries if they don't exist
        IntStream.range(0, locations.size() - 1)
                .forEach(i -> createLocationHierarchyIfNotExists(locations.get(i), locations.get(i + 1)));

		response.add("Hierarchy added");

		return ResponseEntity.ok(response);
	}

	@PostMapping("/batch/add/{ids}")
	@ResponseBody
	public ResponseEntity<List<String>> addKeywords(@RequestBody Map<String, Object> updates, @PathVariable("ids") String ids) {

		String places = (String) updates.get("places");
		String[] words = places.split(",");

		List<PictureFile> pictureFiles = pictureRangeService.getPictureRangeByIds(updates, ids);
		List<String> response = new ArrayList<>();

		for (PictureFile file : pictureFiles) {

			for (String word : words) {
				locationService.addLocationToPicture(file, word.strip());
			}

			response.add(file.getFilename());
		}

		return ResponseEntity.ok(response);
	}

	private Location findOrCreateLocation(String name) {

		return locationRepository.findByLocation(name)
			.orElseGet(() -> {
				Location newLocation = new Location();
				newLocation.setLocation(name);
				return locationRepository.save(newLocation);
			});
    }

    private void createLocationHierarchyIfNotExists(Location parent, Location child) {
        boolean exists = locationHierarchyRepository.existsByParentLocationAndChildLocation(parent, child);
        if (!exists) {
            LocationHierarchy hierarchy = new LocationHierarchy(child, parent);
            locationHierarchyRepository.save(hierarchy);
        }
    }
}
