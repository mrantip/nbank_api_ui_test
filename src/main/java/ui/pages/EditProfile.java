package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import common.utils.RetryUtils;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.sleep;

public class EditProfile extends BasePage<EditProfile> {
    private SelenideElement enterNewName = $(Selectors.byAttribute("placeholder", "Enter new name"));
    private SelenideElement saveChangesButton = $(Selectors.withText("Save Changes"));
    private SelenideElement homeButton = $(Selectors.withText("Home"));

    @Override
    public String url() {
        return "/edit-profile";
    }

    public EditProfile enterNewName(String newName) {
        enterNewName.shouldBe(Condition.enabled, Condition.visible)
                .doubleClick()
                .sendKeys(Keys.DELETE);
        enterNewName.sendKeys(newName);
        RetryUtils.retry(
                () -> enterNewName.getValue(),
                value -> value.equals(newName),
                3,
                1000
        );
//        enterNewName.shouldHave(Condition.value(newName));
        return this;
    }

    public EditProfile saveChangesButtonClick() {
        saveChangesButton.click();
        return this;
    }

    public EditProfile goHome() {
        homeButton.click();
        sleep(200);
        return this;
    }
}