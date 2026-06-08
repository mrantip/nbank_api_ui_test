package common.context;

import api.models.AccountModel;
import api.models.CreateAccountResponse;
import api.models.DepositResponse;
import api.models.ProfileInfoResponse;

import api.requests.steps.usersteps.UserSteps;
import api.requests.steps.usersteps.UserStepsDeposit;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class UserStepsWithAccountAndDeposit {
    private final UserSteps steps;
    private final CreateAccountResponse account;
    private final UserStepsDeposit depositSteps;
    private final List<DepositResponse> deposits;

    public UserStepsWithAccountAndDeposit(Builder builder) {
        this.steps = builder.steps;
        this.depositSteps = builder.depositSteps;
        this.account = builder.account;
        this.deposits = builder.deposits != null ? builder.deposits : new ArrayList<>();
    }

    public String getAccountNumber() {
        return account != null ? account.getAccountNumber() : null;
    }

    public boolean hasAccount() {
        return account != null;
    }

    // Переиспользовать методы UserSteps
    public AccountModel getAccountByNumber(String accountNumber) {
        return steps.getAccountByNumber(accountNumber);
    }

    public List<AccountModel> getAllUserAccounts() {
        return steps.getAllUserAccounts();
    }

    public ProfileInfoResponse getProfileInfo() {
        return steps.getProfileInfo();
    }

    // Работа с депозитами
    public boolean hasDeposits() {
        return !deposits.isEmpty();
    }

    public int getDepositCount() {
        return deposits.size();
    }

    public double getTotalDeposited() {
        return deposits.stream()
                .mapToDouble(DepositResponse::getBalance)
                .sum();
    }

    public DepositResponse getLastDeposit() {
        return deposits.isEmpty() ? null : deposits.get(deposits.size() - 1);
    }


    // Переиспользовать UserStepsDeposit
    public DepositResponse deposit(double amount) {
        if (account == null) {
            throw new IllegalStateException("Нельзя внести депозит, т.к. не создан аккаунт");
        }
        DepositResponse response = depositSteps.deposit(getAccountNumber(), amount);
        deposits.add(response);
        return response;
    }

    public void depositMaxMultipleTimes(int times) {
        if (account == null) {
            throw new IllegalStateException("Нельзя внести депозит, т.к. не создан аккаунт");
        }
        depositSteps.depositMaxMultipleTimes(account.getAccountNumber(), times);
        // Обновляем список депозитов (нужно, если depositMaxMultipleTimes не возвращает список)
        // В идеале - доработать UserStepsDeposit чтобы возвращал список
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UserSteps steps;
        private UserStepsDeposit depositSteps;
        private CreateAccountResponse account;
        private List<DepositResponse> deposits;

        public Builder steps(UserSteps steps) {
            this.steps = steps;
            return this;
        }

        // Создаем билдер

        public Builder depositSteps(UserStepsDeposit depositSteps) {
            this.depositSteps = depositSteps;
            return this;
        }

        public Builder account(CreateAccountResponse account) {
            this.account = account;
            return this;
        }

        public Builder deposits(List<DepositResponse> deposits) {
            this.deposits = deposits;
            return this;
        }

        public Builder addDeposit(DepositResponse deposit) {
            if (this.deposits == null) {
                this.deposits = new ArrayList<>();
            }
            this.deposits.add(deposit);
            return this;
        }

        public UserStepsWithAccountAndDeposit build() {
            return new UserStepsWithAccountAndDeposit(this);
        }
    }

}