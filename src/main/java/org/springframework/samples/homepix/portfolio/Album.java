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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.style.ToStringCreator;
import org.springframework.samples.homepix.model.BaseEntity;

import java.util.List;
import java.util.stream.Collectors;

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

	@OneToMany(mappedBy = "album")
	private List<AlbumContent> albumContent;

	private final PictureFileRepository pictureFileRepository;

	@Autowired
	public Album(PictureFileRepository pictureFileRepository) {
		this.pictureFileRepository = pictureFileRepository;
	}

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

		PictureFile thumbnail = this.albumContent.stream()
				.filter(entry -> entry.getPictureFile().getId() == this.thumbnail_id).findAny().orElse(null)
				.getPictureFile();

		if (null == thumbnail) {
			return new PictureFile();
		}

		return thumbnail;
	}

	protected List<PictureFile> getPictureFilesInternal() {

		List<PictureFile> pictureFiles = this.albumContent.stream()
				.filter(entry -> entry.getPictureFile().getId() == this.thumbnail_id).map(AlbumContent::getPictureFile)
				.collect(Collectors.toList());

		return pictureFiles;
	}

	protected void setPictureFilesInternal(List<PictureFile> pictureFiles) {
		// this.pictureFiles = pictureFiles;
	}

	public List<PictureFile> getPictureFileRepository() {
		return getPictureFilesInternal();
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

		PictureFile existingFile = this.albumContent.stream()
				.filter(entry -> entry.getPictureFile().getId() == pictureFile.getId())
				.map(AlbumContent::getPictureFile).findAny().orElse(null);

		if (null != existingFile) {
			this.albumContent.removeIf(item -> item.getPictureFile().getId() == pictureFile.getId());
		}
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
		return new ToStringCreator(this).append("id", this.getId()).append("name", this.getName()).toString();
	}

}
