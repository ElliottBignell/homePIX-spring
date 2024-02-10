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
package org.springframework.samples.homepix.portfolio.organise;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.portfolio.PaginationController;
import org.springframework.samples.homepix.portfolio.album.AlbumRepository;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.samples.homepix.portfolio.folder.FolderRepository;
import org.springframework.samples.homepix.portfolio.folder.FolderService;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRepository;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import java.util.Map;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 */
@Controller
class OrganiseController extends PaginationController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "picture/picturefile_organisation.html";

	private final OrganiseRepository organiseRepository;

	@Autowired
	public OrganiseController(OrganiseRepository organiseRepository,
							  AlbumRepository albums,
							  FolderRepository folders,
							  PictureFileRepository pictureFiles,
							  KeywordRepository keyword,
							  KeywordRelationshipsRepository keywordsRelationships,
							  FolderService folderService
	) {

		super(albums, folders, pictureFiles, keyword, keywordsRelationships, folderService);
		this.organiseRepository = organiseRepository;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@Secured("ROLE_ADMIN")
	@GetMapping("/organise/")
	public String initCreationFormSLash(Map<String, Object> model) {
		return initCreationForm(model);
	}

	@Secured("ROLE_ADMIN")
	@GetMapping("/organise")
	public String initCreationForm(Map<String, Object> model) {
		Organise organise = new Organise();
		model.put("organise", organise);
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@Override
	public void close() throws Exception {

	}

}
