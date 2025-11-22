package org.springframework.samples.homepix.portfolio.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.data.util.Pair;
import org.springframework.samples.homepix.CollectionRequestDTO;
import org.springframework.samples.homepix.CredentialsRunner;
import org.springframework.samples.homepix.portfolio.Pagination;
import org.springframework.samples.homepix.portfolio.album.*;
import org.springframework.samples.homepix.portfolio.calendar.Calendar;
import org.springframework.samples.homepix.portfolio.collection.PictureCollection;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.samples.homepix.portfolio.collection.PictureFileService;
import org.springframework.samples.homepix.portfolio.folder.Folder;
import org.springframework.samples.homepix.portfolio.folder.FolderRepository;
import org.springframework.samples.homepix.portfolio.folder.FolderService;
import org.springframework.samples.homepix.portfolio.keywords.*;
import org.springframework.samples.homepix.portfolio.locations.Location;
import org.springframework.samples.homepix.portfolio.locations.LocationRelationship;
import org.springframework.samples.homepix.portfolio.locations.LocationRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.locations.LocationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.logging.Logger;

@Controller
public abstract class PaginationController implements AutoCloseable {

	protected static final Logger logger = Logger.getLogger(PaginationController.class.getName());

	protected Pagination pagination;

	protected Calendar calendar = null;

	protected final AlbumRepository albums;

	@Autowired
	protected FolderRepository folders;

	@Autowired
	protected PictureFileRepository pictureFiles;

	@Autowired
	private PictureFileService pictureFileService;

	@Autowired
	LocationService locationService;

	@Autowired
	KeywordService keywordService;

	protected final KeywordRepository keyword;

	protected final KeywordRelationshipsRepository keywordRelationships;

	protected static final String bucketName = "picture-files";

	protected static final String endpoint = "https://sos-ch-dk-2.exo.io";

	protected static final String region = "ch-dk-2";

	protected Collection<Folder> folderCache = null;

	protected static S3Client s3Client = null;

	private AwsCredentials awsCredentials;

	protected String filepath;

	// @Value("${homepix.images.path}")
	protected static final String imagePath = System.getProperty("user.dir") + "/images/";

	protected static final String pageDescription =
		"homePIX is a photo-sharing site created by Elliott Bignell. " +
		"If you want to licence a photo on this site please contact Elliott at elliott.bignell@gmail.com. " +
		"Licensing direct from the site is coming soon. If you want to have your photos featured on this " +
		"site also contact Elliott. The idea is for this site to be collaborative among photographers, " +
		"so I will feature others' work and connect to your own sites if you want to use my code." +
		" Together we are bigger than any agency, and I plan to let you keep most of your sales revenue. " +
		"First I need some partners!";


	@Autowired
	protected FolderService folderService;

	@Autowired
	protected AlbumService albumService;

	@Autowired
	protected LocationRelationshipsRepository locationRelationships;

	@Autowired
	protected PaginationController(AlbumRepository albums,
								   KeywordRepository keyword,
								   KeywordRelationshipsRepository keywordsRelationships,
								   FolderService folderService
	) {
		this.albums = albums;
		this.keyword = keyword;
		this.keywordRelationships = keywordsRelationships;
		this.folderService = folderService;
		pagination = new Pagination();
	}

	@Override
	public void close() throws Exception {

		if (s3Client != null) {

			s3Client.close();
			s3Client = null;
		}
	}

	protected void addParams(int pictureId, String filename, Iterable<PictureFile> pictureFiles,
			Map<String, Object> model, boolean byID) {

		PictureFile picture;

		if (0 == pictureId) {
			picture = pictureFiles.iterator().next();
		}
		else {
			picture = this.pictureFiles.findById(pictureId).get();
		}

		if (pictureFiles.iterator().hasNext()) {

			PictureFile first = pictureFiles.iterator().next();
			PictureFile next = picture;
			PictureFile last = next;
			PictureFile previous = last;

			for (Iterator<PictureFile> it = pictureFiles.iterator(); it.hasNext();) {
				last = it.next();
			}

			previous = last;

			Iterator<PictureFile> it = pictureFiles.iterator();

			while (it.hasNext()) {

				PictureFile f = it.next();

				if (byID && f.getId() == pictureId) {

					picture = f;
					break;
				}
				else if (!byID && f.getFilename().equals(filename)) {

					picture = f;
					break;
				}

				previous = f;
			}

			if (it.hasNext()) {
				next = it.next();
			}
			else {
				next = first;
			}

			model.put("picture", picture);
			model.put("next", next.getId());
			model.put("previous", previous.getId());
			model.put("nextFile", next.getFilename());
			model.put("previousFile", previous.getFilename());
		}
		model.put("description", pageDescription);
	}

	@ModelAttribute(name = "pagination")
	protected Pagination getPagination() {
		return this.pagination;
	}

	Iterable<Album> findAllAlbums() {
		return this.albums.findAll();
	}

	@Cacheable("yearNames")
	@ModelAttribute("yearNames")
	protected List<List<String>> populateDates() {

		List<String> years = this.pictureFiles.findYears();

		int width = 4;

		List<List<String>> table = new ArrayList<List<String>>() ;

		for (int n = 0; n < 4; n++) {
			table.add(new ArrayList<>());
		}

		int count = 0;

		for (String year : years) {
			table.get(count++ % 4).add(year);
		}

		return table;
	}

	protected String loadFolders(Folder folder, BindingResult result, Map<String, Object> model) {

		// allow parameterless GET request for /folders to return all records
		if (folder.getName() == null) {

			loadBuckets(folder, result, model);
			folder.setName(""); // empty string signifies broadest possible search
		}

		// find folders by last name
		Collection<Folder> results = this.folders.findByName(folder.getName())
			.stream()
			.sorted((item1, item2 ) -> {return item1.getName().compareTo(item2.getName());})
			.collect(Collectors.toList());

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
			model.put("folders", results);
			return "folders/folderList";
		}
	}

	private void load() {

		try {

			List<String> folderNames = Stream.of(Objects.requireNonNull(new File(this.imagePath).listFiles()))
					.filter(File::isDirectory).map(File::getName).sorted().collect(Collectors.toList());

			folders.deleteAll();

			for (String name : folderNames) {

				Folder item = new Folder();

				item.setName(name);
				item.setThumbnailId(36860);

				final Pattern JPEGS = Pattern.compile(".*jpg$");

				long count = Stream.of(new File(this.imagePath + name).listFiles()).filter(file -> !file.isDirectory())
						.filter(file -> JPEGS.matcher(file.getName()).find()).count();
				item.setPicture_count((int) count);

				folders.save(item);
			}
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "An error occurred: " + e.getMessage(), e);
		}
	}

	protected String loadBuckets(Folder folder, BindingResult result, Map<String, Object> model) {

		try {

			initialiseS3Client();

			// Now you can use s3Client to interact with the Exoscale S3-compatible
			// service
			String bucketName = "picture-files";

			if (null == folderCache) {
				folderCache = folderService.listSubFolders( s3Client, "jpegs");
			}

			if (folderCache.isEmpty()) {
				// no folders found
				result.rejectValue("name", "notFound", "not found");
				return "folders/findFolders";
			}
			else if (folderCache.size() == 1) {
				// 1 folder found
				folder = folderCache.iterator().next();
				return "redirect:/folders/" + folder.getId();
			}
			else {
				// multiple folders found
				model.put("folders", folderCache.stream()
					.sorted(Comparator.comparing(Folder::getName))
					.collect(Collectors.toList())
				);
				return "folders/folderList";
			}
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "An error occurred: " + e.getMessage(), e);
		}
		return "folders/folderList";
	}

	// TODO: Refactor out common code
	public String processFindByLocation(String name,
										CollectionRequestDTO requestDTO,
										CollectionRequestDTO redirectedDTO,
										Pageable pageable, // Default page size and sorting
										Map<String, Object> model,
										Authentication authentication
	) {
		// Check if redirectedDTO is not null, use it if available
		if (redirectedDTO != null) {
			requestDTO = redirectedDTO;
		}

		String userRoles = "ROLE_USER";

		if (authentication != null && authentication.isAuthenticated()) {
			// User is authenticated
			boolean isAdmin = authentication.getAuthorities().stream()
				.anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));

			if (isAdmin) {
				// User is an administrator
				userRoles = "ROLE_ADMIN";
			}
		}

		PageRequest pageRequest = PageRequest.of(
			pageable.getPageNumber(),
			pageable.getPageSize(),
			Sort.by(getOrderColumn(requestDTO))
		);

		Pair<LocalDate, LocalDate> dates = getDateRange(requestDTO, model);
		LocalDate endDate = dates.getSecond();
		LocalDateTime endOfDay = endDate.atTime(LocalTime.MAX);

		Page<PictureFile> files = this.pictureFileService.getComplexSearchPageByLocation(
			name,
			dates.getFirst(),
			endOfDay,
			isAdmin(authentication),
			userRoles,
			pageRequest
		);

		model.put("collection", files);
		model.put("sort", requestDTO.getSort());
		model.put("search", requestDTO.getSearch());
		model.put("pageNumber", files.getNumber());
		model.put("totalPages", files.getTotalPages());
		model.put("count", files.getTotalElements());

		return "collections/collection";
	}

	// TODO: Refactor out common code
	public String processFindCollections(CollectionRequestDTO requestDTO,
										 CollectionRequestDTO redirectedDTO,
										 Pageable pageable, // Default page size and sorting
										 PictureCollection pictureCollection,
										 BindingResult result,
										 Map<String, Object> model,
										 Authentication authentication
	) {
		// Check if redirectedDTO is not null, use it if available
		if (redirectedDTO != null) {
			requestDTO = redirectedDTO;
		}

		// allow parameterless GET request for /collections to return all records
		if (pictureCollection.getName() == null) {
			pictureCollection.setName(""); // empty string signifies broadest possible
			// search
		}

		String userRoles = "ROLE_USER";

		if (authentication != null && authentication.isAuthenticated()) {
			// User is authenticated
			boolean isAdmin = authentication.getAuthorities().stream()
				.anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));

			if (isAdmin) {
				// User is an administrator
				userRoles = "ROLE_ADMIN";
			}
		}

		PageRequest pageRequest = PageRequest.of(
			pageable.getPageNumber(),
			pageable.getPageSize(),
			Sort.by(getOrderColumn(requestDTO))
		);

		Pair<LocalDate, LocalDate> dates = getDateRange(requestDTO, model);
		LocalDate endDate = dates.getSecond();
		LocalDateTime endOfDay = endDate.atTime(LocalTime.MAX);

		String searchText = requestDTO.getSearch();

		Page<PictureFile> files = this.pictureFileService.getComplexSearchPage(
			searchText,
			dates.getFirst(),
			endOfDay,
			isAdmin(authentication),
			userRoles,
			pageRequest
		);

		model.put("collection", files);
		model.put("sort", requestDTO.getSort());
		model.put("pageNumber", files.getNumber());
		model.put("totalPages", files.getTotalPages());
		model.put("count", files.getTotalElements());

		pictureFileService.applyArguments(model, requestDTO);

		return "collections/collection";
	}

	protected Pair<LocalDate, LocalDate> getDateRange(
		CollectionRequestDTO requestDTO,
		Map<String, Object> model
	) {

		final String format = "yyyy-M-d";

		Supplier<String> today = () -> {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
			LocalDateTime now = LocalDateTime.now();
			return dtf.format(now);
		};

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format, Locale.ENGLISH);

		String fromDate = requestDTO.getFromDate();
		String toDate = requestDTO.getToDate();

		if (fromDate.equals("null") || fromDate.equals("")) {
			fromDate = "1970-01-01";
		}

		if (toDate.equals("null") || toDate.equals("")) {

			Supplier<String> supplier = () -> {
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
				LocalDateTime now = LocalDateTime.now();
				return dtf.format(now);
			};

			toDate = supplier.get();
		}

		LocalDate startDate = LocalDate.parse(fromDate, formatter);
		LocalDate endDate = LocalDate.parse(toDate, formatter);

		fromDate = startDate.toString();
		toDate = endDate.toString();

		pictureFileService.applyArguments(model, requestDTO);

		return Pair.of(startDate, endDate.atTime(LocalTime.MAX).toLocalDate());
	}

	protected List<PictureFile> listFiles_(S3Client s3Client, String subFolder) {
		String prefix = subFolder.endsWith("/") ? subFolder : subFolder + "/";
		List<PictureFile> results = new ArrayList<>();

		ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
			.bucket(bucketName)
			.prefix(prefix)
			.build();

		ListObjectsV2Response listObjectsResponse;

		do {
			listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);

			List<S3Object> filteredObjects = listObjectsResponse.contents().stream()
				.filter(object -> !object.key().startsWith(prefix + "200px/"))
				.filter(object -> object.key().toLowerCase().endsWith(".jpg"))
				.collect(Collectors.toList());

			for (S3Object s3Object : filteredObjects) {
				try {
					String name = s3Object.key();
					String exifName = "jpegs" + name.substring(5);

					// Fetch only metadata
					GetObjectResponse metadataResponse = s3Client.getObject(GetObjectRequest.builder()
						.bucket(bucketName)
						.key(name)
						.build())
						.response();

					Map<String, String> properties = getExifEntries(exifName);

					// Process metadata and create/update PictureFile objects
					processMetadata(properties, name, subFolder, results);
				} catch (Exception e) {
					logger.log(Level.SEVERE, "An error occurred: " + e.getMessage(), e);
				}
			}

			listObjectsRequest = ListObjectsV2Request.builder()
				.bucket(bucketName)
				.prefix(prefix)
				.continuationToken(listObjectsResponse.nextContinuationToken())
				.build();
		} while (listObjectsResponse.isTruncated());

		return results;
	}

	private void processMetadata(Map<String, String> properties, String name, String subFolder, List<PictureFile> results) {

	}

	protected List<PictureFile> listFiles(S3Client s3Client, String subFolder) {

		String prefix = subFolder.endsWith("/") ? subFolder : subFolder + "/";

		List<PictureFile> results = new ArrayList<>();

		List<S3Object> filteredObjects = new ArrayList<>();

		ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
			.bucket(bucketName)
			.prefix(prefix)
			.build();

		ListObjectsV2Response listObjectsResponse;

		/*
		final String format = "yyyy-MM-dd HH:mm:ss"; // Use uppercase 'HH' for 24-hour format
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format, Locale.ENGLISH);
		String date = "2023-01-12 00:00:00";
		LocalDateTime fromDate = LocalDateTime.parse(date, formatter);

		LocalDateTime localDateTime = LocalDateTime.now(); // Replace with your LocalDateTime
		ZoneId zoneId = ZoneId.of("UTC"); // Use the appropriate time zone
		Instant instant = localDateTime.atZone(zoneId).toInstant();
		 */

		do {
			listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);

			filteredObjects.addAll(listObjectsResponse.contents().stream()
				//.filter(s3Object -> s3Object.lastModified().isAfter(Instant.from(instant)))
				.filter(object -> !object.key().startsWith(prefix + "200px/"))
				.filter(object -> !object.key().startsWith(prefix + "watermark/"))
				.filter(object -> object.key().endsWith(".jpg"))
				.collect(Collectors.toList())
			);

			listObjectsRequest = ListObjectsV2Request.builder()
				.bucket(bucketName)
				.prefix(prefix)
				.continuationToken(listObjectsResponse.nextContinuationToken())
				.build();
		} while (listObjectsResponse.isTruncated());

		for (S3Object s3Object : filteredObjects) {

			try {

				String name = s3Object.key();
				String extension = name.substring(name.length() - 4).toLowerCase();

				if (extension.equals(".jpg")) {

					String suffix = name.substring(5, name.length());
					name = suffix;
					String exifName = "jpegs" + suffix;

					String folderName = name.substring( 1, name.indexOf('/', 1));

					name = name.substring(folderName.length() + 2);

					List<PictureFile> existingFile = this.pictureFiles.findByFilename(name);

					if (existingFile.isEmpty()) {

						PictureFile picture = new PictureFile();

						Map<String, String> properties = getExifEntries(exifName);

						try {
							pictureFileService.setDate(picture, properties);
						}
						catch (DateTimeParseException dtex) {
							System.out.println(dtex);
						}

						picture.setTitle(properties.get("title"));
						picture.setFilename(name);

						try {

							picture.setWidth(Integer.valueOf(properties.get("ImageWidth")));
							picture.setHeight(Integer.valueOf(properties.get("ImageHeight")));
							picture.setAdded_on(java.time.LocalDate.now());
						}
						catch (Exception ex) {
							System.out.println(ex);
							ex.printStackTrace();
						}

						// TODO: Split keywords and add singly!!!
						String iptcKeywords = properties.get("IPTC:Keywords");

						if (null == iptcKeywords) {

							iptcKeywords = subFolder.substring(6);
							addKeywordAndRelationship(picture, iptcKeywords);
						}

						if (picture.getRoles() == null || picture.getRoles().equals("")) {
							picture.setRoles("ROLE_USER");
						}

						this.pictureFiles.save(picture);

						results.add(picture);
					}
					else {

						PictureFile picture = existingFile.iterator().next();

						results.add(picture);

						Map<String, String> properties = getExifEntries(exifName);

						try {

							picture.setTitle(properties.get("title"));
							Optional.ofNullable(properties.get("ImageWidth"))
								.map(Integer::valueOf)
								.ifPresent(picture::setWidth);
							Optional.ofNullable(properties.get("ImageHeight"))
								.map(Integer::valueOf)
								.ifPresent(picture::setHeight);
							picture.setCameraModel(properties.get("CameraModel"));
							picture.setExposureTime(properties.get("ExposureTime"));
							picture.setFNumber(properties.get("FNumber"));
							picture.setExposureProgram(properties.get("ExposureProgram"));
							picture.setMeteringMode(properties.get("MeteringMode"));
							picture.setLightSource(properties.get("LightSource"));
							picture.setFocalLength(properties.get("FocalLength"));

							String gpsValue = properties.get("GPSPosition");
							String cleanedValue = Optional.ofNullable(gpsValue)
								.orElse("")
								.replace(" deg", "°");

							picture.setGps(cleanedValue);

							Pattern pattern = Pattern.compile("(\\d+)° (\\d+)' ([\\d.]+)\" ([NS]), (\\d+)° (\\d+)' ([\\d.]+)\" ([EW])");
							Matcher matcher = pattern.matcher(cleanedValue);

							if (matcher.find()) {

								double latDeg = Double.parseDouble(matcher.group(1));
								double latMin = Double.parseDouble(matcher.group(2));
								double latSec = Double.parseDouble(matcher.group(3));
								String latDir = matcher.group(4);

								double lonDeg = Double.parseDouble(matcher.group(5));
								double lonMin = Double.parseDouble(matcher.group(6));
								double lonSec = Double.parseDouble(matcher.group(7));
								String lonDir = matcher.group(8);

								float latitude = (float)(latDeg + (latMin / 60.0) + (latSec / 3600.0));
								if (latDir.equals("S")) latitude *= -1;

								float longitude = (float)(lonDeg + (lonMin / 60.0) + (lonSec / 3600.0));
								if (lonDir.equals("W")) longitude *= -1;

								picture.setLatitude(latitude);
								picture.setLongitude(longitude);
							}
						}
						catch (Exception ex) {
							System.out.println(ex);
							ex.printStackTrace();
						}

						if (picture.getRoles() == null || picture.getRoles().equals("")) {
							picture.setRoles("ROLE_USER");
						}

						pictureFileService.setDate(picture, properties);

						this.pictureFiles.save(picture);
					}
				}
			}
			catch (Exception ex) {
				System.out.println(ex);
				ex.printStackTrace();
				logger.log(Level.SEVERE, "An error occurred: " + ex.getMessage(), ex);
			}
		}

		try {

			String folderName = subFolder.substring(6);
			Collection<Folder> folders = this.folders.findByName(folderName);

			if (!folders.isEmpty()) {

				Folder folder = folders.iterator().next();

				folder.setPicture_count(filteredObjects.size());
				this.folders.save(folder);

				folderCache = null;
			}
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "An error occurred: " + e.getMessage(), e);
		}

		return results;
	}

	protected void addKeywordAndRelationship(PictureFile picture, String word) {

		Collection<Keyword> existing = this.keyword.findByContent(word);

		Keyword newKeyword;

		if (existing.isEmpty()) {

			newKeyword = new Keyword();
			newKeyword.setWord(word);
			this.keyword.save(newKeyword);
		}
		else
			newKeyword = existing.iterator().next();

		Collection<KeywordRelationships> relations = this.keywordRelationships.findByBothIds(picture.getId(), newKeyword.getId());

		if (relations.isEmpty()) {

			KeywordRelationships relation = new KeywordRelationships();
			relation.setPictureFile(picture);
			relation.setKeyword(newKeyword);
			this.keywordRelationships.save(relation);
		}
	}

	protected List<PictureFile> listFilteredFiles(List<PictureFile> files,
												  CollectionRequestDTO requestDTO,
												  Authentication authentication,
												  Map<String, Object> model) {

		Comparator<PictureFile> orderBy = getOrderComparator(requestDTO);

		Pattern pattern = Pattern.compile("\\b" + requestDTO.getSearch() + "\\b", Pattern.CASE_INSENSITIVE);

		// Fetch all keyword relationships for the given picture files
		Map<Integer, Set<String>> keywordMap = keywordService.fetchKeywordMap(files);

		Pair<LocalDate, LocalDate> dates = getDateRange(requestDTO, model);
		LocalDate endDate = dates.getSecond();
		LocalDateTime endOfDay = endDate.atTime(LocalTime.MAX);

		return files.stream()
			.filter(item -> isAuthorised(item, authentication))
			.filter(PictureFile::isValid)
			.filter(item->item.getTaken_on() != null)
			.filter(
				item -> item.getTaken_on().toLocalDate().isAfter(dates.getFirst()) ||
					item.getTaken_on().toLocalDate().isEqual(dates.getFirst())
			)
			.filter(
				item -> item.getTaken_on().isBefore(endOfDay) ||
				item.getTaken_on().isEqual(endOfDay)
			)
			.filter(item -> matchesTitleOrKeyword(item, pattern, keywordMap))
			.sorted(orderBy)
			.collect(Collectors.toList());
	}

	protected List<PictureFile> listFiles(List<PictureFile> files,
												  CollectionRequestDTO requestDTO,
												  Authentication authentication) {

		Comparator<PictureFile> orderBy = getOrderComparator(requestDTO);

		// Fetch all keyword relationships for the given picture files
		Map<Integer, Set<String>> keywordMap = keywordService.fetchKeywordMap(files);

		return files.stream()
			.filter(item -> isAuthorised(item, authentication))
			.filter(PictureFile::isValid)
			.sorted(orderBy)
			.collect(Collectors.toList());
	}

	protected Page<PictureFile> listFilesPaged(List<PictureFile> files,
											   CollectionRequestDTO requestDTO,
											   Authentication authentication,
											   Pageable pageable
	) {
		Comparator<PictureFile> orderBy = getOrderComparator(requestDTO);

		// Fetch all keyword relationships for the given picture files
		Map<Integer, Set<String>> keywordMap = keywordService.fetchKeywordMap(files);

		List<PictureFile> filteredFiles = files.stream()
			.filter(item -> isAuthorised(item, authentication))
			.filter(PictureFile::isValid)
			.sorted(orderBy)
			.collect(Collectors.toList());

		List<PictureFile> pagedFiles = getPagedFiles(filteredFiles, pageable);

		return new PageImpl<>(pagedFiles, pageable, filteredFiles.size());
	}

	protected Page<PictureFile> listFilteredFilesPaged(List<PictureFile> files,
													   CollectionRequestDTO requestDTO,
													   Authentication authentication,
													   Pageable pageable
	) {
		Comparator<PictureFile> orderBy = getOrderComparator(requestDTO);

		Pattern pattern = Pattern.compile("\\b" + requestDTO.getSearch() + "\\b", Pattern.CASE_INSENSITIVE);

		// Fetch all keyword relationships for the given picture files
		Map<Integer, Set<String>> keywordMap = keywordService.fetchKeywordMap(files);

		List<PictureFile> filteredFiles = files.stream()
			.filter(item -> isAuthorised(item, authentication))
			.filter(PictureFile::isValid)
			.filter(item -> matchesTitleOrKeyword(item, pattern, keywordMap))
			.sorted(orderBy)
			.collect(Collectors.toList());

		List<PictureFile> pagedFiles = getPagedFiles(filteredFiles, pageable);

		return new PageImpl<>(pagedFiles, pageable, filteredFiles.size());
	}

	List<PictureFile> getPagedFiles(List<PictureFile> filteredFiles, Pageable pageable) {

		int pageSize = pageable.getPageSize();
		int currentPage = pageable.getPageNumber();
		int startItem = currentPage * pageSize;

		if (startItem >= filteredFiles.size()) {
			return Collections.emptyList();
		} else {
			int endItem = Math.min(startItem + pageSize, filteredFiles.size());
			return filteredFiles.subList(startItem, endItem);
		}
	}

	private boolean matchesTitleOrKeyword(PictureFile item, Pattern pattern, Map<Integer, Set<String>> keywordMap) {

		try {

			// Check if title matches$
			Matcher titleMatcher = pattern.matcher(item.getTitle());
			if (titleMatcher.find()) {
				return true;
			}

			titleMatcher = pattern.matcher(item.getFolder().getDisplayName());
			if (titleMatcher.find()) {
				return true;
			}

			// Check if any associated keyword matches
			Set<String> keywords = keywordMap.getOrDefault(item.getId(), Collections.emptySet());
			return keywords.stream().anyMatch(word -> pattern.matcher(word).find());
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	protected boolean isAuthorised(PictureFile pictureFile, Authentication authentication) {

		if (authentication != null && authentication.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"))) {
			return true;
		} else {
			// Non-admin logic, filter pictures based on your criteria
			// For example, don't show pictures with scary content to non-admin users
			String roles = pictureFile.getRoles();
			return roles.equals("ROLE_USER") || roles.equals("");
		}
	}

	protected boolean isAdmin(Authentication authentication) {

		if (authentication != null && authentication.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"))) {
			return true;
		}
		else {
			return false;
		}
	}

	public Map<String, String> getExifEntries(String path) throws IOException {

		Map<String, String> results = new HashMap<>();
		results.put("title", "Untitled");

		try {

			byte[] data = null;

			try {
				data = downloadFile(path + ".exif");
			}
			catch (NoSuchKeyException noKey) {
				System.out.println("No EXIF data for " + path);
				return results;
			}

			InputStream targetStream = new ByteArrayInputStream(data);

			XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
			XMLEventReader reader = xmlInputFactory.createXMLEventReader(targetStream);

			while (reader.hasNext()) {

				XMLEvent nextEvent = reader.nextEvent();

				if (nextEvent.isStartElement()) {

					StartElement startElement = nextEvent.asStartElement();

					String key = startElement.getName().getLocalPart();
					String parent = startElement.getName().getPrefix();

					if (key.equals("ImageDescription") && !results.containsKey("title")) {

						nextEvent = reader.nextEvent();
						results.put("title", nextEvent.asCharacters().getData());
					}
					else if (key.equals("Comment")) {

						nextEvent = reader.nextEvent();
						results.put("title", nextEvent.asCharacters().getData());
					}
					else if (key.equals("ImageWidth") && parent.equals("File")) {

						nextEvent = reader.nextEvent();
						results.put("ImageWidth", nextEvent.asCharacters().getData());
					}
					else if (key.equals("ImageHeight") && parent.equals("File")) {

						nextEvent = reader.nextEvent();
						results.put("ImageHeight", nextEvent.asCharacters().getData());
					}
					else if (key.equals("DateTimeOriginal")) {

						nextEvent = reader.nextEvent();
						results.put("DateTimeOriginal", nextEvent.asCharacters().getData());
					}
					else if (key.equals("ProfileDateTime")) {

						nextEvent = reader.nextEvent();
						results.put("ProfileDateTime", nextEvent.asCharacters().getData());
					}
					else if (key.equals("IPTC:Keywords")) {

						nextEvent = reader.nextEvent();
						results.put("IPTC:Keywords", nextEvent.asCharacters().getData());
					}
					else if (key.equals("Model") && parent.equals("IFD0")) {

						nextEvent = reader.nextEvent();
						results.put("CameraModel", nextEvent.asCharacters().getData());
					}
					else if (parent.equals("Composite")) {

						if (key.equals("GPSPosition")) {

							StringBuilder gpsValue = new StringBuilder();

							while (reader.hasNext()) {
								XMLEvent innerEvent = reader.nextEvent();

								if (innerEvent.isCharacters()) {
									gpsValue.append(innerEvent.asCharacters().getData());
								} else if (innerEvent.isEndElement()) {
									EndElement endElement = innerEvent.asEndElement();
									if (endElement.getName().getLocalPart().equals("GPSPosition")) {
										break; // Stop when end of GPSPosition tag is reached
									}
								}
							}

							String cleanedValue = gpsValue.toString().replace(" deg", "°");
							results.put("GPSPosition", cleanedValue);
						}
					}
					else if (parent.equals("ExifIFD")) {

						if (key.equals("ExposureTime")) {

							nextEvent = reader.nextEvent();
							results.put("ExposureTime", nextEvent.asCharacters().getData());
						}
						else if (key.equals("FNumber")) {

							nextEvent = reader.nextEvent();
							results.put("FNumber", nextEvent.asCharacters().getData());
						}
						else if (key.equals("ExposureProgram")) {

							nextEvent = reader.nextEvent();
							results.put("ExposureProgram", nextEvent.asCharacters().getData());
						}
						else if (key.equals("MeteringMode")) {

							nextEvent = reader.nextEvent();
							results.put("MeteringMode", nextEvent.asCharacters().getData());
						}
						else if (key.equals("LightSource")) {

							nextEvent = reader.nextEvent();
							results.put("LightSource", nextEvent.asCharacters().getData());
						}
						else if (key.equals("FocalLength")) {

							nextEvent = reader.nextEvent();
							results.put("FocalLength", nextEvent.asCharacters().getData());
						}
					}
				}
			}
		}
		catch (Exception e) {

			results.put("title", "Error getting EXIF data");
			logger.log(Level.SEVERE, "An error occurred: " + e.getMessage(), e);
		}

		return results;
	}

	protected byte[] downloadFile(String objectKey) throws IOException {

		initialiseS3Client();

		GetObjectRequest objectRequest = GetObjectRequest.builder().bucket(bucketName).key(objectKey).build();

		ResponseBytes<GetObjectResponse> responseResponseBytes = s3Client.getObjectAsBytes(objectRequest);

		byte[] data = responseResponseBytes.asByteArray();

		return data;
	}

	protected void initialiseS3Client() {

		if (s3Client == null) {

			awsCredentials = AwsBasicCredentials.create(CredentialsRunner.getAccessKeyId(),
					CredentialsRunner.getSecretKey());

			s3Client = S3Client.builder().credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
					.region(Region.of(region)).endpointOverride(URI.create(endpoint)).build();

		}
	}

	protected Comparator<PictureFile> getOrderComparator(
		CollectionRequestDTO requestDTO
	) {

		final Comparator<PictureFile> orderBy = (item1, item2 ) -> { return item1.getTitle().compareTo(item2.getTitle()); };

		return getOrderComparator(requestDTO, orderBy);
	}

	protected Comparator<PictureFile> getOrderComparator(
		CollectionRequestDTO requestDTO,
		Comparator<PictureFile> orderBy
	) {

		String sortCriterion = "title";
		Sort.Direction direction = Sort.Direction.ASC;

		if (null != requestDTO.getSort()) {

			switch (requestDTO.getSort()) {
				case "Sort by Filename":
					orderBy = (item1, item2 ) -> { return item1.getFilename().compareTo(item2.getFilename()); };
					break;
				case "Sort by Date":
					orderBy = (item1, item2 ) -> {

						if (item1.getTaken_on() == null && item2.getTaken_on() == null) {
							return 0;
						}
						else if (item1.getTaken_on() == null ) {
							return -1;
						}
						else if (item2.getTaken_on() == null ) {
							return 1;
						}

						return item1.getTaken_on().compareTo(item2.getTaken_on());
					};
					break;
				case "Sort by Size":
					orderBy = (item1, item2 ) -> { return item1.getWidth() * item1.getWidth() - item2.getWidth() * item2.getWidth(); };
					break;
				case "Sort by Aspect Ratio":
					orderBy = (item1, item2 ) -> { return (int)(1000 * (item1.getAspectRatio() - item2.getAspectRatio())); };
					break;
				case "Sort by Saved Order":
					orderBy = (item1, item2 ) -> { return item1.getAdded_on().compareTo(item2.getAdded_on()); };
					break;
			}
		}

		return orderBy;
	}

	protected String getOrderColumn(CollectionRequestDTO requestDTO) {

		if (null != requestDTO.getSort()) {

			switch (requestDTO.getSort()) {
				case "Sort by Filename":
					return "filename";
				case "Sort by Date":
					return "taken_on";
				case "Sort by Size":
					return "width * height";
				case "Sort by Aspect Ratio":
					return "aspect_ratio";
				case "Sort by Saved Order":
					return "taken_on";
			}
		}

		return "filename";
	}

	protected String setModel(CollectionRequestDTO requestDTO,
							  Map<String, Object> model,
							  Collection<Folder> buckets,
							  List<PictureFile> results,
							  String template
	) {
		if (buckets.isEmpty()) {
			return "redirect:/buckets";
		}
		else {

			pictureFileService.applyArguments(model, requestDTO);

			Folder folder = buckets.iterator().next();
			model.put("collection", results);
			model.put("folder", folder);

			return template;
		}
	}

	protected void loadThumbnailsAndKeywords(Map<Integer, PictureFile> thumbnailsMap, Map<String, Object> model) {

		Set<Integer> pictureIds = new HashSet<>(thumbnailsMap.keySet());

		Collection<KeywordRelationships> relations = this.keywordService.findByPictureIds(pictureIds);

		Map<Integer, String> pictureIdToKeywords = relations.stream()
			.collect(Collectors.groupingBy(
				KeywordRelationships::getPictureId, // Group by PictureID
				Collectors.mapping(
					relationship -> relationship.getKeyword().getWord(), // Map each relationship to its keyword word
					Collectors.joining(", ") // Join keywords with a comma and space
				)
			));
		model.put("keyword_lists", pictureIdToKeywords);

		model.put("keywords", relations.stream()
			.map(relationship -> relationship.getKeyword().getWord())
			.distinct()
			.sorted()
			.collect(Collectors.joining(",")));
		model.put("keyword_list", pictureIdToKeywords);
		model.put("thumbnails", thumbnailsMap);

		if (!pictureIds.isEmpty()) {

			List<Location> locations = this.locationRelationships.findByPictureId(pictureIds.iterator().next()).stream()
				.map(LocationRelationship::getLocation)
				.collect(Collectors.toList());
			locations = locationService.sortLocationsByHierarchy(locations);
			model.put("location", locations.stream().map(Location::getLocation)
				.collect(Collectors.joining(", "))
			);
		}
	}

	protected void setStructuredDataForModel(@ModelAttribute CollectionRequestDTO requestDTO,
											 Map<String, Object> model,
											 String title,
											 String type,
											 String description,
											 Collection<PictureFile> pictures,
											 String keywords
	) {
		String baseURL = "https://www.homepix.ch";
		filepath = pictures.isEmpty()
			? baseURL + "/web-images/Stuff/200px/dsc_185760_200px.jpg"
			: pictures.iterator().next().getFilename();

		String structuredData = "{\n"
			+ "    \"@context\": \"http://schema.org\",\n"
			+ "    \"@type\": \"" + type + "\",\n"
			+ "    \"name\": \"" + title + "\",\n"
			+ "    \"description\": \"" + description + "\",\n"
			+ "    \"author\": {\"@type\": \"Person\",\"name\": \"Elliott Bignell\"},\n"
			+ "    \"datePublished\": \"2024-11-04\",\n"
			+ "    \"thumbnailUrl\": \"" + filepath + "\",\n"
			+ "    \"image\": [\n"
			+ pictures.stream().map( item -> {
				return item == null ? "" :
					"        {\n"
					+ "            \"@context\": \"http://schema.org\",\n"
					+ "            \"@type\": \"ImageObject\",\n"
					+ "            \"contentUrl\": \"" + baseURL + item.getLargeFilename() + "\",\n"
					+ "            \"thumbnail\": \"" + baseURL + item.getMediumFilename() + "\",\n"
					+ "            \"description\": \"" + item.getTitle() + "\",\n"
					+ "            \"license\": \"https://www.homepix.ch/licence.html\",\n"
					+ "            \"datePublished\": \"" + item.getTaken_on() + "\",\n"
					+ "            \"name\": \"" + item.getTitle() + "\",\n"
					+ "            \"creditText\": \"Photography by Elliott Bignell\",\n"
					+ "            \"creator\": {\"@type\": \"Person\",\"name\": \"Elliott Bignell\", \"url\": \"https://www.linkedin.com/in/elliottbignell/\" },\n"
					+ "            \"copyrightNotice\": \"© 2024 Elliott Bignell\",\n"
					+ "            \"acquireLicensePage\": \"https://www.homepix.ch/licence.html\""
					+ "            \n"
					+ "        }";
			})
			.collect(Collectors.joining(",\n"))
			+ "\n    ]}\n";

			//"license": "https://example.com/license",
			//"acquireLicensePage": "https://example.com/how-to-use-my-images",

		model.put("structuredData", structuredData);

		model.put("pageKeywords", keywords);
		model.put("description", description);

		pictureFileService.applyArguments(model, requestDTO);
	}

	protected void setStructuredDataForModel(@ModelAttribute CollectionRequestDTO requestDTO,
											 Map<String, Object> model,
											 String type,
											 PictureFile picture,
											 String keywords
	) {
		String baseURL = "https://www.homepix.ch";

		String structuredData = "{\n"
			+ "    \"@context\": \"http://schema.org\",\n"
			+ "    \"@type\": \"" + type + "\",\n"
			+ "    \"name\": \"" + picture.getTitle() + "\",\n"
			+ "    \"description\": \"" + picture.getTitle() + "\",\n"
			+ "    \"author\": {\"@type\": \"Person\",\"name\": \"Elliott Bignell\"},\n"
			+ "    \"datePublished\": \"2024-11-04\",\n"
			+ "    \"contentUrl\": \"" + baseURL + picture.getLargeFilename() + "\",\n"
			+ "    \"thumbnailUrl\": \"" + filepath + "\",\n"
			+ "    \"license\": \"https://www.homepix.ch/licence.html\",\n"
			+ "    \"creditText\": \"Photography by Elliott Bignell\",\n"
			+ "    \"image\": [\n"
			+ "    {\n"
			+ "            \"@context\": \"http://schema.org\",\n"
			+ "            \"@type\": \"ImageObject\",\n"
			+ "            \"contentUrl\": \"" + baseURL + picture.getLargeFilename() + "\",\n"
			+ "            \"thumbnailUrl\": \"" + baseURL + picture.getLargeFilename() + "\",\n"
			+ "            \"description\": \"" + picture.getTitle() + "\",\n"
			+ "            \"license\": \"https://www.homepix.ch/licence.html\",\n"
			+ "            \"author\": {\"@type\": \"Person\",\"name\": \"Elliott Bignell\"},\n"
			+ "            \"datePublished\": \"" + picture.getTaken_on() + "\",\n"
			+ "            \"name\": \"" + picture.getTitle() + "\",\n"
			+ "            \"creditText\": \"Photography by Elliott Bignell\",\n"
			+ "            \"creator\": {\"@type\": \"Person\",\"name\": \"Elliott Bignell\", \"url\": \"https://www.linkedin.com/in/elliottbignell/\" },\n"
			+ "            \"copyrightNotice\": \"© 2024 Elliott Bignell\",\n"
			+ "            \"acquireLicensePage\": \"https://www.homepix.ch/licence.html\",\n"
			+ "            \"keywords\": ["
			+              keywords
			+              "]"
			+ "            \n"
			+ "        }"
			+ "\n]}\n";

		model.put("structuredData", structuredData);

		model.put("description", picture.getTitle());
		model.put("title", picture.getTitle());
		model.put("keywords", keywords);

		pictureFileService.applyArguments(model, requestDTO);
	}
}
