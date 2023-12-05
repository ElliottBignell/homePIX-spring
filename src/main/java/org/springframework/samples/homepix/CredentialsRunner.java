package org.springframework.samples.homepix;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CredentialsRunner implements CommandLineRunner {

	private static String accessKeyId = null;

	private static String secretKey = null;

	@Override
	public void run(String... args) throws Exception {

		// Retrieve AWS access key and secret key from command-line arguments
		for (String arg : args) {
			if (arg.startsWith("--aws.accessKeyId=")) {
				accessKeyId = arg.substring("--aws.accessKeyId=".length());
			}
			else if (arg.startsWith("--aws.secretKey=")) {
				secretKey = arg.substring("--aws.secretKey=".length());
			}
		}
	}

	public static String getAccessKeyId() {
		return accessKeyId;
	}

	public static String getSecretKey() {
		return secretKey;
	}

}
