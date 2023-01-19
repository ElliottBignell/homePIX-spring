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
import org.springframework.samples.petclinic.portfolio.collection.PictureFileType;

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

	@ManyToOne
	@JoinColumn(name = "picture_id")
	private PictureFile pictureFile;

	public PictureFile getPictureFile(String name) {
		return getPictureFile(name, false);
	}

	public PictureFile getPictureFile(String name, boolean ignoreNew) {
		return null;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this)
			.append("id", this.getId())
			.toString();
	}

}
