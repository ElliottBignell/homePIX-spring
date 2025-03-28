package org.springframework.samples.homepix.unit.dates;

import org.junit.jupiter.api.Test;
import org.springframework.samples.homepix.DateParsingService;
import org.springframework.samples.homepix.DateRange;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
public class DateRangeTest {

	@Test
	void testDateParsingWithEmptyStrings() {

		DateParsingService service = new DateParsingService();

		DateRange range = service.parseDateRange("", "");

		assertEquals(LocalDate.of(1970, 1, 1), range.getStartDate());
		assertEquals(LocalDate.now(), range.getEndDate());
	}

	@Test
	void testDateParsingWithSimpleDates() {

		DateParsingService service = new DateParsingService();

		DateRange range = service.parseDateRange("2014-03-12", "2014-03-13");

		assertEquals(LocalDate.of(2014, 3, 12), range.getStartDate());
		assertEquals(LocalDate.of(2014, 3, 13), range.getEndDate());
	}

	@Test
	void testDateContains() {

		DateParsingService service = new DateParsingService();

		DateRange range = service.parseDateRange("2014-03-10", "2014-03-15");

		assertTrue(range.contains(LocalDate.of(2014, 3, 10)));
		assertTrue(range.contains(LocalDate.of(2014, 3, 12)));
		assertTrue(range.contains(LocalDate.of(2014, 3, 15)));
	}

	@Test
	void testDateBefore() {

		DateParsingService service = new DateParsingService();

		DateRange range = service.parseDateRange("2014-03-10", "2014-03-15");

		assertFalse(range.contains(LocalDate.of(2014, 3, 9)));
	}

	@Test
	void testDateAfter() {

		DateParsingService service = new DateParsingService();

		DateRange range = service.parseDateRange("2014-03-10", "2014-03-15");

		assertFalse(range.contains(LocalDate.of(2014, 3, 16)));
	}
}
