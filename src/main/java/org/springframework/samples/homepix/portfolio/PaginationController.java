package org.springframework.samples.homepix.portfolio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.samples.homepix.CollectionRequestDTO;
import org.springframework.samples.homepix.CredentialsRunner;
import org.springframework.samples.homepix.portfolio.calendar.Calendar;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class PaginationController implements AutoCloseable {

	protected Pagination pagination;

	protected Calendar calendar = null;

	protected final AlbumRepository albums;

	protected final FolderRepository folders;

	protected final PictureFileRepository pictureFiles;

	protected final KeywordsRepository keywords;

	protected static final String bucketName = "picture-files";

	protected static final String endpoint = "https://sos-ch-dk-2.exo.io";

	protected static final String region = "ch-dk-2";

	Collection<Folder> folderCache = null;

	protected S3Client s3Client;

	private AwsCredentials awsCredentials;

	// @Value("${homepix.images.path}")
	protected static final String imagePath = System.getProperty("user.dir") + "/images/";

	@Autowired
	protected PaginationController(AlbumRepository albums, FolderRepository folders, PictureFileRepository pictureFiles,
			KeywordsRepository keywords) {
		this.albums = albums;
		this.folders = folders;
		this.pictureFiles = pictureFiles;
		this.keywords = keywords;
		pagination = new Pagination();
	}

	@Override
	public void close() throws Exception {

		if (s3Client != null) {
			s3Client.close();
		}
	}

	protected void addParams(int pictureId, String filename, Iterable<PictureFile> pictureFiles,
			Map<String, Object> model, boolean byID) {

		PictureFile picture = pictureFiles.iterator().next();
		PictureFile first = picture;
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

	@ModelAttribute(name = "pagination")
	Pagination getPagination() {
		return this.pagination;
	}

	@ModelAttribute(name = "albums")
	Iterable<Album> findAllAlbums() {
		return this.albums.findAll();
	}

	@ModelAttribute(name = "/folders/import")
	Collection<Folder> importFolders(Folder folder, BindingResult result, Map<String, Object> model) {

		folderCache = null;
		loadBuckets(folder, result, model);
		folderCache = this.folders.findAll();

		return folderCache;
	}

	@ModelAttribute(name = "/folders")
	Collection<Folder> findAllFolders(Folder folder, BindingResult result, Map<String, Object> model) {

		folderCache = this.folders.findAll();

		if (null == folderCache) {

			loadBuckets(folder, result, model);
			folderCache = this.folders.findAll();
		}
		return folderCache;
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
		catch (Exception ex) {
			System.out.println(ex);
			ex.printStackTrace();
		}
	}

	/*
	 * private void load() {
	 *
	 * try {
	 *
	 * List<String> folderNames = Stream.of(Objects.requireNonNull(new
	 * File(this.imagePath).listFiles()))
	 * .filter(File::isDirectory).map(File::getName).sorted().collect(Collectors.toList())
	 * ;
	 *
	 * folders.deleteAll();
	 *
	 * for (String name : folderNames) {
	 *
	 * Folder item = new Folder();
	 *
	 * item.setName(name); item.setThumbnailId(36860);
	 *
	 * final Pattern JPEGS = Pattern.compile(".*jpg$");
	 *
	 * long count = Stream.of(new File(this.imagePath + name).listFiles()).filter(file ->
	 * !file.isDirectory()) .filter(file -> JPEGS.matcher(file.getName()).find()).count();
	 * item.setPicture_count((int) count);
	 *
	 * folders.save(item); } } catch (Exception ex) { System.out.println(ex);
	 * ex.printStackTrace(); } }
	 */

	protected String loadBuckets(Folder folder, BindingResult result, Map<String, Object> model) {

		try {

			initialiseS3Client();

			// Now you can use s3Client to interact with the Exoscale S3-compatible
			// service
			String bucketName = "picture-files";

			if (null == folderCache) {
				folderCache = listSubFolders(s3Client, "jpegs");
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
				model.put("folders", folderCache);
				return "folders/folderList";
			}
		}
		catch (Exception ex) {
			System.out.println(ex);
			ex.printStackTrace();
		}
		return "folders/folderList";
	}

	protected List<Folder> listSubFolders(S3Client s3Client, String parentFolder) {

		String prefix = parentFolder.endsWith("/") ? parentFolder : parentFolder + "/";

		ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder().bucket(bucketName).prefix(prefix)
				.delimiter("/").build();

		ListObjectsV2Response listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);

		List<Folder> results = new ArrayList<Folder>();

		listObjectsResponse.commonPrefixes().forEach(subFolder -> {

			String name = subFolder.prefix();
			String folderName = name.substring(parentFolder.length() + 1, name.length() - 1);

			Collection<Folder> folders = this.folders.findByName(folderName);

			if (folders.isEmpty()) {

				Folder folder = new Folder();

				folder.setName(folderName);
				folder.setPicture_count(0);

				results.add(folder);
				this.folders.save(folder);
			}
			else {
				results.add(folders.iterator().next());
			}
		});

		return results;
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

		do {
			listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);

			filteredObjects.addAll(listObjectsResponse.contents().stream()
				.filter(object -> !object.key().startsWith(prefix + "200px/"))
				.filter(object -> object.key().endsWith(".jpg"))
				.collect(Collectors.toList()));

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

					String sizePart = name.substring(name.length() - 9, name.length() - 4).toLowerCase();

					String suffix = name.substring(5, name.length());
					name = "/web-images" + suffix;
					String exifName = "jpegs" + suffix;

					List<PictureFile> existingFile = this.pictureFiles.findByFilename(name);

					if (existingFile.isEmpty()) {

						PictureFile picture = new PictureFile();

						Map<String, String> properties = getExifEntries(exifName);

						setDate(picture, properties);

						picture.setTitle(properties.get("title"));
						picture.setFilename(name);
						picture.setWidth(Integer.valueOf(properties.get("ImageWidth")));
						picture.setHeight(Integer.valueOf(properties.get("ImageHeight")));
						picture.setAdded_on(java.time.LocalDate.now());
						// picture.setLocation(properties.get("title")));
						// picture.setPrimaryCategory(properties.get("title")));
						// picture.setSecondaryCategory(properties.get("title")));

						String iptcKeywords = properties.get("IPTC:Keywords");

						if (null == iptcKeywords) {
							iptcKeywords = subFolder.substring(6);
						}

						Collection<Keywords> keywords = this.keywords.findByContent(iptcKeywords);

						if (keywords.isEmpty()) {

							Keywords newKeywords = new Keywords();
							newKeywords.setContent(iptcKeywords);
							newKeywords.setKeyword_count(0);
							this.keywords.save(newKeywords);
							picture.setKeywords(newKeywords);
						}
						else {
							picture.setKeywords(keywords.iterator().next());
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

						picture.setWidth(Integer.valueOf(properties.get("ImageWidth")));
						picture.setHeight(Integer.valueOf(properties.get("ImageHeight")));
						picture.setCameraModel(properties.get("CameraModel"));
						picture.setExposureTime(properties.get("ExposureTime"));
						picture.setFNumber(properties.get("FNumber"));
						picture.setExposureProgram(properties.get("ExposureProgram"));
						picture.setMeteringMode(properties.get("MeteringMode"));
						picture.setLightSource(properties.get("LightSource"));
						picture.setFocalLength(properties.get("FocalLength"));

						if (picture.getRoles() == null || picture.getRoles().equals("")) {
							picture.setRoles("ROLE_USER");
						}

						setDate(picture, properties);

						this.pictureFiles.save(picture);
					}
				}
			}
			catch (Exception ex) {
				System.out.println(ex);
				ex.printStackTrace();
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
		catch (Exception ex) {
			ex.printStackTrace();
		}

		return results;
	}

	private void setDate(PictureFile picture, Map<String, String> properties) {

		try {

			final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
				"yyyy:MM:dd HH:mm:ss",
				Locale.ENGLISH
			);

			String dateTimeString = properties.get("DateTimeOriginal");

			if (null != dateTimeString) {

				final String plus = "[+]";

				if (dateTimeString.contains("+")) {

					String[] parts = dateTimeString.split(plus);
					dateTimeString = parts[0];
				}

				final String minus = "[-]";

				if (dateTimeString.contains("+")) {

					String[] parts = dateTimeString.split(minus);
					dateTimeString = parts[0];
				}

				if (null != dateTimeString) {

					LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatter);
					picture.setTaken_on(dateTime);
				}
			}
		}
		catch (Exception ex) {
			System.out.println(ex);
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

					if (key.equals("ImageDescription")) {

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
					else if (key.equals("IPTC:Keywords")) {

						nextEvent = reader.nextEvent();
						results.put("IPTC:Keywords", nextEvent.asCharacters().getData());
					}
					else if (key.equals("Model") && parent.equals("IFD0")) {

						nextEvent = reader.nextEvent();
						results.put("CameraModel", nextEvent.asCharacters().getData());
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
		catch (Exception ex) {

			results.put("title", "Error getting EXIF data");
			System.out.println(ex);
			ex.printStackTrace();
		}

		return results;
	}

	protected byte[] downloadFile(String objectKey) throws IOException {

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

	protected Comparator<PictureFile> getOrderComparator(CollectionRequestDTO requestDTO) {

		String sortCriterion = "title";
		Sort.Direction direction = Sort.Direction.ASC;

		Comparator<PictureFile> orderBy = (item1, item2 ) -> { return item1.getTitle().compareTo(item2.getTitle()); };

		if (null != requestDTO.getSort()) {

			switch (requestDTO.getSort()) {
				case "Sort by Filename":
					orderBy = (item1, item2 ) -> { return item1.getFilename().compareTo(item2.getFilename()); };
					break;
				case "Sort by Date":
					orderBy = (item1, item2 ) -> { return item1.getTaken_on().compareTo(item2.getTaken_on()); };
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

			model.put("startDate", requestDTO.getFromDate());
			model.put("endDate", requestDTO.getToDate());
			model.put("sort", requestDTO.getSort());
			model.put("search", requestDTO.getSearch());

			Folder folder = buckets.iterator().next();
			model.put("collection", results);
			model.put("folder", folder);

			return template;
		}
	}
}
