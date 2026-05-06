package requests.steps.usersteps;

import io.restassured.specification.RequestSpecification;
import models.*;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import specs.ResponseSpecs;

import java.util.List;

public class UserSteps {
    final RequestSpecification authSpec;

    public UserSteps(RequestSpecification authSpec) {
        this.authSpec = authSpec;
    }

    public CreateAccountResponse createAccount() {

        return new CrudRequester(authSpec, Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post()
                .extract()
                .as(CreateAccountResponse.class);
    }

    public List<AccountModel> getAllUserAccounts() {
        return new CrudRequester(authSpec, Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnsOK())
                .get()
                .extract()
                .jsonPath()
                .getList(".", AccountModel.class);
    }

    public AccountModel getAccountByNumber(String accountNumber) {
        return getAllUserAccounts().stream()
                .filter(account -> account.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account " + accountNumber + " not found"));
    }

    public ProfileInfoResponse getProfileInfo() {
        return new ValidatedCrudRequester<ProfileInfoResponse>(authSpec, Endpoint.PROFILE_INFO,
                ResponseSpecs.requestReturnsOK())
                .get();
    }
}