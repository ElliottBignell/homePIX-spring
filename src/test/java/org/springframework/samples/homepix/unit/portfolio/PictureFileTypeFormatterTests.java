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

package org.springframework.samples.homepix.unit.portfolio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.samples.homepix.portfolio.collection.PictureFileType;
import org.springframework.samples.homepix.portfolio.collection.PictureFileTypeFormatter;

/**
 * Test class for {@link PictureFileTypeFormatter}
 *
 * @author Colin But
 */
@ExtendWith(MockitoExtension.class)
class PictureFileTypeFormatterTests {

	@Mock
	private PictureFileRepository pictureFiles;

	private PictureFileTypeFormatter pictureFileTypeFormatter;

	@BeforeEach
	void setup() {
		this.pictureFileTypeFormatter = new PictureFileTypeFormatter(pictureFiles);
	}

	@Test
	void testPrint() {
		PictureFileType pictureFileType = new PictureFileType();
		pictureFileType.setName("Hamster");
		String petTypeName = this.pictureFileTypeFormatter.print(pictureFileType, Locale.ENGLISH);
		assertThat(petTypeName).isEqualTo("Hamster");
	}

	@Test
	void shouldParse() throws ParseException {
		given(this.pictureFiles.findPictureFileTypes()).willReturn(makePictureFileTypes());
		PictureFileType pictureFileType = pictureFileTypeFormatter.parse("Bird", Locale.ENGLISH);
		assertThat(pictureFileType.getName()).isEqualTo("Bird");
	}

	@Test
	void shouldThrowParseException() throws ParseException {
		given(this.pictureFiles.findPictureFileTypes()).willReturn(makePictureFileTypes());
		Assertions.assertThrows(ParseException.class, () -> {
			pictureFileTypeFormatter.parse("Fish", Locale.ENGLISH);
		});
	}

	/**
	 * Helper method to produce some sample pet types just for test purpose
	 * @return {@link Collection} of {@link PictureFileType}
	 */
	private List<PictureFileType> makePictureFileTypes() {
		List<PictureFileType> pictureFileTypes = new ArrayList<>();
		pictureFileTypes.add(new PictureFileType() {
			{
				setName("Dog");
			}
		});
		pictureFileTypes.add(new PictureFileType() {
			{
				setName("Bird");
			}
		});
		return pictureFileTypes;
	}

}
