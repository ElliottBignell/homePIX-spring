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

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
 * Repository class for <code>Folder</code> domain objects All method names are compliant
 * with Spring Data naming conventions so this interface can easily be extended for Spring
 * Data. See:
 * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Elliott Bignell
 */
public interface FolderRepository extends CrudRepository<Folder, Integer> {

	/**
	 * Retrieve {@link Folder}s from the data store by last name, returning all folders
	 * whose last name <i>starts</i> with the given name.
	 * @param name Value to search for
	 * @return a PictureCollection of matching {@link Folder}s (or an empty
	 * PictureCollection if none found)
	 */

	@Query("SELECT DISTINCT folder FROM Folder folder WHERE folder.name LIKE :name%")
	@Transactional(readOnly = true)
	Collection<Folder> findByName(@Param("name") String name);

	@Query("SELECT DISTINCT folder FROM Folder folder")
	@Transactional(readOnly = true)
	Collection<Folder> findAll();

}
