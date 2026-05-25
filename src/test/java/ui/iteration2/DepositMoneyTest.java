package ui.iteration2;

import com.codeborne.selenide.*;
import models.AccountModel;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Keys;
import requests.steps.AdminSteps;
import requests.steps.usersteps.UserStepsDeposit;
import specs.RequestSpecs;

import java.util.Map;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DepositMoneyTest {
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
    @ValueSource(strings = {"5000.0", "4999.99", "0.01"})
    public void depositValidSumSendKeysTest(String deposit) {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает аккаунт
        CreateUserRequest userRequest = AdminSteps.createUser();

        UserStepsDeposit userSteps = new UserStepsDeposit(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));

        CreateAccountResponse createdAccount = userSteps.createAccount();

        String account = createdAccount.getAccountNumber();

        open("/login");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(userRequest.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(userRequest.getPassword());
        $("button").click();

        // ШАГ 5: юзер вносит депозит, ввод суммы с клавиатуры
        $(Selectors.withText("Deposit Money")).click();

        $(Selectors.byCssSelector("select.form-control.account-selector")).click();

        SelenideElement targetOption = $$("option")
                .findBy(Condition.text(account));
        targetOption.shouldBe(Condition.visible)
                .shouldHave(Condition.text(account));
        targetOption.click();
        targetOption.shouldBe(Condition.selected);

        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(deposit);

        $(Selectors.byCssSelector("button.btn.btn-primary.shadow-custom.mt-4")).click();

        // ШАГ 6: проверка алерта
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        String expectedMessage = "✅ Successfully deposited $" + deposit + " to account " + account + "!";

        alert.accept();
        assertThat(alertText).isEqualTo(expectedMessage);

        // ШАГ 7: проверка на API изменений баланса
        AccountModel foundAccount = userSteps.getAccountByNumber(createdAccount.getAccountNumber());
        double expectedBalance = Double.parseDouble(deposit);
        assertThat(foundAccount.getBalance()).isEqualTo(expectedBalance);
        assertEquals(expectedBalance, foundAccount.getBalance());
        assertThat(foundAccount.getBalance()).isNotEqualTo(createdAccount.getBalance());
    }

    @Test
    public void depositValidSumArrowsTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает аккаунт
        CreateUserRequest userRequest = AdminSteps.createUser();

        UserStepsDeposit userSteps = new UserStepsDeposit(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));

        CreateAccountResponse createdAccount = userSteps.createAccount();

        String account = createdAccount.getAccountNumber();

        open("/login");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(userRequest.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(userRequest.getPassword());
        $("button").click();

        // ШАГ 5: юзер вносит депозит, ввод суммы стрелками
        $(Selectors.withText("Deposit Money")).click();

        $(Selectors.byCssSelector("select.form-control.account-selector")).click();

        SelenideElement targetOption = $$("option")
                .findBy(Condition.text(account));
        targetOption.shouldBe(Condition.visible)
                .shouldHave(Condition.text(account));
        targetOption.click();
        targetOption.shouldBe(Condition.selected);

        // Работа со стрелками
        SelenideElement amountField = $(Selectors.byAttribute("placeholder", "Enter amount"));
        amountField.clear();

        // Нажимаем стрелку UP несколько раз (например, 3 раза)
        int pressCount = 3;
        for (int i = 0; i < pressCount; i++) {
            amountField.press(Keys.UP);
        }

        // Получаем значение из поля после нажатий
        String actualAmount = amountField.getAttribute("value");

        // Преобразуем в число для проверки
        int expectedAmount = pressCount;

        $(Selectors.byCssSelector("button.btn.btn-primary.shadow-custom.mt-4")).click();

        // ШАГ 6: проверка алерта
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        String expectedMessage = "✅ Successfully deposited $" + actualAmount + " to account " + account + "!";

        alert.accept();
        assertThat(alertText).isEqualTo(expectedMessage);

        // ШАГ 7: проверка на API изменений баланса
        AccountModel foundAccount = userSteps.getAccountByNumber(createdAccount.getAccountNumber());
        double expectedBalance = Double.parseDouble(actualAmount);
        assertThat(foundAccount.getBalance()).isEqualTo(expectedBalance);
        assertEquals(expectedBalance, foundAccount.getBalance());
        assertThat(foundAccount.getBalance()).isNotEqualTo(createdAccount.getBalance());
    }

    @ParameterizedTest
    @ValueSource(strings = {"5000.01", "0", "-0.01", "1.01124342"})
    public void depositInvalidSumTest(String deposit) {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает аккаунт
        CreateUserRequest userRequest = AdminSteps.createUser();

        UserStepsDeposit userSteps = new UserStepsDeposit(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));

        CreateAccountResponse createdAccount = userSteps.createAccount();

        String account = createdAccount.getAccountNumber();

        open("/login");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(userRequest.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(userRequest.getPassword());
        $("button").click();

        // ШАГ 5: юзер вносит депозит, некорректная сумма
        $(Selectors.withText("Deposit Money")).click();

        $(Selectors.byCssSelector("select.form-control.account-selector")).click();

        SelenideElement targetOption = $$("option")
                .findBy(Condition.text(account));
        targetOption.shouldBe(Condition.visible)
                .shouldHave(Condition.text(account));
        targetOption.click();
        targetOption.shouldBe(Condition.selected);

        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(deposit);

        $(Selectors.byCssSelector("button.btn.btn-primary.shadow-custom.mt-4")).click();

        // ШАГ 6: проверка алерта
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        String expectedMessage = "❌ Please enter a valid amount.";

        alert.accept();
        assertThat(alertText).isEqualTo(expectedMessage);

        // ШАГ 7: проверка на API изменений баланса
        AccountModel foundAccount = userSteps.getAccountByNumber(createdAccount.getAccountNumber());
        assertThat(foundAccount.getBalance()).isEqualTo(createdAccount.getBalance());
    }

    @Test
    public void depositNotChoosingAccountAndSumTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        CreateUserRequest userRequest = AdminSteps.createUser();

        UserStepsDeposit userSteps = new UserStepsDeposit(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));

        CreateAccountResponse createdAccount = userSteps.createAccount();

        String account = createdAccount.getAccountNumber();

        open("/login");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(userRequest.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(userRequest.getPassword());
        $("button").click();

        // ШАГ 4: юзер не вводит данные в форме депозита
        $(Selectors.withText("Deposit Money")).click();

        $(Selectors.byCssSelector("button.btn.btn-primary.shadow-custom.mt-4")).click();

        // ШАГ 5: проверка алерта
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        String expectedMessage = "❌ Please select an account.";

        alert.accept();
        assertThat(alertText).isEqualTo(expectedMessage);

        // ШАГ 6: проверка на API изменений баланса
        AccountModel foundAccount = userSteps.getAccountByNumber(createdAccount.getAccountNumber());
        assertThat(foundAccount.getBalance()).isEqualTo(createdAccount.getBalance());
    }
}