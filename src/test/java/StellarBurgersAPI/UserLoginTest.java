package StellarBurgersAPI;

import org.junit.BeforeClass;
import org.junit.Test;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

public class UserLoginTest {

    private final UserCreationTest userCreationHelper = new UserCreationTest();

    @BeforeClass
    public static void setup() {
        baseURI = "https://stellarburgers.nomoreparties.site";
    }

    // Метод для регистрации пользователя и получения токена
    private String registerAndGetToken(UserCreation user) {
        return userCreationHelper.registerUser(user);
    }

    // Создаем нового пользователя и получаем токен
    private String createUserAndGetToken() {
        UserCreation user = userCreationHelper.generateUniqueUser();
        return registerAndGetToken(user);
    }

    @Test
    public void createAndLoginUser() {

        UserCreation user = userCreationHelper.generateUniqueUser();
        given()
                .body(user)
                .contentType("application/json")
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200)
                .body("success", equalTo(true));

        given()
                .body(user)
                .contentType("application/json")
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    public void loginWithIncorrectCredentials() {

        UserCreation invalidUser = new UserCreation("wrongemail@example.com", "wrongpassword", "WrongName");

        given()
                .body(invalidUser)
                .contentType("application/json")
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    public void updateUserWithAuthorization() {
        String token = createUserAndGetToken();

        String newEmail = "newemail" + System.currentTimeMillis() + "@example.com";
        String newName = "NewName" + System.currentTimeMillis();

        given()
                .header("Authorization", token)
                .body("{\"email\": \"" + newEmail + "\", \"name\": \"" + newName + "\"}")
                .contentType("application/json")
                .when()
                .patch("/api/auth/user")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(newEmail))
                .body("user.name", equalTo(newName));
    }

    @Test
    public void updateUserWithoutAuthorization() {
        given()
                .body("{\"email\": \"unauth" + System.currentTimeMillis() + "@example.com\", \"name\": \"NoAuth\"}")
                .contentType("application/json")
                .when()
                .patch("/api/auth/user")
                .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }
}