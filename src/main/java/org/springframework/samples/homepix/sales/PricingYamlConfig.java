package org.springframework.samples.homepix.sales;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:application-mysql-debug.yml", factory = YamlPropertySourceFactory.class)
public class PricingYamlConfig {
    // This forces the YAML to be loaded
}
