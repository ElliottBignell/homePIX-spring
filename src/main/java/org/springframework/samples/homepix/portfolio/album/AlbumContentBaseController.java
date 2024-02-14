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
package org.springframework.samples.homepix.portfolio.album;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.homepix.CollectionRequestDTO;
import org.springframework.samples.homepix.portfolio.folder.Folder;
import org.springframework.samples.homepix.portfolio.folder.FolderRepository;
import org.springframework.samples.homepix.portfolio.folder.FolderService;
import org.springframework.samples.homepix.portfolio.PaginationController;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRepository;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Elliott Bignell
 */
public class AlbumContentBaseController extends PaginationController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "albums/createOrUpdateOwnerForm";

	protected final AlbumContentRepository albumContent;
	private AlbumService albumService;

	@Autowired
	public AlbumContentBaseController(AlbumContentRepository albumContent,
									  AlbumRepository albums,
									  FolderRepository folders,
									  PictureFileRepository pictureFiles,
									  KeywordRepository keyword,
									  KeywordRelationshipsRepository keywordsRelationships,
									  FolderService folderService,
									  AlbumService albumService
	) {
		super(albums, folders, pictureFiles, keyword, keywordsRelationships, folderService);
		this.albumContent = albumContent;
		this.albumService = albumService;
	}


	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	protected ModelAndView showAlbumContent(Album album,
										  @ModelAttribute CollectionRequestDTO requestDTO,
										  @PathVariable("id") long id,
										  Map<String, Object> model,
										  String template
	) {

		ModelAndView mav = new ModelAndView(template);

		final Comparator<PictureFile> defaultSort = (item1, item2 ) -> { return
			getSortOrder(this.albumContent, album, item1) - getSortOrder(this.albumContent, album, item2);
		};

		Comparator<PictureFile> orderBy = getOrderComparator(requestDTO, defaultSort);

		Collection<PictureFile> content = this.albumContent.findByAlbumId(id).stream()
			.filter( item -> item.getPictureFile().getTitle().contains(requestDTO.getSearch()))
			.sorted( (item1, item2 ) -> orderBy.compare(item1.getPictureFile(), item2.getPictureFile()) )
			.map(AlbumContent::getPictureFile)
			.collect(Collectors.toList());

		mav.addObject(album);

		// Convert the collection to a map with picture IDs as keys and Picture objects as values
		Map<Integer, PictureFile> pictureMap = content.stream()
			.collect(Collectors.toMap(
				PictureFile::getId,
				picture -> picture
			));

		loadThumbnailsAndKeywords(pictureMap, model);

		model.put("id", album.getId());
		model.put("startDate", requestDTO.getFromDate());
		model.put("endDate", requestDTO.getToDate());
		model.put("sort", requestDTO.getSort());
		model.put("search", requestDTO.getSearch());
		model.put("albums", this.albums.findAll());
		model.put("folders", this.folders.findAll());

		model.put("collection", content);
		model.put("link_params", "");

		return mav;
	}

	@Override
	public void close() throws Exception {

	}
}
