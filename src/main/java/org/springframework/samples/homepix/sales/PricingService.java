package org.springframework.samples.homepix.sales;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
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

	public BigDecimal priceForImage(PictureFile picture, ImageResolution resolution) {

        long deliveredPixels = calculateDeliveredPixels(picture, resolution);
        BigDecimal pricePerPixel = pricingProperties.getPriceChf()
                .divide(
                        BigDecimal.valueOf(pricingProperties.basePixelCount()),
                        10,
                        RoundingMode.HALF_UP
                );

        return pricePerPixel
                .multiply(BigDecimal.valueOf(deliveredPixels))
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    private long calculateDeliveredPixels(PictureFile picture, ImageResolution resolution) {

        int originalWidth = picture.getWidth();
        int originalHeight = picture.getHeight();

        if (resolution == ImageResolution.ORIGINAL) {
            return (long) originalWidth * originalHeight;
        }

        int targetWidth = Math.min(resolution.getMaxWidth(), originalWidth);
        double scale = (double) targetWidth / originalWidth;
        int targetHeight = (int) Math.round(originalHeight * scale);

        return (long) targetWidth * targetHeight;
    }
}
