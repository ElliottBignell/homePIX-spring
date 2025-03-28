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
package org.springframework.samples.homepix.portfolio.folder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import org.springframework.core.style.ToStringCreator;
import org.springframework.samples.homepix.model.BaseEntity;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	@Column(name = "name", unique = true)
	@NotEmpty
	private String name;

	@Column(name = "picture_count")
	private int picture_count;

	@Column(name = "thumbnail_id")
	private int thumbnail_id;

	@Getter
	@Column(name = "description")
	private String description;

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

	public String getLinkName() {
		return name.replace('_', '-').toLowerCase();
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

	public List<PictureFile> getPictureFiles(String imagePath) {
		return loadPictureFiles(imagePath);
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("id", this.getId()).append("name", this.getName()).toString();
	}

	private List<PictureFile> loadPictureFiles(String imagePath) {

		List<PictureFile> pictureFiles = new ArrayList<>();

		String dir = imagePath + this.name;

		List<String> jpegNames = Stream.of(new File(dir).listFiles()).filter(file -> !file.isDirectory())
				.filter(file -> file.getName().endsWith(".webp")).map(File::getName).sorted()
				.collect(Collectors.toList());

		int index = 0;

		for (String name : jpegNames) {

			PictureFile item = new PictureFile();

			item.setId(index++);
			item.setFilename("/images/" + this.name + '/' + name);

			try {
				item.setTitle(getExifTitle(dir + "/" + name));
			}
			catch (IOException e) {
				logger.log(Level.SEVERE, "An input/output error occurred: " + e.getMessage(), e);
			}

			// TODO: Add folder name to keywords in the controller
			/*Keywords keywords = new Keywords();
			keywords.setContent(this.name);

			KeywordRelationships relation = new KeywordRelationships();
			relation.setPictureFile(item);
			relation.setKeywords(keywords);

			item.setKeywords(keywords);*/

			pictureFiles.add(item);
		}

		return pictureFiles;
	}

	public static String getExifTitle(String path) throws IOException {

		String title = "Untitled";

		try {

			XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
			XMLEventReader reader = xmlInputFactory.createXMLEventReader(new FileInputStream(path + ".exif"));

			while (reader.hasNext()) {

				XMLEvent nextEvent = reader.nextEvent();

				if (nextEvent.isStartElement()) {

					StartElement startElement = nextEvent.asStartElement();

					if (startElement.getName().getLocalPart().equals("ImageDescription")) {

						nextEvent = reader.nextEvent();
						title = nextEvent.asCharacters().getData();
						break;
					}
				}
			}
		}
		catch (FileNotFoundException e) {
			title = "No Exif data for title";
			System.out.println("❌ Can't find EXIF file " + path);
		}
		catch (Exception e) {

			title = "Error getting EXIF data";
			logger.log(Level.SEVERE, "An error occurred: " + e.getMessage(), e);
		}

		return title;
	}

	public LocalDate getLastModifiedDate() {
		return java.time.LocalDate.now(); // TODO: Add a date to the folder
	}
}
