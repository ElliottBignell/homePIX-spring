package org.springframework.samples.homepix.sales;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class PricingService {

	@Autowired
    private final PricingProperties pricingProperties;

    private static final int SCALE = 2; // cents precision

    private long calculateDeliveredPixels(PictureFile picture, ImageResolution resolution) {

        int originalWidth = picture.getWidth();
        int originalHeight = picture.getHeight();

        if (resolution == ImageResolution.ORIGINAL) {
            return (long) originalWidth * originalHeight;
        }

        int targetWidth = Math.min(resolution.getWidth(), originalWidth);
        double scale = (double) targetWidth / originalWidth;
        int targetHeight = (int) Math.round(originalHeight * scale);

        return (long) targetWidth * targetHeight;
    }

    public BigDecimal calculatePrice(
            PricingTier tier,
            long width,
            long height
    ) {
		PricingProperties.TierConfig config =
			pricingProperties.getTiers().get(tier);

		if (config == null) {
			throw new IllegalArgumentException("Unknown pricing tier: " + tier);
		}

		long pixels = width * height;

		BigDecimal megapixels = BigDecimal.valueOf(pixels)
			.divide(BigDecimal.valueOf(1_000_000), 4, RoundingMode.HALF_UP);

		// Flat price per tier (as you requested)
		return config.getPriceChf();
    }
}
