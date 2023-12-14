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
package org.springframework.samples.homepix.portfolio.collection;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.samples.homepix.model.BaseEntity;
import org.springframework.samples.homepix.portfolio.AlbumContent;
import org.springframework.samples.homepix.portfolio.Keywords;

/**
 * Simple business object representing a pet.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 */
@Getter
@Setter
@Entity
@Table(name = "picture_file")
public class PictureFile extends BaseEntity {

	@Column(name = "filename", unique = true)
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

	@Column(name = "width")
	private Integer width;

	@Column(name = "height")
	private Integer height;

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
	private LocalDateTime taken_on;

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

	@OneToMany(mappedBy = "pictureFile")
	private List<AlbumContent> albumContent;

	public String fileNameOnly() {

		File f = new File(this.filename);
		return f.getName();
	}

	public String getMediumFilename() {

		try {

			String[] parts = this.filename.split("/");
			String filename = parts[3];
			String[] bodyAndExtension = filename.split("[\\.]");

			filename = bodyAndExtension[0] + "_200px." + bodyAndExtension[1];

			String result = "/" + parts[1] + "/" + parts[2] + "/200px/" + filename;
			return result;
		}
		catch (Exception ex) {

			System.out.println(ex);
			ex.printStackTrace();

			return this.filename;
		}
	}

}
