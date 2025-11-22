package org.springframework.samples.homepix.portfolio.folder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.homepix.portfolio.collection.DateUpdateResultDto;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileService;
import org.springframework.samples.homepix.portfolio.folder.*;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/folders")
@Secured("ROLE_ADMIN")
public class FolderRestController {

	@Autowired
	FolderRepository folderRepository;

	@Autowired
	FolderController folderController;

	@Autowired
	PictureFileService pictureFileService;

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

	@GetMapping("/names")
	public List<String> getPictureNamesForFolder() throws Exception {

		Collection<Folder> folders = this.folderRepository.findAll();

		return folders.stream()
			.map(Folder::getName)
			.sorted()
			.collect(Collectors.toList());
	}

	@GetMapping("/{name}/ids")
	public List<String> getPictureIdsForFolder(@PathVariable("name") String name) throws Exception {

		String bucketName = "picture-files";
		S3Client s3Client = folderController.getS3Clent();

		Collection<Folder> folders = this.folderRepository.findByName(name);

		return folders.stream()
			.flatMap(folder ->
				pictureFileRepository.findByFolderName(folder.getName())
					.stream()
					.map(picture->picture.getId().toString())
			)
			.collect(Collectors.toList());
	}

	@GetMapping("/pictures-without-dates")
	public List<FolderPicturesDto> getPicturesWithoutDates() {

		List<Folder> folders = folderRepository.findAll()
			.stream().sorted(Comparator.comparing(Folder::getName))
			.collect(Collectors.toList());

		List<String> folderNames = folders.stream()
			.map(Folder::getName)
			.collect(Collectors.toList());

		List<PictureFile> allPictures =
			pictureFileRepository.findByFolderNameIn(folderNames);

		Map<String, List<String>> folderToIds = allPictures.stream()
			.filter(picture-> {

				LocalDateTime dateTime = picture.getTaken_on();

				return dateTime == null
					|| "2025-11-18".equals(dateTime.toString().substring(0, 10))
					|| "1970-01-01T00:00".equals(dateTime.toString());
			})
			.collect(Collectors.groupingBy(
				PictureFile::getFolderName,
				Collectors.mapping(PictureFile::getFilename, Collectors.toList())
			));

		return folders.stream()
			.map(folder -> new FolderPicturesDto(
				folder.getName(),
				folderToIds.getOrDefault(folder.getName(), List.of())
			))
			.filter(dto -> !dto.pictureFilenames.isEmpty())
			.collect(Collectors.toList());
	}

	@GetMapping("/folders-with-picture-ids")
	public List<FolderPicturesDto> getFoldersWithPictureIds() {

		List<Folder> folders = folderRepository.findAll()
			.stream().sorted(Comparator.comparing(Folder::getName))
			.collect(Collectors.toList());

		List<String> folderNames = folders.stream()
			.map(Folder::getName)
			.collect(Collectors.toList());

		List<PictureFile> allPictures =
			pictureFileRepository.findByFolderNameIn(folderNames);

		Map<String, List<String>> folderToIds = allPictures.stream()
			.collect(Collectors.groupingBy(
				PictureFile::getFolderName,
				Collectors.mapping(p -> p.getId().toString(), Collectors.toList())
			));

		return folders.stream()
			.map(folder -> new FolderPicturesDto(
				folder.getName(),
				folderToIds.getOrDefault(folder.getName(), List.of())
			))
			.collect(Collectors.toList());
	}

	@PostMapping("/folders-with-picture-ids/update-dates-from-exif")
	public DateUpdateResultDto updateDatesFromExif(@RequestBody List<FolderPicturesDto> foldersWithPictureIds) throws IOException {

        // Flatten all picture IDs from the JSON we just generated
        List<String> pictureIds = foldersWithPictureIds.stream()
            .flatMap(folderDto -> folderDto.pictureFilenames.stream())
			.collect(Collectors.toList());

        return pictureFileService.updateDatesFromExif(folderController, pictureIds);
    }
}
