package gift.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class CategorySteps {

    @Autowired
    private SharedContext context;

    @When("이름 {string}, 색상 {string}, 이미지 {string}로 카테고리를 생성하면")
    public void 카테고리를_생성하면(String name, String color, String imageUrl) {
        context.setResponse(
            given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                    "name", name,
                    "color", color,
                    "imageUrl", imageUrl,
                    "description", ""
                ))
            .when()
                .post("/api/categories")
        );
    }

    @When("카테고리 목록을 조회하면")
    public void 카테고리_목록을_조회하면() {
        context.setResponse(
            given()
                .contentType(ContentType.JSON)
            .when()
                .get("/api/categories")
        );
    }

    @When("해당 카테고리의 이름을 {string}로 수정하면")
    public void 카테고리를_수정하면(String name) {
        Long categoryId = ((Number) context.getId("categoryId")).longValue();
        context.setResponse(
            given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                    "name", name,
                    "color", "#000000",
                    "imageUrl", "http://img.url",
                    "description", ""
                ))
            .when()
                .put("/api/categories/" + categoryId)
        );
    }

    @When("해당 카테고리를 삭제하면")
    public void 카테고리를_삭제하면() {
        Long categoryId = ((Number) context.getId("categoryId")).longValue();
        context.setResponse(
            given()
            .when()
                .delete("/api/categories/" + categoryId)
        );
    }

    @When("존재하지 않는 카테고리를 수정하면")
    public void 존재하지_않는_카테고리를_수정하면() {
        context.setResponse(
            given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                    "name", "없는카테고리",
                    "color", "#000000",
                    "imageUrl", "http://img.url",
                    "description", ""
                ))
            .when()
                .put("/api/categories/999999")
        );
    }

    @And("응답에 카테고리 정보가 포함되어 있다")
    public void 응답에_카테고리_정보가_포함되어_있다() {
        context.getResponse().then()
            .body("id", notNullValue())
            .body("name", notNullValue());
    }

    @And("카테고리 목록에 {int}개의 카테고리가 포함되어 있다")
    public void 카테고리_목록에_N개가_포함되어_있다(int count) {
        context.getResponse().then()
            .body("size()", equalTo(count));
    }

    @And("응답의 카테고리 이름은 {string}이다")
    public void 응답의_카테고리_이름은(String name) {
        context.getResponse().then()
            .body("name", equalTo(name));
    }
}
