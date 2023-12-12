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

import jakarta.transaction.Transactional;
import org.springframework.samples.homepix.CredentialsRunner;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import jakarta.validation.Valid;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.util.*;
import java.net.URI;
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
class BucketController extends PaginationController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "folder/createOrUpdateOwnerForm";

	@FunctionalInterface
	interface BucketOp<One, Two, Three, Four> {

		public Four apply(One one, Two two, Three three);

	}

	public BucketController(FolderRepository folders, AlbumRepository albums, PictureFileRepository pictureFiles) {
		super(albums, folders, pictureFiles);
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@GetMapping("/buckets/new")
	public String initCreationForm(Map<String, Object> model) {
		Folder folder = new Folder();
		model.put("folder", folder);
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/buckets/new")
	public String processCreationForm(@Valid Folder folder, BindingResult result) {
		if (result.hasErrors()) {
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}
		else {
			this.folders.save(folder);
			return "redirect:/buckets/" + folder.getId();
		}
	}

	@GetMapping("/buckets/find")
	public String initFindForm(Map<String, Object> model) {
		model.put("folder", new Folder());
		return "buckets/findbuckets";
	}

	@GetMapping("/buckets/")
	public String processFindFormSlash(Folder folder, BindingResult result, Map<String, Object> model) {
		return processFindForm(folder, result, model);
	}

	@GetMapping("/buckets")
	public String processFindForm(Folder folder, BindingResult result, Map<String, Object> model) {
		return loadBuckets(folder, result, model);
	}

	@GetMapping("/bucket/")
	public String processFindbucketsSlash(Folder folder, BindingResult result, Map<String, Object> model) {
		return loadFolders(folder, result, model);
	}

	@GetMapping("/bucket")
	public String processFindbuckets(Folder folder, BindingResult result, Map<String, Object> model) {

		// allow parameterless GET request for /buckets to return all records
		if (folder.getName() == null) {
			folder.setName(""); // empty string signifies broadest possible search
		}

		// find buckets by last name
		Collection<Folder> results = this.folders.findByName(folder.getName());
		if (results.isEmpty()) {
			// no buckets found
			result.rejectValue("name", "notFound", "not found");
			return "buckets/findbuckets";
		}
		else if (results.size() == 1) {
			// 1 folder found
			folder = results.iterator().next();
			return "redirect:/buckets/" + folder.getId();
		}
		else {
			// multiple buckets found
			model.put("selections", results);
			return "folders/folderListPictorial";
		}
	}

	@GetMapping("/buckets/{ownerId}/edit")
	public String initUpdateOwnerForm(@PathVariable("ownerId") int ownerId, Model model) {
		Optional<Folder> folder = this.folders.findById(ownerId);
		model.addAttribute(folder);
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/buckets/{ownerId}/edit")
	public String processUpdateOwnerForm(@Valid Folder folder, BindingResult result,
			@PathVariable("ownerId") int ownerId) {
		if (result.hasErrors()) {
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}
		else {
			folder.setId(ownerId);
			this.folders.save(folder);
			return "redirect:/buckets/{ownerId}";
		}
	}

	@GetMapping("/bucket/{name}")
	public String showFolder(@PathVariable("name") String name, Map<String, Object> model) {

		initialiseS3Client();

		// Now you can use s3Client to interact with the Exoscale S3-compatible
		// service
		List<PictureFile> results = listFiles(s3Client, "jpegs/" + name);

		Collection<Folder> buckets = this.folders.findByName(name);

		if (buckets.isEmpty()) {
			return "redirect:/buckets";
		}
		else {

			Folder folder = buckets.iterator().next();
			model.put("collection", results);
			model.put("folder", folder);

			return "folders/folderDetails";
		}
	}

	@GetMapping("/buckets/{name}")
	public String showbuckets(@PathVariable("name") String name, Map<String, Object> model) {
		return showFolder(name, model);
	}

	@GetMapping("/buckets/{name}/")
	public String showbucketsByName(@PathVariable("name") String name, Map<String, Object> model) {
		return showFolder(name, model);
	}

	@PostMapping("/buckets/{name}/import/")
	@Transactional
	public String importPicturesFromBucket(@Valid Folder folder, @PathVariable("name") String name,
			Map<String, Object> model) {

		initialiseS3Client();

		List<PictureFile> files = listFiles(s3Client, "jpegs/" + name);

		for (PictureFile file : files) {

			try {

				List<PictureFile> existingFile = pictureFiles.findByFilename(file.getFilename());

				if (existingFile.isEmpty()) {
					pictureFiles.save(file);
				}
			}
			catch (Exception ex) {
				System.out.println(ex);
				ex.printStackTrace();
			}
		}

		model.put("collection", files);
		model.put("baseLink", "/folders/" + name);
		model.put("albums", this.albums.findAll());

		return "redirect:/buckets/{name}";
	}

	@GetMapping("/buckets/{name}/file/{filename}")
	public String showPictureFile(@PathVariable("name") String name, @PathVariable("filename") String filename,
			/* @Value("${homepix.images.path}") String imagePath, */ Map<String, Object> model) {

		final String imagePath = System.getProperty("user.dir") + "/images/";

		Collection<Folder> buckets = this.folders.findByName(name);

		if (buckets.isEmpty()) {
			return "folders/folderList.html";
		}
		else {

			ModelAndView mav = new ModelAndView("albums/albumDetails");
			Folder folder = buckets.iterator().next();

			System.out.println(imagePath);
			System.out.println(folder.getName());

			List<PictureFile> pictureFiles = loadPictureFiles(imagePath, folder.getName());

			System.out.println(pictureFiles.size());
			System.out.println(pictureFiles);

			addParams(0, "/images/" + name + "/" + filename, pictureFiles, model, false);

			mav.addObject(pictureFiles);
			model.put("link_params", "");

			model.put("collection", pictureFiles);
			model.put("baseLink", "/buckets/" + name);
			model.put("albums", this.albums.findAll());

			Iterable<Album> albums = this.albums.findAll();

			return "picture/pictureFile.html";
		}
	}

	@GetMapping("/buckets/{name}/item/{id}")
	public String showPictureFile(@PathVariable("name") String name, @PathVariable("id") int id,
			/* @Value("${homepix.images.path}") String imagePath, */ Map<String, Object> model) {

		final String imagePath = System.getProperty("user.dir") + "/images/";

		Collection<Folder> buckets = this.folders.findByName(name);

		if (buckets.isEmpty()) {
			return "redirect:/buckets/" + name + "/";
		}
		else {

			ModelAndView mav = new ModelAndView("albums/albumDetails");
			Folder folder = buckets.iterator().next();

			PictureFile picture = loadPictureFile(imagePath, folder.getName(), id);

			List<PictureFile> pictureFiles = new ArrayList<>();

			pictureFiles.add(picture);
			pictureFiles.add(picture);
			pictureFiles.add(picture);

			addParams(id, "/images/" + name + "/" + picture.getTitle(), pictureFiles, model, false);

			// mav.addObject(pictureFiles);
			model.put("link_params", "");

			model.put("picture", picture);
			model.put("collection", pictureFiles);
			model.put("baseLink", "/buckets/" + name);

			return "picture/pictureFile.html";
		}
	}

	private List<String> listFileNames(S3Client s3Client, String subFolder) {

		String prefix = subFolder.endsWith("/") ? subFolder : subFolder + "/";

		ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder().bucket(bucketName).prefix(prefix)
				.build();

		ListObjectsV2Response listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);

		List<String> results = new ArrayList<>();

		for (S3Object s3Object : listObjectsResponse.contents()) {

			try {

				String name = s3Object.key();
				String extension = name.substring(name.length() - 4).toLowerCase();

				if (extension.equals(".jpg")) {

					String suffix = name.substring(5, name.length());
					name = "/web-images" + suffix;

					results.add(name);
				}
			}
			catch (Exception ex) {
				System.out.println(ex);
			}
		}

		return results;
	}

	private String getFileName(S3Client s3Client, String subFolder, int id) {

		String prefix = subFolder.endsWith("/") ? subFolder : subFolder + "/";

		ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder().bucket(bucketName).prefix(prefix)
				.build();

		ListObjectsV2Response listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);

		List<S3Object> filteredObjects = listObjectsResponse.contents().stream()
				.filter(object -> !object.key().startsWith(prefix + "200px/"))
				.filter(object -> object.key().endsWith(".jpg")).collect(Collectors.toList());

		S3Object s3Object = filteredObjects.get(id);

		try {

			String name = s3Object.key();
			String extension = name.substring(name.length() - 4).toLowerCase();

			if (extension.equals(".jpg")) {

				String suffix = name.substring(5, name.length());
				name = "/web-images" + suffix;
			}

			return name;
		}
		catch (Exception ex) {
			System.out.println(ex);
		}

		return "Failed to get name";
	}

	private byte[] hitBucket(BucketOp<S3Client, String, String, byte[]> op, String arg1, String arg2) {

		initialiseS3Client();

		// Now you can use s3Client to interact with the Exoscale S3-compatible
		// service
		try {
			return op.apply(s3Client, arg1, arg2);
		}
		catch (Exception e) {
			System.err.println("Error accessing bucket: " + e.getMessage());
		}

		return null;
	}

	// web-images/Milano/dse_020208-dse_020209_6066523_2023_08_05_09_23_52_66_02_00.jpg
	@GetMapping(value = "web-images/{directory}/{file}")
	public @ResponseBody byte[] getFileFromBucket(@PathVariable("directory") String directory,
			@PathVariable("file") String file) {

		return hitBucket((client, arg1, arg2) -> {

			try {
				return downloadFile("jpegs/" + arg1 + "/" + arg2);
			}
			catch (IOException e) {
				System.err.println("Error downloading file: " + e.getMessage());
				return null;
			}
		}, directory, file);
	}

	@GetMapping(value = "web-images/{directory}/200px/{file}")
	public @ResponseBody byte[] getMediumFileFromBucket(@PathVariable("directory") String directory,
			@PathVariable("file") String file) {

		return hitBucket((client, arg1, arg2) -> {

			try {
				return downloadFile("jpegs/" + arg1 + "/200px/" + arg2);
			}
			catch (IOException e) {
				System.err.println("Error downloading file: " + e.getMessage());
				return null;
			}
		}, directory, file);
	}

	private List<PictureFile> loadPictureFiles(String imagePath, String name) {

		List<PictureFile> pictureFiles = new ArrayList<>();

		String dir = imagePath + name;

		List<String> jpegNames = listFileNames(s3Client, "jpegs/" + name);

		int index = 0;

		for (String jpeg : jpegNames) {

			PictureFile item = new PictureFile();

			item.setId(index++);
			item.setFilename(jpeg);

			try {

				String filename = "jpegs/" + name + "/" + jpeg.substring(12, jpeg.length());
				item.setTitle(getExifTitle(filename));
			}
			catch (Exception ex) {
				System.out.println(ex);
				ex.printStackTrace();
			}

			Keywords keywords = new Keywords();
			keywords.setContent(name);
			item.setKeywords(keywords);

			pictureFiles.add(item);
		}

		return pictureFiles;
	}

	private PictureFile loadPictureFile(String imagePath, String name, int id) {

		List<PictureFile> pictureFiles = new ArrayList<>();

		String dir = imagePath + name;

		String jpeg = getFileName(s3Client, "jpegs/" + name, id);

		int index = 0;

		PictureFile item = new PictureFile();

		item.setId(index++);
		item.setFilename(jpeg);

		try {

			String filename = "jpegs/" + name + "/" + jpeg.substring(12);
			item.setTitle(getExifTitle(filename));
		}
		catch (Exception ex) {
			System.out.println(ex);
			ex.printStackTrace();
		}

		Keywords keywords = new Keywords();
		keywords.setContent(name);
		item.setKeywords(keywords);

		pictureFiles.add(item);

		return item;
	}

}
