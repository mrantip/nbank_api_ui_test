package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import common.utils.RetryUtils;

import static com.codeborne.selenide.Selenide.*;

public class MakeATransfer extends BasePage<MakeATransfer> {
    private SelenideElement chooseAnAccount = $(Selectors.byCssSelector("select.form-control.account-selector"));
    private SelenideElement enterRecipientName = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
    private SelenideElement enterRrecipientAccountNumber = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
    private SelenideElement enterAmount = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private SelenideElement confirmDetails = $(Selectors.byAttribute("id", "confirmCheck"));
    private SelenideElement sendTransfer = $(Selectors.byCssSelector("button.btn-primary.shadow-custom.green-btn.mt-4"));

    @Override
    public String url() {
        return "/transfer";
    }

    public MakeATransfer chooseAnAccount(String account) {
        chooseAnAccount.shouldBe(Condition.enabled, Condition.visible).click();

        // XPath поиск по точному тексту
        SelenideElement targetOption = RetryUtils.retry(
                () -> $x(String.format("//option[contains(text(), '%s')]", account)),
                element -> element.isDisplayed() && element.isEnabled(),
                5,
                500
        );

        targetOption.shouldBe(Condition.visible, Condition.enabled).click();
        targetOption.shouldBe(Condition.selected);

        return this;
    }

    public MakeATransfer enterRecipientName(String recipientName) {
        enterRecipientName.clear();
        enterRecipientName.sendKeys(recipientName);
        return this;
    }

    public MakeATransfer enterRecipientAccountNumber(String recipientAccountNumber) {
        enterRrecipientAccountNumber.clear();
        enterRrecipientAccountNumber.sendKeys(recipientAccountNumber);
        return this;
    }

    public MakeATransfer enterAmount(String amount) {
        enterAmount.clear();
        enterAmount.sendKeys(amount);
        return this;
    }

    public MakeATransfer confirmDetailsAreCorrect() {
        confirmDetails.click();
        return this;
    }

    public MakeATransfer sendTransferClick() {
        sendTransfer.click();
        return this;
    }
}