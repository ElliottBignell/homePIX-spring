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

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.homepix.CollectionRequestDTO;
import org.springframework.samples.homepix.portfolio.PaginationController;
import org.springframework.samples.homepix.portfolio.album.AlbumRepository;
import org.springframework.samples.homepix.portfolio.folder.FolderRepository;
import org.springframework.samples.homepix.portfolio.folder.FolderService;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRepository;
import org.springframework.security.access.annotation.Secured;
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
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Elliott Bignell
 */
@Controller
class PictureCollectionController extends PaginationController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "collection/createOrUpdateOwnerForm";

	private final PictureCollectionRepository pictures;

	private final PictureElasticSearchService pictureElasticSearchService;

	public PictureCollectionController(PictureFileRepository pictureFiles,
									   PictureCollectionRepository pictures,
									   AlbumRepository albums,
									   FolderRepository folders,
									   KeywordRepository keyword,
									   KeywordRelationshipsRepository keywordsRelationships,
									   FolderService folderService,
									   PictureElasticSearchService pictureElasticSearchService
	) {

		super(albums, folders, pictureFiles, keyword, keywordsRelationships, folderService);
		this.pictures = pictures;
		this.pictureElasticSearchService = pictureElasticSearchService;
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
		model.put("keyword_list", this.keywordRelationships.findByPictureId(pictureId)
			.stream()
			.map(kr -> kr.getKeyword().getWord()) // Assuming getKeyword() gets the Keyword object, and getWord() gets the String you want
			.collect(Collectors.joining(", "))
			);

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

	@Secured("ROLE_ADMIN")
	@PostMapping("/image/delete/{id}")
	public ResponseEntity<String>  deleteImage(@PathVariable("id") int id, Map<String, Object> model) {

		Optional<PictureFile> file = this.pictureFiles.findById(Integer.valueOf(id));

		if (file.isPresent()) {

			file.get().setIsScary(true);
			this.pictureFiles.save(file.get());

			return ResponseEntity.ok("Deleted successfully");
		}

		return ResponseEntity.ok("Delete failed");
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
        }
		catch (IOException e) {
            throw new RuntimeException(e);
        }
		catch (InterruptedException e) {
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
        }
		catch (IOException e) {
            throw new RuntimeException(e);
        }
		catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Response status code: " + response.statusCode());
		System.out.println("Response body: " + response.body());

		return "redirect:/buckets";
	}
}
