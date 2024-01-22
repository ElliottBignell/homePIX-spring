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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.samples.homepix.CollectionRequestDTO;
import org.springframework.samples.homepix.portfolio.AlbumRepository;
import org.springframework.samples.homepix.portfolio.FolderRepository;
import org.springframework.samples.homepix.portfolio.PaginationController;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

	public PictureCollectionController(PictureFileRepository pictureFiles,
									   PictureCollectionRepository pictures,
									   AlbumRepository albums,
									   FolderRepository folders,
									   KeywordRepository keyword,
									   KeywordRelationshipsRepository keywordsRelationships
	) {

		super(albums, folders, pictureFiles, keyword, keywordsRelationships);
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
											  @ModelAttribute("requestDTO") CollectionRequestDTO redirectedDTO,
											  @PageableDefault(size = 100, sort = "defaultSortField") Pageable pageable, // Default page size and sorting
											  RedirectAttributes redirectAttributes,
											  PictureCollection pictureCollection,
											  BindingResult result,
											  Map<String, Object> model,
											  Authentication authentication) {

		return processFindCollections(requestDTO, redirectedDTO, pageable, pictureCollection, result, model, authentication);
	}


	@GetMapping("/collection")
	public String processFindCollectionsSlashNoSlash(@ModelAttribute CollectionRequestDTO requestDTO,
													 @ModelAttribute("requestDTO") CollectionRequestDTO redirectedDTO,
													 @PageableDefault(size = 100, sort = "defaultSortField") Pageable pageable, // Default page size and sorting
													  PictureCollection pictureCollection,
													  BindingResult result,
													  Map<String, Object> model,
													  Authentication authentication) {

		return processFindCollections(requestDTO, redirectedDTO, pageable, pictureCollection, result, model, authentication);
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
		model.put("keywords", this.keywordRelationships.findByPictureId(pictureId));

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
