package ui.iteration2;

import api.requests.steps.usersteps.UserSteps;
import api.models.CreateUserRequest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import api.requests.steps.AdminSteps;
import ui.BaseUiTest;
import ui.pages.BankAlert;
import ui.pages.EditProfile;
import ui.pages.UserDashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SetNameTest extends BaseUiTest {

    @ParameterizedTest
    @ValueSource(strings = {"Вася Пупкин", "вАсЯ пУпКин", "ввввввввввввввввввв ааааааааааааааааааааааааааааааа"})
    public void setValidNameTest(String newName) {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);
        UserDashboard userDashboard = new UserDashboard().open();

        assertEquals("noname", userDashboard.getWelcomeNameText());
        assertEquals("noname", userDashboard.getNameChangeButtonText());

        userDashboard.changeNameClick();

        EditProfile editProfile = new EditProfile().enterNewName(newName)
                .saveChangesButtonClick();

        userDashboard.checkAlertMessageAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage());

        editProfile.goHome();

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

        assertEquals("noname", userDashboard.getWelcomeNameText());
        assertEquals("noname", userDashboard.getNameChangeButtonText());

        userDashboard.changeNameClick();

        EditProfile editProfile = new EditProfile().enterNewName(newName)
                .saveChangesButtonClick();

        userDashboard.checkAlertMessageAndAccept(BankAlert.NAME_INVALID.getMessage());

        editProfile.goHome();

        assertNotEquals(newName, userDashboard.getWelcomeNameText());
        assertNotEquals(newName, userDashboard.getNameChangeButtonText());
        assertNotEquals(newName, new UserSteps(user.getUsername(), user.getPassword()).getProfileInfo().getName());
    }
}