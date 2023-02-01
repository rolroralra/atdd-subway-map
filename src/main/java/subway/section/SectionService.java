package subway.section;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import subway.exception.NotFoundStationException;
import subway.station.Station;
import subway.station.StationRepository;

@Service
@RequiredArgsConstructor
public class SectionService {
    private final SectionRepository sectionRepository;

    private final StationRepository stationRepository;

    @Transactional
    public Section registerSection(Long upStationId, Long downStationId, int distance) {
        Station upStation = stationRepository.findById(upStationId)
            .orElseThrow(NotFoundStationException::new);

        Station downStation = stationRepository.findById(downStationId)
            .orElseThrow(NotFoundStationException::new);

        return sectionRepository.findSectionByUpStationIdAndDownStationId(upStationId, downStationId)
            .orElse(
                sectionRepository.save(new Section(upStation, downStation, distance))
            );
    }
}