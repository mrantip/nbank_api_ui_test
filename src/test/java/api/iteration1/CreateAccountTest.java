package api.iteration1;

import api.base.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import requests.steps.AdminSteps;
import requests.steps.usersteps.UserSteps;
import specs.RequestSpecs;

import static org.assertj.core.api.Assertions.assertThat;


public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();
        UserSteps userSteps = new UserSteps(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));

        CreateAccountResponse createdAccount = userSteps.createAccount();

        // запросить все аккаунты пользователя и проверить, что наш аккаунт там
        AccountModel foundAccount = userSteps.getAccountByNumber(createdAccount.getAccountNumber());

        assertThat(foundAccount.getId()).isEqualTo(createdAccount.getId());
        assertThat(foundAccount.getAccountNumber()).isEqualTo(createdAccount.getAccountNumber());
    }
}