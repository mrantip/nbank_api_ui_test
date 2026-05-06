package specs;

import configs.Config;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import models.LoginUserRequest;
import requests.LoginUserRequester;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestSpecs {
//    private static Map<String, String> authHeaders = new HashMap<>(Map.of("admin", "Basic YWRtaW46YWRtaW4="));

    private static Map<String, String> authHeaders = new HashMap<>();

    private static String encodeBasicAuth(String username, String password) {
        String auth = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    }

    static {
        // Инициализация admin из конфига
        String adminUsername = Config.getProperty("admin.username");
        String adminPassword = Config.getProperty("admin.password");
        authHeaders.put(adminUsername, encodeBasicAuth(adminUsername, adminPassword));
    }

    private RequestSpecs(){}

    private static RequestSpecBuilder defaultRequestBuilder() {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilters( List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()))
                .setBaseUri(Config.getProperty("server") +Config.getProperty("apiVersion"));
    }

    public static RequestSpecification unauthSpec() {
        return defaultRequestBuilder().build();
    }

    public static RequestSpecification adminSpec() {
        return defaultRequestBuilder()
                .addHeader("Authorization", authHeaders.get("admin"))
                .build();
    }

    public static RequestSpecification authAsUser(String username, String password) {
        String userAuthHeader;

        if (!authHeaders.containsKey(username)) {
            userAuthHeader = new CrudRequester(
                    RequestSpecs.unauthSpec(),
                    Endpoint.LOGIN,
                    ResponseSpecs.requestReturnsOK())
                    .post(LoginUserRequest.builder().username(username).password(password).build())
                    .extract()
                    .header("Authorization");

            authHeaders.put(username, userAuthHeader);
        } else {
            userAuthHeader = authHeaders.get(username);
        }

        return defaultRequestBuilder()
                .addHeader("Authorization", userAuthHeader)
                .build();
    }
}