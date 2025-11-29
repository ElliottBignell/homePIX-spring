package org.springframework.samples.homepix.portfolio.keywords;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

@Service
public class KeywordRelationshipsService {

	@Autowired
	private KeywordRelationshipsRepository keywordRelationships;

	@Cacheable(value = "getRelationshipsByPictureIds", key = "#pictureIds.stream().sorted().toList()")
	public Collection<KeywordRelationships> getRelationshipsByPictureIds(Set<Integer> pictureIds) {
		return this.keywordRelationships.findByPictureIds(pictureIds);
	}

	@CacheEvict(value = { "getRelationshipsByPictureIds" }, allEntries = true)
	@Scheduled(cron = "0 0 3 * * *") // every day at 3 AM
	public void resetCache() {
		// This will clear the "folders" cache.
		// Optionally re-fetch or do nothing here;
		// next call to getSortedFolders() will reload.
	}
}
