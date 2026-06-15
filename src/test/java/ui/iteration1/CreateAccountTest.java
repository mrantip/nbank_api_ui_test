package ui.iteration1;

import api.models.AccountModel;
import api.requests.steps.usersteps.UserSteps;
import api.models.CreateUserRequest;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import api.requests.steps.AdminSteps;
import ui.BaseUiTest;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUiTest {

    @Test
    @UserSession
    public void userCanCreateAccountTest() {
//        CreateUserRequest user = AdminSteps.createUser();
//
//        authAsUser(user);

        new UserDashboard().open().createNewAccount();

        List<AccountModel> createdAccounts = SessionStorage.getSteps()
                .getAllUserAccounts();

        assertThat(createdAccounts).hasSize(1);

        new UserDashboard().checkAlertMessageAndAccept
                (BankAlert.NEW_ACCOUNT_CREATED.getMessage() + createdAccounts.getFirst().getAccountNumber());

        assertThat(createdAccounts.getFirst().getBalance()).isZero();
    }
}