package api.iteration2;

import api.models.ChangeNameResponse;
import api.models.CreateUserRequest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import api.requests.steps.AdminSteps;
import api.requests.steps.usersteps.UserStepsName;
import api.specs.RequestSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SetNameTest {

    @ParameterizedTest
    @ValueSource(strings = {"Вася Пупкин", "вАсЯ пУпКин", "ввввввввввввввввввв ааааааааааааааааааааааааааааааа"})
    public void setValidNameTest(String name) {
        CreateUserRequest userRequest = AdminSteps.createUser();
        UserStepsName userSteps = new UserStepsName(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));

        String initialName = userSteps.getProfileInfo().getName();

        ChangeNameResponse changeNameResponse = userSteps.updateName(name);

        assertEquals(name, changeNameResponse.getCustomer().getName());
        assertNotEquals(initialName, changeNameResponse.getCustomer().getName());

    }

    @ParameterizedTest
    @ValueSource(strings = {"Вася_Пупкин", "в_АсЯ3 пУп4@Кин", "вася", "Вася Пупкин Младший", " ", "а"})
    public void setInvalidNameTest(String name) {
        CreateUserRequest userRequest = AdminSteps.createUser();
        UserStepsName userSteps = new UserStepsName(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));

        String initialName = userSteps.getProfileInfo().getName();

        userSteps.updateInvalidName(name);

        String finalName = userSteps.getProfileInfo().getName();

        assertEquals(initialName, finalName);
    }
}