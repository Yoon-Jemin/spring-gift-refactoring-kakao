package gift.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class ProductSteps {

    @Autowired
    private SharedContext context;

    @When("{string} 상품을 가격 {int}, 이미지 {string}로 생성하면")
    public void 상품을_생성하면(String name, int price, String imageUrl) {
        Long categoryId = ((Number) context.getId("categoryId")).longValue();
        context.setResponse(
            given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                    "name", name,
                    "price", price,
                    "imageUrl", imageUrl,
                    "categoryId", categoryId
                ))
            .when()
                .post("/api/products")
        );
    }

    @When("상품 목록을 조회하면")
    public void 상품_목록을_조회하면() {
        context.setResponse(
            given()
                .contentType(ContentType.JSON)
            .when()
                .get("/api/products")
        );
    }

    @When("해당 상품을 조회하면")
    public void 해당_상품을_조회하면() {
        Long productId = ((Number) context.getId("productId")).longValue();
        context.setResponse(
            given()
                .contentType(ContentType.JSON)
            .when()
                .get("/api/products/" + productId)
        );
    }

    @When("해당 상품의 이름을 {string}, 가격을 {int}으로 수정하면")
    public void 상품을_수정하면(String name, int price) {
        Long productId = ((Number) context.getId("productId")).longValue();
        Long categoryId = ((Number) context.getId("categoryId")).longValue();
        context.setResponse(
            given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                    "name", name,
                    "price", price,
                    "imageUrl", "http://img.url",
                    "categoryId", categoryId
                ))
            .when()
                .put("/api/products/" + productId)
        );
    }

    @When("해당 상품을 삭제하면")
    public void 해당_상품을_삭제하면() {
        Long productId = ((Number) context.getId("productId")).longValue();
        context.setResponse(
            given()
            .when()
                .delete("/api/products/" + productId)
        );
    }

    @When("존재하지 않는 상품을 조회하면")
    public void 존재하지_않는_상품을_조회하면() {
        context.setResponse(
            given()
                .contentType(ContentType.JSON)
            .when()
                .get("/api/products/999999")
        );
    }

    @And("응답에 상품 정보가 포함되어 있다")
    public void 응답에_상품_정보가_포함되어_있다() {
        context.getResponse().then()
            .body("id", notNullValue())
            .body("name", notNullValue())
            .body("price", notNullValue());
    }

    @And("상품 목록에 {int}개의 상품이 포함되어 있다")
    public void 상품_목록에_N개가_포함되어_있다(int count) {
        context.getResponse().then()
            .body("content.size()", equalTo(count));
    }

    @And("응답의 상품 이름은 {string}이다")
    public void 응답의_상품_이름은(String name) {
        context.getResponse().then()
            .body("name", equalTo(name));
    }
}
