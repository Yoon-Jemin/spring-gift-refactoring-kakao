package gift.steps;

import gift.option.Option;
import io.cucumber.spring.ScenarioScope;
import io.restassured.response.Response;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ScenarioScope
public class SharedContext {

    private Response response;
    private final Map<String, Object> storedIds = new HashMap<>();
    private final Map<String, Option> options = new HashMap<>();
    private final Map<String, String> memberTokens = new HashMap<>();
    private String defaultToken;

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public void storeId(String key, Object id) {
        storedIds.put(key, id);
    }

    public Object getId(String key) {
        return storedIds.get(key);
    }

    public void storeOption(String name, Option option) {
        options.put(name, option);
    }

    public Option getOption(String name) {
        return options.get(name);
    }

    public void storeMemberToken(String email, String token) {
        memberTokens.put(email, token);
        if (defaultToken == null) {
            defaultToken = token;
        }
    }

    public String getMemberToken(String email) {
        return memberTokens.get(email);
    }

    public String getDefaultToken() {
        return defaultToken;
    }
}
