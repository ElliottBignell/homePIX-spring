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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.CollectionRequestDTO;
import org.springframework.samples.homepix.portfolio.PaginationController;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.samples.homepix.portfolio.collection.PictureFileService;
import org.springframework.samples.homepix.portfolio.folder.Folder;
import org.springframework.samples.homepix.portfolio.folder.FolderRepository;
import org.springframework.samples.homepix.portfolio.folder.FolderService;
import org.springframework.samples.homepix.portfolio.keywords.Keyword;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationships;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRepository;
import org.springframework.samples.homepix.portfolio.locations.LocationRelationshipsRepository;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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

	@Autowired
	private AlbumService albumService;

	@Autowired
	private PictureFileService pictureFileService;

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

		setStructuredDataForModel(
				requestDTO,
				model,
				"homePIX photo album collection",
				"ImageGallery",
				"Album of photos about the theme of " + album.getName(),
				content,
				"homePIX, photo, landscape, travel, macro, nature, photo, sharing, portfolio, elliott, bignell, collection, folder, album"
		);

		// Fetch the KeywordRelationships for the PictureFiles.
		// This would be a method on your repository that fetches the relationships based on a collection of PictureFile IDs.
		Collection<KeywordRelationships> keywordRelationships = this.keywordRelationships.findByPictureIds(
			content.stream()
				.map(PictureFile::getId)
				.collect(Collectors.toList())
		);

		// Now create a Map<Integer, List<Keyword>> from the KeywordRelationships.
		Map<Integer, List<Keyword>> pictureKeywordsMap = content.stream()
			.collect(Collectors.toMap(
				PictureFile::getId,
				pf -> this.keywordRelationships.findByPictureId(pf.getId())
					.stream()
					.map(KeywordRelationships::getKeyword) // Assuming getKeyword() gives you the Keyword object
					.collect(Collectors.toList()),
				(existing, replacement) -> existing, // Merge function in case of duplicates
				HashMap::new // Supplier for the map
			));

		Map<Integer, String> pictureKeywordsStringMap = pictureKeywordsMap.entrySet().stream()
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				entry -> entry.getValue().stream()
					.map(Keyword::getWord) // Assuming you have a method getKeywordText() to get the keyword string
					.collect(Collectors.joining(", "))
			));

		// Now, ensure every picture ID has an entry in the map, even if it's an empty list
		content.forEach(pf -> pictureKeywordsMap.putIfAbsent(pf.getId(), new ArrayList<>()));

		model.put("title", album.getName()+ " picture album");
		model.put("id", album.getId());

		pictureFileService.applyArguments(model, requestDTO);

		model.put("albums", this.albums.findAll());
		model.put("folders", this.folders.findAll().stream()
			.sorted(Comparator.comparing(Folder::getName))
			.collect(Collectors.toList())
		);

		model.put("collection", content);
		model.put("keyword_map", pictureKeywordsMap);
		model.put("keyword_lists", pictureKeywordsStringMap);
		model.put("link_params", "");

		if (album.getThumbnail() != null) {
			model.put("fullUrl", "collection/" + album.getThumbnail_id());
			model.put("picture", album.getThumbnail());
		}
		else if (!content.isEmpty()) {
			model.put("fullUrl", "collection/" + content.iterator().next().getId());
			model.put("picture", content.iterator().next());
		}

		pictureFileService.addMapDetails(content, model);

		return mav;
	}

	@Override
	public void close() throws Exception {

	}
}
