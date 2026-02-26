package gift.steps;

import gift.auth.JwtProvider;
import gift.category.Category;
import gift.category.CategoryRepository;
import gift.member.Member;
import gift.member.MemberRepository;
import gift.option.Option;
import gift.option.OptionRepository;
import gift.product.Product;
import gift.product.ProductRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class OrderSteps {

    @Autowired
    private SharedContext context;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtProvider jwtProvider;

    private final Map<String, Option> options = new HashMap<>();
    private final Map<String, String> memberTokens = new HashMap<>();
    private String defaultToken;

    @Given("이름이 {string}인 카테고리가 등록되어 있고")
    public void 카테고리가_등록되어_있고(String name) {
        Category category = categoryRepository.save(new Category(name, "#000000", "", ""));
        context.storeId("categoryId", category.getId());
    }

    @And("{string} 상품이 가격 {int}, 이미지 {string}로 등록되어 있고")
    public void 상품이_등록되어_있고(String name, int price, String imageUrl) {
        Long categoryId = ((Number) context.getId("categoryId")).longValue();
        Category category = categoryRepository.findById(categoryId).orElseThrow();
        Product product = productRepository.save(new Product(name, price, imageUrl, category));
        context.storeId("productId", product.getId());
    }

    @And("재고 {int}개인 {string} 옵션이 등록되어 있고")
    public void 옵션이_등록되어_있고(int quantity, String optionName) {
        Long productId = ((Number) context.getId("productId")).longValue();
        Product product = productRepository.findById(productId).orElseThrow();
        Option option = optionRepository.save(new Option(product, optionName, quantity));
        options.put(optionName, option);
    }

    @And("포인트 {int}을 가진 회원 {string}이 등록되어 있고")
    public void 회원이_등록되어_있고(int point, String email) {
        Member member = new Member(email, "password");
        member.chargePoint(point);
        memberRepository.save(member);
        String token = jwtProvider.createToken(email);
        memberTokens.put(email, token);
        if (defaultToken == null) {
            defaultToken = token;
        }
    }

    @When("{string} 옵션 {int}개를 주문하면")
    public void 옵션을_주문하면(String optionName, int quantity) {
        Option option = options.get(optionName);
        context.setResponse(
            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + defaultToken)
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
        Option option = options.get(optionName);
        String token = memberTokens.get(email);
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
                .header("Authorization", "Bearer " + defaultToken)
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
        Option option = options.get("ICE");
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
                .header("Authorization", "Bearer " + defaultToken)
            .when()
                .get("/api/orders")
        );
    }

    @Then("응답 상태 코드는 {int}이다")
    public void 응답_상태_코드는(int statusCode) {
        assertThat(context.getResponse().statusCode()).isEqualTo(statusCode);
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
        Option option = options.get(optionName);
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
