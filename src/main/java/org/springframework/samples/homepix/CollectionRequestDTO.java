package org.springframework.samples.homepix;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Supplier;

@Getter
@Setter
public class CollectionRequestDTO {

	private static final String format = "yyyy-MM-dd";
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format, Locale.ENGLISH);

	private Supplier<String> today = () -> {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	};

	private String search = "";
	private String fromDate = "1970-01-01";
	private String toDate = String.format(today.get(), format);
	private String sort = "title";
}
