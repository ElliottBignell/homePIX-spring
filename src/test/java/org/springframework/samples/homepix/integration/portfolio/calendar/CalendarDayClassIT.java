package org.springframework.samples.homepix.integration.portfolio.calendar;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.samples.homepix.portfolio.calendar.CalendarDay;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test") // Assuming you need the "mysql" profile active for this test
@AutoConfigureMockMvc
@Transactional
@Import(com.example.test.config.TestConfig.class)
public class CalendarDayClassIT {

	@ParameterizedTest
	@MethodSource("formattedDates")
	public void testCalendarEndpoint(String name, int year, int month, int day, String output) throws Exception {

		CalendarDay calendarDay = new CalendarDay(name, null);

		assertEquals(calendarDay.getFormattedDate(year, month, day), output);
	}

	private Stream<Arguments> formattedDates() {

		return Stream.of(
			Arguments.of("Sunday", 2024, 12, 1, "2024-12-01"),
			Arguments.of("Monday", 2024, 12, 2, "2024-12-02"),
			Arguments.of("Tuesday", 2024, 12, 3, "2024-12-03")
		);
	}
}
