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
package org.springframework.samples.homepix.portfolio;

import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRepository;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Elliott Bignell
 */
@Controller
class AboutController extends PaginationController {

	private static final String ABOUT_FORM = "picture/about";

	AboutController(AlbumRepository albums,
					FolderRepository folders,
					PictureFileRepository pictureFiles,
					KeywordRepository keyword,
					KeywordRelationshipsRepository keywordsRelationships
	) {
		super(albums, folders, pictureFiles, keyword, keywordsRelationships);
	}

	@GetMapping("/about/")
	public String aboutSLash(Album album, BindingResult result, Map<String, Object> model) {
		return about(album, result, model);
	}

	@GetMapping("/about")
	public String about(Album album, BindingResult result, Map<String, Object> model) {

		List<String> dummies = Stream.of(new File("..").listFiles()).map(File::getName).sorted()
				.collect(Collectors.toList());

		String dir = System.getProperty("user.dir");
		model.put("working_directory", dir);
		return ABOUT_FORM;
	}

	@Override
	public void close() throws Exception {

	}

}
