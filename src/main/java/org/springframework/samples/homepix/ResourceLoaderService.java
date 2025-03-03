package org.springframework.samples.homepix;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class ResourceLoaderService {

    private final ResourceLoader resourceLoader;

    public ResourceLoaderService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String readFile(String path) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:/static" + path);
        return new String(Files.readAllBytes(Paths.get(resource.getURI())));
    }

	public Map<String, String> getStandardResources() throws IOException {

		Map<String, String> resources = new HashMap<>();

		resources.put("/dist/bundle.js",readFile( "/dist/bundle.js"));
		resources.put("/dist/critical-layout.css", readFile("/dist/critical-layout.css"));
		resources.put("/dist/critical-welcome.css", readFile("/dist/critical-welcome.css"));
		resources.put("/dist/styles.css", readFile("/dist/styles.css"));

		return resources;
	}
}
