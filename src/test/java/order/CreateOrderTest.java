package order;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import model.Order;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@RunWith(Parameterized.class)
public class CreateOrderTest {

    private final List<String> color;

    public CreateOrderTest(List<String> color) {
        this.color = color;
    }

    @Parameterized.Parameters(name = "Цвет: {0}")
    public static Object[][] getColors() {
        return new Object[][]{
                {Collections.singletonList("BLACK")},
                {Collections.singletonList("GREY")},
                {Arrays.asList("BLACK", "GREY")},
                {Collections.emptyList()}
        };
    }

    @Test
    public void testCreateOrderWithColors() {
        Order order = getDefaultOrder(color);
        Response response = createOrder(order);

        response.then()
                .statusCode(201)
                .body("track", notNullValue());
    }

    @Step("Создание заказа")
    private Response createOrder(Order order) {
        return given()
                .baseUri("https://qa-scooter.praktikum-services.ru")
                .header("Content-type", "application/json")
                .body(order)
                .post("/api/v1/orders");
    }

    @Step("Формирование тела заказа")
    private Order getDefaultOrder(List<String> color) {
        return new Order(
                "Катя",
                "QA",
                "Москва, Тестовая 1",
                "4",
                "+79991112233",
                2,
                "2025-08-01",
                "Позвонить за 15 минут",
                color
        );
    }
}