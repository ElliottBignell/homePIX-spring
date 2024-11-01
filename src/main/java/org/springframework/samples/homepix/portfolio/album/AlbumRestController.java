package org.springframework.samples.homepix.portfolio.album;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/albums")
@Secured("ROLE_ADMIN")
public class AlbumRestController {

	@Autowired
	private AlbumService AlbumService;

	@Autowired
	AlbumContentService albumContentService;

	@Autowired
	AlbumContentRepository albumContent;

	@Autowired
	AlbumRepository albumRepository;

	@Autowired
	PictureFileRepository pictureFiles;

	@PostMapping("/add/{id}")
	public ResponseEntity<List<String>> addKeywords(@RequestBody Map<String, Object> updates, @PathVariable("id") Integer id) {

		String albumName = (String) updates.get("album");

		Collection<Album> albums = albumRepository.findByName(albumName);

		for (Album album : albums) {
			albumContentService.addPictureToAlbum(albumContent, pictureFiles, albumRepository, album.getId(), id);
		}

		ArrayList<String> list = new ArrayList<>();
		list.add(albumName);

		return ResponseEntity.ok(list);
	}
}
