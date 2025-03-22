package org.springframework.samples.homepix.portfolio.keywords;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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
}
