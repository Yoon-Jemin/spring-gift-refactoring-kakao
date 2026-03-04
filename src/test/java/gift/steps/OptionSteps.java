package gift.steps;

import gift.option.Option;
import gift.option.OptionRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class OptionSteps {

    @Autowired
    private SharedContext context;

    @Autowired
    private OptionRepository optionRepository;

    @When("이름 {string}, 수량 {int}으로 옵션을 추가하면")
    public void 옵션을_추가하면(String name, int quantity) {
        Long productId = ((Number) context.getId("productId")).longValue();
        context.setResponse(
            given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", name, "quantity", quantity))
            .when()
                .post("/api/products/" + productId + "/options")
        );
    }

    @When("해당 상품의 옵션 목록을 조회하면")
    public void 옵션_목록을_조회하면() {
        Long productId = ((Number) context.getId("productId")).longValue();
        context.setResponse(
            given()
                .contentType(ContentType.JSON)
            .when()
                .get("/api/products/" + productId + "/options")
        );
    }

    @When("{string} 옵션을 삭제하면")
    public void 옵션을_삭제하면(String optionName) {
        Long productId = ((Number) context.getId("productId")).longValue();
        Option option = optionRepository.findByProductId(productId).stream()
            .filter(o -> o.getName().equals(optionName))
            .findFirst()
            .orElseThrow();
        context.setResponse(
            given()
            .when()
                .delete("/api/products/" + productId + "/options/" + option.getId())
        );
    }

    @When("존재하지 않는 상품에 옵션을 추가하면")
    public void 존재하지_않는_상품에_옵션을_추가하면() {
        context.setResponse(
            given()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "ICE", "quantity", 10))
            .when()
                .post("/api/products/999999/options")
        );
    }

    @And("응답에 옵션 정보가 포함되어 있다")
    public void 응답에_옵션_정보가_포함되어_있다() {
        context.getResponse().then()
            .body("id", notNullValue())
            .body("name", notNullValue())
            .body("quantity", notNullValue());
    }

    @And("옵션 목록에 {int}개의 옵션이 포함되어 있다")
    public void 옵션_목록에_N개가_포함되어_있다(int count) {
        context.getResponse().then()
            .body("size()", equalTo(count));
    }
}
