package StellarBurgersAPI;

import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class OrderCreationTest {

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
    public void createOrderWithRegisteredUser() {

        OrderCreation.AuthTokens tokens = OrderCreation.registerUser(
                "test" + System.currentTimeMillis() + "@mail.com",
                "Password123",
                "TestUser"
        );

        String accessToken = tokens.accessToken;

                Response ingredientsResponse = given()
                .header("Authorization", accessToken)
                .when()
                .get("https://stellarburgers.nomoreparties.site/api/ingredients")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .extract().response();


        var ingredients = ingredientsResponse.jsonPath().getList("data._id");

        String ingredientId = (String) ingredients.get(0);
        String orderBody = "{ \"ingredients\": [ \"" + ingredientId + "\" ] }";

        given()
                .header("Authorization", accessToken)
                .header("Content-Type", "application/json")
                .body(orderBody)
                .when()
                .post("https://stellarburgers.nomoreparties.site/api/orders")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
    }

    @Test
    public void createOrderWithoutAuthorizationShouldSucceed() {

        Response ingredientsResponse = given()
                .when()
                .get("https://stellarburgers.nomoreparties.site/api/ingredients")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .extract()
                .response();


        String ingredientId = ingredientsResponse.jsonPath().getString("data[0]._id");
        String orderBody = "{ \"ingredients\": [ \"" + ingredientId + "\" ] }";

        given()
                .contentType("application/json")
                // Не добавляем header Authorization
                .body(orderBody)
                .when()
                .post("https://stellarburgers.nomoreparties.site/api/orders")
                // Проверяем успешный ответ
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
    }

    @Test
    public void createOrderWithoutIngredientsShouldFail() {

        String orderBody = "{ }";

        given()
                .contentType("application/json")
                .body(orderBody)
                .when()
                .post("https://stellarburgers.nomoreparties.site/api/orders")
                .then()
                .log().ifValidationFails()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", containsString("Ingredient ids must be provided"));
    }

    @Test
    public void createOrderWithInvalidIngredientHashShouldReturnServerError() {

        String invalidIngredientHash = "invalidHash1234567890";
        String orderBody = "{ \"ingredients\": [ \"" + invalidIngredientHash + "\" ] }";

        given()
                .contentType("application/json")
                // Можно оставить без авторизации
                .body(orderBody)
                .when()
                .post("https://stellarburgers.nomoreparties.site/api/orders")
                // Проверяем, что возвращается 500
                .then()
                .log().ifValidationFails()
                .statusCode(500);
    }

    @After
    public void cleanup() {
        if (authToken != null) {
            deleteUser(authToken);
            authToken = null;
        }
    }
}