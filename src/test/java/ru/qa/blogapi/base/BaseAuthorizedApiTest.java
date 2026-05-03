package ru.qa.blogapi.base;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import ru.qa.blogapi.auth.AuthApiClient;
import ru.qa.blogapi.auth.AuthSession;

public abstract class BaseAuthorizedApiTest extends BaseApiTest {

    protected AuthSession authSession;
    protected RequestSpecification authorizedRequestSpec;

    @BeforeEach
    void authorize() {
        AuthApiClient authApiClient = new AuthApiClient(requestSpec);
        authSession = authApiClient.createAuthorizedSession();

        authorizedRequestSpec = new RequestSpecBuilder()
                .addRequestSpecification(requestSpec)
                .addHeader("Authorization", "Bearer " + authSession.getAccessToken())
                .build();
    }
}