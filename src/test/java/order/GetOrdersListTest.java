package order;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class GetOrdersListTest {

    private final String BASE_URL = "https://qa-scooter.praktikum-services.ru";

    @Test
    public void testGetOrdersListReturnsList() {
        Response response = getOrdersList();

        response.then()
                .statusCode(200)
                .body("orders", notNullValue())
                .body("orders.size()", greaterThan(0));
    }

    @Step("Получение списка заказов")
    private Response getOrdersList() {
        return given()
                .baseUri(BASE_URL)
                .header("Content-type", "application/json")
                .get("/api/v1/orders");
    }
}