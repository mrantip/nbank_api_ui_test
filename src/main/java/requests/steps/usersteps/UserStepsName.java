package requests.steps.usersteps;

import io.restassured.specification.RequestSpecification;
import models.ChangeNameRequest;
import models.ChangeNameResponse;
import requests.skeleton.Endpoint;
import requests.skeleton.requesters.CrudRequester;
import requests.skeleton.requesters.ValidatedCrudRequester;
import specs.ResponseSpecs;

public class UserStepsName extends UserSteps {
    public UserStepsName(RequestSpecification authSpec) {
        super(authSpec);
    }

    public ChangeNameResponse updateName(String name) {
        return new ValidatedCrudRequester<ChangeNameResponse>(authSpec, Endpoint.UPDATE_NAME,
                ResponseSpecs.profileUpdatedSuccess())
                .update(buildChangeNameRequest(name));
    }

    public void updateInvalidName(String name) {
        new CrudRequester(authSpec, Endpoint.UPDATE_NAME,
                ResponseSpecs.requestReturnsBadRequest())
                .update(buildChangeNameRequest(name));
    }

    private ChangeNameRequest buildChangeNameRequest(String name) {
        return ChangeNameRequest.builder()
                .name(name)
                .build();
    }
}
