package iteration1;

import base.BaseTest;
import generators.RandomData;
import models.*;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.ReceiveAllUserAccountRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        CreateAccountResponse createdAccount = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post()
                .extract().as(CreateAccountResponse.class);

        // запросить все аккаунты пользователя и проверить, что наш аккаунт там
        List <AccountModel> accounts = new ReceiveAllUserAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(null)
                .extract()
                .jsonPath()
                .getList(".", AccountModel.class);

        AccountModel account = accounts.stream()
                .filter(a -> a.getId() == createdAccount.getId())
                .findFirst()
                .orElse(null);

        assertThat(createdAccount.getAccountNumber()).isEqualTo(account.getAccountNumber());
    }
}