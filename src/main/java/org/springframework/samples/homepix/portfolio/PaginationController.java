package org.springframework.samples.homepix.portfolio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.portfolio.calendar.Calendar;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PaginationController {

	protected Pagination pagination;

	protected Calendar calendar;

	protected final AlbumRepository albums;

	protected final FolderRepository folders;

	protected final PictureFileRepository pictureFiles;

	@Autowired
	protected PaginationController(AlbumRepository albums, FolderRepository folders,
			PictureFileRepository pictureFiles) {
		this.albums = albums;
		this.folders = folders;
		this.pictureFiles = pictureFiles;
		pagination = new Pagination();
		calendar = new Calendar();
	}

	protected void addParams(int pictureId, String filename, Iterable<PictureFile> pictureFiles,
			Map<String, Object> model, boolean byID) {

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
	Iterable<Album> findAllAlbums() {
		return this.albums.findAll();
	}

	@ModelAttribute(name = "folders")
	Collection<Folder> findAllFolders() {

		load();
		return this.folders.findAll();
	}
	protected String loadFolders(Folder folder, BindingResult result, Map<String, Object> model) {

		// allow parameterless GET request for /folders to return all records
		if (folder.getName() == null) {

			load();
			folder.setName(""); // empty string signifies broadest possible search
		}

		// find folders by last name
		Collection<Folder> results = this.folders.findByName(folder.getName());
		if (results.isEmpty()) {
			// no folders found
			result.rejectValue("name", "notFound", "not found");
			return "folders/findFolders";
		}
		else if (results.size() == 1) {
			// 1 folder found
			folder = results.iterator().next();
			return "redirect:/folders/" + folder.getId();
		}
		else {
			// multiple folders found
			model.put("selections", results);
			return "folders/folderList";
		}
	}

	private void load() {

		// String dir = "/mnt/homepix/jpegs/";
		String dir = "/home/elliott/SpringFramweworkGuru/homePIX-spring/src/main/resources/static/resources/images/";

		List<String> folderNames = Stream.of(new File(dir).listFiles()).filter(file -> file.isDirectory())
			.map(File::getName).sorted().collect(Collectors.toList());

		folders.deleteAll();

		for (String name : folderNames) {

			Folder item = new Folder();

			item.setName(name);
			item.setThumbnailId(36860);

			final Pattern JPEGS = Pattern.compile(".*jpg$");

			long count = Stream.of(new File(dir + name + "/jpegs/").listFiles()).filter(file -> !file.isDirectory())
				.filter(file -> JPEGS.matcher(file.getName()).find()).count();
			item.setPicture_count((int) count);

			folders.save(item);
		}
	}
}
