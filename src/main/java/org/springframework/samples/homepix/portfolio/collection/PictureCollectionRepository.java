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
package org.springframework.samples.homepix.portfolio.collection;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository class for <code>PictureCollection</code> domain objects All method names are
 * compliant with Spring Data naming conventions so this interface can easily be extended
 * for Spring Data. See:
 * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation
 *
 * @author Elliott Bignell
 */
public interface PictureCollectionRepository extends CrudRepository<PictureCollection, Integer> {

	/**
	 * Retrieve {@link PictureCollection}s from the data store by last name, returning all
	 * pictureCollections whose last name <i>starts</i> with the given name.
	 * @param name Value to search for
	 * @return a PictureCollection of matching {@link PictureCollection}s (or an empty
	 * PictureCollection if none found)
	 */

	@Query("SELECT picture_file FROM PictureFile picture_file WHERE picture_file.filename =:name")
	@Transactional(readOnly = true)
	Collection<PictureCollection> findByName(@Param("name") String name);

	@Query("SELECT picture_file FROM PictureFile picture_file WHERE NOT (DATE(picture_file.taken_on) > :to OR DATE(picture_file.taken_on) < :from)")
	@Transactional(readOnly = true)
	List<PictureFile> findByDates(@Param("from") LocalDate from, @Param("to") LocalDate to);

}
