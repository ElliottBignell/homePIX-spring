package org.springframework.samples.homepix.portfolio.folder;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class FolderPicturesDto {

	String folder;
    List<String> pictureFilenames;
}
