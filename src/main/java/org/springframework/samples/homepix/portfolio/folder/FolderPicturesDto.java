package org.springframework.samples.homepix.portfolio.folder;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import org.springframework.samples.homepix.portfolio.collection.PictureInfoDto;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class FolderPicturesDto {

	String folder;
    List<PictureInfoDto> pictureFilenames;
}
