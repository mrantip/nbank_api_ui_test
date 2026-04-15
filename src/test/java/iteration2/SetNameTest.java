package iteration2;

import generators.RandomData;
import models.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.AdminCreateUserRequester;
import requests.ChangeNameRequester;
import requests.ProfileInfoRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SetNameTest {

    @ParameterizedTest
    @ValueSource(strings = {"Вася Пупкин", "вАсЯ пУпКин", "ввввввввввввввввввв ааааааааааааааааааааааааааааааа"})
    public void setValidNameTest(String name) {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        CreateUserResponse createUserResponse = new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest).extract().as(CreateUserResponse.class);

        String initialName = createUserResponse.getName();

        ChangeNameRequest newName = ChangeNameRequest.builder().name(name).build();

        ChangeNameResponse nameResult = new ChangeNameRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(newName)
                .extract().as(ChangeNameResponse.class);

        assertEquals("Profile updated successfully", nameResult.getMessage());
        assertEquals(newName.getName(), nameResult.getCustomer().getName());
        assertNotEquals(initialName, nameResult.getCustomer().getName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Вася_Пупкин", "в_АсЯ3 пУп4@Кин", "вася", "Вася Пупкин Младший", " ", "а"})
    public void setInvalidNameTest(String name) {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        CreateUserResponse createUserResponse = new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest).extract().as(CreateUserResponse.class);

        String initialName = createUserResponse.getName();

        ChangeNameRequest newName = ChangeNameRequest.builder().name("dd").build();

        new ChangeNameRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest())
                .post(newName);

        ProfileInfoResponse profileInfoResponse = new ProfileInfoRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(null)
                .extract().as(ProfileInfoResponse.class);

        assertEquals(initialName, profileInfoResponse.getName());
    }
}