package org.springframework.samples.homepix.portfolio.graphics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.portfolio.folder.Folder;
import org.springframework.samples.homepix.portfolio.locations.*;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Elliott Bignell
 */
@Controller
public class ChartController {

	@Autowired
	LocationRepository locationRepository;

	@Autowired
	LocationHierarchyRepository locationHierarchyRepository;

	@Autowired
	LocationRelationshipsRepository locationRelationshipsRepository;

	@GetMapping("/chart/bar/location/{name}")
	public String getBarChartSlash(@PathVariable("name") String name, Map<String, Object> model) {
		return getBarChart(name, model);
	}

	@GetMapping("/chart/bar/location/{name}/")
	public String getBarChart(@PathVariable("name") String name, Map<String, Object> model) {

		List<Location> where = locationRepository.findByName(name);

		Integer locationID = !where.isEmpty() ? where.iterator().next().getId() : 0;

		if (locationID != 0) {

			List<Location> locations = locationHierarchyRepository.findHierarchyForParent(locationID).stream()
				.map(LocationHierarchy::getParentLocation)
				.collect(Collectors.toList());

			if (locations.isEmpty() || locations.size() == 1) {

				String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
				return "redirect:/location/" + encodedName;
			}
		}

		model.put("api_url", "/api/chart-data/location/" + name);
		return "graphics/bar-chart";
	}

	@GetMapping("/chart/bar")
	public String getBarChart(Map<String, Object> model) {
		model.put("api_url", "/api/chart-data/country");
		return "graphics/bar-chart";
	}

	@GetMapping("/chart/pie/location/{name}/")
	public String getPieChart(@PathVariable("name") String name, Map<String, Object> model) {

		List<Location> where = locationRepository.findByName(name);

		Integer locationID = !where.isEmpty() ? where.iterator().next().getId() : 0;

		if (locationID != 0) {

			List<Location> locations = locationHierarchyRepository.findHierarchyForParent(locationID).stream()
				.map(LocationHierarchy::getParentLocation)
				.collect(Collectors.toList());

			if (locations.isEmpty()) {
				return "redirect:/location/" + name;
			}
		}

		model.put("api_url", "/api/chart-data/location/" + name);
		return "graphics/pie-chart";
	}

	@GetMapping("/chart/pie")
	public String getPieChart(Map<String, Object> model) {
		model.put("api_url", "/api/chart-data/country");
		return "graphics/pie-chart";
	}
}
