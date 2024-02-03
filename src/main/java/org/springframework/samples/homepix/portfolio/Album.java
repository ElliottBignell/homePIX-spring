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
package org.springframework.samples.homepix.portfolio;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.style.ToStringCreator;
import org.springframework.samples.homepix.model.BaseEntity;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;

/**
 * Simple JavaBean domain object representing an album.
 *
 * @author Elliott Bignell
 */
@Getter
@Setter
@Entity
@Table(name = "albums")
public class Album {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;

	@Column(name = "name")
	@NotBlank(message = "Album name is mandatory")
	@NotEmpty
	private String name;

	@Column(name = "picture_count")
	private int count;

	@Column(name = "thumbnail_id")
	private int thumbnail_id;

	private PictureFile thumbnail;

	protected void setPictureFilesInternal(List<PictureFile> pictureFiles) {
		// this.pictureFiles = pictureFiles;
	}

	public void addPictureFile(PictureFile pictureFile) {

		/*
		 * Optional<PictureFile> newFile =
		 * this.pictureFileRepository.findById(pictureFile.getId());
		 *
		 * if (newFile.isEmpty()) { pictureFileRepository.save(newFile.get()); }
		 *
		 * PictureFile existingFile = this.albumContent.stream() .filter(entry ->
		 * entry.getPictureFile().getId() == pictureFile.getId())
		 * .map(AlbumContent::getPictureFile).findAny().orElse(null);
		 *
		 * if (null == existingFile) { getPictureFilesInternal().add(pictureFile); }
		 */
	}

	public void deletePictureFile(PictureFile pictureFile) {

		/*
		 * PictureFile existingFile = this.albumContent.stream() .filter(entry ->
		 * entry.getPictureFile().getId() == pictureFile.getId())
		 * .map(AlbumContent::getPictureFile).findAny().orElse(null);
		 *
		 * if (null != existingFile) { this.albumContent.removeIf(item ->
		 * item.getPictureFile().getId() == pictureFile.getId()); }
		 */
		// pictureFile.setOwner(this);
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("id", this.getId()).append("name", this.getName()).toString();
	}

	public LocalDate getLastModifiedDate() {
		return java.time.LocalDate.now(); // TODO: Add a date to the album
	}
}
