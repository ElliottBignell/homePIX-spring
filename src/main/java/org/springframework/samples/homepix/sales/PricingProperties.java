package org.springframework.samples.homepix.sales;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "pricing.base")
@Component
@Getter
@Setter
public class PricingProperties {

	private int width;
	private int height;

	// Use @ConfigurationPropertiesBinding or proper setter
	private BigDecimal priceChf;

	public long basePixelCount() {
		return (long) width * height;
	}

	@PostConstruct
	public void debug() {
		//System.out.println("Base price CHF = " + getPriceChf());
		System.out.println("Width: " + width + ", Height: " + height);
	}
}

