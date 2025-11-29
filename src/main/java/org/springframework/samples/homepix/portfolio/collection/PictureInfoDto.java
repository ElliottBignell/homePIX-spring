package org.springframework.samples.homepix.portfolio.collection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class PictureInfoDto {

    Integer id;
    String filename;
	LocalDateTime taken_on;
}
