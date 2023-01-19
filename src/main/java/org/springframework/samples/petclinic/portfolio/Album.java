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

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.core.style.ToStringCreator;
import org.springframework.samples.petclinic.model.BaseEntity;
import org.springframework.samples.petclinic.portfolio.collection.PictureFile;

/**
 * Simple JavaBean domain object representing an album.
 *
 * @author Elliott Bignell
 */
@Entity
@Table(name = "albums")
public class Album extends BaseEntity {

	@Column(name = "name")
	@NotEmpty
	private String name;

	@Column(name = "picture_count")
	@NotEmpty
	private int picture_count;

	@Column(name = "thumbnail_id")
	@NotEmpty
	private int thumbnail_id;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "album", cascade = CascadeType.ALL)
	private Set<PictureFile> pictureFiles;

	public String getName() {
		return this.name;
	}

	public void setName(String address) {
		this.name = address;
	}

	public int getPicture_count() {
		return this.picture_count;
	}

	public void setPicture_count(int picture_count) {
		this.picture_count = picture_count;
	}

	public int getThumbnailId() {
		return this.thumbnail_id;
	}

	public void setThumbnailId(int thumbnail_id) {
		this.thumbnail_id = thumbnail_id;
	}

	public PictureFile getThumbnail() {

		Set<PictureFile> files = getPictureFilesInternal();

		if ( null == files || 0 == files.size() ) {
			return new PictureFile();
		}

		return files.iterator().next();
	}

	protected Set<PictureFile> getPictureFilesInternal() {
		if (this.pictureFiles == null) {
			this.pictureFiles = new HashSet<>();
		}
		return this.pictureFiles;
	}

	protected void setPictureFilesInternal(Set<PictureFile> pictureFiles) {
		this.pictureFiles = pictureFiles;
	}

	public Set<PictureFile> getPictureFiles() {
		if (this.pictureFiles == null) {
			this.pictureFiles = new HashSet<>();
		}
		return this.pictureFiles;
	}

	public void addPictureFile(PictureFile pictureFile) {
		if (pictureFile.isNew()) {
			getPictureFilesInternal().add(pictureFile);
		}
		pictureFile.setOwner(this);
	}

	/**
	 * Return the PictureFile with the given name, or null if none found for this Album.
	 * @param name to test
	 * @return true if pet name is already in use
	 */
	public PictureFile getPictureFile(String name) {
		return getPictureFile(name, false);
	}

	/**
	 * Return the PictureFile with the given name, or null if none found for this Album.
	 * @param name to test
	 * @return true if pet name is already in use
	 */
	public PictureFile getPictureFile(String name, boolean ignoreNew) {
		name = name.toLowerCase();
		for (PictureFile pictureFile : getPictureFilesInternal()) {
			if (!ignoreNew || !pictureFile.isNew()) {
				String compName = pictureFile.getTitle();
				compName = compName.toLowerCase();
				if (compName.equals(name)) {
					return pictureFile;
				}
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this)
				.append("id", this.getId())
				.append("name", this.getName())
				.toString();
	}

}
