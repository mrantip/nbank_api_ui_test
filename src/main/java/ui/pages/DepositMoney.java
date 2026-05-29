package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import org.openqa.selenium.Keys;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

@Getter
public class DepositMoney extends BasePage<DepositMoney> {
    private SelenideElement chooseAnAccount = $(Selectors.byCssSelector("select.form-control.account-selector"));
    private SelenideElement enterAmount = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private SelenideElement depositButton = $(Selectors.byCssSelector("button.btn.btn-primary.shadow-custom.mt-4"));


    @Override
    public String url() {
        return "/deposit";
    }

    public DepositMoney chooseAnAccount(String account) {
        chooseAnAccount.click();
        SelenideElement targetOption = $$("option")
                .findBy(Condition.text(account));
        targetOption.shouldBe(Condition.visible)
                .shouldHave(Condition.text(account));
        targetOption.click();
        targetOption.shouldBe(Condition.selected);
        return this;
    }

    public DepositMoney enterAmount(String amount) {
        enterAmount.sendKeys(amount);
        return this;
    }

    public DepositMoney depositClick() {
        depositButton.click();
        return this;
    }

    public DepositMoney enterAmountArrowUp(int amountPress) {
        for (int i = 0; i < amountPress; i++) {
            enterAmount.press(Keys.UP);
        }
        return this;
    }

    public DepositMoney enterAmountArrowDown(int amountPress) {
        for (int i = 0; i < amountPress; i++) {
            enterAmount.press(Keys.DOWN);
        }
        return this;
    }
}