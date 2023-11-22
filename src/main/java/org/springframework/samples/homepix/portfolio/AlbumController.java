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
package org.springframework.samples.homepix.portfolio;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Elliott Bignell
 */
@Controller
class AlbumController extends PaginationController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "album/createOrUpdateOwnerForm";
	private final AlbumContentRepository albumContent;

	@Autowired
	public AlbumController(AlbumRepository albumService, AlbumRepository albums, FolderRepository folders,
						   PictureFileRepository pictureFiles, AlbumContentRepository albumContent) {
		super(albums, folders, pictureFiles);
		this.albumContent = albumContent;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@GetMapping("/albums/new")
	public String initCreationForm(Map<String, Object> model) {

		Album album = new Album();
		model.put("album", album);
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/albums/new")
	public String processCreationForm(@Valid Album album, BindingResult result) {
		if (result.hasErrors()) {
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}
		else {
			this.albums.save(album);
			return "redirect:/albums/" + album.getId();
		}
	}

	@GetMapping("/albums/find")
	public String initFindForm(Map<String, Object> model) {
		model.put("album", new Album());
		return "albums/findAlbums";
	}

	@GetMapping("/albums/")
	public String processFindFormSlash(Album album, BindingResult result, Map<String, Object> model) {
		return processFindForm(album, result, model);
	}

	@GetMapping("/albums")
	public String processFindForm(Album album, BindingResult result, Map<String, Object> model) {

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
			return "albums/albumList";
		}
	}

	@GetMapping("/album/")
	public String processFindAlbumsSlash(Album album, BindingResult result, Map<String, Object> model) {
		return processFindAlbums(album, result, model);
	}

	@GetMapping("/album")
	public String processFindAlbums(Album album, BindingResult result, Map<String, Object> model) {

		// allow parameterless GET request for /albums to return all records
		if (album.getName() == null) {
			album.setName(""); // empty string signifies broadest possible search
		}

		// find albums by last name
		Iterable<Album> results = this.albums.findAll();
		Iterator<Album> iter = results.iterator();

		while (iter.hasNext()) {

			Album nextAlbum = iter.next();

			int id = nextAlbum.getId();

			Collection<PictureFile> thumbnail = this.albumContent.findThumbnailIds( id );
			int count = this.albumContent.findByAlbumId( id ).size();

			if ( count != nextAlbum.getCount() ) {
				nextAlbum.setCount( count );
			}

			nextAlbum.setThumbnail( thumbnail.iterator().next() );

			/*List<PictureFile> files = contents.stream()
				.filter( item -> item.getAlbum().getId() == id )
				.map( item -> item.getPictureFile() )
				.collect(Collectors.toList());*/
		}

		model.put("selections", results);

		return "albums/albumListPictorial";
	}

	@GetMapping("/albums/{id}/edit")
	public String initUpdateOwnerForm(@PathVariable("id") int id, Model model) {
		Optional<Album> album = this.albums.findById(id);
		model.addAttribute(album);
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/albums/{id}/edit")
	public String processUpdateOwnerForm(@Valid Album album, BindingResult result, @PathVariable("id") int id) {
		if (result.hasErrors()) {
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}
		else {
			album.setId(id);
			this.albums.save(album);
			return "redirect:/albums/{id}";
		}
	}

}
