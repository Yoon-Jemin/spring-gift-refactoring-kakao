package gift.steps;

import gift.auth.JwtProvider;
import gift.member.MemberRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class WishSteps {

    @Autowired
    private SharedContext context;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private MemberRepository memberRepository;

    private Long lastWishId;

    @When("{string} 회원이 해당 상품을 위시에 추가하면")
    public void 위시에_추가하면(String email) {
        Long productId = ((Number) context.getId("productId")).longValue();
        String token = jwtProvider.createToken(email);
        context.setResponse(
            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(Map.of("productId", productId))
            .when()
                .post("/api/wishes")
        );
        if (context.getResponse().statusCode() == 200) {
            lastWishId = context.getResponse().jsonPath().getLong("id");
            context.storeId("wishId", lastWishId);
        }
    }

    @When("{string} 회원이 위시 목록을 조회하면")
    public void 위시_목록을_조회하면(String email) {
        String token = jwtProvider.createToken(email);
        context.setResponse(
            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
            .when()
                .get("/api/wishes")
        );
    }

    @When("{string} 회원이 해당 위시를 삭제하면")
    public void 위시를_삭제하면(String email) {
        Long wishId = ((Number) context.getId("wishId")).longValue();
        String token = jwtProvider.createToken(email);
        context.setResponse(
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .delete("/api/wishes/" + wishId)
        );
    }

    @When("인증 없이 위시 목록을 조회하면")
    public void 인증_없이_위시_목록을_조회하면() {
        context.setResponse(
            given()
                .contentType(ContentType.JSON)
            .when()
                .get("/api/wishes")
        );
    }

    @When("{string} 회원이 존재하지 않는 상품을 위시에 추가하면")
    public void 존재하지_않는_상품을_위시에_추가하면(String email) {
        String token = jwtProvider.createToken(email);
        context.setResponse(
            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(Map.of("productId", 999999))
            .when()
                .post("/api/wishes")
        );
    }

    @And("응답에 위시 정보가 포함되어 있다")
    public void 응답에_위시_정보가_포함되어_있다() {
        context.getResponse().then()
            .body("id", notNullValue())
            .body("productId", notNullValue())
            .body("name", notNullValue());
    }

    @And("응답의 위시 ID는 기존과 동일하다")
    public void 응답의_위시_ID는_기존과_동일하다() {
        Long currentWishId = context.getResponse().jsonPath().getLong("id");
        context.getResponse().then()
            .body("id", equalTo(lastWishId.intValue()));
    }

    @And("위시 목록에 {int}개의 위시가 포함되어 있다")
    public void 위시_목록에_N개가_포함되어_있다(int count) {
        context.getResponse().then()
            .body("content.size()", equalTo(count));
    }
}
