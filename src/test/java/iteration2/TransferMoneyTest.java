package iteration2;

import base.BaseTest;
import generators.RandomData;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.*;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.List;


public class TransferMoneyTest extends BaseTest {

    @ParameterizedTest
    @ValueSource(doubles = {10000, 9999.99, 0.01, 500})
    public void transferValidSumToOwnAccountTest1(double transferSum) {
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
                .post(null)
                .extract().as(CreateAccountResponse.class);

        long createdAccountId = createdAccount.getId();

        DepositRequest depositRequest = DepositRequest.builder()
                .id(createdAccountId)
                .balance(5000)
                .build();

        new DepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        DepositResponse depositResponse = new DepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest)
                .extract().as(DepositResponse.class);

        double initialBalance = depositResponse.getBalance();

        CreateAccountResponse createdAccount2 = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract().as(CreateAccountResponse.class);

        double initialBalance2 = createdAccount2.getBalance();
        long createdAccountId2 = createdAccount2.getId();

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(createdAccountId)
                .receiverAccountId(createdAccountId2)
                .amount(transferSum)
                .build();

        TransferResponse transferResponse = new TransferRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(transferRequest)
                .extract().as(TransferResponse.class);

        List<AccountModel> accounts = new ReceiveAllUserAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(null)
                .extract()
                .jsonPath()
                .getList(".", AccountModel.class);

        AccountModel account1 = accounts.stream()
                .filter(a -> a.getId() == createdAccountId)
                .findFirst()
                .orElse(null);

        AccountModel account2 = accounts.stream()
                .filter(a -> a.getId() == createdAccountId2)
                .findFirst()
                .orElse(null);

        softly.assertThat(transferResponse.getMessage()).isEqualTo("Transfer successful");
        softly.assertThat(initialBalance).isNotEqualTo(account1.getBalance());
        softly.assertThat(initialBalance2).isNotEqualTo(account2.getBalance());
    }

    @ParameterizedTest
    @ValueSource(doubles = {10000, 9999.99, 0.01, 500})
    public void transferValidSumToStrangersAccountTest(double transferSum) {
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
                .post(null)
                .extract().as(CreateAccountResponse.class);

        long createdAccountId = createdAccount.getId();

        DepositRequest depositRequest = DepositRequest.builder()
                .id(createdAccountId)
                .balance(5000)
                .build();

        new DepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        DepositResponse depositResponse = new DepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest)
                .extract().as(DepositResponse.class);

        double initialBalance = depositResponse.getBalance();

        CreateUserRequest userRequest2 = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest2);

        CreateAccountResponse createdAccount2 = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest2.getUsername(), userRequest2.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract().as(CreateAccountResponse.class);

        double initialBalance2 = createdAccount2.getBalance();
        long createdAccountId2 = createdAccount2.getId();

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(createdAccountId)
                .receiverAccountId(createdAccountId2)
                .amount(transferSum)
                .build();

        TransferResponse transferResponse = new TransferRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(transferRequest)
                .extract().as(TransferResponse.class);

        List<AccountModel> accounts = new ReceiveAllUserAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(null)
                .extract()
                .jsonPath()
                .getList(".", AccountModel.class);

        AccountModel account1 = accounts.stream()
                .filter(a -> a.getId() == createdAccountId)
                .findFirst()
                .orElse(null);

        List<AccountModel> accounts2 = new ReceiveAllUserAccountRequester(RequestSpecs.authAsUser(userRequest2.getUsername(), userRequest2.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(null)
                .extract()
                .jsonPath()
                .getList(".", AccountModel.class);

        AccountModel account2 = accounts2.stream()
                .filter(a -> a.getId() == createdAccountId2)
                .findFirst()
                .orElse(null);

        softly.assertThat(transferResponse.getMessage()).isEqualTo("Transfer successful");
        softly.assertThat(initialBalance).isNotEqualTo(account1.getBalance());
        softly.assertThat(initialBalance2).isNotEqualTo(account2.getBalance());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -0.01, 10000.01})
    public void transferInvalidSumToOwnAccountTest(double transferSum) {
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
                .post(null)
                .extract().as(CreateAccountResponse.class);

        long createdAccountId = createdAccount.getId();

        DepositRequest depositRequest = DepositRequest.builder()
                .id(createdAccountId)
                .balance(5000)
                .build();

        new DepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        DepositResponse depositResponse = new DepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest)
                .extract().as(DepositResponse.class);

        double initialBalance = depositResponse.getBalance();

        CreateAccountResponse createdAccount2 = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract().as(CreateAccountResponse.class);

        double initialBalance2 = createdAccount2.getBalance();
        long createdAccountId2 = createdAccount2.getId();

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(createdAccountId)
                .receiverAccountId(createdAccountId2)
                .amount(transferSum)
                .build();

        new TransferRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest())
                .post(transferRequest);

        List<AccountModel> accounts = new ReceiveAllUserAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(null)
                .extract()
                .jsonPath()
                .getList(".", AccountModel.class);

        AccountModel account1 = accounts.stream()
                .filter(a -> a.getId() == createdAccountId)
                .findFirst()
                .orElse(null);

        AccountModel account2 = accounts.stream()
                .filter(a -> a.getId() == createdAccountId2)
                .findFirst()
                .orElse(null);

        softly.assertThat(initialBalance).isEqualTo(account1.getBalance());
        softly.assertThat(initialBalance2).isEqualTo(account2.getBalance());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -0.01, 10000.01})
    public void transferInvalidSumToStrangersAccountTest(double transferSum) {
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
                .post(null)
                .extract().as(CreateAccountResponse.class);

        long createdAccountId = createdAccount.getId();

        DepositRequest depositRequest = DepositRequest.builder()
                .id(createdAccountId)
                .balance(5000)
                .build();

        new DepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        DepositResponse depositResponse = new DepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest)
                .extract().as(DepositResponse.class);

        double initialBalance = depositResponse.getBalance();

        CreateUserRequest userRequest2 = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest2);

        CreateAccountResponse createdAccount2 = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest2.getUsername(), userRequest2.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract().as(CreateAccountResponse.class);

        double initialBalance2 = createdAccount2.getBalance();
        long createdAccountId2 = createdAccount2.getId();

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(createdAccountId)
                .receiverAccountId(createdAccountId2)
                .amount(0)
                .build();

        new TransferRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest())
                .post(transferRequest);

        List<AccountModel> accounts = new ReceiveAllUserAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(null)
                .extract()
                .jsonPath()
                .getList(".", AccountModel.class);

        AccountModel account1 = accounts.stream()
                .filter(a -> a.getId() == createdAccountId)
                .findFirst()
                .orElse(null);

        List<AccountModel> accounts2 = new ReceiveAllUserAccountRequester(RequestSpecs.authAsUser(userRequest2.getUsername(), userRequest2.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(null)
                .extract()
                .jsonPath()
                .getList(".", AccountModel.class);

        AccountModel account2 = accounts2.stream()
                .filter(a -> a.getId() == createdAccountId2)
                .findFirst()
                .orElse(null);

        softly.assertThat(initialBalance).isEqualTo(account1.getBalance());
        softly.assertThat(initialBalance2).isEqualTo(account2.getBalance());
    }

    @Test
    public void transferSumHigherThanBalanceTest() {
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
                .post(null)
                .extract().as(CreateAccountResponse.class);

        long createdAccountId = createdAccount.getId();

        DepositRequest depositRequest = DepositRequest.builder()
                .id(createdAccountId)
                .balance(5000)
                .build();

        DepositResponse depositResponse = new DepositRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest)
                .extract().as(DepositResponse.class);

        double initialBalance = depositResponse.getBalance();

        CreateAccountResponse createdAccount2 = new CreateAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract().as(CreateAccountResponse.class);

        double initialBalance2 = createdAccount2.getBalance();
        long createdAccountId2 = createdAccount2.getId();

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(createdAccountId)
                .receiverAccountId(createdAccountId2)
                .amount(5000.01)
                .build();

        new TransferRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest())
                .post(transferRequest);

        List<AccountModel> accounts = new ReceiveAllUserAccountRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(null)
                .extract()
                .jsonPath()
                .getList(".", AccountModel.class);

        AccountModel account1 = accounts.stream()
                .filter(a -> a.getId() == createdAccountId)
                .findFirst()
                .orElse(null);

        AccountModel account2 = accounts.stream()
                .filter(a -> a.getId() == createdAccountId2)
                .findFirst()
                .orElse(null);

        softly.assertThat(initialBalance).isEqualTo(account1.getBalance());
        softly.assertThat(initialBalance2).isEqualTo(account2.getBalance());
    }
}