package org.springframework.samples.homepix.portfolio.keywords;

import org.springframework.samples.homepix.portfolio.controllers.PaginationController;
import org.springframework.samples.homepix.portfolio.album.AlbumRepository;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.samples.homepix.portfolio.folder.FolderRepository;
import org.springframework.samples.homepix.portfolio.folder.FolderService;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

/**
 * @author Elliott Bignell
 */
@Controller
public class WordsController extends PaginationController {

	protected WordsController(AlbumRepository albums,
							  KeywordRepository keyword,
							  KeywordRelationshipsRepository keywordsRelationships,
							  FolderService folderService
	) {
		super(albums, keyword, keywordsRelationships, folderService);
	}

	@Secured("ROLE_ADMIN")
	@GetMapping("/words")
	public String showOwner(Map<String, Object> model) {

		List<String> words = this.pictureFiles.findAllWords();

		model.put("words", words);
		return "keywords/words";
	}
}
