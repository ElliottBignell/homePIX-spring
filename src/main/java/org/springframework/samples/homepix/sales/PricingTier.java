package org.springframework.samples.homepix.sales;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum PricingTier {

	ORIGINAL(
		new BigDecimal("1.00"),      // base price
		new BigDecimal("1.00"),        // CHF per megapixel
		-1,
		-1
	),
    MEDIUM(
        new BigDecimal("1.00"),      // base price
        new BigDecimal("1.00"),        // CHF per megapixel
		1500,
		1000
    ),
	THUMBNAIL(
        new BigDecimal("1.00"),      // base price
        new BigDecimal("1.00"),        // CHF per megapixel
		300,
		200
	);

    private final BigDecimal basePrice;
    private final BigDecimal pricePerMegapixel;

	private final int width;
	private final int height;

	PricingTier(BigDecimal basePrice, BigDecimal pricePerMegapixel, int width, int height) {
        this.basePrice = basePrice;
        this.pricePerMegapixel = pricePerMegapixel;
		this.width = width;
		this.height = height;
    }

    public BigDecimal calculatePrice(long width, long height) {

        long pixels = width * height;

        BigDecimal megapixels = BigDecimal.valueOf(pixels)
            .divide(BigDecimal.valueOf(1_000_000), 4, RoundingMode.HALF_UP);

        return basePrice.add(
            megapixels.multiply(pricePerMegapixel)
        );
    }

	public int getWidth() { return width; }
	public int getHeight() { return height; }
}

