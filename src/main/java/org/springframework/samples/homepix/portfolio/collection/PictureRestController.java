package org.springframework.samples.homepix.portfolio.collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pictures")
public class PictureRestController {

	@Autowired
	private PictureService pictureService;

	@PostMapping("/locations")
	public ResponseEntity<String> patchGPSData(@RequestBody List<Map<String, Object>> locations) throws Exception {
		try {
			pictureService.patchLocationsAsBatch(locations);
			return ResponseEntity.ok("Records updated successfully");
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Error updating records: " + e.getMessage());
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
