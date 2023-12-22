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

import org.springframework.data.domain.Sort;
import org.springframework.samples.homepix.CollectionRequestDTO;
import org.springframework.samples.homepix.portfolio.*;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Elliott Bignell
 */
@Controller
class PictureCollectionController extends PaginationController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "collection/createOrUpdateOwnerForm";

	private final PictureCollectionRepository pictures;

	public PictureCollectionController(PictureFileRepository pictureFiles, PictureCollectionRepository pictures,
			AlbumRepository albums, FolderRepository folders, KeywordsRepository keywords) {

		super(albums, folders, pictureFiles, keywords);
		this.pictures = pictures;
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
											  PictureCollection pictureCollection,
											  BindingResult result,
											  Map<String, Object> model) {

		return processFindCollections(requestDTO, pictureCollection, result, model);
	}

	@GetMapping("/collection")
	public String processFindCollections(@ModelAttribute CollectionRequestDTO requestDTO,
										 PictureCollection pictureCollection,
										 BindingResult result,
										 Map<String, Object> model) {

		// allow parameterless GET request for /collections to return all records
		if (pictureCollection.getName() == null) {
			pictureCollection.setName(""); // empty string signifies broadest possible
			// search
		}

		final String format = "yyyy-M-d";

		Supplier<String> today = () -> {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
			LocalDateTime now = LocalDateTime.now();
			return dtf.format(now);
		};

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format, Locale.ENGLISH);

		LocalDate startDate = LocalDate.parse(requestDTO.getFromDate(), formatter);
		LocalDate endDate = LocalDate.parse(requestDTO.getToDate(), formatter);

		Comparator<PictureFile> orderBy = getOrderComparator(requestDTO);

		List<PictureFile> files = null;

		LocalDate start = startDate;
		LocalDate end = endDate.atTime(LocalTime.MAX).toLocalDate();;
		files = this.pictures.findByDates(start, end);

		if (null != requestDTO.getSearch()) {

			files = files.stream()
				.filter( item -> item.getTitle().contains(requestDTO.getSearch()))
				.sorted( orderBy )
				.collect(Collectors.toList());
		}

		model.put("collection", files);

		model.put("startDate", requestDTO.getFromDate());
		model.put("endDate", requestDTO.getToDate());
		model.put("sort", requestDTO.getSort());
		model.put("search", requestDTO.getSearch());

		return "collections/collection";
	}

	/**
	 * Custom handler for displaying an collection.
	 * @param id the ID of the collection to display
	 * @return a ModelMap with the model attributes for the view
	 */
	@GetMapping("/collection/{id}")
	public String showCollection(@RequestParam Optional<String> fromDate, @RequestParam Optional<String> toDate,
			@PathVariable("id") int pictureId, Map<String, Object> model) {

		ModelAndView mav = new ModelAndView("albums/albumDetails");
		mav.addObject(this.pictures.findAll());
		model.put("link_params", "");

		addParams(pictureId, "", pictureFiles.findAll(), model, true);

		model.put("collection", pictureFiles);
		model.put("baseLink", "/collection/" + -1);

		return "picture/pictureFile.html";
	}

	@GetMapping("/collections/{id}")
	public String showCollections(@RequestParam Optional<String> fromDate, @RequestParam Optional<String> toDate,
			@PathVariable("id") int id, Map<String, Object> model) {
		return showCollection(fromDate, toDate, id, model);
	}

	@GetMapping("/collection/{id}/")
	public String showCollectionsSlash(@RequestParam Optional<String> fromDate, @RequestParam Optional<String> toDate,
			@PathVariable("id") int id, Map<String, Object> model) {
		return showCollection(fromDate, toDate, id, model);
	}

	@GetMapping("/collections/{id}/")
	public String showCollectionssSlash(@RequestParam Optional<String> fromDate, @RequestParam Optional<String> toDate,
			@PathVariable("id") int id, Map<String, Object> model) {
		return showCollection(fromDate, toDate, id, model);
	}

	@GetMapping("/collection/{dummyId}/item/{id}")
	public String showCollection(@RequestParam Optional<String> fromDate, @RequestParam Optional<String> toDate,
			@PathVariable("dummyId") int dummyId, @PathVariable("id") int id, Map<String, Object> model) {
		return showCollection(fromDate, toDate, id, model);
	}

	@GetMapping("/collections/{dummyId}/item/{id}/")
	public String showCollectionSlash(@RequestParam Optional<String> fromDate, @RequestParam Optional<String> toDate,
			@PathVariable("dummyId") int dummyId, @PathVariable("id") int id, Map<String, Object> model) {
		return showCollection(fromDate, toDate, id, model);
	}

	@Override
	public void close() throws Exception {

	}

}
