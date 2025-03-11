package org.springframework.samples.homepix;

import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Arrays;

@RestController
@RequestMapping("/debug")
public class DebugController {
    private final ApplicationContext applicationContext;

    public DebugController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @GetMapping("/mappings")
    public List<String> getMappings() {
        return Arrays.asList(applicationContext.getBeanDefinitionNames());
    }
}
