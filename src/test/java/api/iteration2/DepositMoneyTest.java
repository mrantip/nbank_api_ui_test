package api.iteration2;

import api.base.BaseTest;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.steps.AdminSteps;
import requests.steps.usersteps.UserStepsDeposit;
import specs.RequestSpecs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class DepositMoneyTest extends BaseTest {

    @ParameterizedTest
    @ValueSource(doubles = {5000.0, 4999.99, 0.01})
    public void depositValidSumTest(double deposit) {
        CreateUserRequest userRequest = AdminSteps.createUser();
        UserStepsDeposit userSteps = new UserStepsDeposit(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));

        CreateAccountResponse createdAccount = userSteps.createAccount();

        DepositResponse depositResponse = userSteps.deposit(createdAccount.getAccountNumber(), deposit);

        assertNotEquals(createdAccount.getBalance(), depositResponse.getBalance());
        assertEquals(deposit, depositResponse.getBalance());
    }


    @ParameterizedTest
    @ValueSource(doubles = {5000.01, 0, -0.01, 1.01124342})
    public void depositInvalidSumTest(double deposit) {
        CreateUserRequest userRequest = AdminSteps.createUser();
        UserStepsDeposit userSteps = new UserStepsDeposit(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));

        CreateAccountResponse createdAccount = userSteps.createAccount();

        userSteps.depositInvalidAmount(createdAccount.getAccountNumber(), deposit);

        AccountModel foundAccount = userSteps.getAccountByNumber(createdAccount.getAccountNumber());

        assertThat(foundAccount.getBalance()).isEqualTo(createdAccount.getBalance());
    }

    @Test
    public void depositToStrangerAccountTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();
        UserStepsDeposit userSteps = new UserStepsDeposit(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));

        CreateAccountResponse createdAccount = userSteps.createAccount();

        CreateUserRequest userRequestStranger = AdminSteps.createUser();
        UserStepsDeposit userStepsStranger = new UserStepsDeposit(RequestSpecs.authAsUser(userRequestStranger.getUsername(), userRequestStranger.getPassword()));

        CreateAccountResponse createdAccountStranger = userStepsStranger.createAccount();

        userSteps.depositToStrangerAccount(createdAccountStranger.getId());

        AccountModel foundAccount = userSteps.getAccountByNumber(createdAccount.getAccountNumber());
        AccountModel foundAccountStranger = userStepsStranger.getAccountByNumber(createdAccountStranger.getAccountNumber());

        assertThat(foundAccount.getBalance()).isEqualTo(createdAccount.getBalance());
        assertThat(foundAccountStranger.getBalance()).isEqualTo(createdAccountStranger.getBalance());
    }
}