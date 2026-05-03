package ru.qa.blogapi.tests;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.qa.blogapi.auth.AuthApiClient;
import ru.qa.blogapi.auth.AuthSession;
import ru.qa.blogapi.auth.TestUserFactory;
import ru.qa.blogapi.base.BaseAuthorizedApiTest;
import ru.qa.blogapi.models.LoginRequest;
import ru.qa.blogapi.models.PostCreateRequest;
import ru.qa.blogapi.models.RefreshTokenRequest;
import ru.qa.blogapi.models.UserRegistrationRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

class BlogApiHomeworkTest extends BaseAuthorizedApiTest {

    @Test
    @Tag("smoke")
    @DisplayName("POST /api/auth/register -> should register user with valid required fields")
    void shouldRegisterUserWithValidRequiredFields() {
        UserRegistrationRequest requestBody = TestUserFactory.validUser();

        given()
                .spec(requestSpec)
                .body(requestBody)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("message", equalTo("User registered successfully"))
                .body("user.id", notNullValue())
                .body("user.email", equalTo(requestBody.getEmail()));
    }

    @Test
    @Tag("regression")
    @DisplayName("POST /api/auth/register -> should return validation error for invalid email")
    void shouldReturnValidationErrorForInvalidEmailOnRegistration() {
        UserRegistrationRequest validUser = TestUserFactory.validUser();
        UserRegistrationRequest requestBody = new UserRegistrationRequest(
                "invalid-email",
                validUser.getPassword(),
                validUser.getFirstName(),
                validUser.getLastName(),
                validUser.getNickname(),
                validUser.getBirthDate(),
                validUser.getPhone()
        );

        given()
                .spec(requestSpec)
                .body(requestBody)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(400)
                .body("error", notNullValue())
                .body("error.code", equalTo(400))
                .body("error.message", not(emptyOrNullString()));
    }

    @Test
    @Tag("smoke")
    @DisplayName("POST /api/login -> should login with valid credentials")
    void shouldLoginWithValidCredentials() {
        AuthApiClient authApiClient = authApiClient();
        UserRegistrationRequest user = TestUserFactory.validUser();
        authApiClient.register(user)
                .then()
                .statusCode(200);

        authApiClient.login(new LoginRequest(user.getEmail(), user.getPassword()))
                .then()
                .statusCode(200)
                .body("token", not(emptyOrNullString()))
                .body("refresh_token", not(emptyOrNullString()));
    }

    @Test
    @Tag("regression")
    @DisplayName("POST /api/login -> should return unauthorized for wrong password")
    void shouldReturnUnauthorizedForWrongPassword() {
        AuthApiClient authApiClient = authApiClient();
        UserRegistrationRequest user = TestUserFactory.validUser();
        authApiClient.register(user)
                .then()
                .statusCode(200);

        authApiClient.login(new LoginRequest(user.getEmail(), "WrongPass123!"))
                .then()
                .statusCode(401);
    }

    @Test
    @Tag("smoke")
    @DisplayName("POST /api/token/refresh -> should refresh access token by refresh token")
    void shouldRefreshAccessToken() {
        AuthApiClient authApiClient = authApiClient();
        UserRegistrationRequest user = TestUserFactory.validUser();
        authApiClient.register(user)
                .then()
                .statusCode(200);

        String refreshToken = authApiClient.login(user.getEmail(), user.getPassword())
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("refresh_token");

        authApiClient.refresh(new RefreshTokenRequest(refreshToken))
                .then()
                .statusCode(200)
                .body("token", not(emptyOrNullString()))
                .body("refresh_token", not(emptyOrNullString()));
    }

    @Test
    @Tag("smoke")
    @DisplayName("GET /api/profile -> should return current user profile for authorized user")
    void shouldReturnCurrentUserProfile() {
        given()
                .spec(authorizedRequestSpec)
                .when()
                .get("/api/profile")
                .then()
                .statusCode(200)
                .body("user", notNullValue())
                .body("user.id", equalTo(authSession.getUserId()));
    }

    @Test
    @Tag("regression")
    @DisplayName("PUT /api/profile -> should update current user profile")
    void shouldUpdateCurrentUserProfile() {
        String suffix = suffix();
        String firstName = "Updated" + suffix;
        String lastName = "User" + suffix;
        String nickname = "updated_" + suffix.toLowerCase();
        Map<String, Object> requestBody = Map.of(
                "firstName", firstName,
                "lastName", lastName,
                "nickname", nickname,
                "phone", randomPhone()
        );

        given()
                .spec(authorizedRequestSpec)
                .body(requestBody)
                .when()
                .put("/api/profile")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("user.firstName", equalTo(firstName))
                .body("user.lastName", equalTo(lastName))
                .body("user.nickname", equalTo(nickname));

        given()
                .spec(authorizedRequestSpec)
                .pathParam("id", authSession.getUserId())
                .when()
                .get("/api/profile/{id}")
                .then()
                .statusCode(200)
                .body("user.firstName", equalTo(firstName))
                .body("user.lastName", equalTo(lastName))
                .body("user.nickname", equalTo(nickname));
    }

    @Test
    @Tag("regression")
    @DisplayName("GET /api/posts -> should return paginated list of posts")
    void shouldReturnPaginatedPostsList() {
        createPost(postRequest("technology", false));

        given()
                .spec(authorizedRequestSpec)
                .queryParam("page", 1)
                .queryParam("limit", 10)
                .when()
                .get("/api/posts")
                .then()
                .statusCode(200)
                .body("items", notNullValue())
                .body("items.size()", lessThanOrEqualTo(10))
                .body("totalItems", notNullValue())
                .body("itemsPerPage", equalTo(10))
                .body("page", equalTo(1))
                .body("pages", notNullValue());
    }

    @Test
    @Tag("regression")
    @DisplayName("GET /api/posts -> should filter posts by category")
    void shouldFilterPostsByCategory() {
        Integer technologyPostId = createPost(postRequest("technology", false)).jsonPath().getInt("post.id");
        createPost(postRequest("diy", false));

        given()
                .spec(authorizedRequestSpec)
                .queryParam("category", "technology")
                .when()
                .get("/api/posts")
                .then()
                .statusCode(200)
                .body("items", not(empty()))
                .body("items.id", hasItem(technologyPostId))
                .body("items.category", everyItem(equalTo("technology")));
    }

    @Test
    @Tag("smoke")
    @DisplayName("POST /api/posts -> should create published post")
    void shouldCreatePublishedPost() {
        PostCreateRequest requestBody = postRequest("technology", false);

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
                .body("post.isDraft", equalTo(false))
                .body("post.author.email", equalTo(authSession.getEmail()));
    }

    @Test
    @Tag("regression")
    @DisplayName("POST /api/posts -> should create draft post")
    void shouldCreateDraftPost() {
        PostCreateRequest requestBody = postRequest("technology", true);

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
                .body("post.isDraft", equalTo(true))
                .body("post.author.email", equalTo(authSession.getEmail()));
    }

    @Test
    @Tag("regression")
    @DisplayName("GET /api/posts/my -> should return only current user posts")
    void shouldReturnOnlyCurrentUserPosts() {
        Integer firstPostId = createPost(postRequest("technology", false)).jsonPath().getInt("post.id");
        Integer secondPostId = createPost(postRequest("travel", false)).jsonPath().getInt("post.id");

        given()
                .spec(authorizedRequestSpec)
                .when()
                .get("/api/posts/my")
                .then()
                .statusCode(200)
                .body("items", not(empty()))
                .body("items.id", hasItems(firstPostId, secondPostId))
                .body("items.author.email", everyItem(equalTo(authSession.getEmail())));
    }

    @Test
    @Tag("e2e")
    @DisplayName("GET /api/posts/feed -> should return posts from other users")
    void shouldReturnFeedPosts() {
        AuthSession otherUser = authApiClient().createAuthorizedSession();
        RequestSpecification otherUserSpec = authorizedSpec(otherUser);
        PostCreateRequest otherUserPost = postRequest("technology", false);
        Integer otherUserPostId = createPost(otherUserSpec, otherUserPost).jsonPath().getInt("post.id");

        given()
                .spec(authorizedRequestSpec)
                .when()
                .get("/api/posts/feed")
                .then()
                .statusCode(200)
                .body("items", not(empty()))
                .body("items.id", hasItem(otherUserPostId))
                .body("items.author.email", everyItem(not(equalTo(authSession.getEmail()))));
    }

    @Test
    @Tag("regression")
    @DisplayName("GET /api/posts/{id} -> should return single post by id")
    void shouldReturnSinglePostById() {
        PostCreateRequest requestBody = postRequest("technology", false);
        Integer postId = createPost(requestBody).jsonPath().getInt("post.id");

        given()
                .spec(authorizedRequestSpec)
                .pathParam("id", postId)
                .when()
                .get("/api/posts/{id}")
                .then()
                .statusCode(200)
                .body("post.id", equalTo(postId))
                .body("post.title", equalTo(requestBody.getTitle()))
                .body("post.description", equalTo(requestBody.getDescription()));
    }

    @Test
    @Tag("regression")
    @DisplayName("PUT /api/posts/{id} -> should update existing post")
    void shouldUpdateExistingPost() {
        Integer postId = createPost(postRequest("technology", false)).jsonPath().getInt("post.id");
        PostCreateRequest updateBody = postRequest("diy", false);

        given()
                .spec(authorizedRequestSpec)
                .pathParam("id", postId)
                .body(updateBody)
                .when()
                .put("/api/posts/{id}")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("post.id", equalTo(postId))
                .body("post.title", equalTo(updateBody.getTitle()))
                .body("post.description", equalTo(updateBody.getDescription()))
                .body("post.category", equalTo(updateBody.getCategory()));

        given()
                .spec(authorizedRequestSpec)
                .pathParam("id", postId)
                .when()
                .get("/api/posts/{id}")
                .then()
                .statusCode(200)
                .body("post.title", equalTo(updateBody.getTitle()))
                .body("post.description", equalTo(updateBody.getDescription()))
                .body("post.category", equalTo(updateBody.getCategory()));
    }

    @Test
    @Tag("regression")
    @DisplayName("DELETE /api/posts/{id} -> should delete post")
    void shouldDeletePost() {
        Integer postId = createPost(postRequest("technology", false)).jsonPath().getInt("post.id");

        given()
                .spec(authorizedRequestSpec)
                .pathParam("id", postId)
                .when()
                .delete("/api/posts/{id}")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("message", not(emptyOrNullString()));

        given()
                .spec(authorizedRequestSpec)
                .pathParam("id", postId)
                .when()
                .get("/api/posts/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    @Tag("regression")
    @DisplayName("POST /api/posts/{id}/favorite -> should add post to favorites")
    void shouldAddPostToFavorites() {
        AuthSession otherUser = authApiClient().createAuthorizedSession();
        Integer postId = createPost(authorizedSpec(otherUser), postRequest("technology", false))
                .jsonPath()
                .getInt("post.id");

        given()
                .spec(authorizedRequestSpec)
                .pathParam("id", postId)
                .body(Map.of("isFavorite", true))
                .when()
                .post("/api/posts/{id}/favorite")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("isFavorite", equalTo(true));

        given()
                .spec(authorizedRequestSpec)
                .when()
                .get("/api/posts/favorites")
                .then()
                .statusCode(200)
                .body("items.id", hasItem(postId));
    }

    @Test
    @Tag("regression")
    @DisplayName("GET /api/posts/favorites -> should return favorite posts")
    void shouldReturnFavoritePosts() {
        AuthSession otherUser = authApiClient().createAuthorizedSession();
        Integer postId = createPost(authorizedSpec(otherUser), postRequest("technology", false))
                .jsonPath()
                .getInt("post.id");

        addPostToFavorites(postId);

        given()
                .spec(authorizedRequestSpec)
                .when()
                .get("/api/posts/favorites")
                .then()
                .statusCode(200)
                .body("items", not(empty()))
                .body("items.id", hasItem(postId));
    }

    @Test
    @Tag("regression")
    @DisplayName("POST /api/files/upload -> should upload image file for post")
    void shouldUploadImageFileForPost() throws IOException {
        File imageFile = createTempPngFile();

        given()
                .spec(authorizedRequestSpec)
                .contentType(ContentType.MULTIPART)
                .multiPart("file", imageFile, "image/png")
                .multiPart("type", "post-image")
                .when()
                .post("/api/files/upload")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("url", not(emptyOrNullString()))
                .body("mimeType", equalTo("image/png"))
                .body("filename", not(emptyOrNullString()));
    }

    @Test
    @Tag("regression")
    @DisplayName("GET /api/files/{id} -> should return uploaded file metadata")
    void shouldReturnUploadedFileMetadata() throws IOException {
        Integer fileId = uploadImageFile().jsonPath().getInt("id");

        given()
                .spec(authorizedRequestSpec)
                .pathParam("id", fileId)
                .when()
                .get("/api/files/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo(fileId))
                .body("url", not(emptyOrNullString()))
                .body("filename", not(emptyOrNullString()))
                .body("size", notNullValue())
                .body("mimeType", equalTo("image/png"));
    }

    @Test
    @Tag("e2e")
    @DisplayName("POST /api/profile/report/{id} -> should create report for user")
    void shouldCreateUserReport() {
        AuthSession reportedUser = authApiClient().createAuthorizedSession();
        Map<String, Object> requestBody = Map.of(
                "descriptionReport", "Automated test report " + suffix()
        );

        given()
                .spec(authorizedRequestSpec)
                .pathParam("id", reportedUser.getUserId())
                .body(requestBody)
                .when()
                .post("/api/profile/report/{id}")
                .then()
                .statusCode(200)
                .body("status", equalTo("success"))
                .body("message", not(emptyOrNullString()));
    }

    private AuthApiClient authApiClient() {
        return new AuthApiClient(requestSpec);
    }

    private RequestSpecification authorizedSpec(AuthSession session) {
        return new RequestSpecBuilder()
                .addRequestSpecification(requestSpec)
                .addHeader("Authorization", "Bearer " + session.getAccessToken())
                .build();
    }

    private Response createPost(PostCreateRequest requestBody) {
        return createPost(authorizedRequestSpec, requestBody);
    }

    private Response createPost(RequestSpecification requestSpecification, PostCreateRequest requestBody) {
        return given()
                .spec(requestSpecification)
                .body(requestBody)
                .when()
                .post("/api/posts")
                .then()
                .statusCode(201)
                .extract()
                .response();
    }

    private void addPostToFavorites(Integer postId) {
        given()
                .spec(authorizedRequestSpec)
                .pathParam("id", postId)
                .body(Map.of("isFavorite", true))
                .when()
                .post("/api/posts/{id}/favorite")
                .then()
                .statusCode(200);
    }

    private Response uploadImageFile() throws IOException {
        return given()
                .spec(authorizedRequestSpec)
                .contentType(ContentType.MULTIPART)
                .multiPart("file", createTempPngFile(), "image/png")
                .multiPart("type", "post-image")
                .when()
                .post("/api/files/upload")
                .then()
                .statusCode(200)
                .extract()
                .response();
    }

    private PostCreateRequest postRequest(String category, boolean isDraft) {
        String suffix = suffix();
        return new PostCreateRequest(
                "API Post " + suffix,
                "This is body for REST-Assured practice post " + suffix,
                "Short description " + suffix,
                category,
                isDraft
        );
    }

    private File createTempPngFile() throws IOException {
        byte[] pngBytes = Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/p9sAAAAASUVORK5CYII="
        );
        Path tempFile = Files.createTempFile("post-image-", ".png");
        Files.write(tempFile, pngBytes);
        File file = tempFile.toFile();
        file.deleteOnExit();
        return file;
    }

    private String suffix() {
        return RandomStringUtils.secure().nextAlphanumeric(8);
    }

    private String randomPhone() {
        return "+7987" + RandomStringUtils.secure().nextNumeric(7);
    }
}
