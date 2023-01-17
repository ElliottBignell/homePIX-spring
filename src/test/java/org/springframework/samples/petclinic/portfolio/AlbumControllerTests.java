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

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.List;

import org.assertj.core.util.Lists;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
<<<<<<< HEAD:src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
=======
import org.springframework.samples.petclinic.portfolio.collection.PictureFile;
import org.springframework.samples.petclinic.portfolio.collection.PictureFileType;
import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.samples.petclinic.visit.VisitRepository;
>>>>>>> ebe21b1 (First version of port from homePIX):src/test/java/org/springframework/samples/petclinic/portfolio/AlbumControllerTests.java
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for {@link AlbumController}
 *
 * @author Colin But
 */
@WebMvcTest(AlbumController.class)
class AlbumControllerTests {

	private static final int TEST_OWNER_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AlbumRepository albums;

<<<<<<< HEAD:src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java
	private Owner george() {
		Owner george = new Owner();
=======
	@MockBean
	private VisitRepository visits;

	private Album george;

	@BeforeEach
	void setup() {
		george = new Album();
>>>>>>> ebe21b1 (First version of port from homePIX):src/test/java/org/springframework/samples/petclinic/portfolio/AlbumControllerTests.java
		george.setId(TEST_OWNER_ID);
		george.setName("George");
		george.setCount(0);
		PictureFile max = new PictureFile();
		PictureFileType dog = new PictureFileType();
		dog.setName("dog");
		max.setType(dog);
<<<<<<< HEAD:src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java
		max.setName("Max");
		max.setBirthDate(LocalDate.now());
		george.addPet(max);
		max.setId(1);
		return george;
	};

	@BeforeEach
	void setup() {

		Owner george = george();
		given(this.owners.findByLastName(eq("Franklin"), any(Pageable.class)))
				.willReturn(new PageImpl<Owner>(Lists.newArrayList(george)));

		given(this.owners.findAll(any(Pageable.class))).willReturn(new PageImpl<Owner>(Lists.newArrayList(george)));

		given(this.owners.findById(TEST_OWNER_ID)).willReturn(george);
		Visit visit = new Visit();
		visit.setDate(LocalDate.now());
		george.getPet("Max").getVisits().add(visit);

=======
		george.setPictureFilesInternal(Collections.singleton(max));
		given(this.albums.findById(TEST_OWNER_ID)).willReturn(george);
		Visit visit = new Visit();
		visit.setDate(LocalDate.now());
		given(this.visits.findByPictureFileId(max.getId())).willReturn(Collections.singletonList(visit));
>>>>>>> ebe21b1 (First version of port from homePIX):src/test/java/org/springframework/samples/petclinic/portfolio/AlbumControllerTests.java
	}

	@Test
	void testInitCreationForm() throws Exception {
		mockMvc.perform(get("/albums/new")).andExpect(status().isOk()).andExpect(model().attributeExists("owner"))
				.andExpect(view().name("albums/createOrUpdateOwnerForm"));
	}

	@Test
	void testProcessCreationFormSuccess() throws Exception {
		mockMvc.perform(post("/albums/new").param("firstName", "Joe").param("lastName", "Bloggs")
				.param("address", "123 Caramel Street").param("city", "London").param("telephone", "01316761638"))
				.andExpect(status().is3xxRedirection());
	}

	@Test
	void testProcessCreationFormHasErrors() throws Exception {
		mockMvc.perform(
				post("/albums/new").param("firstName", "Joe").param("lastName", "Bloggs").param("city", "London"))
				.andExpect(status().isOk()).andExpect(model().attributeHasErrors("owner"))
				.andExpect(model().attributeHasFieldErrors("owner", "address"))
				.andExpect(model().attributeHasFieldErrors("owner", "telephone"))
				.andExpect(view().name("albums/createOrUpdateOwnerForm"));
	}

	@Test
	void testInitFindForm() throws Exception {
		mockMvc.perform(get("/albums/find")).andExpect(status().isOk()).andExpect(model().attributeExists("album"))
				.andExpect(view().name("albums/findAlbums"));
	}

	@Test
	void testProcessFindFormSuccess() throws Exception {
<<<<<<< HEAD:src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java
		Page<Owner> tasks = new PageImpl<Owner>(Lists.newArrayList(george(), new Owner()));
		Mockito.when(this.owners.findByLastName(anyString(), any(Pageable.class))).thenReturn(tasks);
		mockMvc.perform(get("/owners?page=1")).andExpect(status().isOk()).andExpect(view().name("owners/ownersList"));
=======
		given(this.albums.findByName("")).willReturn(Lists.newArrayList(george, new Album()));
		mockMvc.perform(get("/albums")).andExpect(status().isOk()).andExpect(view().name("albums/albumList"));
>>>>>>> ebe21b1 (First version of port from homePIX):src/test/java/org/springframework/samples/petclinic/portfolio/AlbumControllerTests.java
	}

	@Test
	void testProcessFindFormByLastName() throws Exception {
<<<<<<< HEAD:src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java
		Page<Owner> tasks = new PageImpl<Owner>(Lists.newArrayList(george()));
		Mockito.when(this.owners.findByLastName(eq("Franklin"), any(Pageable.class))).thenReturn(tasks);
		mockMvc.perform(get("/owners?page=1").param("lastName", "Franklin")).andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/owners/" + TEST_OWNER_ID));
=======
		given(this.albums.findByName(george.getName())).willReturn(Lists.newArrayList(george));
		mockMvc.perform(get("/albums").param("name", "Franklin")).andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/albums/" + TEST_OWNER_ID));
>>>>>>> ebe21b1 (First version of port from homePIX):src/test/java/org/springframework/samples/petclinic/portfolio/AlbumControllerTests.java
	}

	@Test
	void testProcessFindFormNoOwnersFound() throws Exception {
<<<<<<< HEAD:src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java
		Page<Owner> tasks = new PageImpl<Owner>(Lists.newArrayList());
		Mockito.when(this.owners.findByLastName(eq("Unknown Surname"), any(Pageable.class))).thenReturn(tasks);
		mockMvc.perform(get("/owners?page=1").param("lastName", "Unknown Surname")).andExpect(status().isOk())
				.andExpect(model().attributeHasFieldErrors("owner", "lastName"))
				.andExpect(model().attributeHasFieldErrorCode("owner", "lastName", "notFound"))
				.andExpect(view().name("owners/findOwners"));

=======
		mockMvc.perform(get("/albums").param("lastName", "Unknown Surname")).andExpect(status().isOk())
				.andExpect(model().attributeHasFieldErrors("owner", "lastName"))
				.andExpect(model().attributeHasFieldErrorCode("owner", "lastName", "notFound"))
				.andExpect(view().name("albums/findAlbums"));
>>>>>>> ebe21b1 (First version of port from homePIX):src/test/java/org/springframework/samples/petclinic/portfolio/AlbumControllerTests.java
	}

	@Test
	void testInitUpdateOwnerForm() throws Exception {
		mockMvc.perform(get("/albums/{ownerId}/edit", TEST_OWNER_ID)).andExpect(status().isOk())
				.andExpect(model().attributeExists("owner"))
				.andExpect(model().attribute("owner", hasProperty("lastName", is("Franklin"))))
				.andExpect(model().attribute("owner", hasProperty("firstName", is("George"))))
				.andExpect(model().attribute("owner", hasProperty("address", is("110 W. Liberty St."))))
				.andExpect(model().attribute("owner", hasProperty("city", is("Madison"))))
				.andExpect(model().attribute("owner", hasProperty("telephone", is("6085551023"))))
				.andExpect(view().name("albums/createOrUpdateOwnerForm"));
	}

	@Test
	void testProcessUpdateOwnerFormSuccess() throws Exception {
		mockMvc.perform(post("/albums/{ownerId}/edit", TEST_OWNER_ID).param("firstName", "Joe")
				.param("lastName", "Bloggs").param("address", "123 Caramel Street").param("city", "London")
				.param("telephone", "01616291589")).andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/albums/{ownerId}"));
	}

	@Test
	void testProcessUpdateOwnerFormUnchangedSuccess() throws Exception {
		mockMvc.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID)).andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Test
	void testProcessUpdateOwnerFormHasErrors() throws Exception {
<<<<<<< HEAD:src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java
		mockMvc.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID).param("firstName", "Joe")
				.param("lastName", "Bloggs").param("address", "").param("telephone", "")).andExpect(status().isOk())
=======
		mockMvc.perform(post("/albums/{ownerId}/edit", TEST_OWNER_ID).param("firstName", "Joe")
				.param("lastName", "Bloggs").param("city", "London")).andExpect(status().isOk())
>>>>>>> ebe21b1 (First version of port from homePIX):src/test/java/org/springframework/samples/petclinic/portfolio/AlbumControllerTests.java
				.andExpect(model().attributeHasErrors("owner"))
				.andExpect(model().attributeHasFieldErrors("owner", "address"))
				.andExpect(model().attributeHasFieldErrors("owner", "telephone"))
				.andExpect(view().name("albums/createOrUpdateOwnerForm"));
	}

	@Test
	void testShowOwner() throws Exception {
		mockMvc.perform(get("/albums/{ownerId}", TEST_OWNER_ID)).andExpect(status().isOk())
				.andExpect(model().attribute("owner", hasProperty("lastName", is("Franklin"))))
				.andExpect(model().attribute("owner", hasProperty("firstName", is("George"))))
				.andExpect(model().attribute("owner", hasProperty("address", is("110 W. Liberty St."))))
				.andExpect(model().attribute("owner", hasProperty("city", is("Madison"))))
				.andExpect(model().attribute("owner", hasProperty("telephone", is("6085551023"))))
				.andExpect(model().attribute("owner", hasProperty("pets", not(empty()))))
				.andExpect(model().attribute("owner", hasProperty("pets", new BaseMatcher<List<PictureFile>>() {

					@Override
					public boolean matches(Object item) {
						@SuppressWarnings("unchecked")
						List<PictureFile> pictureFiles = (List<PictureFile>) item;
						PictureFile pictureFile = pictureFiles.get(0);
						if (pictureFile.getVisits().isEmpty()) {
							return false;
						}
						return true;
					}

					@Override
					public void describeTo(Description description) {
						description.appendText("Max did not have any visits");
					}
				}))).andExpect(view().name("albums/albumDetails"));
	}

}
