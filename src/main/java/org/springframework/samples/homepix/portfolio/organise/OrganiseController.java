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
import org.springframework.samples.homepix.portfolio.PaginationController;
import org.springframework.samples.homepix.portfolio.album.*;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.samples.homepix.portfolio.folder.Folder;
import org.springframework.samples.homepix.portfolio.folder.FolderRepository;
import org.springframework.samples.homepix.portfolio.folder.FolderService;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRepository;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 */
@Controller
class OrganiseController extends AlbumContentBaseController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "picture/picturefile_organisation.html";

	private final OrganiseRepository organiseRepository;

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
	@GetMapping("/organise/")
	public String initCreationFormSLash(Map<String, Object> model) {
		return initCreationForm(model);
	}

	@Secured("ROLE_ADMIN")
	@GetMapping("/organise")
	public String initCreationForm(Map<String, Object> model) {
		Organise organise = new Organise();
		model.put("organise", organise);
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@Secured("ROLE_ADMIN")
	@GetMapping("/organise/album/{id}")
	public ModelAndView organiseAlbum(@PathVariable("id") long id) {
		ModelAndView mav = new ModelAndView("organise/organiseAlbumDetails");
		return mav;
	}

	@Override
	public void close() throws Exception {

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
		Collection<PictureFile> pictureFiles = albumContent.findByAlbumId(id).stream()
			.map(item -> item.getPictureFile()).collect(Collectors.toList());

		addParams(pictureId, "", pictureFiles, model, true);

		model.put("baseLink", "/album/" + id);
		model.put("album", album);
		model.put("albums", this.albums.findAll());
		model.put("folders", this.folders.findAll());

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
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}
}
