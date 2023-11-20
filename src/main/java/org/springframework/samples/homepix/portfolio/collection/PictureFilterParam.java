package org.springframework.samples.homepix.portfolio.collection;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class PictureFilterParam {

	private Date fromDate;

	private Date toDate;

}
