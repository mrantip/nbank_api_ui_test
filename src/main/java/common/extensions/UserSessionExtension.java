package common.extensions;

import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.usersteps.UserSteps;
import common.context.UserStepsWithAccountAndDeposit;
import common.storage.SessionStorage;
import common.annotations.UserSession;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import ui.pages.BasePage;

import java.util.LinkedList;
import java.util.List;

public class UserSessionExtension implements BeforeEachCallback {
    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        // Шаг 1: проверка, что у теста есть аннотация UserSession
        UserSession annotation = extensionContext.getRequiredTestMethod().getAnnotation(UserSession.class);

        if (annotation != null) {
            int userCount = annotation.value();
            int authAsUser = annotation.auth();
            boolean withAccount = annotation.withAccount();
            boolean withAccountForAll = annotation.withAccountForAll();
            boolean withDeposit = annotation.withDeposit();
            int depositCount = annotation.depositCount();
            double depositAmount = annotation.depositAmount();

            SessionStorage.clear();

            List<CreateUserRequest> users = new LinkedList<>();

            for (int i = 0; i < userCount; i++) {
                CreateUserRequest user = AdminSteps.createUser();
                users.add(user);
            }

            SessionStorage.addUsers(users);

            if (withAccountForAll) {
                for (int i = 0; i < userCount; i++) {
                    int userIndex = i + 1;
                    CreateUserRequest user = SessionStorage.getUser(userIndex);

                    // Авторизуем текущего пользователя для создания аккаунта
                    BasePage.authAsUser(user);

                    // Создаём аккаунт через UserSteps
                    UserSteps userSteps = SessionStorage.getSteps(userIndex);
                    CreateAccountResponse account = userSteps.createAccount();

                    // Сохраняем аккаунт для пользователя
                    SessionStorage.addAccountForUser(userIndex, account);
                }
            }

            //авторизация текущего пользователя
            CreateUserRequest authUser = SessionStorage.getUser(authAsUser);
            BasePage.authAsUser(authUser);
            SessionStorage.setCurrentUser(authAsUser);


            if (withAccount && !withAccountForAll) {
                UserSteps userSteps = SessionStorage.getSteps();
                CreateAccountResponse account = userSteps.createAccount();
                SessionStorage.addAccountForCurrentUser(account);
            }

            if (withDeposit) {
                // проверка, что есть аккаунт
                if (!SessionStorage.currentUserHasAccount()) {
                    throw new IllegalStateException(
                            "Нельзя внести депозит, т.к. нет аккаунта. Используй аннотации withAccount=true или withAccountForAll=true"
                    );
                }
                // получаем текущего пользователя и вносим депозит
                UserStepsWithAccountAndDeposit context = SessionStorage.getCurrentContext();
                for (int i = 0; i < depositCount; i++) {
                    context.deposit(depositAmount);
                }

            }
        }
    }
}