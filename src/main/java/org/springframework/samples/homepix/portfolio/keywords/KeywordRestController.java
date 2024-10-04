package org.springframework.samples.homepix.portfolio.keywords;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.homepix.portfolio.keywords.Keyword;
import org.springframework.samples.homepix.portfolio.keywords.KeywordService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/keywords")
public class KeywordRestController {

	@Autowired
	private KeywordService keywordService;

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
}
