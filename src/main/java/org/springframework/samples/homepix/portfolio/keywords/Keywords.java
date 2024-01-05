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
package org.springframework.samples.homepix.portfolio.keywords;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.style.ToStringCreator;
import org.springframework.samples.homepix.model.BaseEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * Simple JavaBean domain object representing an album.
 *
 * @author Elliott Bignell
 */
@Entity
@Getter
@Setter
@Table(name = "keywords")
public class Keywords extends BaseEntity {

	@Column(name = "content")
	@NotEmpty
	private String content = "";

	@Column(name = "keyword_count")
	private int keyword_count = 0;

	@Override
	public String toString() {
		return new ToStringCreator(this).append("id", this.getId()).append("content", this.getContent()).toString();
	}

	public Set<String> getItems() {

		String[] elements = content.split(",");

		Set<String> items = new HashSet<>();

		for (String keyword : elements) {
			items.add(keyword);
		}

		return items;
	}
}
