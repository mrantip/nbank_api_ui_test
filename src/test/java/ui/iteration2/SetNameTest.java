package ui.iteration2;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import models.CreateUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.Alert;
import requests.steps.AdminSteps;
import requests.steps.usersteps.UserStepsName;
import specs.RequestSpecs;

import java.util.Map;

import static com.codeborne.selenide.Selectors.withText;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SetNameTest {
    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://192.168.31.215:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"Вася Пупкин", "вАсЯ пУпКин", "ввввввввввввввввввв ааааааааааааааааааааааааааааааа"})
    public void setValidNameTest(String newName) {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        CreateUserRequest userRequest = AdminSteps.createUser();
        UserStepsName userSteps = new UserStepsName(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));

        open("/login");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(userRequest.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(userRequest.getPassword());
        $("button").click();

        // ШАГ 4: проверки начального имени на UI
        String usernameText = $(withText("Welcome")).$("span").text();
        String usernameText2 = $(".user-name").text().toLowerCase();

        assertEquals("noname", usernameText);
        assertEquals("noname", usernameText2);

        // ШАГ 5: юзер меняет имя
        $(Selectors.byAttribute("class", "user-username")).click();
        $(Selectors.byAttribute("placeholder", "Enter new name")).clear();
        sleep(100);
        $(Selectors.byAttribute("placeholder", "Enter new name")).sendKeys(newName);
        $(Selectors.withText("Save Changes")).click();

        // ШАГ 6: проверка алерта
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        String expectedMessage = "✅ Name updated successfully!";

        alert.accept();
        assertThat(alertText).isEqualTo(expectedMessage);

        // ШАГ 7: проверка изменений имени на UI
        $(Selectors.withText("Home")).click();
        assertEquals(newName, $(withText("Welcome")).$("span").text());
        assertEquals(newName, $(".user-name").text().toLowerCase());

        // ШАГ 8: проверка изменений имени на API
        assertEquals(newName, userSteps.getProfileInfo().getName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Вася_Пупкин", "в_АсЯ3 пУп4@Кин", "вася", "Вася Пупкин Младший", " ", "а"})
    public void setInvalidNameTest(String newName) {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        CreateUserRequest userRequest = AdminSteps.createUser();
        UserStepsName userSteps = new UserStepsName(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));

        open("/login");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(userRequest.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(userRequest.getPassword());
        $("button").click();

        // ШАГ 4: проверки начального имени на UI
        String usernameText = $(withText("Welcome")).$("span").text();
        String usernameText2 = $(".user-name").text().toLowerCase();

        assertEquals("noname", usernameText);
        assertEquals("noname", usernameText2);

        // ШАГ 5: юзер меняет имя
        $(Selectors.byAttribute("class", "user-username")).click();
        $(Selectors.byAttribute("placeholder", "Enter new name")).clear();
        $(Selectors.byAttribute("placeholder", "Enter new name")).sendKeys(newName);
        $(Selectors.withText("Save Changes")).click();

        // ШАГ 6: проверка алерта
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        String expectedMessage = "❌ Please enter a valid name.";

        alert.accept();
        assertThat(alertText).isEqualTo(expectedMessage);

        // ШАГ 7: проверка изменений имени на UI
        $(Selectors.withText("Home")).click();
        assertNotEquals(newName, $(withText("Welcome")).$("span").text());
        assertNotEquals(newName, $(".user-name").text().toLowerCase());

        // ШАГ 8: проверка изменений имени на API
        assertNotEquals(newName, userSteps.getProfileInfo().getName());
    }
}