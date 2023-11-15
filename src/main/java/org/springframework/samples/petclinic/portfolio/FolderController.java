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
import org.checkerframework.checker.regex.qual.Regex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.portfolio.collection.PictureFile;
import org.springframework.samples.petclinic.portfolio.collection.PictureFileRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Elliott Bignell
 */
@Controller
class FolderController extends PaginationController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "folder/createOrUpdateOwnerForm";

	public FolderController(FolderRepository folders, AlbumRepository albums, PictureFileRepository pictureFiles) {
		super(albums, folders, pictureFiles);
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@GetMapping("/folders/new")
	public String initCreationForm(Map<String, Object> model) {
		Folder folder = new Folder();
		model.put("folder", folder);
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/folders/new")
	public String processCreationForm(@Valid Folder folder, BindingResult result) {
		if (result.hasErrors()) {
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}
		else {
			this.folders.save(folder);
			return "redirect:/folders/" + folder.getId();
		}
	}

	@GetMapping("/folders/find")
	public String initFindForm(Map<String, Object> model) {
		model.put("folder", new Folder());
		return "folders/findFolders";
	}

	@GetMapping("/folders/")
	public String processFindFormSlash(Folder folder, BindingResult result, Map<String, Object> model) {
		return processFindForm(folder, result, model);
	}

	@GetMapping("/folders")
	public String processFindForm(Folder folder, BindingResult result, Map<String, Object> model) {

		// allow parameterless GET request for /folders to return all records
		if (folder.getName() == null) {

			// String dir = "/mnt/homepix/jpegs/";
			String dir = "/home/elliott/SpringFramweworkGuru/spring-petclinic-old/src/main/resources/static/resources/images/";

			List<String> folderNames = Stream.of(new File(dir).listFiles()).filter(file -> file.isDirectory())
					.map(File::getName).sorted().collect(Collectors.toList());

			folders.deleteAll();

			for (String name : folderNames) {

				Folder item = new Folder();

				item.setName(name);
				item.setThumbnailId(36860);

				final Pattern JPEGS = Pattern.compile(".*jpg$");

				long count = Stream.of(new File(dir + name + "/jpegs/").listFiles()).filter(file -> !file.isDirectory())
						.filter(file -> JPEGS.matcher(file.getName()).find()).count();
				item.setPicture_count((int) count);

				folders.save(item);
			}

			folder.setName(""); // empty string signifies broadest possible search
		}

		// find folders by last name
		Collection<Folder> results = this.folders.findByName(folder.getName());
		if (results.isEmpty()) {
			// no folders found
			result.rejectValue("name", "notFound", "not found");
			return "folders/findFolders";
		}
		else if (results.size() == 1) {
			// 1 folder found
			folder = results.iterator().next();
			return "redirect:/folders/" + folder.getId();
		}
		else {
			// multiple folders found
			model.put("selections", results);
			return "folders/folderList";
		}
	}

	@GetMapping("/folder/")
	public String processFindFoldersSlash(Folder folder, BindingResult result, Map<String, Object> model) {
		return processFindFolders(folder, result, model);
	}

	@GetMapping("/folder")
	public String processFindFolders(Folder folder, BindingResult result, Map<String, Object> model) {

		// allow parameterless GET request for /folders to return all records
		if (folder.getName() == null) {
			folder.setName(""); // empty string signifies broadest possible search
		}

		// find folders by last name
		Collection<Folder> results = this.folders.findByName(folder.getName());
		if (results.isEmpty()) {
			// no folders found
			result.rejectValue("name", "notFound", "not found");
			return "folders/findFolders";
		}
		else if (results.size() == 1) {
			// 1 folder found
			folder = results.iterator().next();
			return "redirect:/folders/" + folder.getId();
		}
		else {
			// multiple folders found
			model.put("selections", results);
			return "folders/folderListPictorial";
		}
	}

	@GetMapping("/folders/{ownerId}/edit")
	public String initUpdateOwnerForm(@PathVariable("ownerId") int ownerId, Model model) {
		Optional<Folder> folder = this.folders.findById(ownerId);
		model.addAttribute(folder);
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/folders/{ownerId}/edit")
	public String processUpdateOwnerForm(@Valid Folder folder, BindingResult result,
			@PathVariable("ownerId") int ownerId) {
		if (result.hasErrors()) {
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}
		else {
			folder.setId(ownerId);
			this.folders.save(folder);
			return "redirect:/folders/{ownerId}";
		}
	}

	@GetMapping("/folder/{name}")
	public String showFolder(@PathVariable("name") String name, Model model) {

		Collection<Folder> folder = this.folders.findByName(name);

		if (folder.isEmpty()) {
			return "/folders";
		}
		else {
			model.addAttribute(folder.iterator().next());
			return "folders/folderDetails";
		}
	}

	@GetMapping("/folders/{name}")
	public String showFolders(@PathVariable("name") String name, Model model) {
		return showFolder(name, model);
	}

	@GetMapping("/folders/{name}/")
	public String showFoldersByName(@PathVariable("name") String name, Model model) {
		return showFolder(name, model);
	}

	@GetMapping("/folders/{name}/file/{filename}")
	public String showPictureFile(@PathVariable("name") String name, @PathVariable("filename") String filename,
			Map<String, Object> model) {

		Collection<Folder> folders = this.folders.findByName(name);

		if (folders.isEmpty()) {
			return "folders/folderList.html";
		}
		else {

			ModelAndView mav = new ModelAndView("albums/albumDetails");
			Folder folder = folders.iterator().next();

			List<PictureFile> pictureFiles = folder.getPictureFiles();

			addParams(0, "/resources/images/" + name + "/jpegs" + '/' + filename, pictureFiles, model, false);

			mav.addObject(pictureFiles);
			model.put("link_params", "");

			model.put("collection", pictureFiles);
			model.put("baseLink", "/folders/" + name);
			model.put("albums", this.albums.findAll());

			Collection<Album> albums = this.albums.findAll();

			return "picture/pictureFile.html";
		}
	}

	@GetMapping("/folders/{name}/item/{id}")
	public String showPictureFile(@PathVariable("name") String name, @PathVariable("id") int id,
			Map<String, Object> model) {

		Collection<Folder> folders = this.folders.findByName(name);

		if (folders.isEmpty()) {
			return "/folders/" + name + "/";
		}
		else {

			ModelAndView mav = new ModelAndView("albums/albumDetails");
			Folder folder = folders.iterator().next();

			List<PictureFile> pictureFiles = folder.getPictureFiles();

			addParams(0, "/resources/images/" + name + "/jpegs" + '/' + pictureFiles.get(id).getTitle(), pictureFiles,
					model, false);

			mav.addObject(pictureFiles);
			model.put("link_params", "");

			model.put("collection", pictureFiles);
			model.put("baseLink", "/folders/" + name);

			return "picture/pictureFile.html";
		}
	}

	@ModelAttribute(name = "albums")
	Collection<Album> findAllAlbums() {
		return this.albums.findAll();
	}

}
