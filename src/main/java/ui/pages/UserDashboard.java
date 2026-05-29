package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class UserDashboard extends BasePage<UserDashboard> {
    private SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
    private SelenideElement createNewAccount = $(Selectors.byText("➕ Create New Account"));
    private SelenideElement depositMoney = $(Selectors.withText("Deposit Money"));
    private SelenideElement makeATransfer = $(Selectors.withText("Make a Transfer"));
    private SelenideElement nameChangeButton = $(".user-name");

    @Override
    public String url() {
        return "/dashboard";
    }

    public UserDashboard createNewAccount() {
        createNewAccount.click();
        return this;
    }

    public UserDashboard depositMoney() {
        depositMoney.click();
        return this;
    }

    public UserDashboard makeATransfer() {
        makeATransfer.click();
        return this;
    }

    public String getWelcomeNameText() {
        return welcomeText.$("span").text();
    }

    public String getNameChangeButtonText() {
        return nameChangeButton.text().toLowerCase();
    }

    public UserDashboard changeNameClick() {
        nameChangeButton.click();
        return this;
    }
}