package org.springframework.samples.homepix.portfolio.keywords;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.portfolio.keywords.Keyword;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class KeywordService {

	@Autowired
	private KeywordRepository KeywordRepository;

	public List<Keyword> findAll() {
		// CrudRepository's findAll() returns an Iterable, convert to List
		return StreamSupport.stream(KeywordRepository.findAll().spliterator(), false)
			.collect(Collectors.toList());
	}

	public void updateKeywords(@RequestBody List<Map<String, Object>> updates) throws Exception {

		for (Map<String, Object> keywordUpdate : updates) {

			Collection<Keyword> keywords = KeywordRepository.findByContent(keywordUpdate.toString());
			if (keywords.size() == 0) {

				Keyword keyword = new Keyword();
				keyword.setWord(keywordUpdate.values().iterator().next().toString());
				this.KeywordRepository.save(keyword);
			}
		}
	}
}
