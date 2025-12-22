package org.springframework.samples.homepix.portfolio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.samples.homepix.portfolio.album.AlbumService;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.samples.homepix.portfolio.folder.FolderService;
import org.springframework.ui.Model;
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

    @ModelAttribute
    public void addNavbarData(Model model) {
        model.addAttribute("albums", albumService.getSortedAlbums());
        model.addAttribute("folders", folderService.getSortedFolders());
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
}
