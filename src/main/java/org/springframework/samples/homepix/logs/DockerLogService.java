package org.springframework.samples.homepix.logs;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class DockerLogService {

	private final WebClient webClient;

	public DockerLogService() {
		this.webClient = WebClient.create("http://172.17.0.1:2375"); // Adjust IP if necessary
	}

	public String getContainerLogs(String containerId, int tail) {
		try {
			return webClient.get()
				.uri(uriBuilder -> uriBuilder
					.path("/containers/{id}/logs")
					.queryParam("stdout", "true")
					.queryParam("stderr", "true")
					.queryParam("tail", tail) // Fetch the last `tail` lines
					.build(containerId))
				.retrieve()
				.bodyToMono(String.class)
				.block();
		} catch (Exception e) {
			e.printStackTrace();
			return "Error retrieving logs: " + e.getMessage();
		}
	}
}

