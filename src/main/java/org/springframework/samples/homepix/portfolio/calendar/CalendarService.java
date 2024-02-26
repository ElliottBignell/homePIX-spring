package org.springframework.samples.homepix.portfolio.calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.samples.homepix.CustomUserDetailsService;
import org.springframework.samples.homepix.portfolio.collection.PictureFileRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class CalendarService {

	@Autowired
	private PictureFileRepository pictureFileRepository;

	@Bean
	public Calendar calendar() {
		return new Calendar(pictureFileRepository);
	}
}
