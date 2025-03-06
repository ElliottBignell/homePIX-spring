package org.springframework.samples.homepix.logs;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.samples.homepix.portfolio.controllers.PaginationController;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

@Controller
@RequestMapping("/logs")
@Secured("ROLE_ADMIN")
public class DockerLogController {

	private final DockerLogService dockerLogService;

	protected static final Logger logger = Logger.getLogger(PaginationController.class.getName());

	public DockerLogController(DockerLogService dockerLogService) {
		this.dockerLogService = dockerLogService;
	}

	@GetMapping("/docker/homepix")
	@ResponseBody
	public String getDockerLogs(HttpServletRequest request) {
		String userAgent = request.getHeader("User-Agent");
		logger.info("Request from User-Agent to getDockerLogs(/docker-logs?containerId=homepix): " + userAgent);

		// Fetch only the last 1000 lines from Docker logs
		String logs = dockerLogService.getContainerLogs("homepix", 1000);
		return (logs != null) ? logs.replace("\n", "<br>") : "No logs available.";
	}
}

