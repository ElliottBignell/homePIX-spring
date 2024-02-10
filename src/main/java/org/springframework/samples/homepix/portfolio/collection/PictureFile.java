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
import org.springframework.samples.homepix.portfolio.album.AlbumContent;
import org.springframework.samples.homepix.portfolio.folder.Folder;

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

	@ManyToOne
	@JoinColumn(name = "folder")
	private Folder folder;

	@Column(name = "width")
	private Integer width;

	@Column(name = "height")
	private Integer height;

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

	@Column(name = "CameraModel")
	private String cameraModel;

	@Column(name = "ExposureTime")
	private String exposureTime;

	@Column(name = "FNumber")
	private String fNumber;

	@Column(name = "ExposureProgram")
	private String exposureProgram;

	@Column(name = "MeteringMode")
	private String meteringMode;

	@Column(name = "LightSource")
	private String lightSource;

	@Column(name = "FocalLength")
	private String focalLength;

	@Column(name = "Roles")
	private String roles;

	@OneToMany(mappedBy = "pictureFile")
	private List<AlbumContent> albumContent;

	@Column(name = "aspect_ratio")
	private Float aspect_ratio;

	public String fileNameOnly() {

		File f = new File(this.filename);
		return f.getName();
	}

	public String getMediumFilename() {

		try {

			String[] parts = this.filename.split("/");
			String filename = parts[parts.length - 1];
			String[] bodyAndExtension = filename.split("[\\.]");

			int length = bodyAndExtension.length;

			if (length > 1) {

				bodyAndExtension[length - 2] = bodyAndExtension[length - 2] + "_200px";

				filename = String.join(".", bodyAndExtension);

				if (null != this.folder) {
					return "/web-images/" + this.folder.getName() + "/200px/" + filename;
				}
				else {

					int slashIndex = this.filename.indexOf('/', 1);

					if (slashIndex > 1) {

						String folderName = this.filename.substring(1, slashIndex);

						return "/web-images/" + folderName + "/200px/" + filename;
					}
				}
			}
		}
		catch (Exception ex) {

			System.out.println(ex);
			ex.printStackTrace();
		}

		return this.filename;
	}

	public String getLargeFilename() {

		try {

			String[] parts = this.filename.split("/");
			String filename = parts[parts.length - 1];
			String[] bodyAndExtension = filename.split("[\\.]");

			filename = bodyAndExtension[0] + "." + bodyAndExtension[1];

			String result = "/web-images/" + this.folder.getName() + "/" + filename;
			return result;
		}
		catch (Exception ex) {

			System.out.println(ex);
			ex.printStackTrace();

			return this.filename;
		}
	}

	public String getDisplayFilename() {
		return this.folder.getName() + "/" + filename;
	}

	public Integer getDisplayWidth() {

		float aspectRratio = (float) width / (float) height;
		return (int) (200.0 * aspectRratio);
	}

	public Integer getDisplayHeight() {
		return 200;
	}

	public float getAspectRatio() {
		return (float)this.width / (float)this.height;
	}

	public boolean getIsScary() {
		return this.roles.equals("ROLE_ADMIN");
	}

	public void setIsScary(boolean scary) {

		if (scary) {
			this.roles = "ROLE_ADMIN";
		}
		else {
			this.roles = "ROLE_USER";
		}
	}

	public boolean isValid() {
		return (null != width && null != height);
	}
}
