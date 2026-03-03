package gift.steps;

import gift.member.Member;
import gift.member.MemberRepository;
import gift.option.Option;
import gift.option.OptionRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class OrderSteps {

    @Autowired
    private SharedContext context;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private MemberRepository memberRepository;

    @When("{string} 옵션 {int}개를 주문하면")
    public void 옵션을_주문하면(String optionName, int quantity) {
        Option option = context.getOption(optionName);
        context.setResponse(
            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + context.getDefaultToken())
                .body(Map.of(
                    "optionId", option.getId(),
                    "quantity", quantity,
                    "message", "선물입니다!"
                ))
            .when()
                .post("/api/orders")
        );
    }

    @When("{string} 회원이 {string} 옵션 {int}개를 주문하면")
    public void 특정_회원이_옵션을_주문하면(String email, String optionName, int quantity) {
        Option option = context.getOption(optionName);
        String token = context.getMemberToken(email);
        context.setResponse(
            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(Map.of(
                    "optionId", option.getId(),
                    "quantity", quantity,
                    "message", "선물입니다!"
                ))
            .when()
                .post("/api/orders")
        );
    }

    @When("존재하지 않는 옵션으로 주문하면")
    public void 존재하지_않는_옵션으로_주문하면() {
        context.setResponse(
            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + context.getDefaultToken())
                .body(Map.of(
                    "optionId", 999999,
                    "quantity", 1,
                    "message", ""
                ))
            .when()
                .post("/api/orders")
        );
    }

    @When("인증 없이 주문하면")
    public void 인증_없이_주문하면() {
        Option option = context.getOption("ICE");
        context.setResponse(
            given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                    "optionId", option.getId(),
                    "quantity", 1,
                    "message", ""
                ))
            .when()
                .post("/api/orders")
        );
    }

    @When("주문 목록을 조회하면")
    public void 주문_목록을_조회하면() {
        context.setResponse(
            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + context.getDefaultToken())
            .when()
                .get("/api/orders")
        );
    }

    @And("응답에 주문 정보가 포함되어 있다")
    public void 응답에_주문_정보가_포함되어_있다() {
        context.getResponse().then()
            .body("id", notNullValue())
            .body("optionId", notNullValue())
            .body("quantity", notNullValue())
            .body("orderDateTime", notNullValue());
    }

    @And("{string} 옵션의 재고는 {int}이다")
    public void 옵션의_재고는(String optionName, int expectedQuantity) {
        Option option = context.getOption(optionName);
        Option updated = optionRepository.findById(option.getId()).orElseThrow();
        assertThat(updated.getQuantity()).isEqualTo(expectedQuantity);
    }

    @And("회원의 포인트는 {int}이다")
    public void 회원의_포인트는(int expectedPoint) {
        Member member = memberRepository.findByEmail("test@test.com").orElseThrow();
        assertThat(member.getPoint()).isEqualTo(expectedPoint);
    }

    @And("주문 목록에 주문 내역이 포함되어 있다")
    public void 주문_목록에_주문_내역이_포함되어_있다() {
        context.getResponse().then()
            .body("content.size()", org.hamcrest.Matchers.greaterThan(0));
    }
}
