package org.springframework.samples.homepix.unit.portfolio.folder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.homepix.portfolio.folder.Folder;
import org.springframework.samples.homepix.portfolio.folder.FolderRepository;
import org.springframework.samples.homepix.portfolio.folder.FolderService;
import software.amazon.awssdk.services.s3.S3Client;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import software.amazon.awssdk.services.s3.model.*;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class FolderServiceTest {

	@Mock
	FolderRepository folderRepository;

	@Mock
	S3Client s3Client;

	@InjectMocks
	FolderService folderService;

	@Test
	void shouldFetchFolderAndListPictures() {

		//Folder mockFolder = new Folder("jpegs");
		//mockFolder.addPictureFile(new PictureFile("pic1.jpg"));

		//when(folderRepository.findById("jpegs")).thenReturn(Optional.of(mockFolder));

		//Folder result = folderService.getFolder("jpegs");

		//assertNotNull(result);
		//assertEquals("jpegs", result.getName());
		//assertEquals(1, result.getPictureFiles().size());
		//verify(folderRepository).findById("jpegs");
	}

	@Test
	void shouldListSubfoldersFromS3AndSaveIfNotExist() {
		// given
		String parentFolder = "jpegs";
		String prefix = parentFolder + "/";

		// Mock S3 response with common prefixes (subfolders)
		ListObjectsV2Response mockS3Response = ListObjectsV2Response.builder()
			.commonPrefixes(
				CommonPrefix.builder().prefix("jpegs/Basel/").build(),
				CommonPrefix.builder().prefix("jpegs/Toscana/").build()
			)
			.build();

		when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(mockS3Response);

		// Mock repository to return empty for 'Basel', and existing for 'Toscana'
		when(folderRepository.findByName("Basel")).thenReturn(Collections.emptyList());

		Folder existingFolder = new Folder();
		existingFolder.setName("Toscana");
		existingFolder.setPicture_count(10);

		when(folderRepository.findByName("Toscana")).thenReturn(List.of(existingFolder));

		// when
		List<Folder> result = folderService.listSubFolders(parentFolder);

		// then
		assertEquals(2, result.size());

		// Verify that new folder was created for "Basel"
		verify(folderRepository).save(argThat(folder -> folder.getName().equals("Basel")));

		// Verify existing folder was retrieved without saving again
		verify(folderRepository, never()).save(argThat(folder -> folder.getName().equals("Toscana")));
	}
}

