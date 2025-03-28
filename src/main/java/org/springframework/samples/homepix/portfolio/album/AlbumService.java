package org.springframework.samples.homepix.portfolio.album;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.samples.homepix.portfolio.folder.Folder;
import org.springframework.samples.homepix.portfolio.maps.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class AlbumService {

	// Assuming you have a PictureFileRepository to fetch PictureFiles
	@Autowired
	private PictureFileRepository pictureFileRepository;

	@Autowired
	private AlbumRepository albumRepository;

	@Autowired
	private AlbumContentRepository albumContentRepository;

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

	public String loadMapPreview(Map<String, Object> model, String name) {

		Collection<Album> albums = this.albumRepository.findByName(name);

		if (!albums.isEmpty()) {

			Album album = albums.iterator().next();

			Collection<PictureFile> content = albumContentRepository.findPictures(album.getId());

			if (!content.isEmpty()) {

				int thumbnailId = album.getThumbnail_id();

				Optional<PictureFile> pictureFile = pictureFileRepository.findById(thumbnailId);
				PictureFile picture = null;

				if (pictureFile.isPresent()) {
					picture = pictureFile.get();
				}
				else {
					picture = content.iterator().next();
				}

				for (PictureFile file : content) {

					if (null != file.getLatitude() && null != file.getLongitude()) {

						model.put("picture", file);
						break;
					}
				}

				// Convert double[] to List<Double> for Thymeleaf compatibility
				List<PictureFile> nearbyPictures = content
					.stream()
					.filter(element -> element.getLatitude() != null && element.getLongitude() != null)
					.collect(Collectors.toList());
				List<List<Float>> nearbyCoordinates = nearbyPictures
					.stream()
					.map(element -> Arrays.asList(element.getLatitude(), element.getLongitude()))
					.collect(Collectors.toList());

				Map.Entry<MapUtils.LatLng, Integer> result = MapUtils.calculateCenterAndZoom(nearbyPictures);

				MapUtils.LatLng center = result.getKey();
				int zoom = result.getValue();

				model.put("title", "Picture map");
				model.put("zoom", zoom);
				model.put("refLatitude", center.getLatitude());
				model.put("refLongitude", center.getLongitude());
				model.put("nearbyCoordinates", nearbyCoordinates);
				model.put("nearbyPictures", nearbyPictures);

				return "picture/pictureMap.html";
			}
		}

		return "redirect:/albums";
	}

	@Cacheable("slides")
	public Collection<PictureFile> geSlides() {

		long id = 0;

		Collection<Album> results = this.albumRepository.findByName("Slides");
		Album album = results.iterator().next();

		id = album.getId();

		Collection<AlbumContent> contents = this.albumContentRepository.findByAlbumId(id);

		final Comparator<PictureFile> defaultSort = (item1, item2 ) -> { return
			getSortOrder(this.albumContentRepository, album, item1) - getSortOrder(this.albumContentRepository, album, item2);
		};

		return contents.stream().map(item -> item.getPictureFile())
			.sorted(defaultSort)
			.collect(Collectors.toList());
	}

	@Cacheable("slidesThumbnails")
	public Map<Integer, PictureFile> slidesThumbnailMap() {

		long id = 0;

		Collection<Album> results = this.albumRepository.findByName("Slides");

		if (!results.isEmpty()) {
			id = results.iterator().next().getId();
		}

		Collection<AlbumContent> contents = this.albumContentRepository.findByAlbumId(id);
		Collection<Album> albums = this.albumRepository.findByName("Slides");
		Album album = albums.iterator().next();

 		final Comparator<PictureFile> defaultSort = (item1, item2 ) -> { return
			getSortOrder(this.albumContentRepository, album, item1) - getSortOrder(this.albumContentRepository, album, item2);
		};

		Comparator<PictureFile> orderBy = (item1, item2 ) -> { return item1.getFilename().compareTo(item2.getFilename()); };

		Collection<PictureFile> slides = contents.stream().map(item -> item.getPictureFile())
			.sorted(orderBy)
			.collect(Collectors.toList());

		return getThumbnailsMap(
			StreamSupport.stream(slides.spliterator(), false) // Convert Iterable to Stream
				.map(PictureFile::getId)
				.collect(Collectors.toList())
		);
	}

	public int getSortOrder(AlbumContentRepository albumContent, Album album, PictureFile item) {

		int pictureId = item.getId();
		long albumId = album.getId();

		Collection<AlbumContent> content = albumContent.findByAlbumIdAndEntryId(albumId, pictureId);

		if (!content.isEmpty()) {
			return content.iterator().next().getSort_order();
		}

		return -1;
	}

	@Cacheable("importedAlbums")
	public List<Album> getSortedAlbums() {
		return StreamSupport.stream(albumRepository.findAll().spliterator(), false)
			.collect(Collectors.toList());
	}
}
