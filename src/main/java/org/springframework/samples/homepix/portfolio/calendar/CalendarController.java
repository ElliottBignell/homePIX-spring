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
package org.springframework.samples.homepix.portfolio.calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.portfolio.AlbumRepository;
import org.springframework.samples.homepix.portfolio.FolderRepository;
import org.springframework.samples.homepix.portfolio.PaginationController;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Elliott Bignell
 */
@Controller
class CalendarController extends PaginationController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "calendar/createOrUpdateOwnerForm";

	@Autowired
	CalendarController(AlbumRepository albums, FolderRepository folders, PictureFileRepository pictureFiles) {
		super(albums, folders, pictureFiles);
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@GetMapping("/calendar")
	public String processFindCalendars(Calendar calendar, BindingResult result, Map<String, Object> model) {
		return "calendar/calendar";
	}

	@ModelAttribute(name = "calendar")
	Calendar getCalendar() {
		return super.calendar;
	}

	@ModelAttribute("yearNames")
	public Collection<String> populateDates() {
		return this.pictureFiles.findYears();
	}

	@ModelAttribute("years")
	public List<CalendarYear> populateYears() {

		List<CalendarYear> years = new ArrayList<CalendarYear>();
		Calendar calendar = new Calendar();

		for (String year : this.pictureFiles.findYears()) {

			CalendarYear calendarYear = new CalendarYear(Integer.parseInt(year));

			calendar.populateYear(calendarYear);
			years.add(calendarYear);
		}

		return years;
	}

}