package org.springframework.samples.homepix.portfolio.calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.portfolio.*;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRepository;
import org.springframework.stereotype.Controller;

/**
 * @author Elliott Bignell
 */
@Controller
public class YearThumbnailController extends PaginationController {

	@Autowired
	public YearThumbnailController(AlbumRepository albums,
								   FolderRepository folders,
								   PictureFileRepository pictureFiles,
								   AlbumContentRepository albumContents,
								   KeywordRepository keyword,
								   KeywordRelationshipsRepository keywordsRelationships,
								   FolderService folderService
	) {
		super(albums, folders, pictureFiles, keyword, keywordsRelationships, folderService);
	}
}
