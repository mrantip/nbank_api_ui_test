package ui.iteration2;

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

public class TransferMoneyTest extends BaseUiTest {

    @ParameterizedTest
    @ValueSource(strings = {"10000", "9999.99", "0.01", "500"})
    public void transferValidSumToAccountTest(String transfer) {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);
        UserStepsDeposit userSteps = UserStepsDeposit.asUser(user);

        CreateAccountResponse createdAccountFirst = userSteps.createAccount();
        CreateAccountResponse createdAccountSecond = userSteps.createAccount();

        String accountFirst = createdAccountFirst.getAccountNumber();
        String accountSecond = createdAccountSecond.getAccountNumber();

        userSteps.depositMaxMultipleTimes(createdAccountFirst.getAccountNumber(), 3);

        double initialBalanceFirst = userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber()).getBalance();
        double initialBalanceSecond = userSteps.getAccountByNumber(createdAccountSecond.getAccountNumber()).getBalance();

        new UserDashboard().open()
                .makeATransfer()
                .chooseAnAccount(accountFirst)
                .enterRecipientName("Gosha")
                .enterRecipientAccountNumber(accountSecond)
                .enterAmount(transfer)
                .confirmDetailsAreCorrect()
                .sendTransferClick()
                .checkAlertMessageAndAccept(BankAlert.SUCCESSFULLY_TRANSFERRED.format(transfer, accountSecond));

        assertThat(userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber()).getBalance()).isNotEqualTo(initialBalanceFirst);
        assertThat(userSteps.getAccountByNumber(createdAccountSecond.getAccountNumber()).getBalance()).isNotEqualTo(initialBalanceSecond);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-0.01", "10000.01"})
    public void transferInvalidSumToAccountTest(String transfer) {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);
        UserStepsDeposit userSteps = UserStepsDeposit.asUser(user);

        CreateAccountResponse createdAccountFirst = userSteps.createAccount();
        CreateAccountResponse createdAccountSecond = userSteps.createAccount();

        String accountFirst = createdAccountFirst.getAccountNumber();
        String accountSecond = createdAccountSecond.getAccountNumber();
        userSteps.depositMaxMultipleTimes(createdAccountFirst.getAccountNumber(), 3);

        double initialBalanceFirst = userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber()).getBalance();
        double initialBalanceSecond = userSteps.getAccountByNumber(createdAccountSecond.getAccountNumber()).getBalance();

        new UserDashboard().open()
                .makeATransfer()
                .chooseAnAccount(accountFirst)
                .enterRecipientName("Gosha")
                .enterRecipientAccountNumber(accountSecond)
                .enterAmount(transfer)
                .confirmDetailsAreCorrect()
                .sendTransferClick()
                .checkAlertMessageAndAccept(BankAlert.INVALID_TRANSFER.format(transfer, accountSecond));

        assertThat(userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber()).getBalance()).isEqualTo(initialBalanceFirst);
        assertThat(userSteps.getAccountByNumber(createdAccountSecond.getAccountNumber()).getBalance()).isEqualTo(initialBalanceSecond);
    }

    @Test
    public void transferNotChoosingTransferDataTest() {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);
        UserStepsDeposit userSteps = UserStepsDeposit.asUser(user);

        CreateAccountResponse createdAccountFirst = userSteps.createAccount();

        double initialBalanceFirst = userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber()).getBalance();

        new UserDashboard().open()
                .makeATransfer()
                .sendTransferClick()
                .checkAlertMessageAndAccept(BankAlert.NO_DATA_FOR_TRANSFER.getMessage());

        assertThat(userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber()).getBalance()).isEqualTo(initialBalanceFirst);
    }
}