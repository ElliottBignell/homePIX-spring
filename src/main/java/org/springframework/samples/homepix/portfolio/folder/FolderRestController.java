package org.springframework.samples.homepix.portfolio.folder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.folder.*;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/folders")
@Secured("ROLE_ADMIN")
public class FolderRestController {

	@Autowired
	FolderRepository folderRepository;

	@Autowired
	PictureFileRepository pictureFileRepository;

	@Autowired
	BucketController bucketController;

	@PostMapping("/move/{id}")
	public ResponseEntity<?> addKeywords(@RequestBody Map<String, Object> updates, @PathVariable("id") Integer id) {

		String folderName = (String) updates.get("folder");

		List<Integer> ids = new ArrayList<>();
		ids.add(id);

		List<PictureFile> files = pictureFileRepository.findAllById(ids);

		for (PictureFile file: files) {

			Collection<Folder> folders = folderRepository.findByName(folderName);

			String currentFileName = file.getFolderName();

			for (Folder folder : folders) {
				bucketController.movePictureToFolder(currentFileName, folderName, file);
			}

			ArrayList<String> list = new ArrayList<>();
			list.add(folderName);

			return ResponseEntity.ok(list);
		}

		return ResponseEntity.badRequest().body("Picture ID not found");
	}
}
