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
package org.springframework.samples.homepix.portfolio.album;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.UserRepository;
import org.springframework.samples.homepix.portfolio.folder.FolderService;
import org.springframework.samples.homepix.portfolio.controllers.PaginationController;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRepository;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.samples.homepix.CollectionRequestDTO;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.samples.homepix.User;

/**
 * @author Elliott Bignell
 */
@Controller
public class AlbumController extends PaginationController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "album/createOrUpdateOwnerForm";

	private final AlbumContentRepository albumContent;
	private AlbumService albumService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	public AlbumController(AlbumRepository albums,
						   AlbumContentRepository albumContents,
						   KeywordRepository keyword,
						   KeywordRelationshipsRepository keywordsRelationships,
						   FolderService folderService,
						   AlbumService albumService
	) {
		super(albums, keyword, keywordsRelationships, folderService);
		this.albumContent = albumContents;
		this.albumService = albumService;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@Secured({"ROLE_ADMIN", "ROLE_USER"})
	@GetMapping("/albums/new/")
	public String initCreationForm(Map<String, Object> model) {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String username = auth.getName();

		Optional<User> user = userRepository.findByUsername(username);

		Album album = new Album();
		album.setCount(0);
		album.setName("Test");
		album.setUser_id(user.get());
		this.albums.save(album);

		model.put("album", album);

		return "redirect:/albums/" + album.getId();
	}

	@Secured({"ROLE_ADMIN", "ROLE_USER"})
	@PostMapping("/albums/new/")
	public String processCreationForm(@Valid Album album, BindingResult result, Model model) {

		if (result.hasErrors()) {
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}
		else {

			try {

				Authentication auth = SecurityContextHolder.getContext().getAuthentication();
				String username = auth.getName();

				Optional<User> user = userRepository.findByUsername(username);
				album.setCount(0);
				album.setUser_id(user.get());
				album.setThumbnail_id(62509); //TODO: Eliminate magic number

				this.albums.save(album);
			}
			catch (Exception ex) {

				System.out.println(ex);
				ex.printStackTrace();
				System.out.println(album);
				return "redirect:/album/";
			}

			return "redirect:/albums/" + Long.toString(album.getId());
		}
	}

	@GetMapping("/albums/find")
	public String initFindForm(Map<String, Object> model) {
		model.put("album", new Album());
		return "albums/findAlbums";
	}

	@GetMapping("/albums/")
	public String processFindFormSlash(
		@ModelAttribute CollectionRequestDTO requestDTO,
		Album album,
		BindingResult result,
		Map<String, Object> model
	) {
		return processFindForm(requestDTO, album, result, model);
	}

	@GetMapping("/albums")
	public String processFindForm(
		@ModelAttribute CollectionRequestDTO requestDTO,
		Album album,
		BindingResult result,
		Map<String, Object> model
	) {


		// allow parameterless GET request for /albums to return all records
		if (album.getName() == null) {
			album.setName(""); // empty string signifies broadest possible search
		}

		// find albums by last name
		Collection<Album> results = this.albums.findByName(album.getName());

		if (results.isEmpty()) {
			// no albums found
			result.rejectValue("name", "notFound", "not found");
			return "albums/findAlbums";
		}
		else if (results.size() == 1) {
			// 1 album found
			album = results.iterator().next();
			return "redirect:/albums/" + album.getId();
		}
		else {
			// multiple albums found
			model.put("selections", results);
			return "redirect:/album";
		}
	}

	@GetMapping("/album/")
	public String processFindAlbumsSlash(
		@ModelAttribute CollectionRequestDTO requestDTO,
		Album album,
		BindingResult result,
		Map<String, Object> model
	) {
		return processFindAlbums(requestDTO, album, result, model);
	}

	@GetMapping("/album")
	public String processFindAlbums(
		CollectionRequestDTO requestDTO,
		Album album,
		BindingResult result,
		Map<String, Object> model
	) {

		// allow parameterless GET request for /albums to return all records
		if (album.getName() == null) {
			album.setName(""); // empty string signifies broadest possible search
		}

		// find albums by last name
		Iterable<Album> results = this.albums.findAll();
		Iterator<Album> iter = results.iterator();

		while (iter.hasNext()) {

			Album nextAlbum = iter.next();

			long id = nextAlbum.getId();

			Collection<PictureFile> thumbnail = this.albumContent.findThumbnailIds(id);
			int count = this.albumContent.findByAlbumId(id).size();

			if (count != nextAlbum.getCount()) {
				nextAlbum.setCount(count);
			}

			if (thumbnail.iterator().hasNext()) {
				nextAlbum.setThumbnail(thumbnail.iterator().next());
			}
		}

		model.put("title", "Gallery of photo albums");
		model.put("selections", results);

		Map<Integer, PictureFile> thumbnailsMap = albumService.getThumbnailsMap(
			this.albums.findAll()
		);

	   setStructuredDataForModel(
				requestDTO,
				model,
				"homePIX photo album collection",
				"ImageGallery",
				"Collection of photo albums",
				StreamSupport.stream(results.spliterator(), false) // Convert Iterable to Stream
						.map(Album::getThumbnail)
						.collect(Collectors.toList()),
				"homePIX, photo, landscape, travel, macro, nature, photo, sharing, portfolio, elliott, bignell, collection, folder, album"
		);

		loadThumbnailsAndKeywords(thumbnailsMap, model);

		return "albums/albumListPictorial";
	}

	@Secured("ROLE_ADMIN")
	@GetMapping("/albums/{id}/edit")
	public String initUpdateOwnerForm(@PathVariable("id") long id, Model model) {
		Optional<Album> album = this.albums.findById(id);
		model.addAttribute(album);
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@Secured("ROLE_ADMIN")
	@PostMapping("/albums/{id}/edit")
	public String processUpdateOwnerForm(@Valid Album album, BindingResult result, @PathVariable("id") long id) {
		if (result.hasErrors()) {
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}
		else {
			album.setId(id);
			this.albums.save(album);
			return "redirect:/albums/{id}";
		}
	}

	@Override
	public void close() throws Exception {

	}

}
