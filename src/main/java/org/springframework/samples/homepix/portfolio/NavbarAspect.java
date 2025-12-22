package org.springframework.samples.homepix.portfolio;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.User;
import org.springframework.samples.homepix.UserRepository;
import org.springframework.samples.homepix.portfolio.album.Album;
import org.springframework.samples.homepix.portfolio.album.AlbumRepository;
import org.springframework.samples.homepix.portfolio.album.AlbumService;
import org.springframework.samples.homepix.portfolio.folder.Folder;
import org.springframework.samples.homepix.portfolio.folder.FolderService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Aspect
@Component
public class NavbarAspect {

	@Autowired
	private AlbumService albumService; // Assume this service fetches the album data

	@Autowired
    private AlbumRepository albumRepository; // Assume this service fetches the album data

    @Autowired
    private FolderService folderService; // Assume this service fetches the folder data

    @Autowired
    private UserRepository userRepository;

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

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		boolean loggedIn =
                authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken);

        // --- 2. If logged in, load their albums ---
        if (loggedIn) {

            String username = authentication.getName();
            Optional<User> user = userRepository.findByUsername(username);
			model.addAttribute("ownAlbums", albumRepository.findByUserId(user.get().getUserId()));
        }
        else {
			model.addAttribute("ownAlbums", List.of());
        }
	}

	private void putInMap(Map<String, Object> model, List<Album> albums, List<Folder> folders) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		model.put("albums", albums);
		model.put("ownAlbums", null);
		model.put("folders", folders);
	}
}
