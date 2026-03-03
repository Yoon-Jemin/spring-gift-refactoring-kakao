package gift.steps;

import io.cucumber.java.en.When;
import io.cucumber.java.en.And;
import io.restassured.http.ContentType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class MemberSteps {

    @Autowired
    private SharedContext context;

    @When("{string} 이메일과 {string} 비밀번호로 회원가입하면")
    public void 회원가입하면(String email, String password) {
        context.setResponse(
            given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", password))
            .when()
                .post("/api/members/register")
        );
    }

    @When("{string} 이메일과 {string} 비밀번호로 로그인하면")
    public void 로그인하면(String email, String password) {
        context.setResponse(
            given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", password))
            .when()
                .post("/api/members/login")
        );
    }

    @And("응답에 토큰이 포함되어 있다")
    public void 응답에_토큰이_포함되어_있다() {
        context.getResponse().then()
            .body("token", notNullValue());
    }
}
