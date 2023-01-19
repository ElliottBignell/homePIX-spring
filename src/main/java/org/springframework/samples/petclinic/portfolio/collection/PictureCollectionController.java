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
package org.springframework.samples.petclinic.portfolio.collection;

import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.samples.petclinic.portfolio.Album;
import org.springframework.samples.petclinic.portfolio.AlbumRepository;
import org.springframework.samples.petclinic.portfolio.PaginationController;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Elliott Bignell
 */
@Controller
class PictureCollectionController extends PaginationController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "collection/createOrUpdateOwnerForm";

	private final PictureFileRepository pictureFiles;

	private final PictureCollectionRepository pictures;

	public PictureCollectionController(PictureFileRepository pictureFiles, PictureCollectionRepository pictures) {
		this.pictureFiles = pictureFiles;
		this.pictures = pictures;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@GetMapping("/collections/find")
	public String initFindForm(Map<String, Object> model) {
		model.put("pagination", super.pagination);
		model.put("collection", new PictureCollection());
		return "collections/findCollections";
	}

	@GetMapping("/collections")
	public String processFindForm(PictureCollection pictureCollection, BindingResult result, Map<String, Object> model) {

		// allow parameterless GET request for /collections to return all records
		if (pictureCollection.getName() == null) {
			pictureCollection.setName(""); // empty string signifies broadest possible search
		}

		List< PictureFile > collection = this.pictures.findAll();

		model.put("pagination", super.pagination);
		model.put("collection", collection);

		return "collections/collection";
	}

	@GetMapping("/collection/")
	public String processFindCollectionsSlash(
		@RequestParam Optional<String> fromDate,
		@RequestParam Optional<String> toDate,
		PictureCollection pictureCollection,
		BindingResult result,
		Map<String, Object> model
	) {
		return processFindCollections( fromDate, toDate, pictureCollection, result, model );
	}

	@GetMapping("/collection")
	public String processFindCollections(
		@RequestParam Optional<String> fromDate,
		@RequestParam Optional<String> toDate,
		PictureCollection pictureCollection,
		BindingResult result,
		Map<String, Object> model
	) {
		// allow parameterless GET request for /collections to return all records
		if (pictureCollection.getName() == null) {
			pictureCollection.setName(""); // empty string signifies broadest possible search
		}

		final String format = "yyyy-MMM-d";

		Supplier< String > today = () -> {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern( format );
			LocalDateTime now = LocalDateTime.now();
			 return dtf.format(now);
		};

		String fromText = fromDate.isEmpty() ? "1970-Jan-01" : fromDate.get();
		String   toText =   toDate.isEmpty() ? today.get()   :   toDate.get();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format, Locale.ENGLISH);
		LocalDate from = LocalDate.parse( fromText, formatter );
		LocalDate to   = LocalDate.parse(   toText, formatter );

		List< PictureFile > collection = this.pictures.findAll();

		model.put("fromDate", from );
		model.put("toDate",   to   );
		model.put("pagination", super.pagination);
		model.put("collection", collection);

		return "collections/collection";
	}
}
