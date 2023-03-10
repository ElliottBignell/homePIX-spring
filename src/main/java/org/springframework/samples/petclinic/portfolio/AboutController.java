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
package org.springframework.samples.petclinic.portfolio;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Elliott Bignell
 */
@Controller
class AboutController extends PaginationController {

	private static final String ABOUT_FORM = "picture/about";

	@GetMapping("/about/")
	public String aboutSLash(Album album, BindingResult result, Map<String, Object> model) {
		return about( album, result, model );
	}

	@GetMapping("/about")
	public String about(Album album, BindingResult result, Map<String, Object> model) {
		model.put("pagination", super.pagination);
		return ABOUT_FORM;
	}
}
