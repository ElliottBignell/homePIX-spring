package org.springframework.samples.homepix.portfolio.licensing;

import org.springframework.samples.homepix.portfolio.PaginationController;
import org.springframework.samples.homepix.portfolio.album.Album;
import org.springframework.samples.homepix.portfolio.album.AlbumRepository;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.samples.homepix.portfolio.folder.FolderRepository;
import org.springframework.samples.homepix.portfolio.folder.FolderService;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRepository;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 * @author Elliott Bignell
 */
@Controller
public class LicenceController extends PaginationController {

	private static final String LICENCE_FORM = "picture/licence";

	protected LicenceController(AlbumRepository albums,
								FolderRepository folders,
								PictureFileRepository pictureFiles,
								KeywordRepository keyword,
								KeywordRelationshipsRepository keywordsRelationships,
								FolderService folderService
	) {
		super(albums, folders, pictureFiles, keyword, keywordsRelationships, folderService);
	}

	@GetMapping("/licence.html")
	public String licence(Album album, BindingResult result, Map<String, Object> model) {
		return LICENCE_FORM;
	}
}
