package org.springframework.samples.homepix.integration.portfolio.album;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.samples.homepix.portfolio.album.Album;
import org.springframework.samples.homepix.portfolio.album.AlbumContent;
import org.springframework.samples.homepix.portfolio.album.AlbumContentRepository;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test") // Assuming you need the "mysql" profile active for this test
@AutoConfigureMockMvc
@Import(com.example.test.config.TestConfig.class)
@Transactional
public class AlbumAddIT {

	@Mock
    AlbumContentRepository albumContentRepository;

	@ParameterizedTest
	@MethodSource("albumNames")
	public void testAlbumAddEndpoint(String name) throws Exception {

		Album album = new Album();

		album.setName(name);

		assertEquals(album.getName(), name);
		System.out.println(album.toString());
		assertTrue(album.toString().matches(".Album@.* id = 0, name = '" + name + "'."));
		assertEquals(0, album.getCount());
		assertNull(album.getThumbnail());
		assertEquals(0, album.getThumbnail_id());
		assertNull(album.getDescription());
	}

	@ParameterizedTest
	@MethodSource("albumNames")
	public void testAlbumAddPictureEndpoint(String name) throws Exception {

		Album album = new Album();
		PictureFile pictureFile = new PictureFile();

		AlbumContent content = new AlbumContent();
		content.setPictureFile(pictureFile);
		content.setAlbum(album);
		albumContentRepository.save(content);

		List<AlbumContent> list = new ArrayList<>();
		albumContentRepository.findAll().iterator().forEachRemaining(list::add);

		assertEquals(0, list.size());
	}

	private Stream<Arguments> albumNames() {

		return Stream.of(
			Arguments.of("First" ),
			Arguments.of("Second" ),
			Arguments.of("Third" )
		);
	}
}
