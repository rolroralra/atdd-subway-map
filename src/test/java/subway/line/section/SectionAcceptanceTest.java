package subway.line.section;

import static org.assertj.core.api.Assertions.assertThat;

import common.AbstractAcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import subway.line.LineAcceptanceTest;
import subway.section.SectionRequest;
import subway.section.SectionResponse;
import subway.station.StationAcceptanceTest;

@DisplayName("지하철역 구간 관련 기능")
public class SectionAcceptanceTest extends AbstractAcceptanceTest {

    private Long aStationId;

    private Long bStationId;

    private Long cStationId;

    private Long lineId;

    @BeforeEach
    protected void beforeEach() {
        super.beforeEach();

        aStationId = StationAcceptanceTest.createStation("aStation");
        bStationId = StationAcceptanceTest.createStation("bStation");
        cStationId = StationAcceptanceTest.createStation("cStation");

        lineId = LineAcceptanceTest.createLine(
            LineAcceptanceTest.LINE_1,
            LineAcceptanceTest.RED_COLOR,
            aStationId,
            bStationId,
            10
        ).getId();
    }

    /**
     * When 지하철 구간을 생성하면
     * Then 지하철 구간 목록 조회시 생성한 구간을 찾을 수 있다
     */
    @Test
    @DisplayName("지하철 구간 생성")
    void createSection() {
        // When
        SectionResponse createdSection = createSection(lineId, bStationId, cStationId, 20);

        // Then
        List<SectionResponse> sections = findSections(lineId);

        assertThat(sections)
            .contains(createdSection);
    }



    /**
     * Given 지하철 구간을 생성하고
     * When 생성한 지하철 구간을 삭제하면
     * Then 해당 지하철 구간 정보는 삭제된다
     */
    @Test
    @DisplayName("지하철 구간 삭제")
    void deleteSection() {
        // Given
        SectionResponse givenSection = createSection(lineId, bStationId, cStationId, 20);

        // When
        ExtractableResponse<Response> response = deleteSection(lineId, givenSection.getDownStationId());

        // Then
        assertThat(response.statusCode())
            .isEqualTo(HttpStatus.NO_CONTENT.value());

        ExtractableResponse<Response> findSectionResponse = findSectionById(lineId, givenSection.getId());

        assertThat(findSectionResponse.statusCode())
            .isEqualTo(HttpStatus.NOT_FOUND.value());

    }

    /**
     * Given 2개의 지하철 구간을 생성하고
     * When 지하철 구간 목록을 조회하면
     * Then 지하철 구간 목록 조회 시 2개의 구간을 조회할 수 있다.
     */
    @Test
    @DisplayName("지하철 구간 목록 조회")
    void searchSections() {
        // Given
        Long downStationId2 = StationAcceptanceTest.createStation("downStation");
        Long upStationId2 = StationAcceptanceTest.createStation("upStation");

        SectionResponse givenSection1 = createSection(lineId, bStationId, aStationId, 10);
        SectionResponse givenSection2 = createSection(lineId, downStationId2, upStationId2, 20);

        // When
        List<SectionResponse> sections = findSections(lineId);

        // Then
        assertThat(sections)
            .hasSizeGreaterThanOrEqualTo(2)
            .contains(givenSection1, givenSection2);
    }

    /**
     * Given 지하철 구간을 생성하고
     * When 생성한 지하철 구간을 조회하면
     * Then 생성한 지하철 구간의 정보를 응답받을 수 있다.
     */
    @Test
    @DisplayName("지하철 구간 조회")
    void searchSection() {
        // Given
        SectionResponse givenSection = createSection(lineId, bStationId, aStationId, 10);

        // When
        SectionResponse findSection = findSection(lineId, givenSection.getId());

        // Then
        assertThat(findSection)
            .isNotNull()
            .isEqualTo(givenSection);
    }

    public static SectionResponse createSection(Long lineId, Long upStationId ,Long downStationId, int distance) {
        return RestAssured
            .given()
                .contentType(ContentType.JSON)
                .body(
                    SectionRequest.builder()
                        .downStationId(downStationId)
                        .upStationId(upStationId)
                        .distance(distance)
                        .build()
                    )
            .when()
                .post("/lines/{lineId}/sections", lineId)
            .then()
                .log().all()
                .extract().jsonPath().getObject("$", SectionResponse.class);
    }

    public static ExtractableResponse<Response> deleteSection(Long lineId, Long stationId) {
        return RestAssured
            .given()
                .param("stationId", stationId)
            .when()
                .delete("/lines/{lineId}/sections", lineId)
            .then()
                .log().all()
                .extract();
    }

    public static ExtractableResponse<Response> findSectionById(Long lineId, Long sectionId) {
        return RestAssured
            .given()
            .when()
                .get("/lines/{lineId}/sections/{sectionId}", lineId, sectionId)
            .then()
                .log().all()
                .extract();
    }
    public static SectionResponse findSection(Long lineId, Long sectionId) {
        return findSectionById(lineId, sectionId)
            .jsonPath().getObject("$", SectionResponse.class);
    }

    public static List<SectionResponse> findSections(Long lineId) {
        return RestAssured
            .given()
            .when()
                .get("/lines/{lineId}/sections", lineId)
            .then()
                .log().all()
                .extract().jsonPath().getList("$", SectionResponse.class);
    }
}
