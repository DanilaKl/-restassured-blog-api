package ru.qa.blogapi.auth;

import org.apache.commons.lang3.RandomStringUtils;
import ru.qa.blogapi.models.UserRegistrationRequest;

public final class TestUserFactory {

    private TestUserFactory() {
    }

    public static UserRegistrationRequest validUser() {
        String suffix = RandomStringUtils.secure().nextAlphanumeric(8).toLowerCase();
        return new UserRegistrationRequest(
                "student_" + suffix + "@example.com",
                "SecurePass123!",
                "Student" + suffix,
                "Api",
                "student_" + suffix,
                "1990-01-02",
                "+7987" + RandomStringUtils.secure().nextNumeric(7)
        );
    }
}
