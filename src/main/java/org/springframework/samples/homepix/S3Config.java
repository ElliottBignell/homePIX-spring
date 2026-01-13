package org.springframework.samples.homepix;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class S3Config {

	protected static final String region = "ch-dk-2";
	protected static final String endpoint = "https://sos-ch-dk-2.exo.io";

	@Bean(destroyMethod = "close")
	public S3Client s3Client() {
		return S3Client.builder()
			.credentialsProvider(DefaultCredentialsProvider.create())
			.region(Region.of(region))
			.endpointOverride(URI.create(endpoint))
			.build();
	}
}
