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
package org.springframework.samples.petclinic.portfolio;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.petclinic.portfolio.collection.PictureFile;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository class for <code>AlbumContent</code> domain objects All method names are compliant
 * with Spring Data naming conventions so this interface can easily be extended for Spring
 * Data. See:
 * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation
 *
 * @author Elliott Bignell
 */
public interface AlbumContentRepository extends Repository<AlbumContent, Integer> {

	/**
	 * Retrieve an {@link AlbumContent} from the data store by id.
	 * @param id the id to search for
	 * @return the {@link AlbumContent} if found
	 */
	//@Query("SELECT albumContent FROM AlbumContent albumContent left join fetch albumContent.pictureFiles WHERE albumContent.id =:id")
	//@Transactional(readOnly = true)
	//AlbumContent findById(@Param("id") Integer id);

	/**
	 * Retrieve an {@link AlbumContent} from the data store by id.
	 * @param id the id to search for
	 * @return the {@link AlbumContent} if found
	 */
	//@Query("select picture_file from (( AlbumContent INNER JOIN picture_file ON picture_file.id=AlbumContent.entry_id) INNER JOIN album ON album.id=AlbumContent.album_id")
	//@Transactional(readOnly = true)
	//List<PictureFile> findPicturesById(@Param("album_id") Integer album_id);

	/**
	 * Save an {@link AlbumContent} to the data store, either inserting or updating it.
	 * @param album the {@link AlbumContent} to save
	 */
	void save(AlbumContent album);

}
