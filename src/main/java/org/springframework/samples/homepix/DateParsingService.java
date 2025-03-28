package org.springframework.samples.homepix;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Supplier;

@Service
public class DateParsingService {

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

		LocalDate startDate = LocalDate.parse(start, formatter);
		LocalDate endDate = LocalDate.parse(end, formatter);

		return new DateRange(startDate, endDate);
	}
}
