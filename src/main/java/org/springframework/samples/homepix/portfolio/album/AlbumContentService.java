package org.springframework.samples.homepix.portfolio.album;

import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.Optional;

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

				content.setSortOrder(allContent.size() + 1);
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
