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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotEmpty;

import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.samples.petclinic.model.BaseEntity;
import org.springframework.samples.petclinic.portfolio.Album;
import org.springframework.samples.petclinic.portfolio.Keywords;
import org.springframework.samples.petclinic.visit.Visit;

/**
 * Simple business object representing a pet.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 */
@Entity
@Table(name = "picture_file")
public class PictureFile extends BaseEntity {

	@Column(name = "filename")
	@NotEmpty
	private String filename;

	@Column(name = "title")
	@NotEmpty
	private String title;

	@Column(name = "last_modified")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate last_modified;

	@ManyToOne
	@JoinColumn(name = "path_id")
	private PictureFileType path;

	@ManyToOne
	@JoinColumn(name = "album_id")
	private Album album;

	@ManyToOne
	@JoinColumn(name = "keywords_id")
	private Keywords keywords;

	@Column(name = "sortkey")
	private Integer sortkey;

	@Column(name = "added_on")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate added_on;

	@Column(name = "taken_on")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate taken_on;

	@ManyToOne
	@JoinColumn(name = "location")
	private PictureFileType location;

	@ManyToOne
	@JoinColumn(name = "primary_category")
	private PictureFileType primaryCategory;

	@ManyToOne
	@JoinColumn(name = "secondary_category")
	private PictureFileType secondaryCategory;

	@Column(name = "hits")
	private int hits;

	@Transient
	private Set<Visit> visits = new LinkedHashSet<>();

	public void setLast_modified(LocalDate last_modified) {
		this.last_modified = last_modified;
	}

	public String getFilename() { return this.filename; }

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getTitle() { return this.title; }

	public void setTitle(String title) {
		this.title = title;
	}

	public LocalDate getLast_modified() {
		return this.last_modified;
	}

	public PictureFileType getType() {
		return this.path;
	}

	public void setType(PictureFileType type) {
		this.path = type;
	}

	public PictureFileType getPrimaryCategory() {
		return this.primaryCategory;
	}

	public void setPrimaryCategory(PictureFileType category) {
		this.primaryCategory = category;
	}

	public PictureFileType getSecondaryCategory() {
		return this.secondaryCategory;
	}

	public void setSecondaryCategory(PictureFileType category) {
		this.secondaryCategory = category;
	}

	public int getHits() {
		return this.hits;
	}

	public void setHits(int hits) {
		this.hits = hits;
	}

	public LocalDate getTakenOn() {
		return this.taken_on;
	}

	public void setTakenOn(LocalDate date) {
		this.taken_on = date;
	}

	public PictureFileType getLocation() {
		return this.location;
	}

	public void setLocation(PictureFileType type) {
		this.location = location;
	}

	public Album getOwner() {
		return this.album;
	}

	protected void setKeywords(Keywords keywords) {
		this.keywords = keywords;
	}

	public Keywords getKeywords() {
		return this.keywords;
	}

	public void setOwner(Album album) {
		this.album = album;
	}

	protected Set<Visit> getVisitsInternal() {
		if (this.visits == null) {
			this.visits = new HashSet<>();
		}
		return this.visits;
	}

	public void setVisitsInternal(Collection<Visit> visits) {
		this.visits = new LinkedHashSet<>(visits);
	}

	public List<Visit> getVisits() {
		List<Visit> sortedVisits = new ArrayList<>(getVisitsInternal());
		PropertyComparator.sort(sortedVisits, new MutableSortDefinition("date", false, false));
		return Collections.unmodifiableList(sortedVisits);
	}

	public void addVisit(Visit visit) {
		getVisitsInternal().add(visit);
		visit.setPictureFileId(this.getId());
	}

}
