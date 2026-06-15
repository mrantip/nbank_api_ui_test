package ui.iteration2;

import api.requests.steps.usersteps.UserSteps;
import api.models.CreateUserRequest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import api.requests.steps.AdminSteps;
import ui.BaseUiTest;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SetNameTest extends BaseUiTest {
    private static final String DEFAULT_USERNAME = "noname";

    @ParameterizedTest
    @ValueSource(strings = {"Вася Пупкин", "вАсЯ пУпКин", "ввввввввввввввввввв ааааааааааааааааааааааааааааааа"})
    public void setValidNameTest(String newName) {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);
        UserDashboard userDashboard = new UserDashboard().open();

        assertEquals(DEFAULT_USERNAME, userDashboard.getWelcomeNameText());
        assertEquals(DEFAULT_USERNAME, userDashboard.getNameChangeButtonText());

        userDashboard.changeNameClick()
                .enterNewName(newName)
                .saveChangesButtonClick()
                .checkAlertMessageAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage())
                .goHome();

        assertEquals(newName, userDashboard.getWelcomeNameText());
        assertEquals(newName, userDashboard.getNameChangeButtonText());
        assertEquals(newName, new UserSteps(user.getUsername(), user.getPassword()).getProfileInfo().getName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Вася_Пупкин", "в_АсЯ3 пУп4@Кин", "вася", "Вася Пупкин Младший", " ", "а"})
    public void setInvalidNameTest(String newName) {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);
        UserDashboard userDashboard = new UserDashboard().open();

        assertEquals(DEFAULT_USERNAME, userDashboard.getWelcomeNameText());
        assertEquals(DEFAULT_USERNAME, userDashboard.getNameChangeButtonText());

        userDashboard.changeNameClick()
                .enterNewName(newName)
                .saveChangesButtonClick()
                .checkAlertMessageAndAccept(BankAlert.NAME_INVALID.getMessage())
                .goHome();

        assertNotEquals(newName, userDashboard.getWelcomeNameText());
        assertNotEquals(newName, userDashboard.getNameChangeButtonText());
        assertNotEquals(newName, new UserSteps(user.getUsername(), user.getPassword()).getProfileInfo().getName());
    }
}