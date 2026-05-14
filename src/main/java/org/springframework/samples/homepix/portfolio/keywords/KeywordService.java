package org.springframework.samples.homepix.portfolio.keywords;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.samples.homepix.SslConfig;
import org.springframework.samples.homepix.portfolio.album.Album;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.folder.Folder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@Transactional(readOnly = true)
public class KeywordService {

	@Autowired
	private KeywordRepository keywordRepository;

	@Autowired
	private KeywordRelationshipsRepository keywordRelationshipsRepository;

    @Autowired
    private CacheManager cacheManager;

	private static final Logger logger = LoggerFactory.getLogger(SslConfig.class);

    @PostConstruct
    public void init() {
        // Clear the problematic individual caches
        Objects.requireNonNull(cacheManager.getCache("keywordsByPictureId")).clear();
        Objects.requireNonNull(cacheManager.getCache("fetchKeywordMapByFilesList")).clear();
    }

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

	// Cache individual picture file keyword lookups
	@Cacheable(value = "keywordsByPictureId")
	public Set<String> getKeywordsForPicture(Integer pictureId) {
		return keywordRelationshipsRepository.findByPictureId(pictureId)
			.stream()
			.map(kr -> kr.getKeyword().getWord())
			.collect(Collectors.toSet());
	}

	@Retryable(value = {SQLException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
	@Cacheable(value = "fetchKeywordMapByFilesList", unless = "#result == null || #result.isEmpty()")
    public Map<Integer, Set<String>> fetchKeywordMap(List<PictureFile> files) {

		try {
			if (files == null || files.isEmpty()) {
				return Collections.emptyMap();
			}

			Set<Integer> fileIds = files.stream()
				.map(PictureFile::getId)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());

			if (fileIds.isEmpty()) {
				return Collections.emptyMap();
			}

			// ONE query to rule them all - no N+1!
			// Modify your repository to accept a collection
			List<KeywordRelationships> relationships =
				keywordRelationshipsRepository.findByPictureIdIn(fileIds);

			// Build the map in memory
			return relationships.stream()
				.collect(Collectors.groupingBy(
					KeywordRelationships::getPictureId,
					Collectors.mapping(rel -> rel.getKeyword().getWord(),
						Collectors.toSet())
				));
		}
		catch (OutOfMemoryError e) {
			logger.error("❌ OOM in fetchKeywordMap for {} files", files.size(), e);
			// Clear cache and return empty
			cacheManager.getCache("fetchKeywordMapByFilesList").clear();
			return Collections.emptyMap();
		}
    }

	@CacheEvict(value = { "keywordRelationshipsCache", "keywordsByPictureId" }, allEntries = true)
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
