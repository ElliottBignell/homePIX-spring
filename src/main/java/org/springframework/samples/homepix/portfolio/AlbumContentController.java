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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
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
class AlbumContentController extends PaginationController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "albums/createOrUpdateOwnerForm";

	private final AlbumContentRepository albumContent;

	@Autowired
	public AlbumContentController(AlbumContentRepository albumContent, AlbumRepository albums, FolderRepository folders,
			PictureFileRepository pictureFiles) {
		super(albums, folders, pictureFiles);
		this.albumContent = albumContent;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@GetMapping("/album/{id}/find")
	public String initFindForm(Map<String, Object> model) {
		model.put("album", new AlbumContent());
		return "albums/findAlbums";
	}

	@GetMapping("/album/{id}/{ownerId}/edit")
	public String initUpdateOwnerForm(@PathVariable("ownerId") long id, @PathVariable("ownerId") int ownerId,
			Model model) {
		// AlbumContent albumContent = this.albums.findById(ownerId);
		// model.addAttribute(albumContent);
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	/**
	 * Custom handler for displaying an album.
	 * @param ownerId the ID of the album to display
	 * @return a ModelMap with the model attributes for the view
	 */
	@GetMapping("/album/{id}/{ownerId}")
	public ModelAndView showOwner(@PathVariable("ownerId") long id, @PathVariable("ownerId") int ownerId) {
		ModelAndView mav = new ModelAndView("albums/albumDetails");
		// AlbumContent albumContent = this.albums.findById(ownerId);
		return mav;
	}

	/**
	 * Custom handler for displaying an album.
	 * @param id the ID of the album to display
	 * @return a ModelMap with the model attributes for the view
	 */
	@GetMapping("/album/{id}")
	public ModelAndView showAlbum(@PathVariable("id") long id, Map<String, Object> model) {

		Optional<Album> album = this.albums.findById(id);

		if (album.isPresent()) {

			ModelAndView mav = new ModelAndView("albums/albumDetails");
			Collection<AlbumContent> content = this.albumContent.findByAlbumId(id);

			mav.addObject(album.get());

			model.put("content", content);
			model.put("link_params", "");

			return mav;
		}
		else {
			return new ModelAndView("redirect:/album/");
		}
	}

	@GetMapping("/albums/{id}")
	public ModelAndView showAlbums(@PathVariable("id") long id, Map<String, Object> model) {
		return showAlbum(id, model);
	}

	@GetMapping("/album/{id}/item/{pictureId}")
	public String showElementById(@PathVariable("id") long id, @PathVariable("pictureId") int pictureId,
			Map<String, Object> model) {
		return showElement(id, pictureId, model);
	}

	@GetMapping("/albums/{id}/item/{pictureId}/")
	public String showElementSlash(@PathVariable("id") long id, @PathVariable("pictureId") int pictureId,
			Map<String, Object> model) {
		return showElement(id, pictureId, model);
	}

	@GetMapping("/album/{id}/item/{pictureId}/")
	public String showElementByIdSlash(@PathVariable("id") long id, @PathVariable("pictureId") int pictureId,
			Map<String, Object> model) {
		return showElement(id, pictureId, model);
	}

	@GetMapping("/albums/{id}/item/{pictureId}")
	public String showElement(@PathVariable("id") long id, @PathVariable("pictureId") int pictureId,
			Map<String, Object> model) {

		Optional<Album> album = this.albums.findById(id);
		Collection<PictureFile> pictureFiles = albumContent.findByAlbumId(id).stream()
				.map(item -> item.getPictureFile()).collect(Collectors.toList());

		addParams(pictureId, "", pictureFiles, model, true);

		model.put("baseLink", "/album/" + id);
		model.put("album", album);

		return "picture/pictureFile.html";
	}

	@GetMapping("/albums/{albumId}/add/{pictureId}")
	public String addPictureToAlbum(@PathVariable("albumId") long albumId, @PathVariable("pictureId") int pictureId,
			Map<String, Object> model) {

		Collection<AlbumContent> entry = this.albumContent.findByAlbumIdAndEntryId(albumId, pictureId);

		if (entry.isEmpty()) {

			AlbumContent content = new AlbumContent();
			Optional<PictureFile> picture = this.pictureFiles.findById(pictureId);
			Optional<Album> album = this.albums.findById(albumId);

			if (picture.isPresent() && album.isPresent()) {

				content.setPictureFile(picture.get());
				content.setAlbum(album.get());

				try {
					this.albumContent.save(content);
				}
				catch (Exception ex) {

					System.out.println(ex);
					System.out.println(albumContent);
					return "redirect:/albums/" + Long.toString(albumId);
				}

				return "redirect:/albums/" + Long.toString(albumId);
			}
		}

		return "redirect:/albums/" + Long.toString(albumId);
	}

	@GetMapping("/albums/{albumId}/delete/{pictureId}")
	public String deletePicture(@PathVariable("albumId") long albumId, @PathVariable("pictureId") int pictureId,
			Map<String, Object> model) {

		// Collection<AlbumContent> content =
		// this.albumContent.findByAlbumIdAndEntryId(albumId, pictureId);
		Collection<AlbumContent> content = (Collection<AlbumContent>) this.albumContent.findAll();

		Optional<Album> album = this.albums.findById(albumId);

		if (album != null) {

			/*
			 * Optional<PictureFile> pictureFile = this.pictureFiles.findById(pictureId);
			 *
			 * if (pictureFile.isPresent()) {
			 * album.get().deletePictureFile(pictureFile.get());
			 * this.albums.save(album.get()); }
			 *
			 * return "redirect:/albums/" + Integer.toString(albumId);
			 */
		}

		return "redirect:/albums/3";
	}

}