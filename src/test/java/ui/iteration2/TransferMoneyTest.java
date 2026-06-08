package ui.iteration2;

import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import common.context.UserStepsWithAccountAndDeposit;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import api.requests.steps.AdminSteps;
import api.requests.steps.usersteps.UserStepsDeposit;
import ui.BaseUiTest;
import ui.pages.BankAlert;
import ui.pages.MakeATransfer;
import ui.pages.UserDashboard;

import static org.assertj.core.api.Assertions.assertThat;

public class TransferMoneyTest extends BaseUiTest {
    //, "9999.99", "0.01", "500"
    @ParameterizedTest
    @ValueSource(strings = {"10000"})
    @UserSession(value = 2, withAccountForAll = true)
    public void transferValidSumToAccountTest(String transfer) {
//        UserStepsWithAccount user1 = SessionStorage.getUserWithAccount(1);
//        UserStepsWithAccount user2 = SessionStorage.getUserWithAccount(2);
//        String accountNumber1 = user1.getAccountNumber();UserStepsWithAccount user = SessionStorage.getCurrentUserWithAccount();
//        String accountNumber2 = user2.getAccountNumber();
//        System.out.println(accountNumber1);
//        System.out.println(accountNumber2);
//        user1.
//        CreateUserRequest user = AdminSteps.createUser();
//
//        authAsUser(user);
//        UserStepsDeposit userSteps = UserStepsDeposit.asUser(user);
//
//        CreateAccountResponse createdAccountFirst = userSteps.createAccount();
//        CreateAccountResponse createdAccountSecond = userSteps.createAccount();
//
//        String accountFirst = createdAccountFirst.getAccountNumber();
//        String accountSecond = createdAccountSecond.getAccountNumber();
//
//        userSteps.depositMaxMultipleTimes(createdAccountFirst.getAccountNumber(), 3);
//
//        double initialBalanceFirst = userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber()).getBalance();
//        double initialBalanceSecond = userSteps.getAccountByNumber(createdAccountSecond.getAccountNumber()).getBalance();
//
//        UserDashboard userDashboard = new UserDashboard().open().makeATransfer();
//        new MakeATransfer().chooseAnAccount(accountFirst)
//                .enterRecipientName("Gosha")
//                .enterRecipientAccountNumber(accountSecond)
//                .enterAmount(transfer)
//                .confirmDetailsAreCorrect()
//                .sendTransferClick();
//
//        userDashboard.checkAlertMessageAndAccept(BankAlert.SUCCESSFULLY_TRANSFERRED.format(transfer, accountSecond));
//
//        assertThat(userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber()).getBalance()).isNotEqualTo(initialBalanceFirst);
//        assertThat(userSteps.getAccountByNumber(createdAccountSecond.getAccountNumber()).getBalance()).isNotEqualTo(initialBalanceSecond);
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

        UserDashboard userDashboard = new UserDashboard().open().makeATransfer();
        new MakeATransfer().chooseAnAccount(accountFirst)
                .enterRecipientName("Gosha")
                .enterRecipientAccountNumber(accountSecond)
                .enterAmount(transfer)
                .confirmDetailsAreCorrect()
                .sendTransferClick();

        userDashboard.checkAlertMessageAndAccept(BankAlert.INVALID_TRANSFER.format(transfer, accountSecond));

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

        UserDashboard userDashboard = new UserDashboard().open().makeATransfer();
        new MakeATransfer().sendTransferClick();

        userDashboard.checkAlertMessageAndAccept(BankAlert.NO_DATA_FOR_TRANSFER.getMessage());

        assertThat(userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber()).getBalance()).isEqualTo(initialBalanceFirst);
    }
}