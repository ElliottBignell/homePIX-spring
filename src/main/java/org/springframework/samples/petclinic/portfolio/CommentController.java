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

import java.util.Map;

<<<<<<< HEAD:src/main/java/org/springframework/samples/petclinic/owner/VisitController.java
=======
import javax.validation.Valid;

import org.springframework.samples.petclinic.portfolio.collection.PictureFile;
import org.springframework.samples.petclinic.portfolio.collection.PictureFileRepository;
import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.samples.petclinic.visit.VisitRepository;
>>>>>>> ebe21b1 (First version of port from homePIX):src/main/java/org/springframework/samples/petclinic/portfolio/CommentController.java
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Dave Syer
 */
@Controller
class CommentController {

	private final OwnerRepository owners;

<<<<<<< HEAD:src/main/java/org/springframework/samples/petclinic/owner/VisitController.java
	public VisitController(OwnerRepository owners) {
		this.owners = owners;
=======
	private final PictureFileRepository pictureFiles;

	public CommentController(VisitRepository visits, PictureFileRepository picturefiles) {
		this.visits = visits;
		this.pictureFiles = picturefiles;
>>>>>>> ebe21b1 (First version of port from homePIX):src/main/java/org/springframework/samples/petclinic/portfolio/CommentController.java
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	/**
	 * Called before each and every @RequestMapping annotated method. 2 goals: - Make sure
	 * we always have fresh data - Since we do not use the session scope, make sure that
	 * PictureFile object always has an id (Even though id is not part of the form fields)
	 * @param petId
	 * @return PictureFile
	 */
	@ModelAttribute("visit")
<<<<<<< HEAD:src/main/java/org/springframework/samples/petclinic/owner/VisitController.java
	public Visit loadPetWithVisit(@PathVariable("ownerId") int ownerId, @PathVariable("petId") int petId,
			Map<String, Object> model) {
		Owner owner = this.owners.findById(ownerId);

		Pet pet = owner.getPet(petId);
		model.put("pet", pet);
		model.put("owner", owner);

=======
	public Visit loadPictureFileWithVisit(@PathVariable("petId") int petId, Map<String, Object> model) {
		PictureFile pictureFile = this.pictureFiles.findById(petId);
		pictureFile.setVisitsInternal(this.visits.findByPictureFileId(petId));
		model.put("pictureFile", pictureFile);
>>>>>>> ebe21b1 (First version of port from homePIX):src/main/java/org/springframework/samples/petclinic/portfolio/CommentController.java
		Visit visit = new Visit();
		pictureFile.addVisit(visit);
		return visit;
	}

<<<<<<< HEAD:src/main/java/org/springframework/samples/petclinic/owner/VisitController.java
	// Spring MVC calls method loadPetWithVisit(...) before initNewVisitForm is
	// called
	@GetMapping("/owners/{ownerId}/pets/{petId}/visits/new")
	public String initNewVisitForm() {
		return "pets/createOrUpdateVisitForm";
	}

	// Spring MVC calls method loadPetWithVisit(...) before processNewVisitForm is
	// called
	@PostMapping("/owners/{ownerId}/pets/{petId}/visits/new")
	public String processNewVisitForm(@ModelAttribute Owner owner, @PathVariable int petId, @Valid Visit visit,
			BindingResult result) {
		if (result.hasErrors()) {
			return "pets/createOrUpdateVisitForm";
		}

		owner.addVisit(petId, visit);
		this.owners.save(owner);
		return "redirect:/owners/{ownerId}";
=======
	// Spring MVC calls method loadPictureFileWithVisit(...) before initNewVisitForm is called
	@GetMapping("/albums/*/pets/{petId}/visits/new")
	public String initNewVisitForm(@PathVariable("petId") int petId, Map<String, Object> model) {
		return "pets/createOrUpdateVisitForm";
	}

	// Spring MVC calls method loadPictureFileWithVisit(...) before processNewVisitForm is called
	@PostMapping("/albums/{ownerId}/pets/{petId}/visits/new")
	public String processNewVisitForm(@Valid Visit visit, BindingResult result) {
		if (result.hasErrors()) {
			return "pets/createOrUpdateVisitForm";
		}
		else {
			this.visits.save(visit);
			return "redirect:/albums/{ownerId}";
		}
>>>>>>> ebe21b1 (First version of port from homePIX):src/main/java/org/springframework/samples/petclinic/portfolio/CommentController.java
	}

}
