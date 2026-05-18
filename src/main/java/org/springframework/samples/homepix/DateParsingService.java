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

	private static final Logger logger = LoggerFactory.getLogger(SslConfig.class);
	static final String format = "yyyy-M-d";

	static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format, Locale.ENGLISH);

	public DateRange parseDateRange(String start, String end) {

		if (start.equals("")) {
			start = "1970-01-01";
		}

		if (end.equals("")) {

			Supplier<String> supplier = () -> {
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
				LocalDateTime now = LocalDateTime.now();
				return dtf.format(now);
			};

			end = supplier.get();
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
}
