package org.springframework.samples.homepix.sales;

import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.folder.FolderController;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArchiveService {

	@Autowired
	FolderController folderController;

    @Value("${homepix.s3.bucket}")
    private String bucket;

    public String createAndUploadArchive(String username, List<PictureFile> pictures) throws IOException {

        String key = "downloads/" + username + "/" + System.currentTimeMillis() + ".tar.gz";

        byte[] tarGzBytes = createTarGzArchive(pictures);

        // Upload to S3
        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("application/gzip")
                .build();

		S3Client s3Client = folderController.getS3Clent();

		s3Client.putObject(put, RequestBody.fromBytes(tarGzBytes));

        return key; // caller converts to a download URL
    }

    private byte[] createTarGzArchive(List<PictureFile> pictures) throws IOException
	{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GzipCompressorOutputStream gzipOut = new GzipCompressorOutputStream(baos);
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(gzipOut);
        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

        for (PictureFile p : pictures) {

            String s3Key = getPathFromPictureFile(p);

            // Download file bytes from S3
            byte[] fileBytes = downloadFromS3(s3Key);
            String fileName = p.getFilename(); // TODO: Make size-dependent

            TarArchiveEntry entry = new TarArchiveEntry(fileName);
            entry.setSize(fileBytes.length);
            tarOut.putArchiveEntry(entry);
            tarOut.write(fileBytes);
            tarOut.closeArchiveEntry();
        }

        tarOut.finish();
        tarOut.close();
        gzipOut.close();

        return baos.toByteArray();
    }

    public byte[] downloadFromS3(String key) {
        GetObjectRequest get = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

		folderController.initialiseS3Client();

		S3Client s3Client = folderController.getS3Clent();

		ResponseBytes<GetObjectResponse> bytes =
                s3Client.getObjectAsBytes(get);

        return bytes.asByteArray();
    }

    private String getPathFromPictureFile(PictureFile p) {

		String filename = p.getFilename();
		String folder = p.getFolderName();

        // Your existing function
        return "jpegs/" + folder + "/watermark/" + filename; //p.getS3Path();
    }
}
