package org.springframework.samples.homepix.portfolio;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.portfolio.album.Album;
import org.springframework.samples.homepix.portfolio.album.AlbumService;
import org.springframework.samples.homepix.portfolio.folder.Folder;
import org.springframework.samples.homepix.portfolio.folder.FolderService;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;

@Aspect
@Component
public class NavbarAspect {

    @Autowired
    private AlbumService albumService; // Assume this service fetches the album data

    @Autowired
    private FolderService folderService; // Assume this service fetches the folder data

	@Before("execution(* org.springframework.samples.homepix.portfolio.*.*Controller.*(..)) @annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void addNavbarDataToModel(JoinPoint joinPoint) {

		if (joinPoint.getArgs() != null) {

			for (Object obj : joinPoint.getArgs()) {

				if (obj instanceof Model || obj instanceof Map) {

					List<Album> albums = albumService.getSortedAlbums();
					List<Folder> folders = folderService.getSortedFolders();

					if (obj instanceof Model) {
						putInModel((Model) obj, albums, folders);
					}
					else if (obj instanceof Map) {
						putInMap((Map<String, Object>) obj, albums, folders);
					}
					break;
				}
			}
		}
    }

	private void putInModel(Model model, List<Album> albums, List<Folder> folders) {

		model.addAttribute("albums", albums);
		model.addAttribute("folders", folders);
	}

	private void putInMap(Map<String, Object> model, List<Album> albums, List<Folder> folders) {

		model.put("albums", albums);
		model.put("folders", folders);
	}
}
