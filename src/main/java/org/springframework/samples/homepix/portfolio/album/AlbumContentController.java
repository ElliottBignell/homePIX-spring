package org.springframework.samples.homepix.portfolio.album;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.homepix.CollectionRequestDTO;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.samples.homepix.portfolio.folder.FolderRepository;
import org.springframework.samples.homepix.portfolio.folder.FolderService;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRepository;
import org.springframework.samples.homepix.portfolio.locations.LocationRelationshipsRepository;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * @author Elliott Bignell
 */
@Controller
public class AlbumContentController extends AlbumContentBaseController {

	@Autowired
	AlbumContentService albumContentService;

	public AlbumContentController(AlbumContentRepository albumContent,
									  AlbumRepository albums,
									  KeywordRepository keyword,
									  KeywordRelationshipsRepository keywordsRelationships,
									  FolderService folderService,
									  AlbumService albumService
	) {
		super(albumContent, albums, keyword, keywordsRelationships, folderService, albumService);
	}

	/**
	 * Custom handler for displaying an album.
	 * @param id the ID of the album to display
	 * @return a ModelMap with the model attributes for the view
	 */
	@GetMapping("/album/{id}/")
	public ModelAndView showAlbumSlash(@ModelAttribute CollectionRequestDTO requestDTO,
								  @PathVariable("id") long id,
								  Map<String, Object> model
	) {
		return showAlbum(requestDTO, id, model);
	}

	/**
	 * Custom handler for displaying an album.
	 * @param id the ID of the album to display
	 * @return a ModelMap with the model attributes for the view
	 */
	@GetMapping("/album/{id}")
	public ModelAndView showAlbum(@ModelAttribute CollectionRequestDTO requestDTO,
								  @PathVariable("id") long id,
								  Map<String, Object> model
	) {

		Optional<Album> album = this.albums.findById(id);

		if (album.isPresent()) {
			return showAlbumContent(album.get(), requestDTO, id, model,"albums/albumDetails");
		}
		else {
			return new ModelAndView("redirect:/album/");
		}
	}

	@GetMapping("/albums/{id}")
	public ModelAndView showAlbums(@ModelAttribute CollectionRequestDTO requestDTO,
								   @PathVariable("id") long id,
								   Map<String, Object> model
	) {
		return showAlbum(requestDTO, id, model);
	}

	/**
	 * Custom handler for displaying an album.
	 * @param ownerId the ID of the album to display
	 * @return a ModelMap with the model attributes for the view
	 */
	@GetMapping("/album/{id}/{ownerId}")
	public ModelAndView showOwner(@PathVariable("ownerId") long id, @PathVariable("ownerId") int ownerId) {
		ModelAndView mav = new ModelAndView("albums/albumDetails");
		// AlbumContent albumContent = this.albums.findById(ownerId);
		return mav;
	}

	@Secured("ROLE_ADMIN")
	@GetMapping("/albums/{albumId}/add/{pictureId}")
	public String addPictureToAlbum(@PathVariable("albumId") long albumId, @PathVariable("pictureId") int pictureId,
									Map<String, Object> model) {
		return albumContentService.addPictureToAlbum(albumContent, pictureFiles, albums, albumId, pictureId);
	}

	@Secured("ROLE_ADMIN")
	@PostMapping("/album/{albumId}/delete/{pictureId}")
	public ResponseEntity<String> deletePicture(@PathVariable("albumId") long albumId,
												@PathVariable("pictureId") int pictureId, Map<String, Object> model) {

		Collection<AlbumContent> content = (Collection<AlbumContent>) this.albumContent.findByAlbumIdAndEntryId(albumId,
			pictureId);

		if (!content.isEmpty()) {

			albumContent.delete(content.iterator().next());
			return ResponseEntity.ok("Deleted successfully");
		}

		return ResponseEntity.ok("Delete failed");
	}

	@Secured("ROLE_ADMIN")
	@GetMapping("/albums/all/initialise")
	public String initialisePictures() {

		Iterable<Album> albums = this.albums.findAll();

		for (Album album : albums) {

			Collection<AlbumContent> albumContent = this.albumContent.findByAlbumId(album.getId());

			int index = 1;

			for (AlbumContent content : albumContent) {

				content.setSort_order(index++);
				this.albumContent.save(content);
			}
		}

		return "redirect:/album/";
	}
}
