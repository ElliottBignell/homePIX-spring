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

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository class for <code>Organise</code> domain objects All method names are compliant
 * with Spring Data naming conventions so this interface can easily be extended for Spring
 * Data. See:
 * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 */
public interface OrganiseRepository extends Repository<Organise, Integer> {

	/**
	 * Retrieve an {@link Organise} from the data store by id.
	 * @param id the id to search for
	 * @return the {@link Organise} if found
	 */
	@Query("SELECT album FROM Organise album left join fetch album.pictureFiles WHERE album.id =:id")
	@Transactional(readOnly = true)
	Organise findById(@Param("id") Integer id);

	/**
	 * Save an {@link Organise} to the data store, either inserting or updating it.
	 * @param album the {@link Organise} to save
	 */
	void save(Organise album);

}
