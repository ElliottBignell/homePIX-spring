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
import org.springframework.samples.homepix.portfolio.folder.Folder;
import org.springframework.samples.homepix.portfolio.folder.FolderService;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRepository;
import org.springframework.samples.homepix.portfolio.album.AlbumRepository;
import org.springframework.samples.homepix.portfolio.folder.FolderRepository;
import org.springframework.samples.homepix.portfolio.PaginationController;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author Elliott Bignell
 */
@Controller
class CalendarController extends PaginationController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "calendar/createOrUpdateOwnerForm";

	private YearThumbnailRepository yearThumbnailRepository;

	@Autowired
	CalendarController(AlbumRepository albums,
					   FolderRepository folders,
					   PictureFileRepository pictureFiles,
					   KeywordRepository keyword,
					   KeywordRelationshipsRepository keywordsRelationships,
					   FolderService folderService,
					   YearThumbnailRepository yearThumbnailRepository
	) {
		super(albums, folders, pictureFiles, keyword, keywordsRelationships, folderService);
		this.yearThumbnailRepository = yearThumbnailRepository;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@GetMapping("/calendar/{year}")
	public String processFindCalendars(@PathVariable("year") long year, Calendar calendar, BindingResult result, Map<String, Object> model) {

		outerLoop:
		for (CalendarYearGroup group : this.calendar.getItems()) {

			for (CalendarYear calendarYear : group.getYears()) {

				if (calendarYear.getYear() == year) {

					if (calendarYear.getQuarters() == null || calendarYear.getQuarters().isEmpty()) {
						this.calendar.populateYear(calendarYear);
					}
					model.put("year", calendarYear);
					break outerLoop;
				}
			}
		}

		if (!model.containsKey("year")) {
			return "redirect:/";
		}

		model.put("title", "Photo calendar view for " + year);
		model.put("albums", albumService.getSortedAlbums());
		model.put("calendar", this.calendar);
		model.put("folders", folderService.getSortedFolders());

		return "calendar/calendarYear";
	}

	@GetMapping("/calendar")
	public String processFindCalendars(Calendar calendar, BindingResult result, Map<String, Object> model) {

		List<YearThumbnailMapEntry> entries = this.yearThumbnailRepository.findYearThumbnailMapEntries();

		Map<Integer, PictureFile> yearThumbnailMap = entries.stream()
			.collect(Collectors.toMap(YearThumbnailMapEntry::getYear, YearThumbnailMapEntry::getThumbnail));

		model.put("title", "Photo calendar view");
		model.put("yearThumbnailMap", yearThumbnailMap);
		model.put("years", this.calendar.getItems());
		model.put("albums", albumService.getSortedAlbums());
		model.put("folders", folderService.getSortedFolders());

		return "calendar/calendar";
	}

	@ModelAttribute("years")
	public List<CalendarYear> populateYears() {

		List<CalendarYear> years = new ArrayList<CalendarYear>();

		if (null == this.calendar) {
			this.calendar = new Calendar(this.pictureFiles);
		}

		for (String year : this.pictureFiles.findYears()) {

			try {

				if (null != year) {
					years.add(new CalendarYear(Integer.parseInt(year)));
				}
			}
			catch (Exception e) {
				logger.log(Level.SEVERE, "An error occurred: " + e.getMessage(), e);
			}
		}

		return years;
	}

	@Override
	public void close() throws Exception {

	}

	public List<PictureFile> findByDate(LocalDate date) {
		return this.pictureFiles.findByDate(date);
	}

}
