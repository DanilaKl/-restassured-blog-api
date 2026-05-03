package ru.qa.blogapi.auth;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import ru.qa.blogapi.models.LoginRequest;
import ru.qa.blogapi.models.RefreshTokenRequest;
import ru.qa.blogapi.models.UserRegistrationRequest;

import static io.restassured.RestAssured.given;

public class AuthApiClient {

    private final RequestSpecification requestSpec;

    public AuthApiClient(RequestSpecification requestSpec) {
        this.requestSpec = requestSpec;
    }

    public AuthSession createAuthorizedSession() {
        UserRegistrationRequest user = TestUserFactory.validUser();

        Response registerResponse = register(user)
                .then()
                .statusCode(200)
                .extract()
                .response();
        Integer userId = registerResponse.jsonPath().getInt("user.id");

        Response loginResponse = login(user.getEmail(), user.getPassword())
                .then()
                .statusCode(200)
                .extract()
                .response();
        String refreshToken = loginResponse.jsonPath().getString("refresh_token");

        Response refreshResponse = refresh(refreshToken)
                .then()
                .statusCode(200)
                .extract()
                .response();
        String accessToken = refreshResponse.jsonPath().getString("token");
        String newRefreshToken = refreshResponse.jsonPath().getString("refresh_token");

        AuthSession session = new AuthSession();
        session.setUserId(userId);
        session.setEmail(user.getEmail());
        session.setPassword(user.getPassword());
        session.setAccessToken(accessToken);
        session.setRefreshToken(newRefreshToken);

        return session;
    }

    public Response register(UserRegistrationRequest body) {
        return given()
                .spec(requestSpec)
                .body(body)
                .when()
                .post("/api/auth/register");
    }

    public Response login(String email, String password) {
        return login(new LoginRequest(email, password));
    }

    public Response login(LoginRequest body) {
        return given()
                .spec(requestSpec)
                .body(body)
                .when()
                .post("/api/login");
    }

    public Response refresh(String refreshToken) {
        return refresh(new RefreshTokenRequest(refreshToken));
    }

    public Response refresh(RefreshTokenRequest body) {
        return given()
                .spec(requestSpec)
                .body(body)
                .when()
                .post("/api/token/refresh");
    }
}
