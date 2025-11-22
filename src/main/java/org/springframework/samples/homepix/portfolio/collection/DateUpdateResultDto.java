package org.springframework.samples.homepix.portfolio.collection;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DateUpdateResultDto
{
    int totalIdsProvided;
    int picturesFound;
    int updatedCount;
    int skippedCount;
    List<String> updatedIds;
    List<String> skippedIds;
    List<String> notFoundId;
}
