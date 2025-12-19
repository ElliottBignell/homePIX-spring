package org.springframework.samples.homepix.sales;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum PricingTier {

    BASE(
        new BigDecimal("10.00"),      // base price
        new BigDecimal("1.00")        // CHF per megapixel
    ),
	THUMBNAIL(
        new BigDecimal("1.00"),      // base price
        new BigDecimal("0.10")        // CHF per megapixel
	);

    private final BigDecimal basePrice;
    private final BigDecimal pricePerMegapixel;

    PricingTier(BigDecimal basePrice, BigDecimal pricePerMegapixel) {
        this.basePrice = basePrice;
        this.pricePerMegapixel = pricePerMegapixel;
    }

    public BigDecimal calculatePrice(long width, long height) {

        long pixels = width * height;

        BigDecimal megapixels = BigDecimal.valueOf(pixels)
            .divide(BigDecimal.valueOf(1_000_000), 4, RoundingMode.HALF_UP);

        return basePrice.add(
            megapixels.multiply(pricePerMegapixel)
        );
    }
}

