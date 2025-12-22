package org.springframework.samples.homepix.portfolio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.samples.homepix.portfolio.album.Album;
import org.springframework.samples.homepix.portfolio.album.AlbumService;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.samples.homepix.portfolio.folder.Folder;
import org.springframework.samples.homepix.portfolio.folder.FolderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class NavbarControllerAdvice {

	@Autowired
    private AlbumService albumService;

	@Autowired
    private FolderService folderService;

	@Autowired
	protected PictureFileRepository pictureFiles;

	@Cacheable("albums")
	@ModelAttribute("albums")
	protected List<Album> populateAlbums() {
		return albumService.getSortedAlbums();
	}

	@Cacheable("folders")
	@ModelAttribute("folders")
	protected List<Folder> populateFolders() {
		return folderService.getSortedFolders();
    }

	@Cacheable("yearNames")
	@ModelAttribute("yearNames")
	protected List<List<String>> populateDates() {

		List<String> years = this.pictureFiles.findYears();

		int width = 4;

		List<List<String>> table = new ArrayList<List<String>>() ;

		for (int n = 0; n < 4; n++) {
			table.add(new ArrayList<>());
		}

		int count = 0;

		for (String year : years) {
			table.get(count++ % 4).add(year);
		}

		return table;
	}

	@CacheEvict(value = { "yearNames" }, allEntries = true)
	@Scheduled(cron = "0 0 3 * * *") // every day at 3 AM
	public void resetCache() {
		// This will clear the "folders" cache.
		// Optionally re-fetch or do nothing here;
		// next call to getSortedFolders() will reload.
	}
}
