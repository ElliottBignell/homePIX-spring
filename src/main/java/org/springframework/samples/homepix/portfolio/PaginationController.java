package org.springframework.samples.homepix.portfolio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.CredentialsRunner;
import org.springframework.samples.homepix.portfolio.calendar.Calendar;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
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

	protected Calendar calendar;

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
		calendar = new Calendar();
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

	@ModelAttribute(name = "folders")
	Collection<Folder> findAllFolders(Folder folder, BindingResult result, Map<String, Object> model) {

		if (null == folderCache) {
			loadBuckets(folder, result, model);
		}
		return this.folders.findAll();
	}

	protected String loadFolders(Folder folder, BindingResult result, Map<String, Object> model) {

		// allow parameterless GET request for /folders to return all records
		if (folder.getName() == null) {

			loadBuckets(folder, result, model);
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

			folderCache = listSubFolders(s3Client, "jpegs");

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
				model.put("selections", folderCache);
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

		folders.deleteAll();

		listObjectsResponse.commonPrefixes().forEach(subFolder -> {

			Folder folder = new Folder();

			String name = subFolder.prefix();
			folder.setName(name.substring(parentFolder.length() + 1, name.length() - 1));
			folder.setPicture_count(0);

			results.add(folder);
			folders.save(folder);
		});

		return results;
	}

	protected List<PictureFile> listFiles(S3Client s3Client, String subFolder) {

		String prefix = subFolder.endsWith("/") ? subFolder : subFolder + "/";

		ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder().bucket(bucketName).prefix(prefix)
				.build();

		ListObjectsV2Response listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);

		List<S3Object> filteredObjects = listObjectsResponse.contents().stream()
				.filter(object -> !object.key().startsWith(prefix + "200px/"))
				.filter(object -> object.key().endsWith(".jpg")).collect(Collectors.toList());

		List<PictureFile> results = new ArrayList<>();

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

						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss",
								Locale.ENGLISH);
						String dateTimeString = properties.get("DateTimeOriginal");
						LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatter);

						picture.setTitle(properties.get("title"));
						picture.setFilename(name);
						picture.setWidth(Integer.valueOf(properties.get("ImageWidth")));
						picture.setHeight(Integer.valueOf(properties.get("ImageHeight")));
						picture.setTaken_on(dateTime);
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

						this.pictureFiles.save(picture);

						results.add(picture);
					}
					else {
						results.add(existingFile.iterator().next());
					}
				}
			}
			catch (Exception ex) {
				System.out.println(ex);
				ex.printStackTrace();
			}
		}

		return results;
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

					if (key.equals("ImageDescription")) {

						nextEvent = reader.nextEvent();
						results.put("title", nextEvent.asCharacters().getData());
					}
					else if (key.equals("ImageWidth")) {

						nextEvent = reader.nextEvent();
						results.put("ImageWidth", nextEvent.asCharacters().getData());
					}
					else if (key.equals("ImageHeight")) {

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

}
