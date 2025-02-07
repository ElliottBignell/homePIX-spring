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
package org.springframework.samples.homepix.portfolio.organise;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.homepix.CollectionRequestDTO;
import org.springframework.samples.homepix.portfolio.album.*;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.samples.homepix.portfolio.collection.PictureFileService;
import org.springframework.samples.homepix.portfolio.folder.Folder;
import org.springframework.samples.homepix.portfolio.folder.FolderRepository;
import org.springframework.samples.homepix.portfolio.folder.FolderService;
import org.springframework.samples.homepix.portfolio.keywords.Keyword;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationships;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRepository;
import org.springframework.samples.homepix.portfolio.locations.LocationRelationshipsRepository;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 */
@Controller
class OrganiseController extends AlbumContentBaseController {

	private static final String VIEWS_ORGANISATION_FORM = "picture/picturefile_organisation.html";
	private static final String VIEWS_WHOLE_PANE_ORGANISATION_FORM = "organise/full-pane-organise.html";

	private final OrganiseRepository organiseRepository;

	@Autowired
	PictureFileService pictureFileService;

	@Autowired
	public OrganiseController(AlbumContentRepository albumContent,
							  AlbumRepository albums,
							  FolderRepository folders,
							  PictureFileRepository pictureFiles,
							  KeywordRepository keyword,
							  KeywordRelationshipsRepository keywordsRelationships,
							  FolderService folderService,
							  AlbumService albumService,
							  OrganiseRepository organiseRepository
	) {
		super(albumContent, albums, folders, pictureFiles, keyword, keywordsRelationships, folderService, albumService);
		this.organiseRepository = organiseRepository;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@Secured("ROLE_ADMIN")
	@GetMapping("/organise/{left}/{right}/")
	public String initCreationFormSLash(Map<String, Object> model,
										@PathVariable("left") String left,
										@PathVariable("right") String right
	) {
		return initCreationForm(model, left, right);
	}

	@Secured("ROLE_ADMIN")
	@GetMapping("/organise/{left}/{right}")
	public String initCreationForm(Map<String, Object> model,
								   @PathVariable("left") String left,
								   @PathVariable("right") String right
	) {

		Organise organise = new Organise();

		List<PictureFile> leftFolder = this.pictureFiles.findByFolderName(left);
		List<PictureFile> rightFolder = this.pictureFiles.findByFolderName(right);
		Folder folderLeft = this.folders.findByName(left).iterator().next();
		Folder folderRight = this.folders.findByName(right).iterator().next();

		model.put("leftFolder", folderLeft);
		model.put("leftCollection", leftFolder);
		model.put("rightFolder", folderRight);
		model.put("rightCollection", rightFolder);
		model.put("folders", this.folders.findAll().stream()
			.sorted(Comparator.comparing(Folder::getName))
			.collect(Collectors.toList())
		);

		return VIEWS_ORGANISATION_FORM;
	}

	@Secured("ROLE_ADMIN")
	@GetMapping("/organise/album/{id}")
	public ModelAndView organiseAlbum(@ModelAttribute CollectionRequestDTO requestDTO,
									  @PathVariable("id") long id,
									  Map<String, Object> model
	) {

		Optional<Album> album = this.albums.findById(id);

		if (album.isPresent()) {

			List<Keyword> keywords = StreamSupport.stream(this.keyword.findAll().spliterator(), false) // Convert Iterable to Stream
				.collect(Collectors.toList()); // Collect the results into a List

			model.put("total_keywords", keywords);

			return showAlbumContent(album.get(), requestDTO, id, model,"organise/organiseAlbumDetails");
		}
		else {
			return new ModelAndView("redirect:/album/");
		}
	}

	@Secured("ROLE_ADMIN")
	@GetMapping("/organise/")
	public String organise(Map<String, Object> model) {

		Collection<Folder> folder = this.folders.findAll();
		Folder folderLeft = folder.iterator().next();

		List<PictureFile> collection = this.pictureFiles.findByFolderName(folderLeft.getName());
		PictureFile file = collection.iterator().next();

		model.put("current_picture", file);
		model.put("folder", folderLeft);
		model.put("collection", collection);
		model.put("folders", this.folders.findAll().stream()
			.sorted(Comparator.comparing(Folder::getName))
			.collect(Collectors.toList())
		);

		return VIEWS_WHOLE_PANE_ORGANISATION_FORM;
	}

	@Secured("ROLE_ADMIN")
	@GetMapping("/organise/{folder}/")
	public String organiseSinglePane(Map<String, Object> model,
									 @PathVariable("folder") String left
	) {

		List<PictureFile> folder = this.pictureFiles.findByFolderName(left);
		Folder folderLeft = this.folders.findByName(left).iterator().next();

		List<PictureFile> collection = this.pictureFiles.findByFolderName(folderLeft.getName());
		PictureFile file = collection.iterator().next();

		model.put("current_picture", file);
		model.put("folder", folderLeft);
		model.put("collection", folder);
		model.put("folders", this.folders.findAll().stream()
			.sorted(Comparator.comparing(Folder::getName))
			.collect(Collectors.toList())
		);

		return VIEWS_WHOLE_PANE_ORGANISATION_FORM;
	}

	@Override
	public void close() throws Exception {

	}

	@Secured("ROLE_ADMIN")
	@GetMapping("/organise/delete/{ids}")
	public ResponseEntity<String> deleteImages(@PathVariable("ids") int[] ids, Map<String, Object> model) {

		List<PictureFile> deletedFiles = new ArrayList<>();
		for (int id : ids) {
			Optional<PictureFile> file = pictureFiles.findById(id);
			file.ifPresent(picFile -> {
				picFile.setIsScary(true);
				pictureFiles.save(picFile);
				deletedFiles.add(picFile);
			});
		}

		if (!deletedFiles.isEmpty()) {
			return ResponseEntity.ok("Deleted successfully");
		}

		return ResponseEntity.ok("Delete failed");
	}


	/**
	 * Custom handler for displaying an album.
	 * @param id the ID of the album to display
	 * @return a ModelMap with the model attributes for the view
	 */
	@GetMapping("/album/{id}/slideshow/")
	public ModelAndView showAlbumSlideshow(@ModelAttribute CollectionRequestDTO requestDTO,
										   @PathVariable("id") long id,
										   Map<String, Object> model
	) {

		Optional<Album> album = this.albums.findById(id);

		if (album.isPresent()) {
			return showAlbumContent(album.get(), requestDTO, id, model, "welcome");
		}
		else {
			return new ModelAndView("redirect:/album/");
		}
	}

	@GetMapping("/albums/{id}/slideshow")
	public ModelAndView showAlbumsSlideshow(@ModelAttribute CollectionRequestDTO requestDTO,
											@PathVariable("id") long id,
											Map<String, Object> model
	) {
		return showAlbumSlideshow(requestDTO, id, model);
	}

	@GetMapping("/album/{id}/item/{pictureId}")
	public String showElementById(@ModelAttribute CollectionRequestDTO requestDTO,
								  @PathVariable("id") long id,
								  @PathVariable("pictureId") int pictureId,
								  Map<String, Object> model
	) {
		return showElement(requestDTO, id, pictureId, model);
	}

	@GetMapping("/albums/{id}/item/{pictureId}/")
	public String showElementSlash(@ModelAttribute CollectionRequestDTO requestDTO,
								   @PathVariable("id") long id,
								   @PathVariable("pictureId") int pictureId,
								   Map<String, Object> model
	) {
		return showElement(requestDTO, id, pictureId, model);
	}

	@GetMapping("/album/{id}/item/{pictureId}/")
	public String showElementByIdSlash(@ModelAttribute CollectionRequestDTO requestDTO,
									   @PathVariable("id") long id,
									   @PathVariable("pictureId") int pictureId,
									   Map<String, Object> model
	) {
		return showElement(requestDTO, id, pictureId, model);
	}

	@GetMapping("/albums/{id}/item/{pictureId}")
	public String showElement(@ModelAttribute CollectionRequestDTO requestDTO,
							  @PathVariable("id") long id,
							  @PathVariable("pictureId") int pictureId,
							  Map<String, Object> model
	) {

		Optional<Album> album = this.albums.findById(id);
		Collection<PictureFile> pictureCollection = albumContent.findByAlbumId(id).stream()
			.map(item -> item.getPictureFile()).collect(Collectors.toList());

		addParams(pictureId, "", pictureCollection, model, true);

		List<Album> albums = StreamSupport.stream(this.albums.findAll().spliterator(), false) // Convert Iterable to Stream
			.collect(Collectors.toList()); // Collect the results into a List
		List<Folder> folders = new ArrayList<>(this.folders.findAll()); // Collect the results into a List

		model.put("baseLink", "/album/" + id);
		model.put("album", album.orElse(null));
		model.put("albums", albums);
		model.put("folders", this.folders.findAll().stream()
			.sorted(Comparator.comparing(Folder::getName))
			.collect(Collectors.toList())
		);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {

			model.put("keyword_list", this.keywordRelationships.findByPictureId(pictureId).stream()
				.map(KeywordRelationships::getKeyword)
				.sorted(Comparator.comparing(Keyword::getWord))
				.collect(Collectors.toList()));

			model.put("keywords", this.keywordRelationships.findByPictureId(pictureId).stream()
				.map(KeywordRelationships::getKeyword)
				.sorted(Comparator.comparing(Keyword::getWord))
				.map(Keyword::getWord)
				.collect(Collectors.joining(",")));

			List<String> tags = this.keywordRelationships.findByNameContainingOrderByUsageDesc().stream()
				.map(obj -> (String)obj[1])
				.collect(Collectors.toList());
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
		}

		Optional<PictureFile> picture = pictureFiles.findById(pictureId);

		Collection<PictureFile> content = new ArrayList<>();

		picture.ifPresent(content::add);

		setStructuredDataForModel(
			requestDTO,
			model,
			"homePIX photo album collection",
			"ImageGallery",
			"Collection of photo albums",
			content,
			"homePIX, photo, landscape, travel, macro, nature, photo, sharing, portfolio, elliott, bignell, collection, folder, album"
		);

		picture.ifPresent(pictureFile -> pictureFileService.addMapDetails(pictureFile, model));

		return "picture/pictureFile.html";
	}

	@Secured("ROLE_ADMIN")
	@PostMapping("/albums/{name}/curate/")
	public String importPicturesFromBucket(@Valid Folder folder, @PathVariable("name") String name,
										   Map<String, Object> model) {
		return "folders/folderList.html";
	}

	@Secured("ROLE_ADMIN")
	@PostMapping("/album/{id}/delete/")
	public String deleteAlbum(@PathVariable("id") Long id, Map<String, Object> model) {

		Optional<Album> album = albums.findById(id);

		if (album.isPresent()) {

			model.put("id", id);
			model.put("name", "Churches");// album.get().getName());
			model.put("album", album);// album.get().getName());
			return "confirm";
		}

		return "redirect:albums/albumList";
	}

	@Secured("ROLE_ADMIN")
	@PostMapping("/album/{id}/delete/confirm")
	public String confirmDeleteAlbum(@PathVariable("id") Long id, Map<String, Object> model) {

		Optional<Album> album = albums.findById(id);

		if (album.isPresent()) {

			albums.delete(album.get());
			return "redirect:albums/albumList";
		}

		return "redirect:/album/" + Long.toString(id);
	}

	@Secured("ROLE_ADMIN")
	@PostMapping("/album/{id}/delete/abort")
	public String abortDeleteAlbum(@PathVariable("id") Long id, Map<String, Object> model) {
		return "redirect:/album/" + Long.toString(id);
	}

	@Secured("ROLE_ADMIN")
	@GetMapping("/album/{id}/curate")
	public String curateAlbum(@PathVariable("id") Long id, Map<String, Object> model) {
		return postCurateAlbum(id, model);
	}

	@Secured("ROLE_ADMIN")
	@PostMapping("/album/{id}/curate")
	public String postCurateAlbum(@PathVariable("id") Long id, Map<String, Object> model) {

		Optional<Album> album = albums.findById(id);

		Comparator<Folder> nameComparator = Comparator.comparing(Folder::getName);
		Collection<Folder> folderList = folders.findAll().stream().sorted(nameComparator).collect(Collectors.toList());

		if (album.isPresent() && !folderList.isEmpty()) {

			Folder firstFolder = folderList.iterator().next();

			model.put("id", id);
			model.put("name", "Churches");// album.get().getName());
			model.put("album", album);// album.get().getName());
			model.put("folder", firstFolder);// album.get().getName());

			List<PictureFile> results = listFiles(s3Client, "jpegs/" + firstFolder.getName());

			Collection<Folder> buckets = this.folders.findByName(firstFolder.getName());

			if (buckets.isEmpty()) {
				return "redirect:albums/albumList";
			}
			else {

				Folder folder = buckets.iterator().next();
				model.put("collection", results);

				return "albums/curateAlbum";
			}
		}

		return "redirect:albums/albumList";
	}

	@Secured("ROLE_ADMIN")
	@PostMapping("/album/{id}/add/{pictureID}")
	public ResponseEntity<String> ccurateAlbum(@PathVariable("id") Long id,
											   @PathVariable("pictureID") Integer pictureID, Map<String, Object> model) {

		Collection<AlbumContent> existing = albumContent.findByAlbumIdAndEntryId(id, pictureID);

		if (existing.isEmpty()) {

			Optional<Album> album = albums.findById(id);

			if (album.isPresent()) {

				Optional<PictureFile> picture = pictureFiles.findById(pictureID);

				if (picture.isPresent()) {

					AlbumContent newEntry = new AlbumContent();

					newEntry.setAlbum(album.get());
					newEntry.setPictureFile(picture.get());

					albumContent.save(newEntry);

					return ResponseEntity.ok("Added successfully");
				}
			}
		}

		return ResponseEntity.status(400).body("Already added or other error");
	}

	@Secured("ROLE_ADMIN")
	@PostMapping("/album/{albumId}/move/{pictureId}/{index}")
	@ResponseBody
	public String movePicture(
		@PathVariable Long albumId,
		@PathVariable Integer pictureId,
		@PathVariable Integer index
	) {

		Collection<AlbumContent> content1 = this.albumContent.findByAlbumIdAndEntryId(albumId, pictureId);

		if (!content1.isEmpty()) {

			AlbumContent entry1 = content1.iterator().next();

			Long otherIndex = 0L;

			switch (index) {
				case 0:

					while (entry1.getSort_order() > 1) {

						movePicture(albumId, pictureId, -1);

						content1 = this.albumContent.findByAlbumIdAndEntryId(albumId, pictureId);

						if (!content1.isEmpty()) {
							entry1 = content1.iterator().next();
						}
					}
					return "success";

				case -1:
					otherIndex = (long) (entry1.getSort_order() - 1);
					break;
				case 1:
					otherIndex = (long) (entry1.getSort_order() + 1);
					break;
				default:

					Collection<PictureFile> wholeAlbum = this.albumContent.findPictures(albumId);

					while (entry1.getSort_order() < wholeAlbum.size()) {

						movePicture(albumId, pictureId, +1);

						content1 = this.albumContent.findByAlbumIdAndEntryId(albumId, pictureId);

						if (!content1.isEmpty()) {
							entry1 = content1.iterator().next();
						}
					}
					return "success";
			}

			Collection<AlbumContent> content2 = this.albumContent.findByAlbumAndSortOrder(albumId, otherIndex);

			if (!content2.isEmpty()) {

				AlbumContent entry2 = content2.iterator().next();

				int cacheOrder = entry2.getSort_order();

				entry2.setSort_order(entry1.getSort_order());
				entry1.setSort_order(cacheOrder);

				this.albumContent.save(entry1);
				this.albumContent.save(entry2);

				return "success"; // Return a response as needed
			}
		}

		return "error"; // Return a response as needed
	}

	@GetMapping("/album/{id}/find")
	public String initFindForm(Map<String, Object> model) {
		model.put("album", new AlbumContent());
		return "albums/findAlbums";
	}

	@Secured("ROLE_ADMIN")
	@GetMapping("/album/{id}/{ownerId}/edit")
	public String initUpdateOwnerForm(@PathVariable("ownerId") long id, @PathVariable("ownerId") int ownerId,
									  Model model) {
		// AlbumContent albumContent = this.albums.findById(ownerId);
		// model.addAttribute(albumContent);
		return VIEWS_ORGANISATION_FORM;
	}

	@Secured("ROLE_ADMIN")
	@GetMapping("/organise/api")
	public String showPIIForm(Map<String, Object> model) {

		List<String> dummies = Stream.of(new File("..").listFiles()).map(File::getName).sorted()
			.collect(Collectors.toList());

		String dir = System.getProperty("user.dir");
		model.put("working_directory", dir);

		return "organise/api.html";
	}
}
