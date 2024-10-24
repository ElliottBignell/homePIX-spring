package org.springframework.samples.homepix.portfolio.keywords;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
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
	private KeywordRepository keywordRepository;

	@Autowired
	private KeywordRelationshipsRepository keywordRelationshipsRepository;

	public List<Keyword> findAll() {
		// CrudRepository's findAll() returns an Iterable, convert to List
		return StreamSupport.stream(keywordRepository.findAll().spliterator(), false)
			.collect(Collectors.toList());
	}

	public void updateKeywords(@RequestBody List<Map<String, Object>> updates) throws Exception {

		for (Map<String, Object> keywordUpdate : updates) {

			Collection<Keyword> keywords = keywordRepository.findByContent(keywordUpdate.toString());
			if (keywords.size() == 0) {

				Keyword keyword = new Keyword();
				keyword.setWord(keywordUpdate.values().iterator().next().toString());
				this.keywordRepository.save(keyword);
			}
		}
	}

	public void addKeywordToPicture(PictureFile picture, String word) {

		Collection<Keyword> existing = keywordRepository.findByContent(word.toLowerCase());
		Keyword newKeyword;

		if (existing.isEmpty()) {

			newKeyword = new Keyword();
			newKeyword.setWord(word);
			this.keywordRepository.save(newKeyword);
		} else
			newKeyword = existing.iterator().next();

		Collection<KeywordRelationships> relations = this.keywordRelationshipsRepository.findByBothIds(picture.getId(), newKeyword.getId());

		if (relations.isEmpty()) {

			KeywordRelationships relation = new KeywordRelationships();
			relation.setPictureFile(picture);
			relation.setKeyword(newKeyword);
			this.keywordRelationshipsRepository.save(relation);
		}
	}
}
