package StellarBurgersAPI;

import io.restassured.RestAssured;

import io.restassured.response.Response;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;


import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class UserCreationTest {

    private String authToken;

    @BeforeClass
    public static void setup() {
        baseURI = "https://stellarburgers.nomoreparties.site";
    }

    public UserCreation generateUniqueUser() {
        String email = "test" + System.currentTimeMillis() + "@yandex.ru";
        String password = "Password" + System.currentTimeMillis();
        String name = "User" + System.currentTimeMillis();
        return new UserCreation(email, password, name);
    }

    public String registerUser(UserCreation user) {
        Response response = given()
                .header("Content-Type", "application/json")
                .body("{\"email\":\"" + user.getEmail() + "\", \"password\":\"" + user.getPassword() + "\", \"name\":\"" + user.getName() + "\"}")
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(201)))
                .body("success", equalTo(true))
                .extract().response();

        return response.path("accessToken");
    }

    private void deleteUser(String token) {
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/api/auth/user")
                .then()
                .statusCode(202);
    }

    @Test
    public void createUniqueUser() {
        UserCreation user = generateUniqueUser();

        RestAssured.given()
                .body(user)
                .contentType("application/json")
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .body("success", equalTo(true));
    }

    @Test
    public void createExistingUser() {
        UserCreation user = generateUniqueUser();
        registerUser(user);

        given()
                .header("Content-Type", "application/json")
                .body("{\"email\":\"" + user.getEmail() + "\", \"password\":\"" + user.getPassword() + "\", \"name\":\"" + user.getName() + "\"}")
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Test
    public void createUserWithoutRequiredFields() {
        UserCreation user = generateUniqueUser();

        // Без email
        given()
                .header("Content-Type", "application/json")
                .body("{\"password\":\"" + user.getPassword() + "\", \"name\":\"" + user.getName() + "\"}")
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));

        // Без password
        given()
                .header("Content-Type", "application/json")
                .body("{\"email\":\"" + user.getEmail() + "\", \"name\":\"" + user.getName() + "\"}")
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));

        // Без name
        given()
                .header("Content-Type", "application/json")
                .body("{\"email\":\"" + user.getEmail() + "\", \"password\":\"" + user.getPassword() + "\"}")
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @After
    public void cleanup() {
        if (authToken != null) {
            deleteUser(authToken);
            authToken = null;
        }
    }
}