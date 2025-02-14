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

package org.springframework.samples.homepix.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.CollectionRequestDTO;
import org.springframework.samples.homepix.portfolio.*;
import org.springframework.samples.homepix.portfolio.album.*;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.samples.homepix.portfolio.collection.PictureFileService;
import org.springframework.samples.homepix.portfolio.folder.Folder;
import org.springframework.samples.homepix.portfolio.folder.FolderRepository;
import org.springframework.samples.homepix.portfolio.folder.FolderService;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationships;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRepository;
import org.springframework.samples.homepix.portfolio.locations.Location;
import org.springframework.samples.homepix.portfolio.locations.LocationRelationship;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Controller
class WelcomeController extends PaginationController {

	private final AlbumContentRepository albumContents;

	@Autowired
	private AlbumService albumService;

	@Autowired
	private PictureFileService pictureFileService;

	@Autowired
	public WelcomeController(PictureFileRepository pictureFiles,
							 AlbumRepository albums,
							 FolderRepository folders,
							 AlbumContentRepository albumContents,
							 KeywordRepository keyword,
							 KeywordRelationshipsRepository keywordsRelationships,
							 FolderService folderService
	) {
		super(albums, folders, pictureFiles, keyword, keywordsRelationships, folderService);
		this.albumContents = albumContents;
	}

	@GetMapping("/")
	public String welcome(@ModelAttribute CollectionRequestDTO requestDTO,
						  RedirectAttributes redirectAttributes,
						  Album album,
						  BindingResult result,
						  Map<String, Object> model
	) {
		final String format = "yyyy-MM-dd";
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format, Locale.ENGLISH);

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

		long id = 0;

		Collection<Album> results = this.albums.findByName("Slides");

		if (!results.isEmpty()) {
			id = results.iterator().next().getId();
		}

		Collection<AlbumContent> contents = this.albumContents.findByAlbumId(id);

		if (!contents.isEmpty()) {

			final Comparator<PictureFile> defaultSort = (item1, item2 ) -> { return
				getSortOrder(this.albumContents, album, item1) - getSortOrder(this.albumContents, album, item2);
			};

			Comparator<PictureFile> orderBy = getOrderComparator(requestDTO, defaultSort);

			Collection<PictureFile> slides = contents.stream().map(item -> item.getPictureFile())
				.sorted(orderBy)
				.collect(Collectors.toList());

			Map<Integer, PictureFile> thumbnailsMap = albumService.getThumbnailsMap(
				StreamSupport.stream(slides.spliterator(), false) // Convert Iterable to Stream
					.map(PictureFile::getId)
					.collect(Collectors.toList())
			);

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

			if (results.isEmpty()) {
				model.put("fullUrl", "collection/58123");
			}
			else {
				model.put("fullUrl", "collection/" + results.iterator().next().getThumbnail_id());
			}
		}

		pictureFileService.applyArguments(model, requestDTO);

		model.put("albums", this.albums.findAll());
		model.put("folders", this.folders.findAll().stream()
			.sorted(Comparator.comparing(Folder::getName))
			.collect(Collectors.toList())
		);
		model.put("canonical", "https://www.homepix.ch/");

		Iterable<Album> albumIterable = this.albums.findAll();
		Iterable<Folder> folderIterable = this.folders.findAll();
		Iterable<LocationRelationship> locationIterable = this.locationRelationships.findAll();

		List<String> names = Stream.of(
				StreamSupport.stream(albumIterable.spliterator(), false).map(Album::getName),
				StreamSupport.stream(folderIterable.spliterator(), false).map(Folder::getName),
				StreamSupport.stream(locationIterable.spliterator(), false)
					.map(LocationRelationship::getLocation) // Get Location object
					.map(Location::getName) // Get name from Location
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

		model.put("keywords", names);
		// 1 album found
		return "welcome";
	}

	@Override
	public void close() throws Exception {

	}

	@GetMapping("/logout")
	public String logout(Album album, BindingResult result, Map<String, Object> model) {
		return "redirect:/welcome";
	}

	protected final void loadThumbnailsAndKeywords(Map<Integer, PictureFile> thumbnailsMap, Map<String, Object> model) {
		super.loadThumbnailsAndKeywords(thumbnailsMap, model);
	}
}
