/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.homepix.portfolio;

import java.util.Collection;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository class for <code>AlbumContent</code> domain objects All method names are
 * compliant with Spring Data naming conventions so this interface can easily be extended
 * for Spring Data. See:
 * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation
 *
 * @author Elliott Bignell
 */
@Repository
public interface AlbumContentRepository extends CrudRepository<AlbumContent, Long> {

	/**
	 * Retrieve an {@link AlbumContent} from the data store by id.
	 * @param id the id to search for
	 * @return the {@link AlbumContent} if found
	 */
	@Query("SELECT albumcontent FROM AlbumContent albumcontent left join fetch albumcontent.pictureFile WHERE albumcontent.album.id =:album_id")
	@Transactional(readOnly = true)
	Collection<AlbumContent> findByAlbumId(@Param("album_id") Long album_id);

	/**
	 * Retrieve an {@link AlbumContent} from the data store by id and {@link PictureFile }
	 * from the data store by id
	 * @param album_id the id to search for
	 * @param entry_id the id to search for
	 * @return the {@link AlbumContent} if found
	 */
	@Query("SELECT albumcontent FROM AlbumContent albumcontent left join fetch albumcontent.pictureFile left join fetch albumcontent.album WHERE albumcontent.album.id =:album_id AND  albumcontent.pictureFile.id =:entry_id")
	@Transactional(readOnly = true)
	Collection<AlbumContent> findByAlbumIdAndEntryId(@Param("album_id") Long album_id,
			@Param("entry_id") Integer entry_id);

	@Query("SELECT pictureFile FROM AlbumContent albumcontent WHERE albumcontent.album.id =:album_id ORDER BY pictureFile.id LIMIT 1")
	@Transactional(readOnly = true)
	Collection<PictureFile> findThumbnailIds(@Param("album_id") Long album_id);

	/**
	 * Retrieve an {@link AlbumContent} from the data store by sort_order.
	 * @param sort_order the sort_order to search for
	 * @return the {@link AlbumContent} if found
	 */
	@Query("SELECT albumcontent FROM AlbumContent albumcontent WHERE albumcontent.album.id=:album_id AND albumcontent.sort_order =:sort_order")
	@Transactional(readOnly = true)
	Collection<AlbumContent> findByAlbumAndSortOrder(@Param("album_id") Long album_id, @Param("sort_order") Long sort_order);

	@Query("SELECT pictureFile FROM AlbumContent albumcontent WHERE albumcontent.album.id =:album_id")
	@Transactional(readOnly = true)
	Collection<PictureFile> findPictures(@Param("album_id") Long album_id);}
