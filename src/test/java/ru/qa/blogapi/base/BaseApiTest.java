package ru.qa.blogapi.base;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import ru.qa.blogapi.config.TestConfig;

public abstract class BaseApiTest {

    protected static RequestSpecification requestSpec;

    @BeforeAll
    static void beforeAll() {
        RestAssured.baseURI = TestConfig.baseUrl();

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(RestAssured.baseURI)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();
    }
}
