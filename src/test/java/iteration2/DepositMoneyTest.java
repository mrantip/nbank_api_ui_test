package iteration2;

import base.BaseTest;
import generators.RandomData;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.DepositRequester;
import requests.ReceiveAllUserAccountRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DepositMoneyTest extends BaseTest {

    @ParameterizedTest
    @ValueSource(doubles = {5000.0, 4999.99, 0.01})
    public void depositValidSumTest(double deposit) {
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

        long createdAccountId = createdAccount.getId();
        double initialBalance = createdAccount.getBalance();

        DepositRequest depositRequest = DepositRequest.builder()
                .id(createdAccountId)
                .balance(deposit)
                .build();

        DepositResponse depositResponse = new DepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest)
                .extract().as(DepositResponse.class);

        assertNotEquals(initialBalance, depositResponse.getBalance());
        assertEquals(deposit, depositResponse.getBalance());
    }

    @ParameterizedTest
    @ValueSource(doubles = {5000.01, 0, -0.01, 1.01124342})
    public void depositInvalidSumTest(double deposit) {
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

        long createdAccountId = createdAccount.getId();
        double initialBalance = createdAccount.getBalance();

        DepositRequest depositRequest = DepositRequest.builder()
                .id(createdAccountId)
                .balance(deposit)
                .build();

        new DepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest())
                .post(depositRequest);

        List<AccountModel> accounts = new ReceiveAllUserAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(null)
                .extract()
                .jsonPath()
                .getList(".", AccountModel.class);

        AccountModel account = accounts.stream()
                .filter(a -> a.getId() == createdAccountId)
                .findFirst()
                .orElse(null);
        assertEquals(initialBalance, account.getBalance());
    }

    @Test
    public void depositWrongAccountTest() {
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

        double initialBalance = createdAccount.getBalance();
        long createdAccountId = createdAccount.getId();

        DepositRequest depositRequest = DepositRequest.builder()
                .id(1234524567)
                .balance(100)
                .build();

        new DepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsUnauthorized())
                .post(depositRequest);

        List<AccountModel> accounts = new ReceiveAllUserAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(null)
                .extract()
                .jsonPath()
                .getList(".", AccountModel.class);

        AccountModel account = accounts.stream()
                .filter(a -> a.getId() == createdAccountId)
                .findFirst()
                .orElse(null);
        assertEquals(initialBalance, account.getBalance());
    }
}