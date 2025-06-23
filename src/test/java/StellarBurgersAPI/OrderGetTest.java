package StellarBurgersAPI;

import org.junit.After;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;

public class OrderGetTest {

    private String authToken;
    private void deleteUser(String token) {
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("https://stellarburgers.nomoreparties.site/api/auth/user")
                .then()
                .statusCode(202);
    }

    @Test
    public void getUserOrdersWithAuthorizationShouldReturnOrders() {

        OrderCreation.AuthTokens tokens = OrderCreation.registerUser(
                "test" + System.currentTimeMillis() + "@mail.com",
                "Password123",
                "TestUser"
        );



        given()
                .header("Authorization", tokens.accessToken)
                .when()
                .get("https://stellarburgers.nomoreparties.site/api/orders")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("orders", notNullValue())
                .body("total", greaterThanOrEqualTo(0))
                .body("totalToday", greaterThanOrEqualTo(0));
    }

    @Test
    public void getUserOrdersWithoutAuthorizationShouldReturnUnauthorized() {
        given()

                .when()
                .get("https://stellarburgers.nomoreparties.site/api/orders")
                .then()
                .log().ifValidationFails()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", containsString("You should be authorised"));
    }

    @After
    public void cleanup() {
        if (authToken != null) {
            deleteUser(authToken);
            authToken = null;
        }
    }
}
