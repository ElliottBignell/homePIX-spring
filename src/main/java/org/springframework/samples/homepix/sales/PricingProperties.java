package org.springframework.samples.homepix.sales;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

@ConfigurationProperties(prefix = "pricing")
@Component
@Getter
@Setter
public class PricingProperties {

    private Map<PricingTier, TierConfig> tiers = new EnumMap<>(PricingTier.class);

	@Getter
    @Setter
    public static class TierConfig {

        private int width;
        private int height;
        private BigDecimal priceChf;

        public long pixelCount() {
            return (long) width * height;
        }

		public long basePixelCount() {
			return (long) width * height;
		}
    }

	@PostConstruct
	void debug() {
		System.out.println("Pricing tiers loaded: " + tiers.keySet());
	}
}

