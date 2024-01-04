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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
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

	public FolderController(FolderRepository folders, AlbumRepository albums, PictureFileRepository pictureFiles,
			KeywordsRepository keywords) {
		super(albums, folders, pictureFiles, keywords);
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@Secured("ROLE_ADMIN")
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
		return loadFolders(folder, result, model);
	}

	@GetMapping("/folder/")
	public String processFindFoldersSlash(Folder folder, BindingResult result, Map<String, Object> model) {
		return loadFolders(folder, result, model);
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

	@Secured("ROLE_ADMIN")
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

	@Secured("ROLE_ADMIN")
	@PostMapping("/folders/{name}/import/")
	public String importPicturesFromFolder(@Valid Folder folder, Map<String, Object> model) {

		String folderName = folder.getName();
		String filename = this.imagePath + folderName;
		String localName = "/images/" + folderName + "/";

		List<String> fileNames = Stream.of(new File(filename).listFiles()).filter(file -> !file.isDirectory())
				.filter(file -> file.getName().endsWith(".jpg")).map(File::getName)
				.filter(file -> this.pictureFiles.findByFilename(file).isEmpty()).sorted().collect(Collectors.toList());
		List<PictureFile> pictures = new ArrayList<>();

		for (String name : fileNames) {

			PictureFile item = new PictureFile();

			item.setFilename(localName + name);

			try {
				item.setTitle(Folder.getExifTitle(filename + "/" + name));
			}
			catch (Exception ex) {
				System.out.println(ex);
				ex.printStackTrace();
			}

			/*
			 * Keywords keywords = new Keywords(); keywords.setContent(folderName);
			 * item.setKeywords(keywords);
			 *
			 * try { keywordsRepository.save(item); } catch (Exception ex) {
			 * System.out.println(ex); }
			 */

			try {
				pictureFiles.save(item);
			}
			catch (Exception ex) {
				System.out.println(ex);
				ex.printStackTrace();
			}

			pictures.add(item);
		}

		model.put("collection", pictures);
		model.put("baseLink", "/folders/" + folderName);
		model.put("albums", this.albums.findAll());

		return "redirect:/folders/{name}";
	}

	@GetMapping("/folder/{name}")
	public String showFolder(@PathVariable("name") String name, Map<String, Object> model) {

		Collection<Folder> folders = this.folders.findByName(name);

		if (folders.isEmpty()) {
			return "redirect:/folders";
		}
		else {

			Folder folder = folders.iterator().next();
			model.put("folder", folder);
			importPicturesFromFolder(folder, model);

			return "folders/folderDetails";
		}
	}

	@GetMapping("/folders/{name}")
	public String showFolders(@PathVariable("name") String name, Map<String, Object> model) {
		return showFolder(name, model);
	}

	@GetMapping("/folders/{name}/")
	public String showFoldersByName(@PathVariable("name") String name, Map<String, Object> model) {
		return showFolder(name, model);
	}

	@GetMapping("/folders/{name}/file/{filename}")
	public String showPictureFile(@PathVariable("name") String name, @PathVariable("filename") String filename,
			/* @Value("${homepix.images.path}") String imagePath, */ Map<String, Object> model) {

		final String imagePath = System.getProperty("user.dir") + "/images/";

		Collection<Folder> folders = this.folders.findByName(name);

		if (folders.isEmpty()) {
			return "folders/folderList.html";
		}
		else {

			ModelAndView mav = new ModelAndView("albums/albumDetails");
			Folder folder = folders.iterator().next();

			List<PictureFile> pictureFiles = folder.getPictureFiles(imagePath);

			addParams(0, "/images/" + name + "/" + filename, pictureFiles, model, false);

			mav.addObject(pictureFiles);
			model.put("link_params", "");

			model.put("collection", pictureFiles);
			model.put("baseLink", "/folders/" + name);
			model.put("albums", this.albums.findAll());

			Iterable<Album> albums = this.albums.findAll();

			return "picture/pictureFile.html";
		}
	}

	@GetMapping("/folders/{name}/item/{id}")
	public String showPictureFile(@PathVariable("name") String name, @PathVariable("id") int id,
			/* @Value("${homepix.images.path}") String imagePath, */ Map<String, Object> model) {

		final String imagePath = System.getProperty("user.dir") + "/images/";

		Collection<Folder> folders = this.folders.findByName(name);

		if (folders.isEmpty()) {
			return "redirect:/folders/" + name + "/";
		}
		else {

			ModelAndView mav = new ModelAndView("albums/albumDetails");
			Folder folder = folders.iterator().next();

			List<PictureFile> pictureFiles = folder.getPictureFiles(imagePath);

			addParams(0, "/images/" + name + "/" + pictureFiles.get(id).getTitle(), pictureFiles, model, false);

			mav.addObject(pictureFiles);
			model.put("link_params", "");

			model.put("collection", pictureFiles);
			model.put("baseLink", "/folders/" + name);

			return "picture/pictureFile.html";
		}
	}

	@ModelAttribute(name = "albums")
	Iterable<Album> findAllAlbums() {
		return this.albums.findAll();
	}

	@GetMapping(value = "/images/{folder}/{file}")
	public @ResponseBody byte[] getImage(@PathVariable("folder") String folder, @PathVariable("file") String file)
			throws IOException {

		try {

			String filename = System.getProperty("user.dir") + "/images/" + folder + "/" + file;

			File initialFile = new File(filename);
			InputStream targetStream = new FileInputStream(initialFile);

			return targetStream.readAllBytes();
		}
		catch (IOException ex) {

			System.out.println(ex);
			ex.printStackTrace();
			return null;
		}
	}

	@Override
	public void close() throws Exception {

	}

	@PostMapping("/toggleRestricted")
	public String toggleRestricted(@RequestParam(name = "showScary", defaultValue = "false") boolean showScary,
								   Map<String, Object> model
	) {
		// Set a model attribute to track the state of showScary
		model.put("showScary", showScary);

		// Additional logic to update scary pictures in the database or session
		// ...

		return "redirect:/buckets/"; // Redirect back to the original page
	}

	@PostMapping("/restrict/{id}")
	@ResponseBody
	public String restrict(@PathVariable Integer id) {

		Optional<PictureFile> picture = this.pictureFiles.findById(id);

		if (picture.isPresent()) {

			picture.get().setRoles("ROLE_ADMIN");
			this.pictureFiles.save(picture.get());

			return "success"; // Return a response as needed
		}

		return "failure"; // Return a response as needed
	}
	@PostMapping("/derestrict/{id}")
	@ResponseBody
	public String derestrict(@PathVariable Integer id) {

		Optional<PictureFile> picture = this.pictureFiles.findById(id);

		if (picture.isPresent()) {

			picture.get().setRoles("ROLE_USER");
			this.pictureFiles.save(picture.get());

			return "success"; // Return a response as needed
		}

		return "failure"; // Return a response as needed
	}
}
