package ui.iteration2;

import com.codeborne.selenide.*;
import api.models.AccountModel;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import api.requests.steps.AdminSteps;
import api.requests.steps.usersteps.UserStepsDeposit;
import ui.BaseUiTest;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

import static org.assertj.core.api.Assertions.assertThat;

public class DepositMoneyTest extends BaseUiTest {

    @ParameterizedTest
    @ValueSource(strings = {"5000.0", "4999.99", "0.01"})
    public void depositValidSumSendKeysTest(String deposit) {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);
        UserStepsDeposit userSteps = UserStepsDeposit.asUser(user);

        CreateAccountResponse createdAccount = userSteps.createAccount();

        String account = createdAccount.getAccountNumber();

        new UserDashboard().open()
                .depositMoney()
                .chooseAnAccount(account)
                .enterAmount(deposit)
                .depositClick()
                .andExpect()
                .checkAlertMessageAndAccept(BankAlert.SUCCESSFULLY_DEPOSITED.format(deposit, account));

        AccountModel foundAccount = userSteps.getAccountByNumber(createdAccount.getAccountNumber());
        double expectedBalance = Double.parseDouble(deposit);
        assertThat(foundAccount.getBalance()).isEqualTo(expectedBalance);
        assertThat(foundAccount.getBalance()).isNotEqualTo(createdAccount.getBalance());
    }

    @Test
    public void depositValidSumArrowsTest() {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);
        UserStepsDeposit userSteps = UserStepsDeposit.asUser(user);

        CreateAccountResponse createdAccount = userSteps.createAccount();

        String account = createdAccount.getAccountNumber();
        String actualAmount = "3";

        new UserDashboard().open()
                .depositMoney()
                .chooseAnAccount(account)
                .enterAmountArrowUp(3)
                .depositClick()
                .andExpect()
                .checkAlertMessageAndAccept(BankAlert.SUCCESSFULLY_DEPOSITED.format(actualAmount, account));

        AccountModel foundAccount = userSteps.getAccountByNumber(createdAccount.getAccountNumber());
        double expectedBalance = Double.parseDouble(actualAmount);
        assertThat(foundAccount.getBalance()).isEqualTo(expectedBalance);
        assertThat(foundAccount.getBalance()).isNotEqualTo(createdAccount.getBalance());
    }

    @ParameterizedTest
    @ValueSource(strings = {"5000.01", "10000", "1000000"})
    public void depositBiggerValidSumTest(String deposit) {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);
        UserStepsDeposit userSteps = UserStepsDeposit.asUser(user);

        CreateAccountResponse createdAccount = userSteps.createAccount();

        String account = createdAccount.getAccountNumber();

        new UserDashboard().open()
                .depositMoney()
                .chooseAnAccount(account)
                .enterAmount(deposit)
                .depositClick()
                .andExpect()
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_LESS_OR_EQUAL_5000.getMessage());

        AccountModel foundAccount = userSteps.getAccountByNumber(createdAccount.getAccountNumber());
        assertThat(foundAccount.getBalance()).isEqualTo(createdAccount.getBalance());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-0.01", "1.01124342"})
    public void depositInvalidSumTest(String deposit) {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);
        UserStepsDeposit userSteps = UserStepsDeposit.asUser(user);

        CreateAccountResponse createdAccount = userSteps.createAccount();

        String account = createdAccount.getAccountNumber();

        new UserDashboard().open()
                .depositMoney()
                .chooseAnAccount(account)
                .enterAmount(deposit)
                .depositClick()
                .andExpect()
                .checkAlertMessageAndAccept(BankAlert.INVALID_AMOUNT_DEPOSIT.getMessage());

        AccountModel foundAccount = userSteps.getAccountByNumber(createdAccount.getAccountNumber());
        assertThat(foundAccount.getBalance()).isEqualTo(createdAccount.getBalance());
    }

    @Test
    public void depositNotChoosingAccountAndSumTest() {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);
        UserStepsDeposit userSteps = UserStepsDeposit.asUser(user);

        CreateAccountResponse createdAccount = userSteps.createAccount();

        new UserDashboard().open()
                .depositMoney()
                .depositClick()
                .andExpect()
                .checkAlertMessageAndAccept(BankAlert.NO_ACCOUNT_DEPOSIT.getMessage());

        AccountModel foundAccount = userSteps.getAccountByNumber(createdAccount.getAccountNumber());
        assertThat(foundAccount.getBalance()).isEqualTo(createdAccount.getBalance());
    }
}