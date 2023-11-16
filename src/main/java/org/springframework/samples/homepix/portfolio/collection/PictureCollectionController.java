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
import org.springframework.samples.homepix.portfolio.AlbumRepository;
import org.springframework.samples.homepix.portfolio.PaginationController;
import org.springframework.samples.homepix.portfolio.FolderRepository;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;

import org.springframework.samples.homepix.portfolio.collection.PictureCollectionRepository;
import org.springframework.samples.homepix.portfolio.PaginationController;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.samples.homepix.portfolio.collection.PictureCollection;

/**
 * @author Elliott Bignell
 */
@Controller
class PictureCollectionController extends PaginationController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "collection/createOrUpdateOwnerForm";

	private final PictureCollectionRepository pictures;

	public PictureCollectionController(PictureFileRepository pictureFiles, PictureCollectionRepository pictures,
			AlbumRepository albums, FolderRepository folders) {
		super(albums, folders, pictureFiles);
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

		List<PictureFile> collection = this.pictures.findAll();

		model.put("collection", collection);

		return "collections/collection";
	}

	@GetMapping("/collection/")
	public String processFindCollectionsSlash(@RequestParam Optional<String> fromDate,
			@RequestParam Optional<String> toDate, @RequestParam Optional<String> sort,
			PictureCollection pictureCollection, BindingResult result, Map<String, Object> model) {
		return processFindCollections(fromDate, toDate, sort, pictureCollection, result, model);
	}

	@GetMapping("/collection")
	public String processFindCollections(@RequestParam Optional<String> fromDate, @RequestParam Optional<String> toDate,
			@RequestParam Optional<String> sort, PictureCollection pictureCollection, BindingResult result,
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

		String fromText = !fromDate.isPresent() || fromDate.get() == "" ? "1970-01-01" : fromDate.get();
		String toText = !toDate.isPresent() || toDate.get() == "" ? today.get() : toDate.get();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format, Locale.ENGLISH);

		LocalDate startDate = LocalDate.of(2022, 10, 10);
		LocalDate endDate = LocalDateTime.now().toLocalDate();

		boolean datesValid = true;

		try {
			LocalDate from = LocalDate.parse(fromText, formatter);
			startDate = from;
		}
		catch (Exception ex) {
			startDate = LocalDate.parse("1970-01-01", formatter);
			datesValid = false;
		}

		try {
			endDate = LocalDate.parse(toText, formatter);
		}
		catch (Exception ex) {
			endDate = LocalDate.parse(today.get(), formatter);
			datesValid = false;
		}

		String sortCriterion = "title";
		Sort.Direction direction = Sort.Direction.ASC;

		if (sort.isPresent()) {

			switch (sort.get()) {
			case "Filename":
				sortCriterion = "filename";
				break;
			case "Date":
				sortCriterion = "taken_on";
				break;
			case "Size":
				sortCriterion = "filename";
				break;
			case "Aspect Ratio":
				sortCriterion = "sortkey";
				break;
			case "Saved Order":
				sortCriterion = "sortkey";
				break;
			}
		}

		Sort sorter = Sort.by(direction, sortCriterion);

		List<PictureFile> collection;

		if (datesValid) {
			collection = this.pictures.findByDates(startDate, endDate, sorter);
		}
		else {
			collection = this.pictures.findAll();
		}

		model.put("collection", collection);
		model.put("startDate", startDate);
		model.put("endDate", endDate);

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
		List<PictureFile> pictureFiles = this.pictures.findAll();
		mav.addObject(pictureFiles);
		model.put("link_params", "");

		addParams(pictureId, "", pictureFiles, model, true);

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

}
