package org.springframework.samples.homepix.portfolio.folder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FolderService {

	// Assuming you have a PictureFileRepository to fetch PictureFiles
	@Autowired
	private PictureFileRepository pictureFileRepository;

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
}