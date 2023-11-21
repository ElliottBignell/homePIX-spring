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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.core.style.ToStringCreator;
import org.springframework.samples.homepix.model.BaseEntity;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Simple JavaBean domain object representing a folder.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Elliott Bignell
 */
@Entity
@Table(name = "folders")
public class Folder extends BaseEntity {

	@Column(name = "name")
	@NotEmpty
	private String name;

	@Column(name = "picture_count")
	private int picture_count;

	@Column(name = "thumbnail_id")
	private int thumbnail_id;

	public String getName() {
		return this.name;
	}

	public void setName(String address) {
		this.name = address;
	}

	public String getDisplayName() {

		final Pattern JS_PATTERN = Pattern.compile("(?<=[a-zA-Z])(?=[A-Z])");

		Matcher matcher = JS_PATTERN.matcher(this.name);

		String result = matcher.replaceAll(" ").replaceAll("_", " ");

		return result;
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

	public List<PictureFile> getPictureFiles() {
		return loadPictureFiles();
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("id", this.getId()).append("name", this.getName()).toString();
	}

	private List<PictureFile> loadPictureFiles() {

		List<PictureFile> pictureFiles = null;

		if (pictureFiles == null) {

			pictureFiles = new ArrayList<>();

			String dir = "/home/elliott/SpringFramweworkGuru/homePIX-spring/src/main/resources/static/resources/images/"
					+ this.name + "/jpegs";

			List<String> folderNames = Stream.of(new File(dir).listFiles()).filter(file -> !file.isDirectory())
					.map(File::getName).sorted().collect(Collectors.toList());

			int index = 0;

			for (String name : folderNames) {

				PictureFile item = new PictureFile();

				item.setId(index++);
				item.setFilename("/resources/images/" + this.name + "/jpegs" + '/' + name);
				item.setTitle(name);

				Keywords keywords = new Keywords();
				keywords.setContent(this.name);
				item.setKeywords(keywords);

				pictureFiles.add(item);
			}
		}

		return pictureFiles;
	}

}
