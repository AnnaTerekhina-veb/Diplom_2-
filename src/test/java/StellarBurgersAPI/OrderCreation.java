package StellarBurgersAPI;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class OrderCreation {


    public static AuthTokens registerUser(String email, String password, String name) {
        Response response = given()
                .header("Content-Type", "application/json")
                .body("{\"email\":\"" + email + "\", \"password\":\"" + password + "\", \"name\":\"" + name + "\"}")
                .when()
                .post("https://stellarburgers.nomoreparties.site/api/auth/register")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(201)))
                .body("success", equalTo(true))
                .extract().response();

        String accessToken = response.path("accessToken");
        String refreshToken = response.path("refreshToken");
        return new AuthTokens(accessToken, refreshToken);
    }


    public static class AuthTokens {
        public final String accessToken;
        public final String refreshToken;

        public AuthTokens(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }
}