package org.springframework.samples.homepix.sales;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class ConfigDebugController {

    @Autowired
    private Environment env;

    @GetMapping("/debug-config")
    public Map<String, Object> debugConfig() {
        Map<String, Object> config = new HashMap<>();

        // Check if property exists
        config.put("pricing.base.width", env.getProperty("pricing.base.width"));
        config.put("pricing.base.height", env.getProperty("pricing.base.height"));
        config.put("pricing.base.price-chf", env.getProperty("pricing.base.price-chf"));

        // Check active profiles
        config.put("activeProfiles", env.getActiveProfiles());
        config.put("defaultProfiles", env.getDefaultProfiles());

        // Check if profile-specific file is being loaded
        config.put("spring.profiles.active", env.getProperty("spring.profiles.active"));

        return config;
    }

	    @GetMapping("/check-resources")
    public Map<String, Object> checkResources() throws IOException {
        Map<String, Object> result = new HashMap<>();

        // Check various possible locations
        String[] possiblePaths = {
			"application-mysql-debug.yml",
            "classpath:application-mysql-debug.yml",
			"/application-mysql-debug.yml"
        };

        for (String path : possiblePaths) {
            try {
                Resource resource = new ClassPathResource(path);
                boolean exists = resource.exists();
                result.put(path, exists);
                if (exists) {
                    result.put(path + "_content",
                        new BufferedReader(new InputStreamReader(resource.getInputStream()))
                            .lines().collect(Collectors.joining("\n")));
                }
            } catch (Exception e) {
                result.put(path + "_error", e.getMessage());
            }
        }

        // Also check what's in the resources directory
        result.put("allResources", getResourcesListing());

        return result;
    }

    private List<String> getResourcesListing() throws IOException {
        List<String> resources = new ArrayList<>();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] allResources = resolver.getResources("classpath*:*");
        for (Resource resource : allResources) {
            resources.add(resource.getFilename() + " - " + resource.getURI());
        }
        return resources;
    }
}
