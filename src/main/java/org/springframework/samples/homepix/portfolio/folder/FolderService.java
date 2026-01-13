package org.springframework.samples.homepix.portfolio.folder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.samples.homepix.CredentialsRunner;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Getter
public class FolderService {

	private final PictureFileRepository pictureFileRepository;
	private final FolderRepository folderRepository;
	private final PictureFileRepository pictureFiles;
	private final S3Client s3Client;

	protected static final String bucketName = "picture-files";

	@Cacheable(value = "thumbnails for folders", key = "#folders.![thumbnailId].stream().sorted().toList()")
	public Map<Integer, PictureFile> getThumbnailsMap(List<Folder> folders) {
		// Extract thumbnail IDs
		List<Integer> thumbnailIds = folders.stream()
			.map(Folder::getThumbnailId)
			.collect(Collectors.toList());

		// Fetch PictureFiles
		List<PictureFile> thumbnails = pictureFileRepository.findAllById(thumbnailIds);

		// Create a map of thumbnail ID to PictureFile
		return thumbnails.stream()
			.collect(Collectors.toMap(PictureFile::getId, pictureFile -> pictureFile));
	}

	public List<String> listFileNames(S3Client s3Client, String bucketName, String subFolder) {

		String prefix = subFolder.endsWith("/") ? subFolder : subFolder + "/";

		ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder().bucket(bucketName).prefix(prefix)
			.build();

		ListObjectsV2Response listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);

		List<S3Object> filteredObjects = listObjectsResponse.contents().stream()
			.filter(object -> !object.key().startsWith(prefix + "200px/"))
			.filter(object -> object.key().endsWith(".webp")).collect(Collectors.toList());

		List<String> results = new ArrayList<>();

		for (S3Object s3Object : filteredObjects) {

			try {

				String name = s3Object.key();
				String extension = name.substring(name.length() - 4).toLowerCase();

				if (extension.equals(".jpg")) {

					String suffix = name.substring(5, name.length());
					results.add(suffix);
				}
			}
			catch (Exception ex) {
				System.out.println(ex);
				ex.printStackTrace();
			}
		}

		return results;
	}

	@Cacheable("importedFolders")
	public List<Folder> getSortedFolders() {
		return folderRepository.findAll().stream()
			.sorted(Comparator.comparing(Folder::getName))
			.collect(Collectors.toList());
	}

	@CacheEvict(value = { "importedFolders", "listSubFolders", "getThumbnailsForFolders", "thumbnails for folders" }, allEntries = true)
	@Scheduled(cron = "0 0 3 * * *") // every day at 3 AM
    public void resetFolders() {
        // This will clear the "folders" cache.
        // Optionally re-fetch or do nothing here;
        // next call to getSortedFolders() will reload.
    }

	@Cacheable(value = "listSubFolders", key = "#parentFolder")
	public List<Folder> listSubFolders(String parentFolder) {

		String prefix = parentFolder.endsWith("/") ? parentFolder : parentFolder + "/";

		ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder().bucket(bucketName).prefix(prefix)
			.delimiter("/").build();

		ListObjectsV2Response listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);

		List<Folder> results = new ArrayList<Folder>();

		listObjectsResponse.commonPrefixes().forEach(subFolder -> {

			String name = subFolder.prefix();
			String folderName = name.substring(parentFolder.length() + 1, name.length() - 1);

			Collection<Folder> folders = this.folderRepository.findByName(folderName);

			if (folders.isEmpty()) {

				Folder folder = new Folder();

				folder.setName(folderName);
				folder.setPicture_count(0);

				results.add(folder);
				this.folderRepository.save(folder);
			}
			else {
				results.add(folders.iterator().next());
			}
		});

		return results;
	}

	@Cacheable(value = "getThumbnailsForFolders", key = "#folders.![thumbnailId].stream().sorted().toList()")
	public List<PictureFile> getThumbnails(List<Folder> folders) {

		return folders.stream().map(item -> {
				return this.pictureFiles.findById(item.getThumbnailId()).orElse(null);
			})
			.filter(file -> file != null)
			.collect(Collectors.toList());
	}

	@CacheEvict(value = { "thumbnails for folders", "importedFolders" , "listSubFolders" , "getThumbnailsForFolders" }, allEntries = true)
	@Scheduled(cron = "0 0 3 * * *") // every day at 3 AM
	public void resetCache() {
		// This will clear the "folders" cache.
		// Optionally re-fetch or do nothing here;
		// next call to getSortedFolders() will reload.
	}
}

