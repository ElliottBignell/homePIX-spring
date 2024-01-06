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

package org.springframework.samples.homepix.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.CollectionRequestDTO;
import org.springframework.samples.homepix.portfolio.*;
import org.springframework.samples.homepix.portfolio.Album;
import org.springframework.samples.homepix.portfolio.AlbumContent;
import org.springframework.samples.homepix.portfolio.AlbumContentRepository;
import org.springframework.samples.homepix.portfolio.AlbumRepository;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRepository;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
class WelcomeController extends PaginationController {

	private final AlbumContentRepository albumContents;

	@Autowired
	public WelcomeController(PictureFileRepository pictureFiles,
							 AlbumRepository albums,
							 FolderRepository folders,
							 AlbumContentRepository albumContents,
							 KeywordRepository keyword,
							 KeywordRelationshipsRepository keywordsRelationships
	) {
		super(albums, folders, pictureFiles, keyword, keywordsRelationships);
		this.albumContents = albumContents;
	}

	@GetMapping("/")
	public String welcome(@ModelAttribute CollectionRequestDTO requestDTO,
						  Album album,
						  BindingResult result,
						  Map<String, Object> model
	) {
		if (!requestDTO.getSearch().equals("")) {
			return "redirect:/collection/";
		}

		long id = 0;

		Collection<Album> results = this.albums.findByName("Slides");

		if (!results.isEmpty()) {
			id = results.iterator().next().getId();
		}

		Collection<AlbumContent> contents = this.albumContents.findByAlbumId(id);

		if (!contents.isEmpty()) {

			Collection<PictureFile> slides = contents.stream().map(item -> item.getPictureFile())
					.collect(Collectors.toList());

			model.put("collection", slides);
		}

		// 1 album found
		return "welcome";
	}

	@Override
	public void close() throws Exception {

	}

	@GetMapping("/logout")
	public String logout(Album album, BindingResult result, Map<String, Object> model) {
		return "redirect:/welcome";
	}

}
