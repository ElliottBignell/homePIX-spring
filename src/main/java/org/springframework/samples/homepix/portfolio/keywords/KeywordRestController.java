package org.springframework.samples.homepix.portfolio.keywords;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/keywords")
@Secured("ROLE_ADMIN")
public class KeywordRestController {

	@Autowired
	private KeywordService keywordService;

	@Autowired
	private PictureFileRepository pictureFileRepository;

	@Autowired
	private KeywordRepository keywordRepository;

	@Autowired
	private KeywordRelationshipsRepository keywordRelationshipsRepository;

	// Get all pictures and return as JSON
	@GetMapping("/")
	public List<Keyword> getAllKeywords() {
		return keywordService.findAll();
	}

	// General method to update any fields based on JSON structure
	@PostMapping("/update")
	public ResponseEntity<String> updateKeywords(@RequestBody List<Map<String, Object>> updates) {
		try {
			keywordService.updateKeywords(updates);
			return ResponseEntity.ok("Records updated successfully");
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Error updating records: " + e.getMessage());
		}
	}

	@PostMapping("/add/{id}")
	@ResponseBody
	public ResponseEntity<List<String>> addKeywords(@RequestBody Map<String, Object> updates, @PathVariable("id") Integer id) {

		String vocabulary = (String) updates.get("vocabulary");
		String[] words = vocabulary.split(",");

		List<Integer> ids = new ArrayList<>();
		ids.add(id);

		List<PictureFile> files = pictureFileRepository.findAllById(ids);

		for (PictureFile pictureFile : files) {
			for (String word : words) {
				keywordService.addKeywordToPicture(pictureFile, word.strip().toLowerCase());
			}
		}

		List<String> list = keywordRelationshipsRepository.findByPictureId(ids.iterator().next()).stream()
			.map(KeywordRelationships::getKeyword)
			.map(Keyword::getWord)
			.sorted()
			.collect(Collectors.toList());

		return ResponseEntity.ok(list);
	}

	@PostMapping("/remove/{id}")
	@ResponseBody
	public ResponseEntity<List<String>> removeKeywords(@RequestBody Map<String, Object> updates, @PathVariable("id") Integer id) {

		String vocabulary = (String) updates.get("vocabulary");
		String[] words = vocabulary.split(",");

		List<Integer> ids = new ArrayList<>();
		ids.add(id);

		List<PictureFile> files = pictureFileRepository.findAllById(ids);

		for (PictureFile pictureFile : files) {
			for (String word : words) {
				keywordService.removeKeywordFromPicture(pictureFile, word);
			}
		}

		List<String> list = keywordRelationshipsRepository.findByPictureId(ids.iterator().next()).stream()
			.map(KeywordRelationships::getKeyword)
			.map(Keyword::getWord)
			.sorted()
			.collect(Collectors.toList());

		return ResponseEntity.ok(list);
	}
}
