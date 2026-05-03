package ru.qa.blogapi.tests;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.qa.blogapi.base.BaseAuthorizedApiTest;
import ru.qa.blogapi.models.PostCreateRequest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class AuthorizedApiExamplesTest extends BaseAuthorizedApiTest {

    @Test
    @Tag("smoke")
    @DisplayName("GET /api/profile/{id} -> должен вернуть профиль пользователя по id")
    void shouldReturnUserProfileById() {
        given()
                .spec(authorizedRequestSpec)
                .pathParam("id", authSession.getUserId())
                .when()
                .get("/api/profile/{id}")
                .then()
                .statusCode(200)
                .body("user", notNullValue())
                .body("user.id", equalTo(authSession.getUserId()))
                .body("user.email", equalTo(authSession.getEmail()));
    }

    @Test
    @Tag("smoke")
    @DisplayName("POST /api/posts -> должен создать пост от авторизованного пользователя")
    void shouldCreatePostForAuthorizedUser() {
        String suffix = RandomStringUtils.secure().nextAlphanumeric(6);

        PostCreateRequest requestBody = new PostCreateRequest(
                "API Post " + suffix,
                "This is body for REST-Assured practice post " + suffix,
                "Short description " + suffix,
                "technology",
                false
        );

        given()
                .spec(authorizedRequestSpec)
                .body(requestBody)
                .when()
                .post("/api/posts")
                .then()
                .statusCode(201)
                .body("status", equalTo("success"))
                .body("post.id", notNullValue())
                .body("post.title", equalTo(requestBody.getTitle()))
                .body("post.author.email", equalTo(authSession.getEmail()));
    }
}
