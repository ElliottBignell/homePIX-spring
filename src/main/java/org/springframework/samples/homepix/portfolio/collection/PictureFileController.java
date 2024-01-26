/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.homepix.portfolio.collection;

import org.springframework.samples.homepix.portfolio.*;
import org.springframework.samples.homepix.portfolio.Album;
import org.springframework.samples.homepix.portfolio.AlbumRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Elliott Bignell
 */
@Controller
@RequestMapping("/albums/{albumId}")
public class PictureFileController extends PaginationController {

	private static final String VIEWS_PETS_CREATE_OR_UPDATE_FORM = "pets/createOrUpdatePictureFileForm";

	private static final String ABOUT_FORM = "picture/about.html";

	public PictureFileController(PictureFileRepository pictureFiles,
								 AlbumRepository albums,
								 FolderRepository folders,
								 KeywordRepository keyword,
								 KeywordRelationshipsRepository keywordsRelationships,
								 FolderService folderService
	) {
		super(albums, folders, pictureFiles, keyword, keywordsRelationships, folderService);
	}

	@ModelAttribute("types")
	public Collection<PictureFileType> populatePictureFileTypes() {
		return this.pictureFiles.findPictureFileTypes();
	}

	@ModelAttribute("album")
	public Album findOwner(@PathVariable("albumId") long albumId) {
		return this.albums.findById(albumId).get();
	}

	@InitBinder("album")
	public void initOwnerBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@Override
	public void close() throws Exception {

	}

}
