package org.springframework.samples.homepix.unit.folder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.samples.homepix.portfolio.collection.PictureFileService;
import org.springframework.samples.homepix.portfolio.folder.FolderRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.locations.LocationRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.folder.Folder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test") // Assuming you need the "mysql" profile active for this test
@AutoConfigureMockMvc
@Import(com.example.test.config.TestConfig.class)
public class BucketControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private FolderRepository folders;

	@MockBean
	private PictureFileRepository pictureFiles;

	@MockBean
	private KeywordRelationshipsRepository keywordRelationships;

	@MockBean
	private LocationRelationshipsRepository locationRelationships;

	@MockBean
	private PictureFileService pictureFileService;

	@Test
	void testShowPictureFileBucketNotFound() throws Exception {

		when(folders.findByName("unknown")).thenReturn(List.of());

		mockMvc.perform(get("/buckets/unknown/item/0"))
			.andExpect(status().isOk())
			.andExpect(view().name("folders/folderList.html"));
	}

	@Test
	void testShowPictureFileIndexOverflow() throws Exception {

		Folder mockFolder = new Folder();
		mockFolder.setName("photos");

		when(folders.findByName("photos")).thenReturn(List.of(mockFolder));
		when(pictureFiles.findByFolderName("photos")).thenReturn(List.of());

		mockMvc.perform(get("/buckets/photos/item/10"))
			.andExpect(status().isOk())
			.andExpect(view().name("error-404"))
			.andExpect(model().attribute("errorMessage", containsString("Failed to retrieve picture")));
	}
}
