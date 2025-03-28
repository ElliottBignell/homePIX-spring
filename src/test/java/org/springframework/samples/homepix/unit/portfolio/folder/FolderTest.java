package org.springframework.samples.homepix.unit.portfolio.folder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.folder.Folder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test") // Assuming you need the "mysql" profile active for this test
@AutoConfigureMockMvc
@Import(com.example.test.config.TestConfig.class)
public class FolderTest {

	@Autowired
	ResourceLoader resourceLoader;

	@Test
	void folderShouldReturnCorrectNumberOfPictures() {

		Folder folder = new Folder();
		assertEquals(0, folder.getPicture_count());
	}

	@Test
	void folderDisplayName() {

		Folder folder = new Folder();
		folder.setName("Test");
		assertEquals("Test", folder.getDisplayName());
	}

	@Test
	void folderDisplayNameUnderscores() {

		Folder folder = new Folder();
		folder.setName("Test_under_scores");
		assertEquals("Test under scores", folder.getDisplayName());
	}

	@Test
	void folderDisplayNameCamelCaseAndUnderscores() {

		Folder folder = new Folder();
		folder.setName("TestCamelCase_And_Underscores");
		assertEquals("Test Camel Case And Underscores", folder.getDisplayName());
	}

	@Test
	void folderDisplayNameCamelCase() {

		Folder folder = new Folder();
		folder.setName("TestCamelCase");
		assertEquals("Test Camel Case", folder.getDisplayName());
	}

	@Test
	void loadPicturesTest() throws IOException {

		Resource resource = resourceLoader.getResource("classpath:static/test/");
		File directory = resource.getFile();

		assertTrue(directory.exists());
		assertTrue(directory.isDirectory());

		Folder folder = new Folder();
		folder.setName("Basel");
		List<PictureFile> files = folder.getPictureFiles(directory.getAbsolutePath() + "/");

		assertEquals(3, files.size());
	}

	@Test
	void loadPictureWithoutExifTest() throws IOException {

		Resource resource = resourceLoader.getResource("classpath:static/test/");
		File directory = resource.getFile();

		assertTrue(directory.exists());
		assertTrue(directory.isDirectory());

		Folder folder = new Folder();
		folder.setName("NoExif");
		List<PictureFile> files = folder.getPictureFiles(directory.getAbsolutePath() + "/");

		assertEquals(1, files.size());

		PictureFile file = files.get(0);
		assertEquals("No Exif data for title", file.getTitle());
	}

	@Test
	void checkTitle() throws IOException {

		Resource resource = resourceLoader.getResource("classpath:static/test/");
		File directory = resource.getFile();

		assertTrue(directory.exists());
		assertTrue(directory.isDirectory());

		Folder folder = new Folder();
		folder.setName("Basel");
		List<PictureFile> files = folder.getPictureFiles(directory.getAbsolutePath() + "/");

		assertEquals(null, folder.getDescription());
	}

	@Test
	void checkDescription() throws IOException {

		Resource resource = resourceLoader.getResource("classpath:static/test/");
		File directory = resource.getFile();

		assertTrue(directory.exists());
		assertTrue(directory.isDirectory());

		Folder folder = new Folder();
		folder.setName("Basel");
		List<PictureFile> files = folder.getPictureFiles(directory.getAbsolutePath() + "/");

		assertEquals("Basel", folder.getName());
	}

	@Test
	void checkLastTakenOn() throws IOException {

		Folder folder = new Folder();
		LocalDate date = folder.getLastModifiedDate();
		assertTrue(date.getYear() > 2024);
	}

	@Test
	void checkLinkName() throws IOException {

		Folder folder = new Folder();
		folder.setName("Basel");
		assertEquals("basel", folder.getLinkName());
		assertEquals("Basel", folder.getName());
		folder.setName("Basel and spaces");
		assertEquals("basel and spaces", folder.getLinkName());
		assertEquals("Basel and spaces", folder.getName());
		folder.setName("Basel_and_underscores");
		assertEquals("basel-and-underscores", folder.getLinkName());
		assertEquals("Basel_and_underscores", folder.getName());
	}

	@Test
	void checkToString() throws IOException {

		Resource resource = resourceLoader.getResource("classpath:static/test/");
		File directory = resource.getFile();

		assertTrue(directory.exists());
		assertTrue(directory.isDirectory());

		Folder folder = new Folder();
		folder.setName("Basel");
		folder.setId(666);
		List<PictureFile> files = folder.getPictureFiles(directory.getAbsolutePath() + "/");

		System.out.println(folder.toString());
		assertTrue(folder.toString().matches(".Folder@.* id = 666, name = 'Basel'."));
	}
}
