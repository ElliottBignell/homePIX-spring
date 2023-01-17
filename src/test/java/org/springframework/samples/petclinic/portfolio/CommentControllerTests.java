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

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
<<<<<<< HEAD:src/test/java/org/springframework/samples/petclinic/owner/VisitControllerTests.java
=======
import org.springframework.samples.petclinic.portfolio.collection.PictureFile;
import org.springframework.samples.petclinic.portfolio.collection.PictureFileRepository;
import org.springframework.samples.petclinic.visit.VisitRepository;
>>>>>>> ebe21b1 (First version of port from homePIX):src/test/java/org/springframework/samples/petclinic/portfolio/CommentControllerTests.java
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for {@link CommentController}
 *
 * @author Colin But
 */
@WebMvcTest(CommentController.class)
class CommentControllerTests {

	private static final int TEST_OWNER_ID = 1;

	private static final int TEST_PET_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@MockBean
<<<<<<< HEAD:src/test/java/org/springframework/samples/petclinic/owner/VisitControllerTests.java
	private OwnerRepository owners;

	@BeforeEach
	void init() {
		Owner owner = new Owner();
		Pet pet = new Pet();
		owner.addPet(pet);
		pet.setId(TEST_PET_ID);
		given(this.owners.findById(TEST_OWNER_ID)).willReturn(owner);
=======
	private VisitRepository visits;

	@MockBean
	private PictureFileRepository pictureFiles;

	@BeforeEach
	void init() {
		given(this.pictureFiles.findById(TEST_PET_ID)).willReturn(new PictureFile());
>>>>>>> ebe21b1 (First version of port from homePIX):src/test/java/org/springframework/samples/petclinic/portfolio/CommentControllerTests.java
	}

	@Test
	void testInitNewVisitForm() throws Exception {
<<<<<<< HEAD:src/test/java/org/springframework/samples/petclinic/owner/VisitControllerTests.java
		mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID))
				.andExpect(status().isOk()).andExpect(view().name("pets/createOrUpdateVisitForm"));
=======
		mockMvc.perform(get("/albums/*/pets/{petId}/visits/new", TEST_PET_ID)).andExpect(status().isOk())
				.andExpect(view().name("pets/createOrUpdateVisitForm"));
>>>>>>> ebe21b1 (First version of port from homePIX):src/test/java/org/springframework/samples/petclinic/portfolio/CommentControllerTests.java
	}

	@Test
	void testProcessNewVisitFormSuccess() throws Exception {
<<<<<<< HEAD:src/test/java/org/springframework/samples/petclinic/owner/VisitControllerTests.java
		mockMvc.perform(post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID)
				.param("name", "George").param("description", "Visit Description"))
				.andExpect(status().is3xxRedirection()).andExpect(view().name("redirect:/owners/{ownerId}"));
=======
		mockMvc.perform(post("/albums/*/pets/{petId}/visits/new", TEST_PET_ID).param("name", "George")
				.param("description", "Visit Description")).andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/albums/{ownerId}"));
>>>>>>> ebe21b1 (First version of port from homePIX):src/test/java/org/springframework/samples/petclinic/portfolio/CommentControllerTests.java
	}

	@Test
	void testProcessNewVisitFormHasErrors() throws Exception {
<<<<<<< HEAD:src/test/java/org/springframework/samples/petclinic/owner/VisitControllerTests.java
		mockMvc.perform(
				post("/owners/{ownerId}/pets/{petId}/visits/new", TEST_OWNER_ID, TEST_PET_ID).param("name", "George"))
=======
		mockMvc.perform(post("/albums/*/pets/{petId}/visits/new", TEST_PET_ID).param("name", "George"))
>>>>>>> ebe21b1 (First version of port from homePIX):src/test/java/org/springframework/samples/petclinic/portfolio/CommentControllerTests.java
				.andExpect(model().attributeHasErrors("visit")).andExpect(status().isOk())
				.andExpect(view().name("pets/createOrUpdateVisitForm"));
	}

}
