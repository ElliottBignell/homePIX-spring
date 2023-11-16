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

import jakarta.persistence.*;
import org.springframework.core.style.ToStringCreator;
import org.springframework.samples.homepix.model.BaseEntity;

/**
 * Simple JavaBean domain object representing an collection.
 *
 * @author Elliott Bignell
 */
@Entity
public class PictureCollection extends BaseEntity {

	private String name;

	private int count;

	private int thumbnail_id;

	public String getName() {
		return this.name;
	}

	public void setName(String address) {
		this.name = address;
	}

	public int getCount() {
		return this.count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getThumbnailId() {
		return this.thumbnail_id;
	}

	public void setThumbnailId(int thumbnail_id) {
		this.thumbnail_id = thumbnail_id;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("id", this.getId()).append("name", this.getName()).toString();
	}

}
