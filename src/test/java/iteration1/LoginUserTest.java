package iteration1;

import base.BaseTest;
import configs.Config;
import models.CreateUserRequest;
import models.CreateUserResponse;
import models.LoginUserRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;


public class LoginUserTest extends BaseTest {

    @Test
    public void adminCanGenerateAuthTokenTest() {
        String username = Config.getUsername();
        String password = Config.getPassword();

        LoginUserRequest userRequest = LoginUserRequest.builder()
                .username(username)
                .password(password)
                .build();

        new ValidatedCrudRequester<CreateUserResponse>(RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(userRequest);
    }

    @Test
    public void userCanGenerateAuthTokenTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        new CrudRequester(RequestSpecs.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(userRequest.getUsername()).password(userRequest.getPassword()).build())
                .header("Authorization", Matchers.notNullValue());
    }
}