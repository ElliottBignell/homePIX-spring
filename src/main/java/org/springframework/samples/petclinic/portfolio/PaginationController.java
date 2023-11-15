package org.springframework.samples.petclinic.portfolio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.portfolio.calendar.Calendar;
import org.springframework.samples.petclinic.portfolio.collection.PictureFile;
import org.springframework.samples.petclinic.portfolio.collection.PictureFileRepository;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class PaginationController {

	protected Pagination pagination;

	protected Calendar calendar;

	@Autowired
	protected final AlbumRepository albums;

	@Autowired
	protected final FolderRepository folders;

	protected final PictureFileRepository pictureFiles;

	protected PaginationController(AlbumRepository albums, FolderRepository folders,
			PictureFileRepository pictureFiles) {
		this.albums = albums;
		this.folders = folders;
		this.pictureFiles = pictureFiles;
		pagination = new Pagination();
		calendar = new Calendar();
	}

	protected void addParams(int pictureId, String filename, List<PictureFile> pictureFiles, Map<String, Object> model,
			boolean byID) {

		PictureFile picture = pictureFiles.iterator().next();
		PictureFile first = picture;
		PictureFile next = picture;
		PictureFile last = next;
		PictureFile previous = last;

		for (Iterator<PictureFile> it = pictureFiles.iterator(); it.hasNext();) {
			last = it.next();
		}

		previous = last;

		Iterator<PictureFile> it = pictureFiles.iterator();

		while (it.hasNext()) {

			PictureFile f = it.next();

			if (byID && f.getId() == pictureId) {

				picture = f;
				break;
			}
			else if (!byID && f.getFilename().equals(filename)) {

				picture = f;
				break;
			}

			previous = f;
		}

		if (it.hasNext()) {
			next = it.next();
		}
		else {
			next = first;
		}

		model.put("picture", picture);
		model.put("next", next.getId());
		model.put("previous", previous.getId());
		model.put("nextFile", next.getFilename());
		model.put("previousFile", previous.getFilename());
	}

	@ModelAttribute(name = "pagination")
	Pagination getPagination() {
		return this.pagination;
	}

	@ModelAttribute(name = "albums")
	Collection<Album> findAllAlbums() {
		return this.albums.findAll();
	}

	@ModelAttribute(name = "folders")
	Collection<Folder> findAllFolders() {
		return this.folders.findAll();
	}

}
