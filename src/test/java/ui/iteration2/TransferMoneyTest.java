package ui.iteration2;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.Alert;
import requests.steps.AdminSteps;
import requests.steps.usersteps.UserStepsDeposit;
import specs.RequestSpecs;

import java.util.Map;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class TransferMoneyTest {
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
    @ValueSource(strings = {"10000", "9999.99", "0.01", "500"})
    public void transferValidSumToAccountTest(String transfer) {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает 2 аккаунта
        // ШАГ 5: юзер вносит депозит
        CreateUserRequest userRequest = AdminSteps.createUser();

        UserStepsDeposit userSteps = new UserStepsDeposit(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));

        CreateAccountResponse createdAccountFirst = userSteps.createAccount();
        CreateAccountResponse createdAccountSecond = userSteps.createAccount();

        String accountFirst = createdAccountFirst.getAccountNumber();
        String accountSecond = createdAccountSecond.getAccountNumber();
        double initialBalanceFirst = createdAccountFirst.getBalance();
        double initialBalanceSecond = createdAccountSecond.getBalance();

        userSteps.depositMaxMultipleTimes(createdAccountFirst.getAccountNumber(), 3);

        open("/login");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(userRequest.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(userRequest.getPassword());
        $("button").click();

        // ШАГ 6: юзер выполняет перевод
        $(Selectors.withText("Make a Transfer")).click();

        $(Selectors.byCssSelector("select.form-control.account-selector")).click();

        SelenideElement targetOption = $$("option")
                .findBy(Condition.text(accountFirst));
        targetOption.shouldBe(Condition.visible)
                .shouldHave(Condition.text(accountFirst));
        targetOption.click();
        targetOption.shouldBe(Condition.selected);

        $(Selectors.byAttribute("placeholder", "Enter recipient name")).sendKeys("Alesha");
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).sendKeys(accountSecond);
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(transfer);
        $(Selectors.byAttribute("id", "confirmCheck")).click();
        $(Selectors.byCssSelector("button.btn-primary.shadow-custom.green-btn.mt-4")).click();

        // ШАГ 7: проверка алерта
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        String expectedMessage = "✅ Successfully transferred $" + transfer + " to account " + accountSecond + "!";
        alert.accept();
        assertThat(alertText).isEqualTo(expectedMessage);

        // ШАГ 8: проверка на API изменений баланса
        assertThat(userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber()).getBalance()).isNotEqualTo(initialBalanceFirst);
        assertThat(userSteps.getAccountByNumber(createdAccountSecond.getAccountNumber()).getBalance()).isNotEqualTo(initialBalanceSecond);

    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-0.01", "10000.01"})
    public void transferInvalidSumToAccountTest(String transfer) {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает 2 аккаунта
        // ШАГ 5: юзер вносит депозит
        CreateUserRequest userRequest = AdminSteps.createUser();

        UserStepsDeposit userSteps = new UserStepsDeposit(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));

        CreateAccountResponse createdAccountFirst = userSteps.createAccount();
        CreateAccountResponse createdAccountSecond = userSteps.createAccount();

        String accountFirst = createdAccountFirst.getAccountNumber();
        String accountSecond = createdAccountSecond.getAccountNumber();

        userSteps.depositMaxMultipleTimes(createdAccountFirst.getAccountNumber(), 3);

        double initialBalanceFirst = userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber()).getBalance();
        double initialBalanceSecond = userSteps.getAccountByNumber(createdAccountSecond.getAccountNumber()).getBalance();

        open("/login");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(userRequest.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(userRequest.getPassword());
        $("button").click();

        // ШАГ 6: юзер выполняет перевод
        $(Selectors.withText("Make a Transfer")).click();

        $(Selectors.byCssSelector("select.form-control.account-selector")).click();

        SelenideElement targetOption = $$("option")
                .findBy(Condition.text(accountFirst));
        targetOption.shouldBe(Condition.visible)
                .shouldHave(Condition.text(accountFirst));
        targetOption.click();
        targetOption.shouldBe(Condition.selected);

        $(Selectors.byAttribute("placeholder", "Enter recipient name")).sendKeys("Alesha");
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).sendKeys(accountSecond);
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(transfer);
        $(Selectors.byAttribute("id", "confirmCheck")).click();
        $(Selectors.byCssSelector("button.btn-primary.shadow-custom.green-btn.mt-4")).click();

        // ШАГ 7: проверка алерта
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        String expectedMessage = "❌ Error: Invalid transfer: insufficient funds or invalid accounts";
        alert.accept();
        assertThat(alertText).isEqualTo(expectedMessage);

        // ШАГ 8: проверка на API изменений баланса
        assertThat(userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber()).getBalance()).isEqualTo(initialBalanceFirst);
        assertThat(userSteps.getAccountByNumber(createdAccountSecond.getAccountNumber()).getBalance()).isEqualTo(initialBalanceSecond);
    }

    @Test
    public void transferNotChoosingTransferDataTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает аккаунт
        // ШАГ 5: юзер вносит депозит
        CreateUserRequest userRequest = AdminSteps.createUser();

        UserStepsDeposit userSteps = new UserStepsDeposit(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));

        CreateAccountResponse createdAccountFirst = userSteps.createAccount();

        String accountFirst = createdAccountFirst.getAccountNumber();

        userSteps.depositMaxMultipleTimes(createdAccountFirst.getAccountNumber(), 3);

        double initialBalanceFirst = userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber()).getBalance();

        open("/login");

        $(Selectors.byAttribute("placeholder", "Username")).sendKeys(userRequest.getUsername());
        $(Selectors.byAttribute("placeholder", "Password")).sendKeys(userRequest.getPassword());
        $("button").click();

        // ШАГ 6: юзер в форме перевода не заполняет данные
        $(Selectors.withText("Make a Transfer")).click();

        $(Selectors.byCssSelector("select.form-control.account-selector")).click();

        SelenideElement targetOption = $$("option")
                .findBy(Condition.text("-- Choose an account --"));
        targetOption.shouldBe(Condition.visible)
                .shouldHave(Condition.text("-- Choose an account --"));
        targetOption.click();
        targetOption.shouldBe(Condition.selected);

        $(Selectors.byAttribute("placeholder", "Enter recipient name")).clear();
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).clear();
        $(Selectors.byAttribute("placeholder", "Enter amount")).clear();
        $(Selectors.byAttribute("id", "confirmCheck")).shouldBe(Condition.enabled);
        $(Selectors.byCssSelector("button.btn-primary.shadow-custom.green-btn.mt-4")).click();

        // ШАГ 7: проверка алерта
        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        String expectedMessage = "❌ Please fill all fields and confirm.";
        alert.accept();
        assertThat(alertText).isEqualTo(expectedMessage);

        // ШАГ 8: проверка на API изменений баланса
        assertThat(userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber()).getBalance()).isEqualTo(initialBalanceFirst);
    }
}