package org.springframework.samples.homepix.unit.portfolio.calendar;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.samples.homepix.portfolio.calendar.Calendar;
import org.springframework.samples.homepix.portfolio.calendar.CalendarYear;
import org.springframework.samples.homepix.portfolio.collection.PictureFile;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import javax.transaction.Transactional;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test") // Assuming you need the "mysql" profile active for this test
@AutoConfigureMockMvc
@Transactional
@Import(com.example.test.config.TestConfig.class)
public class CalendarPageIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PictureFileRepository pictureFiles;

	@Autowired
	private org.springframework.samples.homepix.portfolio.calendar.Calendar calendar;

	private RestTemplate restTemplate;

	@BeforeAll
    void setUp() {
        restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                if (connection instanceof HttpsURLConnection) {
                    ((HttpsURLConnection) connection).setHostnameVerifier((hostname, session) -> true);
                    ((HttpsURLConnection) connection).setSSLSocketFactory(getTrustingSSLSocketFactory());
                }
                super.prepareConnection(connection, httpMethod);
            }
        });

        // Populate test data
        addExamplePictureFiles();
    }

	private void addExamplePictureFiles() {

		Month[] months = Month.values();

		for (int i = 0; i < months.length; i++) {
			PictureFile pic = new PictureFile();
			pic.setFilename("test" + (i + 1) + ".jpg");
			pic.setTitle("Dummy " + (i + 1));

			// Pick different days for each month, including fencepost cases
			int day;
			if (i == 0) {
				day = 1; // first day of January
			} else if (i == 1) {
				day = months[i].length(true); // last day of February in leap year (fencepost)
			} else if (i == 11) {
				day = 31; // last day of December (fencepost)
			} else {
				// For other months, pick the 15th
				day = 15;
			}

			pic.setTaken_on(LocalDateTime.of(2024, months[i], day, 0, 0, 0));
			pictureFiles.save(pic);
		}
	}

	@ParameterizedTest
	@MethodSource("years")
	public void PopulateYearIntegrationTest(CalendarYear calendarYear) throws Exception {

		org.springframework.samples.homepix.portfolio.calendar.Calendar calendar = new org.springframework.samples.homepix.portfolio.calendar.Calendar(pictureFiles);
		calendar.populateYear(calendarYear);
		assertEquals(0, calendar.getCount());
	}

	private Stream<CalendarYear> years() {

		return Stream.of(
			new CalendarYear(1988),
			new CalendarYear(2013),
			new CalendarYear(2024)
		);
	}

	private static SSLSocketFactory getTrustingSSLSocketFactory() {

		TrustManager[] trustAllCerts = new TrustManager[]{
			new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}
			}
		};

		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			return sslContext.getSocketFactory();
		} catch (Exception e) {
			throw new RuntimeException("Failed to create SSLSocketFactory", e);
		}
	}

	private List<String> getAnnualTags() {

		org.springframework.samples.homepix.portfolio.calendar.Calendar calendar = new Calendar(this.pictureFiles);

		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(calendar.getItems().iterator(), 0), false)
			.flatMap(yearGroup ->
				StreamSupport.stream(Spliterators.spliteratorUnknownSize(yearGroup.getYears().iterator(), 0), false)
					.map(year ->
						String.valueOf(year.getYear()))
			)
			.collect(Collectors.toList());
	}

	public Stream<Arguments> getCalendarEndpoints() {
		return getAnnualTags().stream().map(Arguments::of);
	}

	public Stream<Arguments> calendarEndpoints() {
		return getCalendarEndpoints();
	}

	private Stream<Arguments> getDailyTags() {

		return Stream.of(
			Arguments.of("2013-09-02"),
			Arguments.of("2016-03-06"),
			Arguments.of("2024-12-01"),
			Arguments.of("2024-12-02"),
			Arguments.of("2024-12-03")
		);
	}

	public Stream<Arguments> getCalendarDayEndpoints() {
		return getDailyTags();
	}

	public Stream<Arguments> calendarDayEndpoints() {
		return getCalendarDayEndpoints();
	}

	@ParameterizedTest
	@MethodSource("calendarEndpoints")
	public void testCalendarEndpoint(String endpoint) throws Exception {

		String url = "http://localhost:8443/calendar/" + endpoint;

		MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.get(url))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andReturn().getResponse();

		String content = response.getContentAsString();

		// Assert that the body contains the endpoint text
		assertTrue(content.contains(endpoint));
	}

	@ParameterizedTest
	@MethodSource("calendarDayEndpoints")
	public void testCalendarDayEndpoint(String endpoint) throws Exception {

		String url = "http://localhost:8443/collection/?fromDate=" + endpoint + "&toDate=" + endpoint + "&ID=&Key=&search=&sort=";

		MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.get(url))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andReturn().getResponse();
		//ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

		String content = response.getContentAsString();

		//assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP status code for " + endpoint);

		// Extract the body of the response
		//String responseBody = response.getBody();

		// Assert that the body contains the endpoint text
		assertTrue(content.contains("Collection from " + endpoint));
	}

	@Test
	public void testCalendarPageLinks() throws Exception {

		// Perform a GET request to the calendar page
		MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.get("/calendar/2011"))
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andReturn().getResponse();

		// Add assertions to verify the rendered view, model attributes, etc.
		// For example:
		String content = response.getContentAsString();
		assertThat(content).contains("2011");
	}

	@Test
	public void testCalendarCount() throws Exception {
		assertEquals(calendar.getCount(), 0);
	}

	@Test
	public void testName() throws Exception {

		assertEquals(calendar.getName(), null);
		assertEquals(calendar.getId(), null);

		calendar.setName("Fred");
		assertEquals("Fred", calendar.getName());
	}
}
