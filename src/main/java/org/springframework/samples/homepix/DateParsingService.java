package org.springframework.samples.homepix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Supplier;

@Service
public class DateParsingService {

	private static final Logger logger = LoggerFactory.getLogger(DateParsingService.class);
	static final String format = "yyyy-M-d";

	static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

	public DateRange parseDateRange(String start, String end) {

	    // Trim and remove any stray quotes
		start = cleanDateString(start);
		end = cleanDateString(end);

		if (start.isEmpty()) {
			start = "1970-01-01";
		}

		if (end.isEmpty()) {
			end = LocalDate.now().format(formatter);
		}

		LocalDate startDate = null;
		LocalDate endDate = null;

		try {
			startDate = LocalDate.parse(start, formatter);
		}
		catch (Exception ex) {
			logger.error("❌ Unparseable start date in DateParsingService.parseDateRange: {}", start);
			startDate = LocalDate.of(1970, 1, 1);
		}

		try {
			endDate = LocalDate.parse(end, formatter);
		}
		catch (Exception ex) {
			logger.error("❌ Unparseable end date in DateParsingService.parseDateRange: {}", end);
			endDate = LocalDate.now();
		}

		return new DateRange(startDate, endDate);
	}

	public String cleanDateString(String dateStr) {

		if (dateStr == null) return "";
		// Remove surrounding quotes, trim whitespace
		return dateStr.trim()
			.replaceAll("^['\"]|['\"]$", "")  // Remove leading/trailing quotes
			.trim();
	}
}
