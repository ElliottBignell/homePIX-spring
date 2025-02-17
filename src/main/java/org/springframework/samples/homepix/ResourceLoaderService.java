package org.springframework.samples.homepix;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
}
