////package common.extensions;
////
////public class MakaFaka {
////    import org.junit.jupiter.api.extension.BeforeEachCallback;
////import org.junit.jupiter.api.extension.ExtensionContext;
////import java.util.LinkedList;
////import java.util.List;
////
////    public class UserSessionExtension implements BeforeEachCallback {
////
////        @Override
////        public void beforeEach(ExtensionContext extensionContext) throws Exception {
////            UserSession annotation = extensionContext.getRequiredTestMethod()
////                    .getAnnotation(UserSession.class);
////
////            if (annotation != null) {
////                int userCount = annotation.value();
////                int authAsUser = annotation.auth();
////                boolean withAccount = annotation.withAccount();
////                boolean withAccountForAll = annotation.withAccountForAll();
////                double initialBalance = annotation.initialBalance();
////
////                SessionStorage.clear();
////
////                List<CreateUserRequest> users = new LinkedList<>();
////                List<CreateAccountResponse> allAccounts = new LinkedList<>();
////
////                // ШАГ 1: Создаём пользователей (без аккаунтов)
////                for (int i = 0; i < userCount; i++) {
////                    CreateUserRequest user = AdminSteps.createUser();
////                    users.add(user);
////                }
////
////                SessionStorage.addUsers(users);
////
////                // ШАГ 2: Авторизуем нужного пользователя (ТОЛЬКО ЕГО!)
////                CreateUserRequest authUser = SessionStorage.getUser(authAsUser);
////                BasePage.authAsUser(authUser);
////
////                // ШАГ 3: Теперь, когда пользователь авторизован, создаём аккаунты
////
////                // Вариант А: Создаём аккаунт для авторизованного пользователя
////                if (withAccount && !withAccountForAll) {
////                    UserSteps userSteps = new UserSteps(authUser.getUsername(), authUser.getPassword());
////                    CreateAccountResponse account = userSteps.createAccount();
////                    SessionStorage.addAccount(account);
////                }
////
////                // Вариант Б: Создаём аккаунты для ВСЕХ пользователей
////                // Для этого нужно по очереди авторизовывать каждого, создавать аккаунт,
////                // потом возвращать авторизацию обратно к исходному пользователю
////                if (withAccountForAll) {
////                    // Запоминаем, кто был авторизован
////                    CreateUserRequest originallyAuthUser = authUser;
////
////                    for (int i = 0; i < userCount; i++) {
////                        CreateUserRequest currentUser = SessionStorage.getUser(i + 1);
////
////                        // Авторизуем текущего пользователя
////                        BasePage.authAsUser(currentUser);
////
////                        // Создаём аккаунт
////                        UserSteps userSteps = new UserSteps(currentUser.getUsername(), currentUser.getPassword());
////                        CreateAccountResponse account = userSteps.createAccount();
////                        allAccounts.add(account);
////
////                        // Сохраняем связь
////                        SessionStorage.addAccountForUser(i + 1, account);
////
////                        // Если есть начальный баланс - пополняем
////                        if (initialBalance > 0) {
////                            // Здесь может быть API для пополнения баланса
////                            // Например: userSteps.deposit(account.getAccountNumber(), initialBalance);
////                        }
////                    }
////
////                    // Возвращаем авторизацию обратно к исходному пользователю
////                    BasePage.authAsUser(originallyAuthUser);
////
////                    // Устанавливаем аккаунт для текущего пользователя
////                    if (authAsUser <= allAccounts.size()) {
////                        SessionStorage.setCurrentAccount(allAccounts.get(authAsUser - 1));
////                    }
////                }
////            }
////        }
////    }
////
////    public class UserSessionExtension implements BeforeEachCallback {
////
////        @Override
////        public void beforeEach(ExtensionContext extensionContext) throws Exception {
////            UserSession annotation = extensionContext.getRequiredTestMethod()
////                    .getAnnotation(UserSession.class);
////
////            if (annotation != null) {
////                int userCount = annotation.value();
////                int authAsUser = annotation.auth();
////                boolean withAccount = annotation.withAccount();
////                boolean withAccountForAll = annotation.withAccountForAll();
////                double initialBalance = annotation.initialBalance();
////
////                SessionStorage.clear();
////
////                // ШАГ 1: Создаём всех пользователей
////                List<CreateUserRequest> users = new LinkedList<>();
////                for (int i = 0; i < userCount; i++) {
////                    CreateUserRequest user = AdminSteps.createUser();
////                    users.add(user);
////                }
////                SessionStorage.addUsers(users);
////
////                // ШАГ 2: Если нужны аккаунты для всех - создаём их
////                if (withAccountForAll) {
////                    for (int i = 0; i < userCount; i++) {
////                        CreateUserRequest user = SessionStorage.getUser(i + 1);
////
////                        // Авторизуем пользователя
////                        BasePage.authAsUser(user);
////
////                        // Создаём аккаунт
////                        UserSteps userSteps = new UserSteps(user.getUsername(), user.getPassword());
////                        CreateAccountResponse account = userSteps.createAccount();
////
////                        // Сохраняем
////                        SessionStorage.addAccountForUser(i + 1, account);
////
////                        // Устанавливаем начальный баланс если нужно
////                        if (initialBalance > 0) {
////                            // TODO: добавить API для пополнения баланса
////                            // depositViaApi(user, account.getAccountNumber(), initialBalance);
////                        }
////                    }
////                }
////
////                // ШАГ 3: Авторизуем нужного пользователя (финальная авторизация)
////                CreateUserRequest authUser = SessionStorage.getUser(authAsUser);
////                BasePage.authAsUser(authUser);
////
////                // ШАГ 4: Если нужен аккаунт только для авторизованного пользователя
////                if (withAccount && !withAccountForAll) {
////                    UserSteps userSteps = new UserSteps(authUser.getUsername(), authUser.getPassword());
////                    CreateAccountResponse account = userSteps.createAccount();
////                    SessionStorage.addAccount(account);
////                }
////
////                // ШАГ 5: Если создавали аккаунты для всех - устанавливаем текущий аккаунт
////                if (withAccountForAll) {
////                    CreateAccountResponse currentAccount = SessionStorage.getAccountForUser(authAsUser);
////                    SessionStorage.setCurrentAccount(currentAccount);
////                }
////            }
////        }
////    }
////
////    import java.util.HashMap;
////import java.util.LinkedList;
////import java.util.List;
////import java.util.Map;
////
////    public class SessionStorage {
////        private static List<CreateUserRequest> users = new LinkedList<>();
////        private static Map<Integer, CreateAccountResponse> userAccountMap = new HashMap<>();
////        private static CreateUserRequest currentUser;
////        private static CreateAccountResponse currentAccount;
////        private static int currentUserId = 0;
////
////        public static void clear() {
////            users.clear();
////            userAccountMap.clear();
////            currentUser = null;
////            currentAccount = null;
////            currentUserId = 0;
////        }
////
////        public static void addUsers(List<CreateUserRequest> newUsers) {
////            users.addAll(newUsers);
////        }
////
////        public static void addAccountForUser(int userIndex, CreateAccountResponse account) {
////            userAccountMap.put(userIndex, account);
////        }
////
////        public static void addAccount(CreateAccountResponse account) {
////            currentAccount = account;
////            if (currentUserId > 0) {
////                userAccountMap.put(currentUserId, account);
////            }
////        }
////
////        public static CreateUserRequest getUser(int index) {
////            if (index < 1 || index > users.size()) {
////                throw new IllegalArgumentException("User index " + index + " not found. Total users: " + users.size());
////            }
////            return users.get(index - 1);
////        }
////
////        public static CreateAccountResponse getAccountForUser(int userIndex) {
////            return userAccountMap.get(userIndex);
////        }
////
////        public static CreateUserRequest getCurrentUser() {
////            if (currentUserId == 0 && !users.isEmpty()) {
////                currentUserId = 1;
////                currentUser = users.get(0);
////            }
////            return currentUser;
////        }
////
////        public static CreateAccountResponse getCurrentAccount() {
////            return currentAccount;
////        }
////
////        public static void setCurrentUser(int userId) {
////            currentUserId = userId;
////            currentUser = getUser(userId);
////            currentAccount = getAccountForUser(userId);
////        }
////
////        public static void setCurrentAccount(CreateAccountResponse account) {
////            currentAccount = account;
////        }
////
////        public static UserSteps getCurrentUserSteps() {
////            CreateUserRequest user = getCurrentUser();
////            return new UserSteps(user.getUsername(), user.getPassword());
////        }
////
////        public static UserSteps getUserSteps(int userIndex) {
////            CreateUserRequest user = getUser(userIndex);
////            return new UserSteps(user.getUsername(), user.getPassword());
////        }
////    }
////}
//
//
//package common.storage;
//
//import api.models.CreateAccountResponse;
//import api.models.CreateUserRequest;
//import api.models.DepositResponse;
//import api.requests.steps.usersteps.UserSteps;
//import api.requests.steps.usersteps.UserStepsDeposit;
//import common.context.UserStepsWithAccountAndDeposit;
//
//import java.util.ArrayList;
//import java.util.LinkedHashMap;
//import java.util.List;
//
//public class SessionStorage {
//    private static final SessionStorage INSTANCE = new SessionStorage();
//    //
////    private final LinkedHashMap<CreateUserRequest, UserSteps> userStepsMap = new LinkedHashMap<>();
////
////    private final LinkedHashMap<CreateUserRequest, CreateAccountResponse> userAccountMap = new LinkedHashMap<>();
////
////    private final LinkedHashMap<CreateUserRequest, UserStepsDeposit> userStepsDepositMap = new LinkedHashMap<>();
////    private final LinkedHashMap<CreateUserRequest, List<DepositResponse>> userDepositsMap = new LinkedHashMap<>();
////
//    private final LinkedHashMap<CreateUserRequest, UserStepsWithAccountAndDeposit> userContextMap = new LinkedHashMap<>();
//
//    private CreateUserRequest currentUser = null;
//    private int currentUserIndex = 1;
//
//    private SessionStorage() {}
//
//    public static void addUsers(List<CreateUserRequest> users) {
//        for (CreateUserRequest user: users) {
//            INSTANCE.userStepsMap.put(user, new UserSteps(user.getUsername(), user.getPassword()));
//            // Также создаём UserStepsDeposit для каждого пользователя
//            INSTANCE.userStepsDepositMap.put(user, UserStepsDeposit.asUser(user));
//            INSTANCE.userDepositsMap.put(user, new ArrayList<>());
//        }
//        // Устанавливаем текущего пользователя (первого)
//        if (!users.isEmpty()) {
//            INSTANCE.currentUser = users.get(0);
//            INSTANCE.currentUserIndex = 1;
//        }
//    }
//
//    /**
//     * Возвращаем объект CreateUserRequest по его порядковому номеру в списке созданных пользователей.
//     * @param number Порядковый номер, начиная с 1 (а не с 0).
//     * @return Объект CreateUserRequest, соответствующий указанному порядковому номеру.
//     */
//    public static CreateUserRequest getUser(int number) {
//        return new ArrayList<>(INSTANCE.userStepsMap.keySet()).get(number-1);
//    }
//
//    public static CreateUserRequest getUser() {
//        return INSTANCE.currentUser != null ? INSTANCE.currentUser : getUser(1);
//    }
//
//    public static UserSteps getSteps(int number) {
//        return new ArrayList<>(INSTANCE.userStepsMap.values()).get(number-1);
//    }
//
//    public static UserSteps getSteps() {
//        return getSteps(getCurrentUserIndex());
//    }
//
//    public static void clear() {
//        INSTANCE.userStepsMap.clear();
//        INSTANCE.userAccountMap.clear();
//        INSTANCE.currentUser = null;
//        INSTANCE.currentUserIndex = 1;
//        INSTANCE.userStepsDepositMap.clear();
//        INSTANCE.userDepositsMap.clear();
//    }
//
//    public static int getCurrentUserIndex() {
//        return INSTANCE.currentUserIndex;
//    }
//
//    public static void setCurrentUser(int number) {
//        INSTANCE.currentUser = getUser(number);
//        INSTANCE.currentUserIndex = number;
//    }
//
//    /**
//     * Добавляет аккаунт для указанного пользователя
//     * @param userIndex Порядковый номер пользователя (начиная с 1)
//     * @param account Аккаунт пользователя
//     */
//    public static void addAccountForUser(int userIndex, CreateAccountResponse account) {
//        CreateUserRequest user = getUser(userIndex);
//        INSTANCE.userAccountMap.put(user, account);
//    }
//
//    public static CreateAccountResponse getAccountForUser(int userIndex) {
//        CreateUserRequest user = getUser(userIndex);
//        return INSTANCE.userAccountMap.get(user);
//    }
//
//    /**
//     * Добавляет аккаунт для текущего пользователя
//     */
//    public static void addAccountForCurrentUser(CreateAccountResponse account) {
//        if (INSTANCE.currentUser != null) {
//            INSTANCE.userAccountMap.put(INSTANCE.currentUser, account);
//        }
//    }
//
//    public static CreateAccountResponse getCurrentAccount() {
//        if (INSTANCE.currentUser != null) {
//            return INSTANCE.userAccountMap.get(INSTANCE.currentUser);
//        }
//        return null;
//    }
//
//    /**
//     * Получает UserSteps для пользователя и его аккаунт (если есть)
//     */
//    public static UserStepsWithAccountAndDeposit getUserWithAccount(int userIndex) {
//        UserSteps steps = getSteps(userIndex);
//        CreateAccountResponse account = getAccountForUser(userIndex);
//        return new UserStepsWithAccountAndDeposit(steps, account);
//    }
//
//    /**
//     * Получает UserSteps и аккаунт для текущего пользователя
//     */
//    public static UserStepsWithAccount getCurrentUserWithAccount() {
//        UserSteps steps = getSteps();
//        CreateAccountResponse account = getCurrentAccount();
//        return new UserStepsWithAccount(steps, account);
//    }
//
//    public static UserStepsDeposit getDepositSteps(int number) {
//        return new ArrayList<>(INSTANCE.userStepsDepositMap.values()).get(number - 1);
//    }
//
//    //Работа с депозитами
//    public static UserStepsDeposit getDepositSteps() {
//        return getDepositSteps(getCurrentUserIndex());
//    }
//
//    public static UserStepsDeposit getDepositStepsForUser(int userIndex) {
//        CreateUserRequest user = getUser(userIndex);
//        return INSTANCE.userStepsDepositMap.get(user);
//    }
//
//    public static UserStepsDeposit getDepositStepsForCurrentUser() {
//        if (INSTANCE.currentUser != null) {
//            return INSTANCE.userStepsDepositMap.get(INSTANCE.currentUser);
//        }
//        return null;
//    }
//
//    public static void addDepositForUser(int userIndex, DepositResponse deposit) {
//        CreateUserRequest user = getUser(userIndex);
//        List<DepositResponse> deposits = INSTANCE.userDepositsMap.get(user);
//        if (deposits != null) {
//            deposits.add(deposit);
//        }
//    }
//
//    public static void addDepositForCurrentUser(DepositResponse deposit) {
//        if (INSTANCE.currentUser != null) {
//            List<DepositResponse> deposits = INSTANCE.userDepositsMap.get(INSTANCE.currentUser);
//            if (deposits != null) {
//                deposits.add(deposit);
//            }
//        }
//    }
//
//    public static List<DepositResponse> getDepositsForUser(int userIndex) {
//        CreateUserRequest user = getUser(userIndex);
//        return INSTANCE.userDepositsMap.get(user);
//    }
//
//    public static List<DepositResponse> getCurrentUserDeposits() {
//        if (INSTANCE.currentUser != null) {
//            return INSTANCE.userDepositsMap.get(INSTANCE.currentUser);
//        }
//        return new ArrayList<>();
//    }
//
//    public static double getTotalDepositedForUser(int userIndex) {
//        return getDepositsForUser(userIndex).stream()
//                .mapToDouble(DepositResponse::getBalance)
//                .sum();
//    }
//
//    public static double getCurrentUserTotalDeposited() {
//        return getCurrentUserDeposits().stream()
//                .mapToDouble(DepositResponse::getBalance)
//                .sum();
//    }
//}