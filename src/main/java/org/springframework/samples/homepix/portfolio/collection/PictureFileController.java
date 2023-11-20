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

import jakarta.validation.Valid;
import org.springframework.samples.homepix.portfolio.Album;
import org.springframework.samples.homepix.portfolio.AlbumRepository;
import org.springframework.samples.homepix.portfolio.FolderRepository;
import org.springframework.samples.homepix.portfolio.PaginationController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Optional;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Elliott Bignell
 */
@Controller
@RequestMapping("/albums/{albumId}")
public class PictureFileController extends PaginationController {

	private static final String VIEWS_PETS_CREATE_OR_UPDATE_FORM = "pets/createOrUpdatePictureFileForm";

	private static final String ABOUT_FORM = "picture/about.html";

	public PictureFileController(PictureFileRepository pictureFiles, AlbumRepository albums, FolderRepository folders) {
		super(albums, folders, pictureFiles);
	}

	@ModelAttribute("types")
	public Collection<PictureFileType> populatePictureFileTypes() {
		return this.pictureFiles.findPictureFileTypes();
	}

	@ModelAttribute("album")
	public Album findOwner(@PathVariable("albumId") int albumId) {
		return this.albums.findById(albumId).get();
	}

	@InitBinder("album")
	public void initOwnerBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@InitBinder("pet")
	public void initPictureFileBinder(WebDataBinder dataBinder) {
		dataBinder.setValidator(new PictureFileValidator());
	}

	@GetMapping("/pets/new")
	public String initCreationForm(Album album, ModelMap model) {
		PictureFile pictureFile = new PictureFile();
		album.addPictureFile(pictureFile);
		model.put("pictureFile", pictureFile);
		return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/pets/new")
	public String processCreationForm(Album album, @Valid PictureFile pictureFile, BindingResult result,
			ModelMap model) {
		if (StringUtils.hasLength(pictureFile.getTitle()) && pictureFile.isNew()
				&& album.getPictureFile(pictureFile.getTitle(), true) != null) {
			result.rejectValue("name", "duplicate", "already exists");
		}
		album.addPictureFile(pictureFile);
		if (result.hasErrors()) {
			model.put("pictureFile", pictureFile);
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		}
		else {
			this.pictureFiles.save(pictureFile);
			return "redirect:/album/{ownerId}";
		}
	}

	@GetMapping("/pets/{petId}/edit")
	public String initUpdateForm(@PathVariable("petId") int petId, ModelMap model) {

		Optional<PictureFile> pictureFile = this.pictureFiles.findById(petId);

		if (pictureFile.isPresent()) {
			model.put("pictureFile", pictureFile);
		}
		return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/pets/{petId}/edit")
	public String processUpdateForm(@Valid PictureFile pictureFile, BindingResult result, Album album, ModelMap model) {
		if (result.hasErrors()) {
			model.put("pictureFile", pictureFile);
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		}
		else {
			album.addPictureFile(pictureFile);
			this.pictureFiles.save(pictureFile);
			return "redirect:/albums/{albumId}";
		}
	}

}
