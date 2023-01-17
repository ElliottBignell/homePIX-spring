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

import org.springframework.core.style.ToStringCreator;
import org.springframework.samples.petclinic.model.BaseEntity;
import org.springframework.samples.petclinic.portfolio.collection.PictureFile;

/**
 * Simple JavaBean domain object representing an album.
 *
 * @author Elliottt Bignell
 */
@Entity
@Table(name = "albumcontent")
public class AlbumContent extends BaseEntity {

	@ManyToOne
	@JoinColumn(name = "album_id")
	private Album album;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "album", cascade = CascadeType.ALL)
	private Set<PictureFile> pictureFiles;

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
	}

	/**
	 * Return the PictureFile with the given name, or null if none found for this AlbumContent.
	 * @param name to test
	 * @return true if pet name is already in use
	 */
	public PictureFile getPictureFile(String name) {
		return getPictureFile(name, false);
	}

	/**
	 * Return the PictureFile with the given name, or null if none found for this AlbumContent.
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
			.toString();
	}

}
