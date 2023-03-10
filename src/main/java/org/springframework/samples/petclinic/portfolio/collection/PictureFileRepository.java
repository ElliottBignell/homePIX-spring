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
package org.springframework.samples.petclinic.portfolio.collection;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository class for <code>PictureFile</code> domain objects All method names are compliant
 * with Spring Data naming conventions so this interface can easily be extended for Spring
 * Data. See:
 * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 */
public interface PictureFileRepository extends Repository<PictureFile, Integer> {

	/**
	 * Retrieve all {@link PictureFileType}s from the data store.
	 * @return a PictureCollection of {@link PictureFileType}s.
	 */
	@Query("SELECT ptype FROM PictureFileType ptype ORDER BY ptype.name")
	@Transactional(readOnly = true)
	List<PictureFileType> findPictureFileTypes();

	/**
	 * Retrieve a {@link PictureFile} from the data store by id.
	 * @param id the id to search for
	 * @return the {@link PictureFile} if found
	 */
	@Transactional(readOnly = true)
	PictureFile findById(Integer id);

	/**
	 * Save a {@link PictureFile} to the data store, either inserting or updating it.
	 * @param pictureFile the {@link PictureFile} to save
	 */
	void save(PictureFile pictureFile);

}
