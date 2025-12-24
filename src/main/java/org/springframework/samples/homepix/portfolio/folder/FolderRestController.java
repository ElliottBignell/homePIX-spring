package org.springframework.samples.homepix.portfolio.folder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.homepix.portfolio.collection.*;
import org.springframework.samples.homepix.portfolio.folder.*;
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

	@Autowired
	FolderService folderService;

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
		S3Client s3Client = folderService.getS3Client();

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

		List<PictureFile> datelessPictures = allPictures.stream()
			.filter(picture-> {

				LocalDateTime dateTime = picture.getTaken_on();

				return dateTime == null
					|| "2025-11-18".equals(dateTime.toString().substring(0, 10))
					|| "1970-01-01T00:00".equals(dateTime.toString());
			})
			.collect(Collectors.toList());

		Map<String, List<String>> folderToIds = datelessPictures.stream()
			.collect(Collectors.groupingBy(
				PictureFile::getFolderName,
				Collectors.mapping(PictureFile::getFilename, Collectors.toList())
			));

		// Group PictureFile -> folder name
		Map<String, List<PictureFile>> folderToPictures = datelessPictures.stream()
			.collect(Collectors.groupingBy(PictureFile::getFolderName));

		// Build DTOs, skipping folders with no pictures
		return folders.stream()
			.map(folder -> {
				List<PictureFile> picturesForFolder =
					folderToPictures.getOrDefault(folder.getName(), List.of());

				List<PictureInfoDto> pictureDtos = picturesForFolder.stream()
					.map(p -> new PictureInfoDto(
						p.getId(),
						p.getFilename(),
						p.getTaken_on()
					))
					.collect(Collectors.toList());

				return new FolderPicturesDto(folder.getName(), pictureDtos);
			})
			.filter(dto -> !dto.pictureFilenames.isEmpty())   // omit empty folders
			.collect(Collectors.toList());
	}

	@GetMapping("/folders-with-pictures-without-dates")
	public List<FolderPicturesDto> getFoldersWithPictures() {

		List<Folder> folders = folderRepository.findAll().stream()
			.sorted(Comparator.comparing(Folder::getName))
			.collect(Collectors.toList());

		List<String> folderNames = folders.stream()
			.map(Folder::getName)
			.collect(Collectors.toList());

		List<PictureFile> allPictures =
			pictureFileRepository.findByFolderNameIn(folderNames);

		// Group PictureFile -> folder name
		Map<String, List<PictureFile>> folderToPictures = allPictures.stream()
			.collect(Collectors.groupingBy(PictureFile::getFolderName));

		// Build DTOs, skipping folders with no pictures
		return folders.stream()
			.map(folder -> {
				List<PictureFile> picturesForFolder =
					folderToPictures.getOrDefault(folder.getName(), List.of());

				List<PictureInfoDto> pictureDtos = picturesForFolder.stream()
					.filter(p-> {

						LocalDateTime dateTime = p.getTaken_on();

						return dateTime == null
							|| "2025-11-18".equals(dateTime.toString().substring(0, 10))
							|| "1970-01-01T00:00".equals(dateTime.toString());
					})
					.map(p -> new PictureInfoDto(
						p.getId(),
						p.getFilename(),
						p.getTaken_on()
					))
					.collect(Collectors.toList());

				return new FolderPicturesDto(folder.getName(), pictureDtos);
			})
			.filter(dto -> !dto.pictureFilenames.isEmpty())   // omit empty folders
			.collect(Collectors.toList());
	}

	@PostMapping("/folders-with-picture-ids/update-dates-from-exif")
	public DateUpdateResultDto updateDatesFromExif(@RequestBody List<FolderPicturesDto> foldersWithPictureIds) throws IOException {

        // Flatten all picture IDs from the JSON we just generated
        List<PictureInfoDto> pictureIds = foldersWithPictureIds.stream()
            .flatMap(folderDto -> folderDto.pictureFilenames.stream())
			.collect(Collectors.toList());

        return pictureFileService.updateDates(folderController, pictureIds);
    }
}
