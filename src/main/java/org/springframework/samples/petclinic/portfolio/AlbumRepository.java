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

import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository class for <code>Album</code> domain objects All method names are compliant
 * with Spring Data naming conventions so this interface can easily be extended for Spring
 * Data. See:
 * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation
 *
 * @author Elliott Bignell
 */
public interface AlbumRepository extends CrudRepository<Album, Integer> {

	/**
	 * Retrieve {@link Album}s from the data store by last name, returning all albums
	 * whose last name <i>starts</i> with the given name.
	 * @param name Value to search for
	 * @return a PictureCollection of matching {@link Album}s (or an empty
	 * PictureCollection if none found)
	 */

	@Query("SELECT DISTINCT album FROM Album album WHERE album.name LIKE :name%")
	// @Query("SELECT DISTINCT album FROM Album album left join fetch album.pictureFiles
	// WHERE album.name LIKE :name%")
	@Transactional(readOnly = true)
	Collection<Album> findByName(@Param("name") String name);

	@Query("SELECT DISTINCT album FROM Album album")
	@Transactional(readOnly = true)
	Collection<Album> findAll();

}
