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
package org.springframework.samples.petclinic.portfolio;

import jakarta.validation.Valid;
import org.springframework.samples.petclinic.portfolio.collection.PictureFile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

/**
 * @author Elliott Bignell
 */
@Controller
class AlbumController extends PaginationController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "album/createOrUpdateOwnerForm";

	private final AlbumRepository albums;

	public AlbumController(AlbumRepository albumService ) {
		this.albums = albumService;
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
		model.put("pagination", super.pagination);
		model.put("album", new Album());
		return "albums/findAlbums";
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
			model.put("pagination", super.pagination);
			model.put("selections", results);
			return "albums/albumList";
		}
	}

	@GetMapping("/album")
	public String processFindAlbums(Album album, BindingResult result, Map<String, Object> model) {

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
			model.put("pagination", super.pagination);
			model.put("selections", results);
			return "albums/albumListPictorial";
		}
	}

	@GetMapping("/albums/{id}/edit")
	public String initUpdateOwnerForm(@PathVariable("id") int id, Model model) {
		Album album = this.albums.findById(id);
		model.addAttribute(album);
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/albums/{id}/edit")
	public String processUpdateOwnerForm(@Valid Album album, BindingResult result,
										 @PathVariable("id") int id) {
		if (result.hasErrors()) {
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}
		else {
			album.setId(id);
			this.albums.save(album);
			return "redirect:/albums/{id}";
		}
	}

	/**
	 * Custom handler for displaying an album.
	 * @param id the ID of the album to display
	 * @return a ModelMap with the model attributes for the view
	 */
	@GetMapping("/album/{id}")
	public ModelAndView showAlbum(@PathVariable("id") int id,  Map<String, Object> model) {
		ModelAndView mav = new ModelAndView("albums/albumDetails");
		Album album = this.albums.findById(id);
		mav.addObject(album);
		model.put("pagination", super.pagination);
		model.put("link_params", "");
		return mav;
	}

	@GetMapping("/albums/{id}")
	public ModelAndView showAlbums(@PathVariable("id") int id,  Map<String, Object> model) {
		return showAlbum(id, model);
	}

	@GetMapping("/albums/{id}/item/{pictureId}")
	public String showElement(@PathVariable("id") int id, @PathVariable("pictureId") int pictureId,  Map<String, Object> model) {

		Album album = this.albums.findById(id);
		Set<PictureFile> pictureFiles = album.getPictureFiles();

        PictureFile picture = pictureFiles.iterator().next();
		PictureFile first = picture;
		PictureFile next = picture;
		PictureFile last  = next;
		PictureFile previous  = last;

		for (Iterator<PictureFile> it = pictureFiles.iterator(); it.hasNext(); ) {
			last = it.next();
		}

		previous = last;

		Iterator<PictureFile> it = pictureFiles.iterator();

		while ( it.hasNext() ) {

			PictureFile f = it.next();

            if ( f.getId() == pictureId ) {

				picture = f;
				break;
			}

			previous = f;
        }

		if ( it.hasNext() ) {
			next = it.next();
		}
		else {
			next = first;
		}

		model.put("pagination", super.pagination);
		model.put("picture", picture );
		model.put("next", next.getId() );
		model.put("previous", previous.getId() );
		model.put("album", album );
		model.put("albums", albums );

		return "picture/pictureFile.html";
	}
}
