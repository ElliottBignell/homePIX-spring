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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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
 */
public interface FolderRepository extends Repository<Folder, Integer> {

	/**
	 * Retrieve {@link Folder}s from the data store by last name, returning all folders
	 * whose last name <i>starts</i> with the given name.
	 * @param name Value to search for
	 * @return a PictureCollection of matching {@link Folder}s (or an empty PictureCollection if none
	 * found)
	 */

	@Query("SELECT DISTINCT folder FROM Folder folder WHERE folder.name LIKE :name%")
	@Transactional(readOnly = true)
	Collection<Folder> findByName(@Param("name") String name);

	/**
	 * Retrieve an {@link Folder} from the data store by id.
	 * @param id the id to search for
	 * @return the {@link Folder} if found
	 */
	@Query("SELECT folder FROM Folder folder WHERE folder.id =:id")
	@Transactional(readOnly = true)
	Folder findById(@Param("id") Integer id);

	/**
	 * Save an {@link Folder} to the data store, either inserting or updating it.
	 * @param folder the {@link Folder} to save
	 */
	void save(Folder folder);

	/**
	 * Returnes all the owners from data store
	 **/
	@Query("SELECT owner FROM Owner owner")
	@Transactional(readOnly = true)
	Page<Owner> findAll(Pageable pageable);

}
