package org.springframework.samples.homepix.portfolio.collection;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/pictures")
@Secured("ROLE_ADMIN")
public class PictureRestController {

	@Autowired
	private PictureService pictureService;

	@Autowired
	private PictureFileRepository pictureFileRepository;

	@PostMapping("/locations")
	public ResponseEntity<String> patchGPSData(@RequestBody List<Map<String, Object>> locations) throws Exception {
		try {
			pictureService.patchLocationsAsBatch(locations);
			return ResponseEntity.ok("Records updated successfully");
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Error updating records: " + e.getMessage());
		}
	}

	@PostMapping("/description/{id}")
	public ResponseEntity<String> setDescription(@RequestBody Map<String, Object> updates, @PathVariable("id") Integer id) throws Exception
	{
		try {

			Optional<PictureFile> pictureFile = pictureFileRepository.findById(id);

			if (pictureFile.isPresent()) {

				String description = (String)(updates.get("description"));

				if (description.length() > 0) {

					pictureFile.get().setTitle(description);
					pictureFile.get().setLast_modified(java.time.LocalDate.now());
					pictureFileRepository.save(pictureFile.get());
				}

				return ResponseEntity.ok("Records updated successfully");
			}
			else {
				return ResponseEntity.ok("Picture ID not found");
			}
		}
		catch (Exception e) {
			return ResponseEntity.badRequest().body("Error updating records: " + e.getMessage());
		}
	}

	@PostMapping("/location/{id}")
	public ResponseEntity<String> setLocation(@RequestBody Map<String, Object> updates, @PathVariable("id") Integer id) throws Exception {

		try {

			Optional<PictureFile> pictureFile = pictureFileRepository.findById(id);

			if (pictureFile.isPresent()) {

				Float latitude = Float.valueOf((String)(updates.get("latitude")));
				Float longitude = Float.valueOf((String)(updates.get("longitude")));

				pictureFile.get().setLongitude(longitude);
				pictureFile.get().setLatitude(latitude);
				pictureFileRepository.save(pictureFile.get());

				return ResponseEntity.ok("Records updated successfully");
			}
			else {
				return ResponseEntity.ok("Picture ID not found");
			}
		}
		catch (Exception e) {
			return ResponseEntity.badRequest().body("Error updating records: " + e.getMessage());
		}
	}

	@Transactional
	@PostMapping("/titles/replace")
	public ResponseEntity<String> editTitles(@RequestBody Map<String, Object> updates) throws Exception {

		try {

			// Retrieve the `criteria` object
			Map<String, Object> criteria = (Map<String, Object>) updates.get("criteria");
			if (criteria == null) {
				return ResponseEntity.badRequest().body("Missing 'criteria' field\n");
			}

			// Extract `titles` from criteria
			List<String> titles = (List<String>) criteria.get("titles");
			if (titles == null) {
				return ResponseEntity.badRequest().body("Missing 'titles' field in 'criteria'\n");
			}

			// Extract `specificWord` from criteria
			String specificWord = (String) criteria.get("specificWord");
			if (specificWord == null) {
				return ResponseEntity.badRequest().body("Missing 'specificWord' field in 'criteria'\n");
			}

			// Retrieve the `replacement` object
			Map<String, Object> replacement = (Map<String, Object>) updates.get("replacement");
			if (replacement == null) {
				return ResponseEntity.badRequest().body("Missing 'replacement' field\n");
			}

			// Extract `newWord` from replacement
			String newWord = (String) replacement.get("newWord");
			if (newWord == null) {
				return ResponseEntity.badRequest().body("Missing 'newWord' field in 'replacement'\n");
			}

			// Process the update logic here using titles, specificWord, and newWord
			// For example: perform database operations or title modifications.
			pictureFileRepository.replaceWords(specificWord, newWord);

			return ResponseEntity.ok("Records updated successfully\n");

		} catch (ClassCastException e) {
			return ResponseEntity.badRequest().body("Invalid data types in JSON payload: " + e.getMessage() + "\n");
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Error updating records: " + e.getMessage() + "\n");
		}
	}

	@PostMapping("/descriptions/{ids}")
	public ResponseEntity<String> setLocations(@RequestBody Map<String, Object> updates, @PathVariable("ids") String ids) throws Exception {

		try {

			final Pattern HIDE = Pattern.compile(".*[(]hide[)]$");

			String[] idArray = ids.split( "-" );

			String sStart = idArray[0];
			String sEnd = idArray[1];

			Integer start = Integer.parseInt(sStart);
			Integer end = Integer.parseInt(sEnd);

			for (int id = start; id <= end; id++) {

				Optional<PictureFile> pictureFile = pictureFileRepository.findById(id);

				if (pictureFile.isPresent()) {

					String description = (String)(updates.get("description"));

					if (Objects.equals(pictureFile.get().getTitle(), "Flower")) {

						pictureFile.get().setTitle(description);
						pictureFile.get().setLast_modified(java.time.LocalDate.now());

						Matcher hideMatcher = HIDE.matcher(description);
						if (hideMatcher.find()) {
							pictureFile.get().setIsScary(true);
						}

						//TODO: Save all pictures at once
						pictureFileRepository.save(pictureFile.get());
					}
				}
				else {
					throw new Exception("Picture ID not found");
				}
			}

			return ResponseEntity.ok("Records updated successfully\n");
		}
		catch (Exception e) {
			return ResponseEntity.badRequest().body("Error updating records: " + e.getMessage() + "\n");
		}
	}

	// Get all pictures and return as JSON
	@GetMapping("/buckets/{bucket}")
	public List<PictureFile> getAllPictures(@PathVariable("bucket") String bucket) {
		return pictureService.findByBucket(bucket);
	}

	// General method to update any fields based on JSON structure
	@PostMapping("/update")
	public ResponseEntity<String> updatePictures(@RequestBody List<Map<String, Object>> updates) {
		try {
			pictureService.updatePictures(updates);
			return ResponseEntity.ok("Records updated successfully");
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Error updating records: " + e.getMessage());
		}
	}

	// Get all pictures with their keywords
	@GetMapping("/buckets/{bucket}/deep")
	public List<Map<String, Object>> getAllPicturesWithKeywords(@PathVariable("bucket") String bucket) throws Exception {
		return pictureService.getAllPicturesWithKeywords(bucket);
	}
}
