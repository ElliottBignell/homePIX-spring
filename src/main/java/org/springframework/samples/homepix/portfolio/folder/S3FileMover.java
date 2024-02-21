package org.springframework.samples.homepix.portfolio.folder;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

public class S3FileMover {

	private S3Client s3Client;

	public S3FileMover(S3Client s3Client) {
		this.s3Client = s3Client;
	}

	public void moveFile(String bucketName, String originalKey, String newKey) {

		// Copy the original file to the new location
		s3Client.copyObject(CopyObjectRequest.builder()
			.sourceBucket(bucketName)
			.sourceKey(originalKey)
			.destinationBucket(bucketName)
			.destinationKey(newKey)
			.build());

		// Delete the original file
		s3Client.deleteObject(DeleteObjectRequest.builder()
			.bucket(bucketName)
			.key(originalKey)
			.build());
	}

	public static void main(String[] args) {
		S3Client s3Client = S3Client.builder()
			// Configuration details (e.g., region, credentials) go here
			.build();

		S3FileMover fileMover = new S3FileMover(s3Client);

		String bucketName = "your-bucket-name";
		String originalImagePath = "jpegs/Glarus/Glarus 6.jpg";
		String newImagePath = "jpegs/Grindelwald/Glarus 6.jpg";
		String originalThumbnailPath = "jpegs/Glarus/200px/Glarus 6_200px.jpg";
		String newThumbnailPath = "jpegs/Grindelwald/200px/Glarus 6_200px.jpg";

		// Move the image
		fileMover.moveFile(bucketName, originalImagePath, newImagePath);

		// Move the thumbnail
		fileMover.moveFile(bucketName, originalThumbnailPath, newThumbnailPath);
	}
}

