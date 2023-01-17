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

package org.springframework.samples.petclinic.system;

import org.springframework.samples.petclinic.portfolio.Album;
import org.springframework.samples.petclinic.portfolio.AlbumRepository;
import org.springframework.samples.petclinic.visit.VisitRepository;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection;
import java.util.Map;

@Controller
class WelcomeController {

	private final AlbumRepository albums;

	public WelcomeController(AlbumRepository clinicService) {
		this.albums = clinicService;
	}

	@GetMapping("/")
	public String welcome(Album album, BindingResult result, Map<String, Object> model) {

		Collection<Album> results = this.albums.findByName( "Slides" );
		if (results.isEmpty()) {
			// no albums found
			result.rejectValue("name", "notFound", "not found");
			return "albums/findAlbums";
		}
		else if (results.size() == 1) {
			// 1 album found
			album = results.iterator().next();
			model.put("selections", results.iterator().next());
			return "welcome";
		}
		else {
			// multiple albums found
			model.put("selections", results);
			return "albums/albumListPictorial";
		}
	}
}
