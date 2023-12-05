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
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PaginationController {

	protected Pagination pagination;

	protected Calendar calendar;

	protected final AlbumRepository albums;

	protected final FolderRepository folders;

	protected final PictureFileRepository pictureFiles;

	Collection<Folder> folderCache = null;

	// @Value("${homepix.images.path}")
	protected static final String imagePath = System.getProperty("user.dir") + "/images/";

	@Autowired
	protected PaginationController(AlbumRepository albums, FolderRepository folders,
			PictureFileRepository pictureFiles) {
		this.albums = albums;
		this.folders = folders;
		this.pictureFiles = pictureFiles;
		pagination = new Pagination();
		calendar = new Calendar();
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
			System.out.println("findAllFolders");
			loadBuckets(folder, result, model);
		}
		return this.folders.findAll();
	}

	protected String loadFolders(Folder folder, BindingResult result, Map<String, Object> model) {

		// allow parameterless GET request for /folders to return all records
		if (folder.getName() == null) {

			System.out.println("loadFolders");
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

			AwsCredentials awsCredentials = AwsBasicCredentials.create(CredentialsRunner.getAccessKeyId(),
					CredentialsRunner.getSecretKey());

			S3Client s3Client = S3Client.builder().credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
					.region(Region.of("ch-dk-2")).endpointOverride(URI.create("https://sos-ch-dk-2.exo.io")).build();

			// Now you can use s3Client to interact with the Exoscale S3-compatible
			// service
			String bucketName = "picture-files";

			System.out.println("Load buckets");

			folderCache = listSubFolders(s3Client, bucketName, "jpegs");

			// Don't forget to close the S3Client when you're done
			s3Client.close();

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

	protected List<Folder> listSubFolders(S3Client s3Client, String bucketName, String parentFolder) {

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

			System.out.println("Sub-Folder: " + subFolder.prefix());
		});

		return results;
	}

}
