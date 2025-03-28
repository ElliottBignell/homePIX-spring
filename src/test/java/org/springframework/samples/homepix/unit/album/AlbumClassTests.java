package org.springframework.samples.homepix.unit.album;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import org.springframework.samples.homepix.portfolio.album.AlbumRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test") // Assuming you need the "mysql" profile active for this test
@AutoConfigureMockMvc
@Import(com.example.test.config.TestConfig.class)
public class AlbumClassTests {

	private final MockMvc mockMvc;
	private final AlbumRepository albumRepository;

	@Autowired
	public AlbumClassTests(MockMvc mockMvc, AlbumRepository albumRepository) {
		this.mockMvc = mockMvc;
		this.albumRepository = albumRepository;
	}

	/*@Test
	public void testAlbumContent() throws Exception {

		String url = "http://localhost:8443/album/";

		MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.get(url))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andReturn().getResponse();

		String content = response.getContentAsString();
		assertThat(content).contains("Texture");
	}

	@Test
	public void testAlbumRepoInitialisation() throws Exception {

		Iterable<Album> iter = albumRepository.findAll();
		Iterator<Album> iterator = iter.iterator();
		assert(iterator.hasNext());

		if (iterator.hasNext()) {

			String name = iterator.next().getName();
			assertThat(name.equals("Texture"));
		}
	}*/
}
