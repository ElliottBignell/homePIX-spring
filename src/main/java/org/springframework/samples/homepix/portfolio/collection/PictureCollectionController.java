/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.homepix.portfolio.collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.data.util.Pair;
import org.springframework.data.web.PageableDefault;
import org.springframework.samples.homepix.CollectionRequestDTO;
import org.springframework.samples.homepix.portfolio.PaginationController;
import org.springframework.samples.homepix.portfolio.album.*;
import org.springframework.samples.homepix.portfolio.folder.Folder;
import org.springframework.samples.homepix.portfolio.folder.FolderRepository;
import org.springframework.samples.homepix.portfolio.folder.FolderService;
import org.springframework.samples.homepix.portfolio.keywords.Keyword;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationships;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRepository;
import org.springframework.samples.homepix.portfolio.locations.Location;
import org.springframework.samples.homepix.portfolio.locations.LocationRelationship;
import org.springframework.samples.homepix.portfolio.locations.LocationService;
import org.springframework.samples.homepix.portfolio.maps.MapUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Elliott Bignell
 */
@Controller
class PictureCollectionController extends PaginationController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "collection/createOrUpdateOwnerForm";

	private final PictureCollectionRepository pictures;
	private final PictureFileRepository pictureFiles;
	private final PictureFileService pictureFileService;
	private final PictureElasticSearchService pictureElasticSearchService;
	protected final AlbumContentRepository albumContent;
	private final AlbumService albumService;

	@Autowired
	private PictureService pictureService;

	@Autowired
	LocationService locationService;

	public PictureCollectionController(PictureFileRepository pictureFiles,
									   PictureCollectionRepository pictures,
									   AlbumRepository albums,
									   FolderRepository folders,
									   KeywordRepository keyword,
									   KeywordRelationshipsRepository keywordsRelationships,
									   FolderService folderService, PictureFileRepository pictureFiles1,
									   PictureFileService pictureFileService,
									   PictureElasticSearchService pictureElasticSearchService, AlbumContentRepository albumContent, AlbumService albumService
	) {

		super(albums, folders, pictureFiles, keyword, keywordsRelationships, folderService);
		this.pictures = pictures;
		this.pictureFiles = pictureFiles1;
		this.pictureFileService = pictureFileService;
		this.pictureElasticSearchService = pictureElasticSearchService;
		this.albumContent = albumContent;
		this.albumService = albumService;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@GetMapping("/collections/find")
	public String initFindForm(Map<String, Object> model) {
		model.put("collection", new PictureCollection());
		return "collections/findCollections";
	}

	@GetMapping("/collections")
	public String processFindForm(PictureCollection pictureCollection, BindingResult result,
								  Map<String, Object> model) {

		// allow parameterless GET request for /collections to return all records
		if (pictureCollection.getName() == null) {
			pictureCollection.setName(""); // empty string signifies broadest possible
			// search
		}

		Iterable<PictureCollection> collection = this.pictures.findAll();

		model.put("collection", collection);

		return "collections/collection";
	}

	@GetMapping("/collection/")
	public String processFindCollectionsSlash(@ModelAttribute CollectionRequestDTO requestDTO,
											  @ModelAttribute("requestDTO") CollectionRequestDTO redirectedDTO,
											  @PageableDefault(size = 100, sort = "defaultSortField") Pageable pageable, // Default page size and sorting
											  RedirectAttributes redirectAttributes,
											  PictureCollection pictureCollection,
											  BindingResult result,
											  Map<String, Object> model,
											  Authentication authentication
	) {
		return processFindCollections(requestDTO, redirectedDTO, pageable, pictureCollection, result, model, authentication);
	}


	@GetMapping("/collection")
	public String processFindCollectionsSlashNoSlash(@ModelAttribute CollectionRequestDTO requestDTO,
													 @ModelAttribute("requestDTO") CollectionRequestDTO redirectedDTO,
													 @PageableDefault(size = 100, sort = "defaultSortField") Pageable pageable, // Default page size and sorting
													 PictureCollection pictureCollection,
													 BindingResult result,
													 Map<String, Object> model,
													 Authentication authentication
	) {
		return processFindCollections(requestDTO, redirectedDTO, pageable, pictureCollection, result, model, authentication);
	}

	// Get all pictures and return as JSON
	@GetMapping("/location/{name}")
	public String getPictureByLocation(@ModelAttribute CollectionRequestDTO requestDTO,
									   @ModelAttribute("requestDTO") CollectionRequestDTO redirectedDTO,
									   @PathVariable("name") String name,
									   @PageableDefault(size = 100, sort = "defaultSortField") Pageable pageable, // Default page size and sorting
									   Authentication authentication,
									   Map<String, Object> model,
									   HttpServletRequest request
	) {
		return processFindByLocation(name, requestDTO, redirectedDTO, pageable, model, authentication);
	}

	// Get all pictures and return as JSON
	@GetMapping("/location/{name}/")
	public String getPictureByLocationSlash(@ModelAttribute CollectionRequestDTO requestDTO,
											@ModelAttribute("requestDTO") CollectionRequestDTO redirectedDTO,
											@PathVariable("name") String name,
											@PageableDefault(size = 100, sort = "defaultSortField") Pageable pageable, // Default page size and sorting
											Authentication authentication,
											Map<String, Object> model,
											HttpServletRequest request
	) {
		return processFindByLocation(name, requestDTO, redirectedDTO, pageable, model, authentication);
	}

	/**
	 * Custom handler for displaying a collection.
	 *
	 * @param id1 the ID of the first element to display
	 * @param id2 the ID of the last element to display
	 * @return a ModelMap with the model attributes for the view
	 */
	@GetMapping("/collection/items/{id1}-{id2}")
	public String showMultiFilteredCollection(CollectionRequestDTO requestDTO,
									     @PathVariable("id1") int start,
									     @PathVariable("id2") int end,
										 Map<String, Object> model,
										 @PageableDefault(size = 100, sort = "defaultSortField") Pageable pageable, // Default page size and sorting
										 Authentication authentication
	) {
		showFilteredCollection(requestDTO, start, model, pageable, authentication);

		Page<PictureFile> results;
		List<PictureFile> files = this.pictureFiles.findAllIdRange((long)start, (long)end);

		model.put("collection", files);

		return "picture/pictureList.html";
	}

	/**
	 * Custom handler for displaying an collection.
	 *
	 * @param id the ID of the collection to display
	 * @return a ModelMap with the model attributes for the view
	 */
	@GetMapping("/collection/{id}")
	public String showFilteredCollection(CollectionRequestDTO requestDTO,
										 @PathVariable("id") int pictureID,
										 Map<String, Object> model,
										 @PageableDefault(size = 100, sort = "defaultSortField") Pageable pageable, // Default page size and sorting
										 Authentication authentication
	) {
		PageRequest pageRequest = PageRequest.of(
			pageable.getPageNumber(),
			100, // pageable.getPageSize(),
			Sort.by(getOrderColumn(requestDTO))
		);

		String userRoles = "ROLE_USER";

		Pair<LocalDate, LocalDate> dates = getDateRange(requestDTO, model);
		LocalDate endDate = dates.getSecond();
		LocalDateTime endOfDay = endDate.atTime(LocalTime.MAX);

		ModelAndView mav = new ModelAndView("albums/albumDetails");
		mav.addObject(this.pictures.findAll());
		model.put("link_params", "");

		String searchText = requestDTO.getSearch();
		Page<PictureFile> files = this.pictureFileService.getComplexSearchPage(
			searchText,
			dates.getFirst(),
			endOfDay,
			isAdmin(authentication),
			userRoles,
			pageRequest
		);

		addParams(pictureID, "", files.toList(), model, true);

		List<Keyword> keywords = this.keywordRelationships.findByPictureId(pictureID).stream()
			.map(KeywordRelationships::getKeyword)
			.collect(Collectors.toList());

		model.put("collection", pictureFiles);
		model.put("baseLink", "/collection/" + -1);
		model.put("keywords", this.keywordRelationships.findByPictureId(pictureID).stream()
			.map(relationship -> relationship.getKeyword().getWord())
			.distinct()
			.sorted()
			.collect(Collectors.joining(",")));
		model.put("keyword_list", keywords);

		// Construct the full URL
		model.put("fullUrl", "collection/" + pictureID);
		model.put("focusedField", "send_description");
		model.put("search", requestDTO.getSearch());
		model.put("pageNumber", files.getNumber());
		model.put("albums", albumService.getSortedAlbums());
		model.put("folders", folderService.getSortedFolders());

		List<Location> locations = this.locationRelationships.findByPictureId(pictureID).stream()
			.map(LocationRelationship::getLocation)
			.collect(Collectors.toList());
		locations = locationService.sortLocationsByHierarchy(locations);
		model.put("location_list", locations);
		Optional<PictureFile> picture = pictureFiles.findById(pictureID);

		if (picture.isPresent()) {

			pictureFileService.addMapDetails(picture.get(), model);
			model.put("picture", picture.get());
		}

		List<String> tags = this.keywordRelationships.findByNameContainingOrderByUsageDesc().stream()
			.map(obj -> (String)obj[1])
			.collect(Collectors.toList());
		List<Album> albums = StreamSupport.stream(this.albums.findAll().spliterator(), false)
			.collect(Collectors.toList());
		List<Folder> folders = new ArrayList<>(folderService.getSortedFolders());

		model.put("tags", tags);
		model.put("album_names", albums.stream()
			.map(Album::getName)
			.sorted()
			.collect(Collectors.toList())
		);
		model.put("folder_names", folders.stream()
			.map(Folder::getName)
			.sorted()
			.collect(Collectors.toList())
		);

		return "picture/pictureFile.html";
	}

	@GetMapping("/collections/{id}")
	public String showCollections(CollectionRequestDTO requestDTO,
								  @PathVariable("id") int pictureID,
								  Map<String, Object> model,
								  @PageableDefault(size = 100, sort = "defaultSortField") Pageable pageable, // Default page size and sorting
								  Authentication authentication
	) {
		return showFilteredCollection(requestDTO, pictureID, model, pageable, authentication);
	}

	@GetMapping("/collection/{id}/")
	public String showCollectionsSlash(CollectionRequestDTO requestDTO,
									   @PathVariable("id") int pictureID,
									   Map<String, Object> model,
									   @PageableDefault(size = 100, sort = "defaultSortField") Pageable pageable, // Default page size and sorting
									   Authentication authentication
	) {
		return showFilteredCollection(requestDTO, pictureID, model, pageable, authentication);
	}

	@GetMapping("/collections/{id}/")
	public String showCollectionssSlash(CollectionRequestDTO requestDTO,
										@PathVariable("id") int pictureID,
										Map<String, Object> model,
										@PageableDefault(size = 100, sort = "defaultSortField") Pageable pageable, // Default page size and sorting
										Authentication authentication
	) {
		return showFilteredCollection(requestDTO, pictureID, model, pageable, authentication);
	}

	@GetMapping("/collection/{dummyId}/item/{id}")
	public String showCollection(CollectionRequestDTO requestDTO,
								 @PathVariable("id") int pictureID,
								 Map<String, Object> model,
								 @PageableDefault(size = 100, sort = "defaultSortField") Pageable pageable, // Default page size and sorting
								 Authentication authentication
	) {
		return showFilteredCollection(requestDTO, pictureID, model, pageable, authentication);
	}

	@GetMapping("/collections/{dummyId}/item/{id}/")
	public String showCollectionSlash(CollectionRequestDTO requestDTO,
									  @PathVariable("id") int pictureID,
									  Map<String, Object> model,
									  @PageableDefault(size = 100, sort = "defaultSortField") Pageable pageable, // Default page size and sorting
									  Authentication authentication
	) {
		return showFilteredCollection(requestDTO, pictureID, model, pageable, authentication);
	}

	@Override
	public void close() throws Exception {

	}

	public static String buildJsonPayload(Iterable<PictureFile> pictureFiles) {
		return StreamSupport.stream(pictureFiles.spliterator(), false)
			.flatMap(pictureFile ->
				Stream.of(
					String.format("{ \"index\" : { \"_index\" : \"images\", \"_id\" : \"%s\" } }", pictureFile.getId()),
					String.format("{ \"id\": \"%s\", \"filename\": \"%s\", \"description\": \"%s\" }",
						pictureFile.getId(), pictureFile.getFilename(),
						pictureFile.getTitle().split("\n", 2)[0]) // Split the title at the first newline and take the first part
				)
			)
			.collect(Collectors.joining("\n")) + "\n";
	}


	@GetMapping("/elastic/index")
	public String bulkIndexElastic() throws Exception {

		HttpClient client = pictureElasticSearchService.getHttpClient();
		String encodedCredentials = pictureElasticSearchService.getEncodedCredentials();

		String esUrl = "https://localhost:9200/_bulk";

		// Manually formatted JSON payload for bulk insertion
		String jsonPayload = buildJsonPayload(this.pictureFiles.findAll());

		System.out.println(jsonPayload);

		// Manually formatted JSON payload for bulk insertion
		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(esUrl))
			.header("Content-Type", "application/x-ndjson")
			.header("Authorization", "Basic " + encodedCredentials)
			.POST(BodyPublishers.ofString(jsonPayload))
			.build();

		HttpResponse<String> response = null;

		try {
			response = client.send(request, BodyHandlers.ofString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		System.out.println("Response status code: " + response.statusCode());
		System.out.println("Response body: " + response.body());

		return "redirect:/buckets";
	}

	@GetMapping("/elastic/retrieve")
	public String retrieveElastic() throws Exception {

		HttpClient client = pictureElasticSearchService.getHttpClient();
		String encodedCredentials = pictureElasticSearchService.getEncodedCredentials();

		String esUrl = "https://localhost:9200/images/_search";

		String jsonQuery =
			"{\n" +
				"  \"query\": {\n" +
				"    \"query_string\" : {\n" +
				"      \"query\" : \"(chiesa OR bergamo)\",\n" +
				"      \"fields\" : [\"description\"]\n" +
				"    }\n" +
				"  }\n" +
				"}\n";

		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(esUrl))
			.header("Content-Type", "application/json")
			.header("Authorization", "Basic " + encodedCredentials)
			.POST(BodyPublishers.ofString(jsonQuery))
			.build();

		HttpResponse<String> response = null;

		try {
			response = client.send(request, BodyHandlers.ofString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		System.out.println("Response status code: " + response.statusCode());
		System.out.println("Response body: " + response.body());

		return "redirect:/buckets";
	}

	@GetMapping("/maps/album/{name}")
	public String retrieveAlbumMap(Map<String, Object> model, @PathVariable("name") String name) throws Exception {
		return albumService.loadMapPreview(model, name);
	}

	@GetMapping("/maps/bucket/{name}")
	public String retrieveBucketMap(Map<String, Object> model, @PathVariable("name") String name) throws Exception {
		return pictureFileService.loadMapPreview(model, name);
	}

	@GetMapping("/maps/{id}")
	public String retrieveMap(Map<String, Object> model, @PathVariable("id") int id) throws Exception {

		Optional<PictureFile> picture = pictureFiles.findById(id);

		if (picture.isPresent()) {

			PictureFile pictureFile = picture.get();

			model.put("picture", pictureFile);

			double refLatitude = pictureFile.getLatitude();
			double refLongitude = pictureFile.getLongitude();

			// Convert double[] to List<Double> for Thymeleaf compatibility
			List<PictureFile> nearbyPictures = pictureFileService.getNearbyPictures(refLatitude, refLongitude, 10000)
				.stream()
				.filter(element -> element.getLatitude() != null && element.getLongitude() != null)
				.collect(Collectors.toList());
			List<List<Float>> nearbyCoordinates = nearbyPictures
				.stream()
				.map(element -> Arrays.asList(element.getLatitude(), element.getLongitude()))
				.collect(Collectors.toList());

			Map.Entry<MapUtils.LatLng, Integer> result = MapUtils.calculateCenterAndZoom(nearbyPictures);

			MapUtils.LatLng center = result.getKey();
			int zoom = result.getValue();

			model.put("zoom", zoom);
			model.put("refLatitude", center.getLatitude());
			model.put("refLongitude", center.getLongitude());
			model.put("nearbyCoordinates", nearbyCoordinates);
			model.put("nearbyPictures", nearbyPictures);

			return "picture/pictureMap.html";
		}

		return "redirect:/buckets";
	}

	@Transactional
	@GetMapping("/maps/clusters")
	public String retrieveClusters(Map<String, Object> model) throws Exception {

		try {

			List<Object[]> results = pictureFiles.findAllWithRoundedCoordinates();

			// Map to hold clusters, keyed by the rounded coordinates
			Map<String, List<PictureFile>> clusters = new HashMap<>();

			for (Object[] row : results) {

				int id = ((Number)row[0]).intValue();
				Optional<PictureFile> picture = pictureFiles.findById(id);

				if (picture.isPresent()) {

					// Get the grouping key
					double roundedLatitude = ((Number) row[row.length - 2]).doubleValue();
					double roundedLongitude = ((Number) row[row.length - 1]).doubleValue();

					String key = roundedLatitude + "," + roundedLongitude;

					// Group pictures by the key
					clusters.computeIfAbsent(key, k -> new ArrayList<>()).add(picture.get());
				}
			}

			// Process each cluster
			for (List<PictureFile> cluster : clusters.values()) {
				adjustClusterPositions(cluster); // Adjust positions for markers in this cluster
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		return "redirect:/";
	}

	private void adjustClusterPositions(List<PictureFile> cluster) {

		double baseLat = cluster.get(0).getLatitude();
		double baseLng = cluster.get(0).getLongitude();
		int clusterSize = cluster.size();
		double angleIncrement = (2.0 * Math.PI) / clusterSize;
		double radius = 0.0001; // Adjust as needed (approximately 10 meters)

		for (int i = 1; i < clusterSize; i++) {

			double angle = angleIncrement * i;
			double offsetLat = radius * Math.cos(angle);
			double offsetLng = radius * Math.sin(angle);

			double adjustedLat = baseLat + offsetLat;
			double adjustedLng = baseLng + offsetLng;

			PictureFile picture = cluster.get(i);
			picture.setLatitude((float)adjustedLat);
			picture.setLongitude((float)adjustedLng);
		}

		pictureFiles.saveAll(cluster);
	}
}
