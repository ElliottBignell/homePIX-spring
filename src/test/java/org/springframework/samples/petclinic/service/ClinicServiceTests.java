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

package org.springframework.samples.petclinic.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.samples.petclinic.portfolio.*;
import org.springframework.samples.petclinic.portfolio.Album;
import org.springframework.samples.petclinic.portfolio.collection.PictureFile;
import org.springframework.samples.petclinic.portfolio.collection.PictureFileRepository;
import org.springframework.samples.petclinic.portfolio.collection.PictureFileType;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test of the Service and the Repository layer.
 * <p>
 * ClinicServiceSpringDataJpaTests subclasses benefit from the following services provided
 * by the Spring TestContext Framework:
 * </p>
 * <ul>
 * <li><strong>Spring IoC container caching</strong> which spares us unnecessary set up
 * time between test execution.</li>
 * <li><strong>Dependency Injection</strong> of test fixture instances, meaning that we
 * don't need to perform application context lookups. See the use of
 * {@link Autowired @Autowired} on the <code> </code> instance variable, which uses
 * autowiring <em>by type</em>.
 * <li><strong>Transaction management</strong>, meaning each test method is executed in
 * its own transaction, which is automatically rolled back by default. Thus, even if tests
 * insert or otherwise change database state, there is no need for a teardown or cleanup
 * script.
 * <li>An {@link org.springframework.context.ApplicationContext ApplicationContext} is
 * also inherited and can be used for explicit bean lookup if necessary.</li>
 * </ul>
 *
 * @author Ken Krebs
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Dave Syer
 */
@DataJpaTest(includeFilters = @ComponentScan.Filter(Service.class))
// Ensure that if the mysql profile is active we connect to the real database:
@AutoConfigureTestDatabase(replace = Replace.NONE)
// @TestPropertySource("/application-postgres.properties")
class ClinicServiceTests {

	@Autowired
	protected AlbumRepository albums;

	@Autowired
	protected PictureFileRepository pictureFiles;

	@Autowired
	protected VisitRepository visits;

	@Autowired
	protected VetRepository vets;

	Pageable pageable;

	@Test
	void shouldFindOwnersByLastName() {
		Collection<Album> albums = this.albums.findByName("Davis");
		assertThat(albums).hasSize(2);

		albums = this.albums.findByName("Daviss");
		assertThat(albums).isEmpty();
	}

	@Test
	void shouldFindSingleOwnerWithPictureFile() {
		Album album = this.albums.findById(1);
		assertThat(album.getPictureFiles()).hasSize(1);
		assertThat(album.getPictureFiles().iterator().next().getType()).isNotNull();
		assertThat(album.getPictureFiles().iterator().next().getType().getName()).isEqualTo("cat");
	}

	@Test
	@Transactional
	void shouldInsertOwner() {
		Collection<Album> albums = this.albums.findByName("Schultz");
		int found = albums.size();

		Album album = new Album();
		album.setName("Sam");
		album.setCount(0);
		this.albums.save(album);
		assertThat(album.getId().longValue()).isNotEqualTo(0);

		albums = this.albums.findByName("Schultz");
		assertThat(albums.size()).isEqualTo(found + 1);
	}

	@Test
	@Transactional
	void shouldUpdateOwner() {
		Album album = this.albums.findById(1);

		this.albums.save(album);

		// retrieving new name from database
		album = this.albums.findById(1);
	}

	@Test
	void shouldFindPictureFileWithCorrectId() {
		PictureFile pictureFile7 = this.pictureFiles.findById(7);
		assertThat(pictureFile7.getTitle()).startsWith("Samantha");

	}

	@Test
	void shouldFindAllPictureFileTypes() {
		Collection<PictureFileType> pictureFileTypes = this.pictureFiles.findPictureFileTypes();

		PictureFileType pictureFileType1 = EntityUtils.getById(pictureFileTypes, PictureFileType.class, 1);
		assertThat(pictureFileType1.getName()).isEqualTo("cat");
		PictureFileType pictureFileType4 = EntityUtils.getById(pictureFileTypes, PictureFileType.class, 4);
		assertThat(pictureFileType4.getName()).isEqualTo("snake");
	}

	@Test
	@Transactional
	void shouldInsertPictureFileIntoDatabaseAndGenerateId() {
		Album album6 = this.albums.findById(6);
		int found = album6.getPictureFiles().size();

		PictureFile pictureFile = new PictureFile();
		pictureFile.setTitle("bowser");
		Collection<PictureFileType> types = this.pictureFiles.findPictureFileTypes();
		pictureFile.setType(EntityUtils.getById(types, PictureFileType.class, 2));
		pictureFile.setTakenOn(LocalDate.now());
		album6.addPictureFile(pictureFile);
		assertThat(album6.getPictureFiles().size()).isEqualTo(found + 1);

		this.pictureFiles.save(pictureFile);
		this.albums.save(album6);

		album6 = this.albums.findById(6);
		assertThat(album6.getPictureFiles().size()).isEqualTo(found + 1);
		// checks that id has been generated
		assertThat(pictureFile.getId()).isNotNull();
	}

	@Test
	@Transactional
	void shouldUpdatePictureFileName() throws Exception {
		PictureFile pictureFile7 = this.pictureFiles.findById(7);
		String oldName = pictureFile7.getTitle();

		String newName = oldName + "X";
		pictureFile7.setTitle(newName);
		this.pictureFiles.save(pictureFile7);

		pictureFile7 = this.pictureFiles.findById(7);
		assertThat(pictureFile7.getTitle()).isEqualTo(newName);
	}

	@Test
	void shouldFindVets() {
		Collection<Vet> vets = this.vets.findAll();

		Vet vet = EntityUtils.getById(vets, Vet.class, 3);
		assertThat(vet.getLastName()).isEqualTo("Douglas");
		assertThat(vet.getNrOfSpecialties()).isEqualTo(2);
		assertThat(vet.getSpecialties().get(0).getName()).isEqualTo("dentistry");
		assertThat(vet.getSpecialties().get(1).getName()).isEqualTo("surgery");
	}

	@Test
	@Transactional
	void shouldAddNewVisitForPictureFile() {
		PictureFile pictureFile7 = this.pictureFiles.findById(7);
		int found = pictureFile7.getVisits().size();
		Visit visit = new Visit();
		pictureFile7.addVisit(visit);
		visit.setDescription("test");
		this.visits.save(visit);
		this.pictureFiles.save(pictureFile7);

		pictureFile7 = this.pictureFiles.findById(7);
		assertThat(pictureFile7.getVisits().size()).isEqualTo(found + 1);
		assertThat(visit.getId()).isNotNull();
	}

	@Test
	void shouldFindVisitsByPictureFileId() throws Exception {
		Collection<Visit> visits = this.visits.findByPictureFileId(7);
		assertThat(visits).hasSize(2);
		Visit[] visitArr = visits.toArray(new Visit[visits.size()]);
		assertThat(visitArr[0].getDate()).isNotNull();
		assertThat(visitArr[0].getPictureFileId()).isEqualTo(7);
	}

}
