package org.springframework.samples.homepix.portfolio.keywords;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.portfolio.album.Album;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.folder.Folder;
import org.springframework.samples.homepix.portfolio.locations.Location;
import org.springframework.samples.homepix.portfolio.locations.LocationRelationship;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
		}
		else
			newKeyword = existing.iterator().next();

		Collection<KeywordRelationships> relations = this.keywordRelationshipsRepository.findByBothIds(picture.getId(), newKeyword.getId());

		if (relations.isEmpty()) {

			KeywordRelationships relation = new KeywordRelationships();
			relation.setPictureFile(picture);
			relation.setKeyword(newKeyword);
			this.keywordRelationshipsRepository.save(relation);
		}
	}

	public void removeKeywordFromPicture(PictureFile picture, String word) {

		Collection<Keyword> existing = keywordRepository.findByContent(word.toLowerCase());

		if (!existing.isEmpty()) {

			Keyword keyword = existing.iterator().next();

			Collection<KeywordRelationships> relations = this.keywordRelationshipsRepository.findByBothIds(picture.getId(), keyword.getId());

			if (!relations.isEmpty()) {

				for (KeywordRelationships relationship : relations) {
					keywordRelationshipsRepository.delete(relationship);
				}
			}
		}
	}

	public int keyordsCount(PictureFile picture) {
		return keywordRelationshipsRepository.findByPictureId(picture.getId()).size();
	}

	@Cacheable(value = "keywordRelationshipsCache")
	public List<Object[]> findKeywordsByPictureIds(Set<Integer> pictureIds) {
		return keywordRelationshipsRepository.findKeywordsByPictureIds(pictureIds);
	}

	@Cacheable(value = "fetchKeywordMapByFilesList")
	public Map<Integer, Set<String>> fetchKeywordMap(List<PictureFile> files) {

		Set<Integer> fileIds = files.stream().map(PictureFile::getId).collect(Collectors.toSet());

		// Fetch all keyword relationships for the given picture files
		Collection<KeywordRelationships> keywordRelationshipsList = keywordRelationshipsRepository.findByPictureIds(fileIds);

		// Build a map of file IDs to associated keywords
		return keywordRelationshipsList.stream()
			.collect(Collectors.groupingBy(
				KeywordRelationships::getPictureId,
				Collectors.mapping(relationship -> relationship.getKeyword().getWord(), Collectors.toSet())
			));
	}

	@CacheEvict(value = { "keywordRelationshipsCache", "fetchKeywordMapByFilesList" }, allEntries = true)
	@Scheduled(cron = "0 0 3 * * *") // every day at 3 AM
	public void resetCache() {
		// This will clear the "folders" cache.
		// Optionally re-fetch or do nothing here;
		// next call to getSortedFolders() will reload.
	}

	@Cacheable(value = "keywords", key = "'keywords'")
	public List<String> getKeywords(Iterable<Album> albumIterable,
									 Iterable<Folder> folderIterable,
									 List<String> locationIterable) {

		List<String> names = Stream.of(
				StreamSupport.stream(albumIterable.spliterator(), false).map(Album::getName),
				StreamSupport.stream(folderIterable.spliterator(), false).map(Folder::getName),
				locationIterable.stream()
			).flatMap(s -> s) // Flatten into a single stream
			.distinct()
			.collect(Collectors.toList());

		names.add("homePIX");
		names.add("Stock");
		names.add("Licenseable");
		names.add("Photography");
		names.add("Nature");
		names.add("Landscape");
		names.add("Urban");
		names.add("Macro");
		names.add("Calendar");

		return names;
	}
}
