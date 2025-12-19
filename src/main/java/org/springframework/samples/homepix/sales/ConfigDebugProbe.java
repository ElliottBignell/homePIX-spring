package org.springframework.samples.homepix.sales;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class ConfigDebugProbe {

    private final org.springframework.core.env.Environment env;

    @PostConstruct
    void dump() {
        System.out.println("pricing.base.width = " + env.getProperty("pricing.base.width"));
        System.out.println("pricing.base.price-chf = " + env.getProperty("pricing.base.price-chf"));
		System.out.println("Active profiles: " + Arrays.toString(env.getActiveProfiles()));
    }
}
