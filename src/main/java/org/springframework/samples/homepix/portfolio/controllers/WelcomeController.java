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

package org.springframework.samples.homepix.portfolio.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.samples.homepix.CanonicalRedirectFilter;
import org.springframework.samples.homepix.CollectionRequestDTO;
import org.springframework.samples.homepix.ResourceLoaderService;
import org.springframework.samples.homepix.portfolio.album.*;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.samples.homepix.portfolio.collection.PictureFileService;
import org.springframework.samples.homepix.portfolio.folder.Folder;
import org.springframework.samples.homepix.portfolio.folder.FolderRepository;
import org.springframework.samples.homepix.portfolio.folder.FolderService;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRepository;
import org.springframework.samples.homepix.portfolio.locations.Location;
import org.springframework.samples.homepix.portfolio.locations.LocationRelationship;
import org.springframework.samples.homepix.portfolio.locations.LocationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.springframework.cache.annotation.Cacheable;

@Controller
class WelcomeController extends PaginationController {

	@Autowired
	private final AlbumContentRepository albumContents;

	@Autowired
	private final ResourceLoaderService resourceLoaderService;

	@Autowired
	private CanonicalRedirectFilter canonicalRedirectController;

	@Autowired
	private AlbumService albumService;

	@Autowired
	private PictureFileService pictureFileService;

	@Autowired
	LocationService locationService;

	@Autowired
	public WelcomeController(AlbumRepository albums,
							 AlbumContentRepository albumContents,
							 KeywordRepository keyword,
							 KeywordRelationshipsRepository keywordsRelationships,
							 FolderService folderService, ResourceLoaderService resourceLoaderService
	) {
		super(albums, keyword, keywordsRelationships, folderService);
		this.resourceLoaderService = resourceLoaderService;
		this.albumContents = albumContents;
	}

	@GetMapping("/")
	public String welcome(@ModelAttribute CollectionRequestDTO requestDTO,
						  RedirectAttributes redirectAttributes,
						  Album album,
						  BindingResult result,
						  Map<String, Object> model
	) throws IOException {

		final String format = "yyyy-MM-dd";
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format, Locale.ENGLISH);

		Map<String, String> resources = getResources();

		String bundleJsContent = resources.get("/dist/bundle.js");
		String stylesCriticalContentLayout = resources.get("/dist/critical-layout.css"); // Read CSS file
		String stylesCriticalContentWelcome = resources.get("/dist/critical-welcome.css"); // Read CSS file
		String stylesContent = resources.get("/dist/styles.css"); // Read CSS file

		model.put("inlineJs", bundleJsContent);
		model.put("inlineCss", stylesContent);
		model.put("inlineCriticalLayoutCss", stylesCriticalContentLayout);
		model.put("inlineCriticalWelcomeCss", stylesCriticalContentWelcome);

		Supplier<String> today = () -> {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
			LocalDateTime now = LocalDateTime.now();
			return dtf.format(now);
		};

		String toDate = String.format(today.get(), format);

		if (!requestDTO.getSearch().equals("") ||
			!requestDTO.getFromDate().equals("1970-01-01") ||
			!requestDTO.getToDate().equals(toDate)
		) {

			redirectAttributes.addAttribute("search", requestDTO.getSearch());
			redirectAttributes.addAttribute("startDate", requestDTO.getFromDate());
			redirectAttributes.addAttribute("endDate", requestDTO.getToDate());
			redirectAttributes.addAttribute("sort", requestDTO.getSort());

			return "redirect:/collection/";
		}

		Map<Integer, PictureFile> thumbnailsMap = albumService.slidesThumbnailMap();
		final Comparator<PictureFile> defaultSort = (item1, item2 ) -> { return
			albumService.getSortOrder(this.albumContents, album, item1) - albumService.getSortOrder(this.albumContents, album, item2);
		};

		Collection<PictureFile> slides = albumService.geSlides();

		loadThumbnailsAndKeywords(thumbnailsMap, model);

		setStructuredDataForModel(
				requestDTO,
				model,
				"homePIX Photo-sharing Site",
				"ImageGallery",
				"homePIX photo-sharing site featuring landscape, travel, macro and nature photography from Switzerland, Italy, Germany, France and England by Elliott Bignell",
				slides,
				"photo, sharing, portfolio, elliott, bignell"
		);

		model.put("collection", slides);

		Collection<Album> results = this.albums.findByName("Slides");

		if (results.isEmpty()) {
			model.put("fullUrl", "collection/58123");
		}
		else {
			model.put("fullUrl", "collection/" + results.iterator().next().getThumbnail_id());
		}

		pictureFileService.applyArguments(model, requestDTO);

		model.put("albums", albumService.getSortedAlbums());
		model.put("folders", folderService.getSortedFolders());
		model.put("canonical", "https://www.homepix.ch/");
		model.put("root", "true");

		model.put("keywords", getKeywords());
		// 1 album found
		return "welcome";
	}

	@Override
	public void close() throws Exception {

	}

	@GetMapping("/prelogin")
	public String showPrelogin(@RequestParam(required = false) String redirectTo,
							   HttpSession session,
							   Model model) {

		Object currentUrl = session.getAttribute("currentUrl");
		if (currentUrl != null) {
			model.addAttribute("currentUrl", currentUrl);
		} else if (redirectTo != null) {
			session.setAttribute("currentUrl", redirectTo);
		}

		return "login"; // your login page
	}

	@PostMapping("/prelogin")
	public String handlePrelogin(@RequestParam(required = false) String redirectTo,
							   HttpSession session) {
		session.setAttribute("currentUrl", redirectTo);
		return "login"; // your login page
	}

	public String logout(Album album, BindingResult result, Map<String, Object> model) {
		return "redirect:/welcome";
	}

	protected final void loadThumbnailsAndKeywords(Map<Integer, PictureFile> thumbnailsMap, Map<String, Object> model) {
		super.loadThumbnailsAndKeywords(thumbnailsMap, model);
	}

	@Cacheable(value = "css_resources", key = "'css_resources'")
	private Map<String, String> getResources() throws IOException {
		return this.resourceLoaderService.getStandardResources();
	}

	@Cacheable(value = "keywords", key = "'keywords'")
	private List<String> getKeywords() {

		Iterable<Album> albumIterable = this.albums.findAll();
		Iterable<Folder> folderIterable = folderService.getSortedFolders();
		Iterable<LocationRelationship> locationIterable = this.locationService.findAll();

		List<String> names = Stream.of(
				StreamSupport.stream(albumIterable.spliterator(), false).map(Album::getName),
				StreamSupport.stream(folderIterable.spliterator(), false).map(Folder::getName),
				StreamSupport.stream(locationIterable.spliterator(), false)
					.map(LocationRelationship::getLocation) // Get Location object
					.map(Location::getLocation) // Get name from Location
			).flatMap(s -> s) // Flatten into a single stream
			.distinct()
			.collect(Collectors.toList());

		names.add("homePIX");
		names.add("Stock");
		names.add("Licenseable");
		names.add("Photography");
		names.add("Nature");
		names.add("Landscape");
		names.add("Urban");
		names.add("Macro");
		names.add("Calendar");

		return names;
	}

	@CacheEvict(value = { "css_resources", "keywords" }, allEntries = true)
	@Scheduled(cron = "0 0 3 * * *") // every day at 3 AM
	public void resetCache() {
		// This will clear the "folders" cache.
		// Optionally re-fetch or do nothing here;
		// next call to getSortedFolders() will reload.
	}
}
