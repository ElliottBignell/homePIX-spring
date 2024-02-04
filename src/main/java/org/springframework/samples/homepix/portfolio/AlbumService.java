package org.springframework.samples.homepix.portfolio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class AlbumService {

	// Assuming you have a PictureFileRepository to fetch PictureFiles
	@Autowired
	private PictureFileRepository pictureFileRepository;

	public Map<Integer, PictureFile> getThumbnailsMap(Iterable<Album> albums) {

		List<Integer> thumbnailIds = StreamSupport.stream(albums.spliterator(), false) // Convert Iterable to Stream
			.map(Album::getThumbnail) // Map each Album to its PictureFile thumbnail
			.map(PictureFile::getId)
			.collect(Collectors.toList()); // Collect the results into a List

		// Create a map of thumbnail ID to PictureFile
		return getThumbnailsMap(thumbnailIds);
	}

	public Map<Integer, PictureFile> getThumbnailsMap(List<Integer> thumbnailIds) {

		// Fetch PictureFiles
		List<PictureFile> thumbnails = pictureFileRepository.findAllById(thumbnailIds);

		// Create a map of thumbnail ID to PictureFile
		return thumbnails.stream()
			.collect(Collectors.toMap(PictureFile::getId, pictureFile -> pictureFile));
	}
}
