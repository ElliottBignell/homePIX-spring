package org.springframework.samples.homepix.sales;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartPricingService {

    private final PricingService pricingService;

    public BigDecimal calculateCartTotal(List<CartItem> items) {

        return items.stream()
                .map(item ->
                        pricingService.priceForImage(
                                item.getPicture(),
                                item.getResolution()
                        )
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
