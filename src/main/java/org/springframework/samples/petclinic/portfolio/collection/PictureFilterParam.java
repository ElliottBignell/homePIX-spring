package org.springframework.samples.petclinic.portfolio.collection;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
public class PictureFilterParam {

	private Date fromDate;

	private Date toDate;

}
