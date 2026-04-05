package com.jmp.e2e;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RedisContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * End-to-End tests for Conference Management API.
 * Uses Testcontainers for isolated database and Redis instances.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConferenceE2ETest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("jmp_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static RedisContainer redis = new RedisContainer(
            DockerImageName.parse("redis:7-alpine"));

    @LocalServerPort
    private Integer port;

    @Autowired
    private org.springframework.test.web.reactive.server.WebTestClient webTestClient;

    private String authToken;

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379).toString());
    }

    @BeforeAll
    static void beforeAll() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        RestAssured.port = port;
    }

    @Test
    @Order(1)
    @DisplayName("Should authenticate user and return JWT token")
    void testAuthentication() {
        // First, register a test user
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("""
                {
                    "email": "admin@test.com",
                    "password": "SecurePass123!",
                    "firstName": "Test",
                    "lastName": "Admin"
                }
                """)
        .when()
            .post("/api/v1/auth/register")
        .then()
            .statusCode(anyOf(equalTo(HttpStatus.CREATED.value()), equalTo(HttpStatus.OK.value())));

        // Login and get token
        Response response = given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("""
                {
                    "email": "admin@test.com",
                    "password": "SecurePass123!"
                }
                """)
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("accessToken", notNullValue())
            .body("refreshToken", notNullValue())
            .extract().response();

        authToken = response.jsonPath().getString("accessToken");
        
        Assertions.assertNotNull(authToken);
        Assertions.assertTrue(authToken.length() > 50);
    }

    @Test
    @Order(2)
    @DisplayName("Should create a new conference")
    void testCreateConference() {
        given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("""
                {
                    "name": "E2E Test Conference",
                    "description": "Conference created during E2E testing",
                    "scheduledStartAt": "2026-04-06T10:00:00Z",
                    "settings": {
                        "allowRecording": true,
                        "requireModerator": false,
                        "maxParticipants": 50
                    }
                }
                """)
        .when()
            .post("/api/v1/conferences")
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .body("id", notNullValue())
            .body("name", equalTo("E2E Test Conference"))
            .body("status", equalTo("SCHEDULED"))
            .body("roomName", notNullValue());
    }

    @Test
    @Order(3)
    @DisplayName("Should retrieve conferences with pagination")
    void testGetConferences() {
        given()
            .header("Authorization", "Bearer " + authToken)
            .queryParam("page", 0)
            .queryParam("size", 10)
            .queryParam("sort", "createdAt,desc")
        .when()
            .get("/api/v1/conferences")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("content", notNullValue())
            .body("content.size()", greaterThan(0))
            .body("totalElements", greaterThan(0))
            .body("totalPages", greaterThan(0))
            .body("number", equalTo(0))
            .body("size", equalTo(10));
    }

    @Test
    @Order(4)
    @DisplayName("Should generate JWT token for conference access")
    void testGenerateConferenceToken() {
        // Get first conference ID
        String conferenceId = given()
            .header("Authorization", "Bearer " + authToken)
            .queryParam("page", 0)
            .queryParam("size", 1)
        .when()
            .get("/api/v1/conferences")
        .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getString("content[0].id");

        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .post("/api/v1/conferences/" + conferenceId + "/token")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("token", notNullValue())
            .body("expiresIn", greaterThan(0));
    }

    @Test
    @Order(5)
    @DisplayName("Should update conference status")
    void testUpdateConference() {
        String conferenceId = given()
            .header("Authorization", "Bearer " + authToken)
            .queryParam("page", 0)
            .queryParam("size", 1)
        .when()
            .get("/api/v1/conferences")
        .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getString("content[0].id");

        given()
            .header("Authorization", "Bearer " + authToken)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("""
                {
                    "name": "Updated Conference Name",
                    "description": "Updated description"
                }
                """)
        .when()
            .put("/api/v1/conferences/" + conferenceId)
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("id", equalTo(conferenceId))
            .body("name", equalTo("Updated Conference Name"))
            .body("description", equalTo("Updated description"));
    }

    @Test
    @Order(6)
    @DisplayName("Should handle Jitsi webhook events")
    void testJitsiWebhook() {
        // Simulate Jitsi conference_created webhook
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .header("X-Jitsi-Signature", "test-signature")
            .body("""
                {
                    "event": "conference_created",
                    "room": "test-room-e2e",
                    "timestamp": 1712345000,
                    "tenant_id": "default"
                }
                """)
        .when()
            .post("/api/v1/webhooks/jitsi")
        .then()
            .statusCode(HttpStatus.ACCEPTED.value());
    }

    @Test
    @Order(7)
    @DisplayName("Should retrieve user profile")
    void testGetUserProfile() {
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get("/api/v1/users/me")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("email", equalTo("admin@test.com"))
            .body("firstName", equalTo("Test"))
            .body("lastName", equalTo("Admin"));
    }

    @Test
    @Order(8)
    @DisplayName("Should handle authentication errors")
    void testUnauthorizedAccess() {
        given()
            .header("Authorization", "Bearer invalid-token")
        .when()
            .get("/api/v1/conferences")
        .then()
            .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @AfterAll
    static void afterAll() {
        // Cleanup containers
        if (postgres.isRunning()) {
            postgres.stop();
        }
        if (redis.isRunning()) {
            redis.stop();
        }
    }
}
