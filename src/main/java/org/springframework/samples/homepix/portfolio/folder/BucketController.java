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
package org.springframework.samples.homepix.portfolio.folder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.homepix.CollectionRequestDTO;
import org.springframework.samples.homepix.portfolio.controllers.PaginationController;
import org.springframework.samples.homepix.portfolio.album.Album;
import org.springframework.samples.homepix.portfolio.album.AlbumRepository;
import org.springframework.samples.homepix.portfolio.album.AlbumService;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.samples.homepix.portfolio.collection.PictureFileService;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationships;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsService;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRepository;
import org.springframework.samples.homepix.portfolio.locations.LocationRelationship;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
// Define a class-level logger

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

	private static Map<String, byte[]> image200pxCacheMap = new HashMap<>();

	private AlbumService albumService;

	@Autowired
	FolderRepository folderRepository;

	@Autowired
	KeywordRelationshipsService keywordRelationshipsService;

	private final PictureFileService pictureFileService;

	@FunctionalInterface
	interface BucketOp<One, Two, Three, Four> {

		public Four apply(One one, Two two, Three three);

	}

	public BucketController(FolderRepository folders,
							AlbumRepository albums,
							PictureFileRepository pictureFiles,
							KeywordRepository keyword,
							KeywordRelationshipsRepository keywordsRelationships,
							FolderService folderService,
							AlbumService albumService,
							PictureFileService pictureFileService
	) {
		super(albums, folders, pictureFiles, keyword, keywordsRelationships, folderService);
		this.albumService = albumService;
		this.pictureFileService = pictureFileService;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@Secured("ROLE_ADMIN")
	@GetMapping("/buckets/new")
	public String initCreationForm(Map<String, Object> model) {
		Folder folder = new Folder();
		model.put("folder", folder);
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@Secured("ROLE_ADMIN")
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
	public String processFindFormSlash(
		Folder folder,
		@ModelAttribute CollectionRequestDTO requestDTO,
		BindingResult result,
		Map<String, Object> model
	) {
		return processFindForm(folder, requestDTO, result, model);
	}

	@GetMapping("/buckets")
	public String processFindForm(
		Folder folder,
		@ModelAttribute CollectionRequestDTO requestDTO,
		BindingResult result,
		Map<String, Object> model
	) {

		folderCache = null;

		loadBuckets(folder, result, model);

		List<Folder> folders = new ArrayList<>(this.folderService.getSortedFolders());
		Map<Integer, PictureFile> thumbnailsMap = folderService.getThumbnailsMap(folders);

		List<PictureFile> files = folderService.getThumbnails(folders);

	    setStructuredDataForModel(
				requestDTO,
				model,
				"homePIX Photo-sharing Site",
				"ImageGallery",
				"Photo collection",
				files,
				"homePIX, photo, landscape, travel, macro, nature, photo, sharing, portfolio, elliott, bignell, collection, folder, album"
		);

		Set<Integer> pictureIds = new HashSet<>(thumbnailsMap.keySet());

		Collection<KeywordRelationships> relations = keywordRelationshipsService.getRelationshipsByPictureIds(pictureIds);

		model.put("thumbnails", thumbnailsMap);
		model.put("title", "Gallery of picture folders");
		model.put("description", pageDescription);
		model.put("keywords", relations.stream()
			.map(relationship -> relationship.getKeyword().getWord())
			.distinct()
			.sorted()
			.collect(Collectors.joining(",")));

		model.put("fullUrl", "collection/" + pictureIds.iterator().next());
		model.put("picture", files.iterator().next());

		// User is not authenticated or not an admin
		return "folders/folderListPictorial";
	}

	@GetMapping("/buckets/admin")
	public String folderAdminPage(
		Folder folder,
		@ModelAttribute CollectionRequestDTO requestDTO,
		BindingResult result,
		Map<String, Object> model
	) {

		folderCache = null;

		loadBuckets(folder, result, model);

		// multiple folders found
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.isAuthenticated()) {
			// User is authenticated
			boolean isAdmin = authentication.getAuthorities().stream()
				.anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));

			if (isAdmin) {
				// User is an administrator
				return "folders/folderList";
			}
		}

		List<Folder> folders = new ArrayList<>(this.folderService.getSortedFolders());
		Map<Integer, PictureFile> thumbnailsMap = folderService.getThumbnailsMap(folders);
		model.put("thumbnails", thumbnailsMap);

		// User is not authenticated or not an admin
		return "folders/folderListPictorial";
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

	@Secured("ROLE_ADMIN")
	@GetMapping("/buckets/{ownerId}/edit")
	public String initUpdateOwnerForm(@PathVariable("ownerId") int ownerId, Model model) {
		Optional<Folder> folder = this.folders.findById(ownerId);
		model.addAttribute(folder);
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@Secured("ROLE_ADMIN")
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

	public String showFolder(
		CollectionRequestDTO requestDTO,
		String name,
		Pageable pageable,
		Map<String, Object> model,
		Authentication authentication,
		boolean reload,
		HttpServletRequest request
	) {
		name = name.replace("-", "_");

		name = folderRepository.findByNameCaseInsensitive(name).iterator().next().getName();

		String userAgent = request.getHeader("User-Agent");
		logger.info("Request from User-Agent to showFolder: " + userAgent);

		model.put("showScary", true);

		Page<PictureFile> results;

		if (reload) {

			initialiseS3Client();

			results = listFilteredFilesPaged(listFiles(s3Client, "jpegs/" + name), requestDTO, authentication, pageable);
		}
		else {

			final String format = "yyyy-M-d";

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format, Locale.ENGLISH);

			String fromDate = requestDTO.getFromDate();
			String toDate = requestDTO.getToDate();

			if (fromDate.equals("")) {
				fromDate = "1970-01-01";
			}

			if (toDate.equals("")) {

				Supplier<String> supplier = () -> {
					DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
					LocalDateTime now = LocalDateTime.now();
					return dtf.format(now);
				};

				toDate = supplier.get();
			}

			LocalDate startDate = LocalDate.parse(fromDate, formatter);
			LocalDate endDate = LocalDate.parse(toDate, formatter).plusDays(1);

			String search = requestDTO.getSearch();

			if (search.equals(":!*")) {
				List<PictureFile> files = this.pictureFiles.findByFolderNameAndNoKeywords(name, startDate, endDate);
				results = listFilesPaged(
					files,
					requestDTO,
					authentication,
					pageable
				);
			}
			else {
				results = listFilteredFilesPaged(
					this.pictureFiles.findByFolderName(name, search, startDate, endDate),
					requestDTO,
					authentication,
					pageable
				);
			}
		}

		int pageSize = results.getSize();
		int number = results.getNumber();
		long total = results.getTotalElements();
		int firstIndex = results.getNumber() * pageSize + 1;
		long lastIndex = firstIndex + pageSize - 1;

		if (lastIndex > total) {
			lastIndex= total;
		}

		Map<Integer, PictureFile> thumbnailsMap = albumService.getThumbnailsMap(
			StreamSupport.stream(results.spliterator(), false) // Convert Iterable to Stream
				.map(PictureFile::getId)
				.collect(Collectors.toList())
		);

		loadThumbnailsAndKeywords(thumbnailsMap, model);

		// Add the results to the model
		model.put("results", results);
		model.put("pageNumber", results.getNumber());
		model.put("pageSize", results.getSize());
		model.put("totalPages", results.getTotalPages());
		model.put("firstIndex", firstIndex);
		model.put("lastIndex", lastIndex);
		model.put("count", results.getTotalElements());
		model.put("title", name + " picture folder");

		if (results != null) {

			if (results.iterator().hasNext()) {

				PictureFile picture = results.iterator().next();
				model.put("fullUrl", "collection/" + picture.getId());
				model.put("picture", picture);
			}
		}
		else  {
			model.put("fullUrl", "collection/58123");
			model.put("picture", pictureFiles.findById(58123).get());
		}

		pictureFileService.addMapDetails(pictureFiles.findByFolderName(name), model);

		setStructuredDataForModel(
			requestDTO,
			model,
			"homePIX Photo-sharing folder for " + name,
			"ImageGallery",
			"Photo folder containing shots taken in " + name,
			results.getContent(),
			"photo, sharing, portfolio, elliott, bignell"
		);

		return setModel(requestDTO, model, this.folders.findByName(name), results.getContent(), "folders/folderDetails");
	}


	public String showFolderSlideshow(
		CollectionRequestDTO requestDTO,
		@PathVariable("name")
		String name,
		Authentication authentication,
		Map<String, Object> model,
		HttpServletRequest request
	) {
		String userAgent = request.getHeader("User-Agent");
		logger.info("Request from User-Agent to showFolderSlideshow: " + userAgent);

		initialiseS3Client();

		Comparator<PictureFile> orderBy = getOrderComparator(requestDTO);

		List<PictureFile> results = listFilteredFiles(
			this.pictureFiles.findByFolderName(name + "/"),
			requestDTO,
			authentication
		);

		Collection<Folder> buckets = this.folders.findByName(name);

		setStructuredDataForModel(
			requestDTO,
			model,
			"homePIX Photo-sharing Site",
			"ImageGallery",
			"homePIX photo-sharing site featuring landscape, travel, macro and nature photography from Switzerland, Italy, Germany, France and England by Elliott Bignell",
			results,
			"photo, sharing, portfolio, elliott, bignell"
		);

		return setModel(requestDTO, model, this.folders.findByName(name), results, "welcome");
	}

	@GetMapping("/buckets/{name}")
	public String showbuckets(@ModelAttribute CollectionRequestDTO requestDTO,
							  @PathVariable("name") String name,
							  @PageableDefault(size = 100, sort = "defaultSortField") Pageable pageable, // Default page size and sorting
							  Authentication authentication,
							  Map<String, Object> model,
							  HttpServletRequest request
	) {
		return showFolder(requestDTO, name, pageable, model, authentication,false, request);
	}

	@GetMapping("/buckets/{name}/")
	public String showbucketsByName(@ModelAttribute CollectionRequestDTO requestDTO,
									@PathVariable("name") String name,
									@PageableDefault(size = 100, sort = "defaultSortField") Pageable pageable, // Default page size and sorting
									Authentication authentication,
									Map<String, Object> model,
									HttpServletRequest request
	) {
		return showFolder(requestDTO, name, pageable, model, authentication, false, request);
	}

	@GetMapping("/buckets/{name}/slideshow")
	public String showbucketsSlideshow(@ModelAttribute CollectionRequestDTO requestDTO,
							  @PathVariable("name") String name,
							  Authentication authentication,
							  Map<String, Object> model,
							  HttpServletRequest request
	) {
		return showFolderSlideshow(requestDTO, name, authentication, model, request);
	}

	@GetMapping("/buckets/{name}/slideshow/")
	public String showbucketsByNameSlideshow(@ModelAttribute CollectionRequestDTO requestDTO,
									@PathVariable("name") String name,
									Authentication authentication,
									Map<String, Object> model,
									HttpServletRequest request
	) {
		return showFolderSlideshow(requestDTO, name, authentication, model, request);
	}

	@Secured("ROLE_ADMIN")
	@PostMapping("/buckets/{name}/import/")
	@Transactional
	public String importPicturesFromBucket(@Valid Folder folder, @PathVariable("name") String name,
										   Map<String, Object> model) {

		initialiseS3Client();

		List<PictureFile> files = listFiles(s3Client, "jpegs/" + name);

		Collection<Folder> existingFolder = this.folders.findByName(folder.getName());

		if (!existingFolder.isEmpty()) {
			folder = existingFolder.iterator().next();
		}
		else {

			folder.setPicture_count(files.size());
			this.folders.save(folder);
		}

		// Fetch existing filenames from the database
		List<PictureFile> existingFiles = pictureFiles.findByFilenames(
			files.stream().map(PictureFile::getFilename).collect(Collectors.toList())
		);

		final Folder newFolder = folder;

		for (PictureFile file : files) {

			try {

				// Check if the filename already exists in the database
				Optional<PictureFile> existingFileOptional = existingFiles.stream()
					.filter(existingFile -> existingFile.getFilename().equals(file.getFilename()))
					.findFirst();

				if (existingFileOptional.isPresent()) {
					// Update existing record
					PictureFile existingFile = existingFileOptional.get();
					existingFile.setFolder(folder);
					pictureFiles.save(existingFile);
				} else {
					// Save new record
					file.setFolder(folder);
					pictureFiles.save(file);
				}
			}
			catch (Exception e) {
				logger.log(Level.SEVERE, "An error occurred: " + e.getMessage(), e);
			}
		}

		model.put("collection", files);
		model.put("baseLink", "/folders/" + name);

		return "redirect:/buckets/{name}";
	}

	@Secured("ROLE_ADMIN")
	@PostMapping("/buckets/{name}/import_async/")
	@Transactional
	public ResponseEntity<Map<String, Object>> importPicturesFromBucketAsync(@Valid Folder folder, @PathVariable("name") String name,
																			 Map<String, Object> model) {

		importPicturesFromBucket(folder, name, model);

		Collection<Folder> folders = this.folders.findByName(name);

		Map<String, Object> responseData = new HashMap<>();

		if (!folders.isEmpty()) {

			// Assuming you have the picture count in the folder object
			int pictureCount = folders.iterator().next().getPicture_count();

			// Create a new map to hold the response data
			responseData.put("newPictureCount", pictureCount);

			// Return the response data with a success status code
		}

		return ResponseEntity.ok(responseData);
	}

	@GetMapping("/buckets/{name}/item/{id}/")
	public String showPictureFileSlash(@ModelAttribute CollectionRequestDTO requestDTO,
								  @PathVariable("name") String name,
								  @PathVariable("id") Integer id,
								  Map<String, Object> model,
								  Authentication authentication,
								  HttpServletRequest request
	) {
		return showPictureFile(requestDTO, name, id, model, authentication, request);
	}

	@GetMapping("/buckets/{name}/item/{id}")
	public String showPictureFile(@ModelAttribute CollectionRequestDTO requestDTO,
								  @PathVariable("name") String name,
								  @PathVariable("id") Integer id,
								  Map<String, Object> model,
								  Authentication authentication,
								  HttpServletRequest request
	) {
		String userAgent = request.getHeader("User-Agent");
		logger.info("Request from User-Agent to showPictureFile(/buckets/" + name + "/item/"+ id + "): " + userAgent);

		final String imagePath = System.getProperty("user.dir") + "/images/";

		Collection<Folder> buckets = this.folders.findByName(name);

		if (buckets.isEmpty()) {
			return "folders/folderList.html";
		}
		else {

			ModelAndView mav = new ModelAndView("albums/albumDetails");
			Folder folder = buckets.iterator().next();

			Comparator<PictureFile> orderBy = getOrderComparator(requestDTO);

			final String format = "yyyy-M-d";

			// TODO: Put this repeated fragment dealing with dates into a service
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format, Locale.ENGLISH);

			String search = requestDTO.getSearch();
			List<PictureFile> pictureFiles;

			String fromDate = requestDTO.getFromDate();
			String toDate = requestDTO.getToDate();

			if (fromDate.equals("")) {
				fromDate = "1970-01-01";
			}

			if (toDate.equals("")) {

				Supplier<String> supplier = () -> {
					DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
					LocalDateTime now = LocalDateTime.now();
					return dtf.format(now);
				};

				toDate = supplier.get();
			}

			if (search.equals(":!*")) {

				LocalDate startDate = LocalDate.parse(fromDate, formatter);
				LocalDate endDate = LocalDate.parse(toDate, formatter);

				List<PictureFile> files = this.pictureFiles.findByFolderNameAndNoKeywords(name, startDate, endDate);
				pictureFiles = listFiles(
					files,
					requestDTO,
					authentication
				);
			}
			else {
				pictureFiles = listFilteredFiles(this.pictureFiles.findByFolderName(name), requestDTO, authentication);
			}

			int count = pictureFiles.size();

			mav.addObject(pictureFiles);
			model.put("link_params", "");

			PictureFile file = null;

			try {
				// Attempt to access the element at index 'id'
				file = pictureFiles.get(id);
			}
			catch (IndexOutOfBoundsException e) {

				// Log the size of the pictureFiles list
				logger.severe("IndexOutOfBoundsException occurred. Size of pictureFiles list: " + count);
				// Optionally, you can log the value of 'id' as well
				logger.severe("                                    Attempted bucket: " + name);
				logger.severe("                                    Attempted index: " + id);

				if (count > 0) {

					try {
						// Attempt to access the element at index 'id' modulus 'size'
						file = pictureFiles.get(id % pictureFiles.size());
					}
					catch (IndexOutOfBoundsException e2) {

						model.put("errorMessage", "Failed to retrieve picture using backup wrap; index number overflowed end of collection");
						return "picture/pictureFile";
					}
				}
				else {

					model.put("errorMessage", "Failed to retrieve picture; index number overflowed end of collection");
					return "picture/pictureFile";
				}
			}

			int pictureID = file.getId();

			model.put("picture", file);
			model.put("current", id);
			model.put("image", "https://www.homepix.ch/web-images/Aletschgletscher/dsc_229068-dsc_229082.jpg");
			model.put("description", "This description");
			model.put("baseLink", "/buckets/" + name);
			model.put("id", id);
			model.put("next", (id + 1) % count);
			model.put("previous", (id + count - 1) % count);
			model.put("keywords", this.keywordRelationships.findByPictureId(pictureID).stream()
				.map(KeywordRelationships::getKeyword)
				.collect(Collectors.toList()));
			model.put("keyword_list", this.keywordRelationships.findByPictureId(pictureID).stream()
				.map(KeywordRelationships::getKeyword)
				.collect(Collectors.toList()));
			model.put("location_list", this.locationRelationships.findByPictureId(pictureID).stream()
				.map(LocationRelationship::getLocation)
				.collect(Collectors.toList()));

			model.put("fullUrl", "collection/" + pictureID);

			Iterable<Album> albums = this.albums.findAll();

			String keywords = this.keywordRelationships.findByPictureId(pictureID)
					.stream()
					.map(kr -> kr.getKeyword().getWord()) // Assuming getKeyword() gets the Keyword object, and getWord() gets the String you want
					.collect(Collectors.joining(", "));

			if (keywords.length() > 0) {
				keywords += ',';
			}

			keywords += "photo, sharing, portfolio, elliott, bignell";

			setStructuredDataForModel(
				requestDTO,
				model,
				"ImageObject",
				file,
				keywords
			);

			pictureFileService.addMapDetails(file, model);

			return setModel(requestDTO, model, this.folders.findByName(name), pictureFiles, "picture/pictureFile");
		}
	}

	public String movePictureToFolder(String sourceFolder, String targetFolder, PictureFile file) {

		initialiseS3Client();

		String sourcePrefix = "jpegs/" + (sourceFolder.endsWith("/") ? sourceFolder : sourceFolder + "/");
		String targetPrefix = "jpegs/" + (targetFolder.endsWith("/") ? targetFolder : targetFolder + "/");
		String filename = file.getFilename();

		// Define the specific files to move based on the filename
		List<String> fileKeysToMove = List.of(
			sourcePrefix + filename,                      // Main file: dsc_012345.jpg
			sourcePrefix + filename + ".exif",            // EXIF file: dsc_012345.jpg.exif
			sourcePrefix + "200px/" + filename.replace(".jpg", "_200px.jpg"), // Thumbnail: 200px/dsc_012345_200px.jpg
			sourcePrefix + "watermark/" + filename        // Watermarked file: watermark/dsc_012345.jpg
		);

		for (String sourceKey : fileKeysToMove) {
			try {
				// Generate the target key by replacing the source prefix with the target prefix
				String targetKey = sourceKey.replaceFirst(sourcePrefix, targetPrefix);

				// Copy the object to the target location
				CopyObjectRequest copyReq = CopyObjectRequest.builder()
					.copySource(bucketName + "/" + sourceKey)
					.destinationBucket(bucketName)
					.destinationKey(targetKey)
					.build();

				s3Client.copyObject(copyReq);

				// Delete the original object from the source folder
				DeleteObjectRequest deleteReq = DeleteObjectRequest.builder()
					.bucket(bucketName)
					.key(sourceKey)
					.build();

				s3Client.deleteObject(deleteReq);

				System.out.println("Moved file: " + sourceKey + " to " + targetKey);
			} catch (Exception e) {
				System.err.println("Error moving file: " + sourceKey + " - " + e.getMessage());
				e.printStackTrace();
			}
		}

		Folder tgtfolder = folders.findByName(targetFolder).iterator().next();
		file.setFolder(tgtfolder);
		tgtfolder.setPicture_count(tgtfolder.getPicture_count() + 1);
		Folder srcfolder = folders.findByName(sourceFolder).iterator().next();
		srcfolder.setPicture_count(srcfolder.getPicture_count() - 1);

		folders.save(tgtfolder);
		folders.save(srcfolder);
		pictureFiles.save(file);

		System.out.println("Files successfully moved from " + sourceFolder + " to " + targetFolder);

		return "Moved picture successfully";
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
				name = name.substring(5, name.length());
			}

			return name;
		}
		catch (Exception ex) {
			System.out.println(ex);
			ex.printStackTrace();
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

	@GetMapping(value = "/web-images/{directory}/200px/{file}_200px.webp")
	@Cacheable("getCompressedImageAsBytes")
	public ResponseEntity<byte[]> getSmallFileFromBucket(@PathVariable("directory") String directory,
															  @PathVariable("file") String file) {
		return getSmallCompressedFileFromBucket(directory, file);
	}

	private byte[] getCompressedImageAsBytes(String compressedPath, String directory, String file) {

		byte[] compressedImage = null;

		try {
			compressedImage = downloadFile("jpegs/" + compressedPath + ".webp");
		}
		catch (NoSuchKeyException ex) {
			logger.info("Compressed file missing; creating " + directory + '/' + file);
		}
		catch (IOException ex) {
			logger.info("Error accessing compressed file \" + directory + '/' + file");
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "An error occurred: " + e.getMessage(), e);
		}

		if (null == compressedImage) {
			//if (true) {

			compressedImage = hitBucket((client, arg1, arg2) -> {

				try {
					byte[] data = downloadFile("jpegs/" + arg1 + "/" + arg2 + ".jpg");
					byte[] compressedBytes = convertToSmallWebP200px(data, 1f);
					return compressedBytes;
				} catch (Exception e) {
					logger.severe("Error downloading file: " + directory + '/' + file + "    " + e.getMessage());
					return null;
				}
			}, directory, file);

			if (null != compressedImage) {

				// Save watermarked image to the S3-compatible bucket
				PutObjectRequest putObjectRequest = PutObjectRequest.builder()
					.bucket(bucketName)
					.key("jpegs/" + compressedPath + ".webp")
					.build();

				s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromBytes(compressedImage));
			}
			else {
				System.out.println("");
			}
		}

		return compressedImage;
	}

	@GetMapping(value = "/web-images/{directory}/{file}_200px.webp")
	public ResponseEntity<byte[]> getSmallCompressedFileFromBucket(@PathVariable("directory") String directory,
															  @PathVariable("file") String file) {

		String filepath = directory + '/' + file;
		String compressedPath = directory + "/200px/" + file;

		byte[] compressedImage = getCompressedImageAsBytes(compressedPath, directory, file);

		if (compressedImage != null) {
			return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(compressedImage);
		}
		else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@GetMapping(value = "web-images/{directory}/200px/{file}_max.webp")
	public ResponseEntity<byte[]> getCompressedFileFromBucket(@PathVariable("directory") String directory,
															  @PathVariable("file") String file) {

		String filepath = directory + '/' + file;
		String compressedPath = directory + "/200px/" + file + "_max";

		byte[] compressedImage = null;

		try {
			compressedImage = downloadFile("jpegs/" + compressedPath + ".webp");
		}
		catch (NoSuchKeyException ex) {
			logger.info("Compressed file missing; creating " + directory + '/' + file);
		}
		catch (IOException ex) {
			logger.info("Error accessing compressed file \" + directory + '/' + file");
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "An error occurred: " + e.getMessage(), e);
		}

		if (null == compressedImage) {
		//if (true) {

			compressedImage = hitBucket((client, arg1, arg2) -> {

				try {
					byte[] data = downloadFile("jpegs/" + arg1 + "/" + arg2 + ".jpg");
					byte[] compressedBytes = convertToWebP200px(data, 0.1f);
					return compressedBytes;
				} catch (Exception e) {
					logger.severe("Error downloading file: " + directory + '/' + file + "    " + e.getMessage());
					return null;
				}
			}, directory, file);

			if (null != compressedImage) {

				// Save watermarked image to the S3-compatible bucket
				PutObjectRequest putObjectRequest = PutObjectRequest.builder()
					.bucket(bucketName)
					.key("jpegs/" + compressedPath + ".webp")
					.build();

				s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromBytes(compressedImage));
			}
		}

		if (compressedImage != null) {
			return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(compressedImage);
		}
		else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}


	@GetMapping(value = "web-images/{directory}/{file}.webp")
	public ResponseEntity<byte[]> getWebPFileFromBucket(@PathVariable("directory") String directory,
															  @PathVariable("file") String file) {

		String filepath = directory + '/' + file;
		String watermarkedPath = directory + "/watermark/" + file + ".webp";

		byte[] watermarkedImage = null;

		try {
			watermarkedImage = downloadFile("webp/" + directory + "/watermark/" + file + ".webp");
		} catch (NoSuchKeyException ex) {
			logger.info("WebP watermarked file missing; creating " + directory + '/' + file);
		} catch (IOException ex) {
			logger.info("Error accessing WebP file " + directory + '/' + file);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "An error occurred: " + e.getMessage(), e);
		}

		if (watermarkedImage == null) {
			watermarkedImage = hitBucket((client, arg1, arg2) -> {
				try {
					byte[] jpegBytes = downloadFile("jpegs/" + arg1 + "/" + arg2 + ".jpg");
					return applyWatermarkToWebP(jpegBytes, 0.1f); // Convert to WebP with watermark
				} catch (Exception e) {
					logger.severe("Error downloading file: " + directory + '/' + file + " " + e.getMessage());
					return null;
				}
			}, directory, file);

			if (watermarkedImage != null) {
				// Save the WebP image to Exoscale S3
				PutObjectRequest putObjectRequest = PutObjectRequest.builder()
					.bucket(bucketName)
					.key("webp/" + watermarkedPath)
					.build();

				s3Client.putObject(putObjectRequest,
					software.amazon.awssdk.core.sync.RequestBody.fromBytes(watermarkedImage));
			}
		}

		if (watermarkedImage != null) {
			return ResponseEntity.ok().contentType(MediaType.valueOf("image/webp")).body(watermarkedImage);
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	public static byte[] applyWatermarkToWebP(byte[] originalImageBytes, float quality) throws Exception
	{
		// Convert byte array to BufferedImage
		ByteArrayInputStream in = new ByteArrayInputStream(originalImageBytes);
		BufferedImage originalImage = ImageIO.read(in);

		String watermarkText = "Copyright ©Elliott Bignell 2025";
		float targetWidthRatio = 0.5f; // Target width ratio of the image width
		int imageWidth = originalImage.getWidth();

		if (originalImage.getHeight() < originalImage.getWidth()) {
			imageWidth = originalImage.getHeight();
		}

		int targetWidth = (int) (imageWidth * targetWidthRatio); // Target text width

		// Create a mutable copy of the original image
		BufferedImage watermarkedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = watermarkedImage.createGraphics();
		g2d.drawImage(originalImage, 0, 0, null);

		// Dynamically adjust font size
		Font font = new Font(Font.SANS_SERIF, Font.BOLD, 10); // Start with a base font size
		g2d.setFont(font);
		FontMetrics fontMetrics = g2d.getFontMetrics();
		int textWidth = fontMetrics.stringWidth(watermarkText);
		while (textWidth < targetWidth && font.getSize() < imageWidth * 2.0 / 3.0) { // Avoid excessively large font sizes
			font = new Font(Font.SANS_SERIF, Font.BOLD, font.getSize() + 1);
			g2d.setFont(font);
			fontMetrics = g2d.getFontMetrics();
			textWidth = fontMetrics.stringWidth(watermarkText);
		}

		// Create a mutable copy of the original image to add the watermark
		// Add watermark with dynamically adjusted font
		AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f); // Set transparency
		g2d.setComposite(alphaChannel);
		g2d.setColor(Color.WHITE); // Watermark color
		g2d.setFont(font); // Set the dynamically adjusted font

		// Calculate the position of the watermark
		fontMetrics = g2d.getFontMetrics(font);
		Rectangle2D rect = fontMetrics.getStringBounds(watermarkText, g2d);
		int centerX = (int) ((originalImage.getWidth() - (int) rect.getWidth()) * 2.0 / 3.0);
		int centerY = (int) (originalImage.getHeight() * 2.0 / 3.0);

		g2d.drawString(watermarkText, centerX, centerY);
		g2d.dispose();

		// Save watermarked image as a temporary JPEG
		File tempJpeg = File.createTempFile("watermarked", ".jpg");
		ImageIO.write(watermarkedImage, "jpg", tempJpeg);

		// Convert JPEG to WebP using Google's `cwebp`
		File tempWebP = File.createTempFile("output", ".webp");
		ProcessBuilder pb = new ProcessBuilder(
			"cwebp", "-q", String.valueOf(quality * 10), tempJpeg.getAbsolutePath(), "-o", tempWebP.getAbsolutePath()
		);
		pb.environment().put("PATH", "/usr/local/bin:/usr/bin:/bin");
		pb.redirectErrorStream(true);
		Process process = pb.start();
		process.waitFor();

		// Read the WebP file back into a byte array
		byte[] webpBytes = new byte[(int) tempWebP.length()];
		FileInputStream fis = new FileInputStream(tempWebP);
		fis.read(webpBytes);
		fis.close();

		// Clean up temp files
		tempJpeg.delete();
		tempWebP.delete();

		return webpBytes;
	}


	// web-images/Milano/dse_020208-dse_020209_6066523_2023_08_05_09_23_52_66_02_00.jpg
	@GetMapping(value = "web-images/{directory}/{file}")
	public ResponseEntity<byte[]> getFileFromBucket(@PathVariable("directory") String directory,
			@PathVariable("file") String file) {

		String filepath = directory + '/' + file;
		String watermarkedPath = directory + "/watermark/" + file;

		byte[] watermarkedImage = null;

		try {
			watermarkedImage = downloadFile("jpegs/" + directory + "/watermark/" + file);
		}
		catch (NoSuchKeyException ex) {
			logger.info("Watermarked file missing; creating " + directory + '/' + file);
		}
		catch (IOException ex) {
			logger.info("Error accessing watermarked file \" + directory + '/' + file");
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "An error occurred: " + e.getMessage(), e);
		}

		if (null == watermarkedImage) {

			watermarkedImage = hitBucket((client, arg1, arg2) -> {

				try {
					return applyWatermark(downloadFile("jpegs/" + arg1 + "/" + arg2));
				} catch (IOException e) {
					logger.severe("Error downloading file: " + directory + '/' + file + "    " + e.getMessage());
					return null;
				}
			}, directory, file);

			if (null != watermarkedImage) {

				// Save watermarked image to the S3-compatible bucket
				PutObjectRequest putObjectRequest = PutObjectRequest.builder()
					.bucket(bucketName)
					.key("jpegs/" + watermarkedPath)
					.build();

				s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromBytes(watermarkedImage));
			}
		}

		if (watermarkedImage != null) {
			return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(watermarkedImage);
		}
		else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	public static byte[] convertToSmallWebP200px(byte[] originalImageBytes, float quality) throws Exception
	{
		// Convert byte array to BufferedImage
		ByteArrayInputStream in = new ByteArrayInputStream(originalImageBytes);
		BufferedImage originalImage = ImageIO.read(in);

		// Calculate new width while maintaining aspect ratio
		int newHeight = 200;
		int originalWidth = originalImage.getWidth();
		int originalHeight = originalImage.getHeight();
		int newWidth = (int) ((double) originalWidth / originalHeight * newHeight);

		// Create a resized image
		BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = resizedImage.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
		g2d.dispose();

		// Save resized image as a temporary JPEG
		File tempJpeg = File.createTempFile("resized", ".jpg");
		ImageIO.write(resizedImage, "jpg", tempJpeg);

		// Convert JPEG to WebP using Google's `cwebp`
		File tempWebP = File.createTempFile("output", ".webp");
		ProcessBuilder pb = new ProcessBuilder(
			"cwebp", "-q", String.valueOf(quality * 100),
			"-resize", String.valueOf(newWidth), "200", // Ensure exact 200px height
			tempJpeg.getAbsolutePath(), "-o", tempWebP.getAbsolutePath()
		);
		pb.environment().put("PATH", "/usr/local/bin:/usr/bin:/bin");
		pb.redirectErrorStream(true);
		Process process = pb.start();
		process.waitFor();

		// Read the WebP file back into a byte array
		byte[] webpBytes = new byte[(int) tempWebP.length()];
		FileInputStream fis = new FileInputStream(tempWebP);
		fis.read(webpBytes);
		fis.close();

		// Clean up temp files
		tempJpeg.delete();
		tempWebP.delete();

		return webpBytes;
	}

	public static byte[] convertToWebP200px(byte[] originalImageBytes, float quality) throws Exception
	{
		// Convert byte array to BufferedImage
		ByteArrayInputStream in = new ByteArrayInputStream(originalImageBytes);
		BufferedImage originalImage = ImageIO.read(in);

		int originalWidth = originalImage.getWidth();
		int originalHeight = originalImage.getHeight();

		if (originalWidth > originalHeight) {

			if (originalWidth > 2000) {

				originalHeight = originalHeight * 2000 / originalWidth;
				originalWidth = 2000;
			}
		}
		else {

			if (originalHeight > 2000) {

				originalWidth = originalWidth * 2000 / originalHeight;
				originalHeight = 2000;
			}
		}

		// Create a resized image
		BufferedImage resizedImage = new BufferedImage(originalWidth, originalHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = resizedImage.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.drawImage(originalImage, 0, 0, originalWidth, originalHeight, null);
		g2d.dispose();

		// Save resized image as a temporary JPEG
		File tempJpeg = File.createTempFile("resized", ".jpg");
		ImageIO.write(resizedImage, "jpg", tempJpeg);

		// Convert JPEG to WebP using Google's `cwebp`
		File tempWebP = File.createTempFile("output", ".webp");
		ProcessBuilder pb = new ProcessBuilder(
			"cwebp", "-q", String.valueOf(quality * 100),
			"-resize", String.valueOf(originalWidth), String.valueOf(originalHeight), // Ensure exact 200px height
			tempJpeg.getAbsolutePath(), "-o", tempWebP.getAbsolutePath()
		);
		pb.environment().put("PATH", "/usr/local/bin:/usr/bin:/bin");
		pb.redirectErrorStream(true);
		Process process = pb.start();
		process.waitFor();

		// Read the WebP file back into a byte array
		byte[] webpBytes = new byte[(int) tempWebP.length()];
		FileInputStream fis = new FileInputStream(tempWebP);
		fis.read(webpBytes);
		fis.close();

		// Clean up temp files
		tempJpeg.delete();
		tempWebP.delete();

		return webpBytes;
	}
	public static byte[] convertToWebP(byte[] originalImageBytes, float quality) throws Exception
	{
		// Convert byte array to BufferedImage
		ByteArrayInputStream in = new ByteArrayInputStream(originalImageBytes);
		BufferedImage originalImage = ImageIO.read(in);

		float targetWidthRatio = 0.5f; // Target width ratio of the image width
		int imageWidth = originalImage.getWidth();

		if (originalImage.getHeight() < originalImage.getWidth()) {
			imageWidth = originalImage.getHeight();
		}

		int targetWidth = (int) (imageWidth * targetWidthRatio); // Target text width

		// Create a mutable copy of the original image
		BufferedImage watermarkedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = watermarkedImage.createGraphics();
		g2d.drawImage(originalImage, 0, 0, null);

		// Save watermarked image as a temporary JPEG
		File tempJpeg = File.createTempFile("watermarked", ".jpg");
		ImageIO.write(watermarkedImage, "jpg", tempJpeg);

		// Convert JPEG to WebP using Google's `cwebp`
		File tempWebP = File.createTempFile("output", ".webp");
		ProcessBuilder pb = new ProcessBuilder(
			"cwebp", "-q", String.valueOf(quality * 10), tempJpeg.getAbsolutePath(), "-o", tempWebP.getAbsolutePath()
		);
		pb.environment().put("PATH", "/usr/local/bin:/usr/bin:/bin");
		pb.redirectErrorStream(true);
		Process process = pb.start();
		process.waitFor();

		// Read the WebP file back into a byte array
		byte[] webpBytes = new byte[(int) tempWebP.length()];
		FileInputStream fis = new FileInputStream(tempWebP);
		fis.read(webpBytes);
		fis.close();

		// Clean up temp files
		tempJpeg.delete();
		tempWebP.delete();

		return webpBytes;
	}

	public static BufferedImage resizeImage(BufferedImage originalImage, int maxWidth, int maxHeight)
	{
		int width = originalImage.getWidth();
		int height = originalImage.getHeight();

		// Maintain aspect ratio
		float scale = Math.min((float) maxWidth / width, (float) maxHeight / height);
		int newWidth = (int) (width * scale);
		int newHeight = (int) (height * scale);

		BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = resizedImage.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
		g2d.dispose();

		return resizedImage;
	}

	@GetMapping(value = "web-images/{directory}/200px/{file}_200px.jpg")
	public ResponseEntity<byte[]> getMediumFileFromBucket(@PathVariable("directory") String directory,
			@PathVariable("file") String file) {

		String filename = file + "_200px.jpg";
		String filepath = directory + "/200px/" + filename;

		// Try to serve from cache first
		byte[] cachedImage = image200pxCacheMap.get(filepath);

		// Return watermarked image from cache
		if (cachedImage != null) {
			return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(cachedImage);
		}

		byte[] watermarkedImage = hitBucket((client, arg1, arg2) -> {

			try {
				return downloadFile("jpegs/" + arg1 + "/200px/" + arg2);
			}
			catch (IOException e) {
				logger.log(Level.SEVERE, "Error downloading file: " + e.getMessage(), e);
				return null;
			}
		}, directory, filename);

		if (watermarkedImage != null) {

			image200pxCacheMap.put(filepath, watermarkedImage);
			return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(watermarkedImage);
		}
		else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	public static byte[] compressJpegImage(byte[] originalImageBytes, float compressionQuality) throws IOException
	{
		// Convert byte array to BufferedImage
		ByteArrayInputStream in = new ByteArrayInputStream(originalImageBytes);
		BufferedImage originalImage = ImageIO.read(in);

		// Create a graphics instance to calculate text width
		float targetWidthRatio = 0.5f; // Target width ratio of the image width
		int imageWidth = originalImage.getWidth();
		if (originalImage.getHeight() < originalImage.getWidth()) {
			imageWidth = originalImage.getHeight();
		}
		int targetWidth = (int) (imageWidth * targetWidthRatio); // Target text width

		// Create a mutable copy of the original image
		BufferedImage watermarkedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = watermarkedImage.createGraphics();
		g2d.drawImage(originalImage, 0, 0, null);
		g2d.dispose(); // Always dispose Graphics2D object

		// Convert BufferedImage to JPEG with specified compression quality
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
		ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
		jpgWriter.setOutput(ios);

		ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
		if (jpgWriteParam.canWriteCompressed()) {
			jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			jpgWriteParam.setCompressionQuality(compressionQuality); // Set compression level
		}

		jpgWriter.write(null, new javax.imageio.IIOImage(watermarkedImage, null, null), jpgWriteParam);
		ios.close();
		jpgWriter.dispose();

		return baos.toByteArray();
	}

	public byte[] applyCompression(byte[] originalImageBytes) throws IOException {

		try {

			byte[] compressedBytes = compressJpegImage(originalImageBytes, 0.2f);

			// Convert byte array to BufferedImage
			ByteArrayInputStream in = new ByteArrayInputStream(compressedBytes);
			BufferedImage originalImage = ImageIO.read(in);

			// Create a graphics instance to calculate text width
			Graphics2D g2d = originalImage.createGraphics();

			// Create a mutable copy of the original image to add the watermark
			BufferedImage watermarkedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
			g2d = (Graphics2D) watermarkedImage.getGraphics();
			g2d.drawImage(originalImage, 0, 0, null);

			// Convert the watermarked BufferedImage back to a byte array
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(watermarkedImage, "jpg", baos);
			baos.flush();
			byte[] imageInByte = baos.toByteArray();
			baos.close();

			return imageInByte;
		}
		catch (OutOfMemoryError oome) {

			logger.log(Level.SEVERE, "An error occurred: " + oome.getMessage(), oome);
			logger.severe("Clearing image chaches");
			image200pxCacheMap.clear();
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "An error occurred: " + e.getMessage(), e);
		}

		return null;
	}

	public byte[] applyWatermark(byte[] originalImageBytes) throws IOException {

		try {

			// Convert byte array to BufferedImage
			ByteArrayInputStream in = new ByteArrayInputStream(originalImageBytes);
			BufferedImage originalImage = ImageIO.read(in);

			// Create a graphics instance to calculate text width
			Graphics2D g2d = originalImage.createGraphics();

			String watermarkText = "Copyright ©Elliott Bignell 2023";
			float targetWidthRatio = 0.5f; // Target width ratio of the image width
			int imageWidth = originalImage.getWidth();

			if (originalImage.getHeight() < originalImage.getWidth()) {
				imageWidth = originalImage.getHeight();
			}

			int targetWidth = (int) (imageWidth * targetWidthRatio); // Target text width

			// Dynamically adjust font size
			Font font = new Font(Font.SANS_SERIF, Font.BOLD, 10); // Start with a base font size
			g2d.setFont(font);
			FontMetrics fontMetrics = g2d.getFontMetrics();
			int textWidth = fontMetrics.stringWidth(watermarkText);
			while (textWidth < targetWidth && font.getSize() < imageWidth * 2.0 / 3.0) { // Avoid excessively large font sizes
				font = new Font(Font.SANS_SERIF, Font.BOLD, font.getSize() + 1);
				g2d.setFont(font);
				fontMetrics = g2d.getFontMetrics();
				textWidth = fontMetrics.stringWidth(watermarkText);
			}

			// Create a mutable copy of the original image to add the watermark
			BufferedImage watermarkedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
			g2d = (Graphics2D) watermarkedImage.getGraphics();
			g2d.drawImage(originalImage, 0, 0, null);

			// Add watermark with dynamically adjusted font
			AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f); // Set transparency
			g2d.setComposite(alphaChannel);
			g2d.setColor(Color.WHITE); // Watermark color
			g2d.setFont(font); // Set the dynamically adjusted font

			// Calculate the position of the watermark
			fontMetrics = g2d.getFontMetrics(font);
			Rectangle2D rect = fontMetrics.getStringBounds(watermarkText, g2d);
			int centerX = (int) ((originalImage.getWidth() - (int) rect.getWidth()) * 2.0 / 3.0);
			int centerY = (int) (originalImage.getHeight() * 2.0 / 3.0);

			g2d.drawString(watermarkText, centerX, centerY);
			g2d.dispose();

			// Convert the watermarked BufferedImage back to a byte array
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(watermarkedImage, "jpg", baos);
			baos.flush();
			byte[] imageInByte = baos.toByteArray();
			baos.close();

			return imageInByte;
		}
		catch (OutOfMemoryError oome) {

			logger.log(Level.SEVERE, "An error occurred: " + oome.getMessage(), oome);
			logger.severe("Clearing image chaches");
			image200pxCacheMap.clear();
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "An error occurred: " + e.getMessage(), e);
		}

		return null;
	}

	private List<PictureFile> loadPictureFiles(String imagePath, String name) {

		List<PictureFile> pictureFiles = new ArrayList<>();

		String dir = imagePath + name;

		List<String> jpegNames = folderService.listFileNames(s3Client, bucketName, "jpegs/" + name);

		int index = 0;

		for (String jpeg : jpegNames) {

			PictureFile item = new PictureFile();

			item.setId(index++);
			item.setFilename(jpeg);

			try {

				String filename = "jpegs/" + jpeg.substring(12, jpeg.length());
				item.setTitle(getExifEntries(filename).get("title"));
			}
			catch (Exception e) {
				logger.log(Level.SEVERE, "An error occurred: " + e.getMessage(), e);
			}

			this.addKeywordAndRelationship(item, name);

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
			item.setTitle(getExifEntries(filename).get("title"));
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "An error occurred: " + e.getMessage(), e);
		}

		this.addKeywordAndRelationship(item, name);

		pictureFiles.add(item);

		return item;
	}

	@PostMapping("/moveFile")
	public ResponseEntity<?> moveFile(@RequestBody MoveFileRequest request) {

		initialiseS3Client();
		S3FileMover s3FileMover = new S3FileMover(s3Client);

		String[] idStrings = request.getFileName().split(",");
		List<Integer> ids = Arrays.stream(idStrings)
			.map(Integer::parseInt)
			.collect(Collectors.toList());
		List<PictureFile> pictures = this.pictureFiles.findAllById(ids);

		Folder leftFolder = this.folders.findByName(request.sourceTag).iterator().next();
		Folder rightFolder = this.folders.findByName(request.destinationTag).iterator().next();

		if (leftFolder != null && rightFolder != null) {

			for (PictureFile picture : pictures) {

				String from = "";
				String to = "";
				Folder fromFolder = null;
				Folder toFolder = null;

				if (picture.getFolder().getName().equals(leftFolder.getName())) {

					from = request.getSourceTag();
					to = request.getDestinationTag();
					fromFolder = leftFolder;
					toFolder = rightFolder;
				}
				else if (picture.getFolder().getName().equals(rightFolder.getName())) {

					from = request.getDestinationTag();
					to = request.getSourceTag();
					fromFolder = rightFolder;
					toFolder = leftFolder;
				}
				else {
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error moving file : folder not correctly assigned");
				}

				String baseDirectory = "jpegs/";
				String originalImagePath = baseDirectory + from + "/" + picture.getFilename();
				String newImagePath = baseDirectory + to + "/" + picture.getFilename();

				// Assuming the thumbnail follows a consistent naming pattern: filename_200px.extension
				String originalThumbnailPath = picture.getMediumFilename().replace("/web-images/", "jpegs/");
				String newThumbnailPath = picture.getMediumFilename()
					.replace("/web-images/", "jpegs/")
					.replace(from, to);

				try {

					String bucketName = "picture-files";

					s3FileMover.moveFile(bucketName, originalThumbnailPath, newThumbnailPath);
					s3FileMover.moveFile(bucketName, originalImagePath, newImagePath);

					fromFolder.setPicture_count(fromFolder.getPicture_count() - 1);
					toFolder.setPicture_count(toFolder.getPicture_count() + 1);

					picture.setFolder(toFolder);

					this.pictureFiles.save(picture);
					this.folders.save(fromFolder);
					this.folders.save(toFolder);

				}
				catch (Exception e) {

					logger.log(Level.SEVERE, "An error occurred: " + e.getMessage(), e);

					// Log the exception
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error moving file and thumbnail: " + e.getMessage());
				}
			}

			return ResponseEntity.ok("File and thumbnail successfully moved.");
		}

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error moving file : folder does not exist");
	}

	static class MoveFileRequest {
		private String sourceTag;
		private String destinationTag;
		private String fileName;

		// Getters and setters for sourceTag, destinationTag, and fileName

		public String getSourceTag() {
			return sourceTag;
		}

		public void setSourceTag(String sourceTag) {
			this.sourceTag = sourceTag;
		}

		public String getDestinationTag() {
			return destinationTag;
		}

		public void setDestinationTag(String destinationTag) {
			this.destinationTag = destinationTag;
		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		// Helper methods to extract file name without extension and file extension
		public String getFileNameWithoutExtension() {
			int dotIndex = fileName.lastIndexOf(".");
			return dotIndex != -1 ? fileName.substring(0, dotIndex) : fileName;
		}

		public String getFileExtension() {
			int dotIndex = fileName.lastIndexOf(".");
			return dotIndex != -1 ? fileName.substring(dotIndex + 1) : "";
		}
	}
}
