package org.springframework.samples.homepix.portfolio.album;

import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class AlbumContentService {

	public String addPictureToAlbum(AlbumContentRepository albumContent,
									PictureFileRepository pictureFiles,
									AlbumRepository albums,
									long albumId,
									int pictureId
	) {

		Collection<AlbumContent> entry = albumContent.findByAlbumIdAndEntryId(albumId, pictureId);

		if (entry.isEmpty()) {

			AlbumContent content = new AlbumContent();

			Optional<PictureFile> picture = pictureFiles.findById(pictureId);
			Optional<Album> album = albums.findById(albumId);

			if (picture.isPresent() && album.isPresent()) {

				Collection<AlbumContent> allContent = albumContent.findByAlbumId(albumId);

				content.setSort_order(allContent.size() + 1);
				content.setPictureFile(picture.get());
				content.setAlbum(album.get());

				try {
					albumContent.save(content);
				} catch (Exception ex) {

					System.out.println(ex);
					ex.printStackTrace();
					System.out.println(albumContent);
					return "redirect:/albums/" + Long.toString(albumId);
				}

				return "redirect:/albums/" + Long.toString(albumId);
			}
		}

		return "redirect:/albums/" + Long.toString(albumId);
	}
}
