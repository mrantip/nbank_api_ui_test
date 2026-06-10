package common.storage;

import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.models.DepositResponse;
import api.requests.steps.usersteps.UserSteps;
import api.requests.steps.usersteps.UserStepsDeposit;
import common.context.UserStepsWithAccountAndDeposit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SessionStorage {
    private static final SessionStorage INSTANCE = new SessionStorage();
    private final LinkedHashMap<CreateUserRequest, UserStepsWithAccountAndDeposit> userContextMap = new LinkedHashMap<>();
    private CreateUserRequest currentUser = null;
    private int currentUserIndex = 1;

    private SessionStorage() {
    }

    public static void addUsers(List<CreateUserRequest> users) {
        for (CreateUserRequest user : users) {
            UserSteps steps = new UserSteps(user.getUsername(), user.getPassword());
            UserStepsDeposit depositSteps = UserStepsDeposit.asUser(user);

            UserStepsWithAccountAndDeposit context = UserStepsWithAccountAndDeposit.builder()
                    .steps(steps)
                    .depositSteps(depositSteps)
                    .build();

            INSTANCE.userContextMap.put(user, context);
        }
        // Устанавливаем текущего пользователя (первого)
        if (!users.isEmpty()) {
            INSTANCE.currentUser = users.get(0);
            INSTANCE.currentUserIndex = 1;
        }
    }

    /**
     * Возвращаем объект CreateUserRequest по его порядковому номеру в списке созданных пользователей.
     *
     * @param number Порядковый номер, начиная с 1 (а не с 0).
     * @return Объект CreateUserRequest, соответствующий указанному порядковому номеру.
     */
    public static CreateUserRequest getUser(int number) {
        return new ArrayList<>(INSTANCE.userContextMap.keySet()).get(number - 1);
    }

    public static CreateUserRequest getUser() {
        return INSTANCE.currentUser != null ? INSTANCE.currentUser : getUser(1);
    }

    /**
     * Возвращаем объект UserStepsWithAccountAndDeposit по его порядковому номеру в списке созданных пользователей. Получаем контекст (модель данных для теста)
     *
     * @param number Порядковый номер
     * @return Объект CreateUserRequest, соответствующий указанному порядковому номеру.
     */

    public static UserStepsWithAccountAndDeposit getContext(int number) {
        CreateUserRequest user = getUser(number);
        return INSTANCE.userContextMap.get(user);
    }

    public static UserStepsWithAccountAndDeposit getContext() {
        return getContext(getCurrentUserIndex());
    }

    /**
     * Добавляет аккаунт для указанного пользователя
     *
     * @param userIndex Порядковый номер пользователя (начиная с 1)
     * @param account   Аккаунт пользователя
     */
    public static void addAccountForUser(int userIndex, CreateAccountResponse account) {
        UserStepsWithAccountAndDeposit context = getContext(userIndex);

        // сохраняем существующие депозиты
        List<Double> existingAmounts = context.getDepositAmounts();
        List<DepositResponse> existingResponses = context.getDepositResponses();

        UserStepsWithAccountAndDeposit newContext = UserStepsWithAccountAndDeposit.builder()
                .steps(context.getSteps())
                .depositSteps(context.getDepositSteps())
                .account(account)
                .depositAmounts(existingAmounts)    // ✅ Сохраняем суммы
                .depositResponses(existingResponses)  // ✅ Сохраняем депозиты
                .build();

        CreateUserRequest user = getUser(userIndex);
        INSTANCE.userContextMap.put(user, newContext);
    }

    /**
     * Добавляет аккаунт для текущего пользователя
     */
    public static void addAccountForCurrentUser(CreateAccountResponse account) {
        if (INSTANCE.currentUser != null) {
            addAccountForUser(getCurrentUserIndex(), account);
        }
    }

    public static CreateAccountResponse getAccountForUser(int userIndex) {
        CreateUserRequest user = getUser(userIndex);
        return getContext(userIndex).getAccount();
    }

    public static CreateAccountResponse getCurrentAccount() {
        return getContext().getAccount();
    }

    public static String getCurrentAccountNumber() {
        CreateAccountResponse account = getCurrentAccount();
        return account != null ? account.getAccountNumber() : null;
    }

    public static boolean currentUserHasAccount() {
        return getContext().hasAccount();
    }

    public static void addDepositForCurrentUser(double amount) {
        if (INSTANCE.currentUser != null) {
            addDepositForUser(getCurrentUserIndex(), amount);
        }
    }

    public static void addDepositForUser(int userIndex, double amount) {
        UserStepsWithAccountAndDeposit context = getContext(userIndex);

        // Делаем депозит через контекст
        DepositResponse response = context.deposit(amount);

        List<Double> existingAmounts = context.getDepositAmounts();
        List<DepositResponse> existingResponses = context.getDepositResponses();

        UserStepsWithAccountAndDeposit newContext = UserStepsWithAccountAndDeposit.builder()
                .steps(context.getSteps())
                .depositSteps(context.getDepositSteps())
                .account(context.getAccount())
                .depositAmounts(existingAmounts)
                .depositResponses(existingResponses)
                .build();

        CreateUserRequest user = getUser(userIndex);
        INSTANCE.userContextMap.put(user, newContext);
    }

    public static UserSteps getSteps(int number) {
        return getContext(number).getSteps();
    }

    public static UserSteps getSteps() {
        return getContext().getSteps();
    }

    public static UserStepsDeposit getDepositSteps(int number) {
        return getContext(number).getDepositSteps();
    }

    public static UserStepsDeposit getDepositSteps() {
        return getContext().getDepositSteps();
    }

    public static UserStepsDeposit getDepositStepsForCurrentUser() {
        return getContext().getDepositSteps();
    }

    public static UserStepsWithAccountAndDeposit getCurrentContext() {
        return getContext();
    }

    public static UserStepsWithAccountAndDeposit getContextForUser(int userIndex) {
        return getContext(userIndex);
    }

    public static int getCurrentUserIndex() {
        return INSTANCE.currentUserIndex;
    }

    public static void setCurrentUser(int number) {
        INSTANCE.currentUser = getUser(number);
        INSTANCE.currentUserIndex = number;
    }

    // Очистка
    public static void clear() {
        INSTANCE.userContextMap.clear();
        INSTANCE.currentUser = null;
        INSTANCE.currentUserIndex = 1;
    }
}