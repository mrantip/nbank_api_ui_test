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
    private final List<Double> depositAmounts;  // храним только суммы
    private final List<DepositResponse> depositResponses;

    public UserStepsWithAccountAndDeposit(Builder builder) {
        this.steps = builder.steps;
        this.depositSteps = builder.depositSteps;
        this.account = builder.account;
        this.depositAmounts = builder.depositAmounts != null ? builder.depositAmounts : new ArrayList<>();
        this.depositResponses = builder.depositResponses != null ? builder.depositResponses : new ArrayList<>();
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
        return !depositResponses.isEmpty();
    }

    public int getDepositCount() {
        return depositAmounts.size();
    }

    public double getTotalDeposited() {
        return depositAmounts.stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    public DepositResponse getLastDeposit() {
        return depositResponses.isEmpty() ? null : depositResponses.get(depositResponses.size() - 1);
    }


    // Переиспользовать UserStepsDeposit
    public DepositResponse deposit(double amount) {
        if (account == null) {
            throw new IllegalStateException("Нельзя внести депозит, т.к. не создан аккаунт");
        }
        DepositResponse response = depositSteps.deposit(getAccountNumber(), amount);
        depositAmounts.add(amount);
        depositResponses.add(response);
        return response;
    }

    public void depositMultiple(int count, double amount) {
        for (int i = 0; i < count; i++) {
            deposit(amount);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UserSteps steps;
        private UserStepsDeposit depositSteps;
        private CreateAccountResponse account;
        private List<Double> depositAmounts;
        private List<DepositResponse> depositResponses;

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

        public Builder depositAmounts(List<Double> depositAmounts) {
            this.depositAmounts = depositAmounts;
            return this;
        }

        public Builder depositResponses(List<DepositResponse> depositResponses) {
            this.depositResponses = depositResponses;
            return this;
        }

        public Builder addDepositAmount(double amount) {
            if (this.depositAmounts == null) {
                this.depositAmounts = new ArrayList<>();
            }
            this.depositAmounts.add(amount);
            return this;
        }

        public Builder addDepositResponse(DepositResponse response) {
            if (this.depositResponses == null) {
                this.depositResponses = new ArrayList<>();
            }
            this.depositResponses.add(response);
            return this;
        }

        public UserStepsWithAccountAndDeposit build() {
            return new UserStepsWithAccountAndDeposit(this);
        }
    }
}