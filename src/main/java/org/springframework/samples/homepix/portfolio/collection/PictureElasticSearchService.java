package org.springframework.samples.homepix.portfolio.collection;

import lombok.Getter;
import org.springframework.samples.homepix.portfolio.controllers.PaginationController;
import org.springframework.stereotype.Service;

import java.net.http.HttpClient;
import java.util.Base64;
import java.util.logging.Logger;

@Service
@Getter
public class PictureElasticSearchService {

	protected static final Logger logger = Logger.getLogger(PaginationController.class.getName());

	private final HttpClient httpClient;

	public PictureElasticSearchService() throws Exception {
		this.httpClient = createHttpClientWithCustomTrustStore();
	}

	private HttpClient createHttpClientWithCustomTrustStore() {

		return null;

		/*try {

			String trustStorePath = "/usr/lib/jvm/java-17-openjdk-amd64/lib/security/cacerts";
			String trustStorePassword = "changeit";

			// Load the TrustStore
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			try (FileInputStream trustStoreIS = new FileInputStream(trustStorePath)) {
				trustStore.load(trustStoreIS, trustStorePassword.toCharArray());
			}

			// Initialize TrustManagerFactory
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(trustStore);

			// Create SSLContext
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, tmf.getTrustManagers(), null);

			// Return HttpClient with custom SSLContext
			return HttpClient.newBuilder().sslContext(sslContext).build();
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "An error occurred: " + e.getMessage(), e);
		}

		return null;*/
	}

	String getEncodedCredentials() {

		String username = "elastic";
		String password = System.getenv("ELASTICSEARCH_PASSWORD"); // Accessing environment variable

		if (password == null) {

			System.err.println("ELASTICSEARCH_PASSWORD environment variable is not set.");
			return "redirect:/buckets";
		}

		return Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
	}
}
