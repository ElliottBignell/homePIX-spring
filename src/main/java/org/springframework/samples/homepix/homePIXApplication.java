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

package org.springframework.samples.homepix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.samples.homepix.sales.PricingProperties;

/**
 * homePIX Spring Boot Application.
 *
 * @author Dave Syer
 *
 */
@SpringBootApplication(proxyBeanMethods = false)
@EnableJpaRepositories(basePackages = "org.springframework.samples.homepix")
@EnableConfigurationProperties(PricingProperties.class)
@EnableCaching
@ConfigurationPropertiesScan
public class homePIXApplication {

	// https://api.flickr.com/services/rest/?method=flickr.photosets.getList&api_key=4653c7a47e46e9c6f0ff19388000b524&user_id=50027087@N00&format=json&nojsoncallback=1

	public static void main(String[] args) {
		SpringApplication.run(homePIXApplication.class, args);
	}

}
