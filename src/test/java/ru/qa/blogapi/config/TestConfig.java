package ru.qa.blogapi.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class TestConfig {

    private static final Properties PROPERTIES = loadProperties();

    private TestConfig() {
    }

    public static String baseUrl() {
        String systemPropertyBaseUrl = System.getProperty("base.url");
        if (systemPropertyBaseUrl != null && !systemPropertyBaseUrl.isBlank()) {
            return systemPropertyBaseUrl;
        }

        String environmentBaseUrl = System.getenv("BASE_URL");
        if (environmentBaseUrl != null && !environmentBaseUrl.isBlank()) {
            return environmentBaseUrl;
        }

        return PROPERTIES.getProperty("base.url", "http://localhost:3000");
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = TestConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить application.properties", e);
        }
        return properties;
    }
}
