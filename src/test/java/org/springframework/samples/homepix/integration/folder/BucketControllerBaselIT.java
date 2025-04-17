package org.springframework.samples.homepix.integration.folder;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.samples.homepix.portfolio.collection.PictureFileService;
import org.springframework.samples.homepix.portfolio.folder.BucketController;
import org.springframework.samples.homepix.portfolio.folder.Folder;
import org.springframework.samples.homepix.portfolio.folder.FolderRepository;
import org.springframework.samples.homepix.portfolio.keywords.KeywordRelationshipsRepository;
import org.springframework.samples.homepix.portfolio.locations.LocationRelationshipsRepository;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test") // Assuming you need the "mysql" profile active for this test
@AutoConfigureMockMvc
@Transactional
@Import(com.example.test.config.TestConfig.class)
public class BucketControllerBaselIT {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	BucketController bucketController;

	@Autowired
	ResourceLoader resourceLoader;

	@Autowired
	private FolderRepository folders;

	@Autowired
	private PictureFileRepository pictureFiles;

	@MockBean
	private KeywordRelationshipsRepository keywordRelationships;

	@MockBean
	private LocationRelationshipsRepository locationRelationships;

	@MockBean
	private PictureFileService pictureFileService;

	@BeforeAll
	void setUp() throws IOException {

		Resource resource = resourceLoader.getResource("classpath:static/test/");
		File directory = resource.getFile();

		assertTrue(directory.exists());
		assertTrue(directory.isDirectory());

		Folder folder = new Folder();
		folder.setId(0);
		folder.setName("Basel");
		folder.setPicture_count(0);
		folder.setThumbnailId(0);
		folders.save(folder);

		List<PictureFile> files = folder.getPictureFiles(directory.getAbsolutePath() + "/");
		assertFalse(files.isEmpty());

		Collection<Folder> newfolders = folders.findAll();

		for (PictureFile pictureFile : files) {

			pictureFile.setFolder(newfolders.iterator().next());
			pictureFile.setRoles("ROLE_USER");
			pictureFile.setWidth(600);
			pictureFile.setHeight(400);
			pictureFile.setTaken_on(LocalDateTime.of(2024, 12, 15, 0, 0, 0));
		}

		pictureFiles.saveAll(files);
	}

	@AfterAll
	void Cleanup() {
		pictureFiles.deleteAll();
	}

	@Test
	void testShowFolderSuccess() throws Exception {

		mockMvc.perform(get("/buckets/basel"))
			.andExpect(status().isOk())
			.andExpect(view().name("folders/folderDetails"));
		//.andExpect(model().attributeExists("picture"))
		//.andExpect(model().attribute("picture", hasProperty("filename", is("sample.jpg"))));
	}

	//@Test
	void testShowPictureSuccess() throws Exception {
		mockMvc.perform(get("/buckets/Basel/item/1")
				.with(SecurityMockMvcRequestPostProcessors.user("user")
					.roles("USER")))
			.andExpect(status().isOk())
			.andExpect(view().name("picture/pictureFile"));
	}

	//@Test
	//@WithMockUser(roles = "USER")  // Inject ROLE_USER
	void testShowPictureSuccess_() throws Exception {
		mockMvc.perform(get("/buckets/Basel/item/1"))
			.andExpect(status().isOk())
			.andExpect(view().name("picture/pictureFile"));
	}

	@Test
	void testShowPictureFileSuccess() throws Exception {

		mockMvc.perform(get("/buckets/photos/item/0"))
			.andExpect(status().isOk())
			.andExpect(view().name("folders/folderList.html"));
		//.andExpect(model().attributeExists("picture"))
		//.andExpect(model().attribute("picture", hasProperty("filename", is("sample.jpg"))));
	}

	@Test
	@WithMockUser(roles = "USER")  // Inject ROLE_USER
	void testShowPictureFileWithSearchSuccess() throws Exception {

		mockMvc.perform(get("/buckets/Basel/item/1")
				.param("search", ":!*"))
			.andExpect(status().isOk())
			.andExpect(view().name("picture/pictureFile"));
		//.andExpect(model().attributeExists("picture"))
		//.andExpect(model().attribute("picture", hasProperty("filename", is("sample.jpg"))));
	}

	@Test
	@WithMockUser(roles = "USER")  // Inject ROLE_USER
	void testResizeImage() throws Exception {

		Iterable<PictureFile> files = pictureFiles.findAll();

		for (PictureFile file : files) {

			int width = file.getWidth();
			int height = file.getHeight();

			//byte[] bytes = bucketController.downloadFile("/photos/Basel/dsc_195630-dsc_195635.webp");

			//System.out.println("Width: " + width + " , Height: " + height);
			//bucketController.resizeImage(file, 300, 200);
		}
	}
}
