package org.springframework.samples.homepix.portfolio.calendar;

import net.bytebuddy.implementation.bind.annotation.Argument;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.util.*;
import java.util.Calendar;
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
@Import(com.example.test.config.TestConfig.class)
public class CalendarPageIntegrationTest {

	private final MockMvc mockMvc;
	private final PictureFileRepository pictureFiles;

	private final RestTemplate restTemplate;

	@Autowired
	public CalendarPageIntegrationTest(MockMvc mockMvc, PictureFileRepository pictureFiles) {

		this.mockMvc = mockMvc;
		this.pictureFiles = pictureFiles;

		// Create a RestTemplate instance
		restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory() {
			@Override
			protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
				if (connection instanceof HttpsURLConnection) {
					((HttpsURLConnection) connection).setHostnameVerifier(new HostnameVerifier() {
						public boolean verify(String hostname, SSLSession session) {
							return true;
						}
					});
					((HttpsURLConnection) connection).setSSLSocketFactory(getTrustingSSLSocketFactory());
				}
				super.prepareConnection(connection, httpMethod);
			}
		});
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

		org.springframework.samples.homepix.portfolio.calendar.Calendar calendar = new org.springframework.samples.homepix.portfolio.calendar.Calendar(this.pictureFiles);

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
			Arguments.of("2024-12-01"),
			Arguments.of("2024-12-02"),
			Arguments.of("2024-12-03")
		);

		/*org.springframework.samples.homepix.portfolio.calendar.Calendar calendar = new org.springframework.samples.homepix.portfolio.calendar.Calendar(this.pictureFiles);

		for (CalendarYearGroup group : calendar.getItems()) {

			for (CalendarYear calendarYear : group.getYears()) {

				if (calendarYear.getQuarters() == null || calendarYear.getQuarters().isEmpty()) {
					calendar.populateYear(calendarYear);
				}
			}
		}

		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(calendar.getItems().iterator(), 0), false)
			.flatMap(yearGroup ->
				StreamSupport.stream(Spliterators.spliteratorUnknownSize(yearGroup.getYears().iterator(), 0), false)
					.flatMap(year ->
						Optional.ofNullable(year.getQuarters()).orElse(Collections.emptyList()).stream()
							.flatMap(quarter ->
								StreamSupport.stream(Spliterators.spliteratorUnknownSize(quarter.getMonths().iterator(), 0), false)
									.flatMap(month ->
										StreamSupport.stream(Spliterators.spliteratorUnknownSize(month.getWeeks().iterator(), 0), false)
											.flatMap(week ->
												week.getDays().stream()
													.filter(day -> !pictureFiles.findByDate(LocalDate.of(year.getYear(), Integer.valueOf(month.getIndex()), day.getDayOfMonth() + 1)).isEmpty())
													.limit(1) // Limit to the first record
													.map(day -> String.format("%d-%02d-%02d", year.getYear(), Integer.valueOf(month.getIndex()), day.getDayOfMonth() + 1))
											)
									)
							)
					)
			)
			.collect(Collectors.toList());*/
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
}
