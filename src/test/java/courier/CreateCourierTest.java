package courier;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import model.Courier;
import model.CourierCredentials;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class CreateCourierTest {

    private final String BASE_URL = "https://qa-scooter.praktikum-services.ru";
    private Courier courier;
    private int courierId;

    @Before
    public void setUp() {
        baseURI = BASE_URL;
        courier = new Courier("twicy_test", "1234", "Катя");
    }

    @After
    public void tearDown() {
        if (courierId != 0) {
            deleteCourier(courierId);
        }
    }

    @Step("Создание курьера")
    private Response createCourier(Courier courier) {
        return given()
                .header("Content-type", "application/json")
                .body(courier)
                .when()
                .post("/api/v1/courier");
    }

    @Step("Удаление курьера по ID")
    private void deleteCourier(int id) {
        given()
                .header("Content-type", "application/json")
                .when()
                .delete("/api/v1/courier/" + id);
    }

    @Step("Логин под курьером")
    private int loginCourier(CourierCredentials credentials) {
        return given()
                .header("Content-type", "application/json")
                .body(credentials)
                .when()
                .post("/api/v1/courier/login")
                .then()
                .statusCode(200)
                .extract()
                .path("id");
    }

    @Test
    public void testCreateCourierSuccess() {
        Response response = createCourier(courier);
        response.then().statusCode(201).body("ok", is(true));

        // авторизуемся и сохраняем ID
        courierId = loginCourier(new CourierCredentials(courier.getLogin(), courier.getPassword()));
    }

    @Test
    public void testCreateDuplicateCourier() {
        createCourier(courier).then().statusCode(201);
        createCourier(courier)
                .then()
                .statusCode(409)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));

        // авторизуемся и сохраняем ID
        courierId = loginCourier(new CourierCredentials(courier.getLogin(), courier.getPassword()));
    }

    @Test
    public void testCreateCourierWithoutPassword() {
        Courier noPasswordCourier = new Courier(courier.getLogin(), null, courier.getFirstName());
        createCourier(noPasswordCourier)
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    public void testCreateCourierWithoutLogin() {
        Courier noLoginCourier = new Courier(null, courier.getPassword(), courier.getFirstName());
        createCourier(noLoginCourier)
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    public void testCreateCourierWithOnlyLoginAndPassword() {
        Courier minimalCourier = new Courier(courier.getLogin() + "_2", courier.getPassword());
        createCourier(minimalCourier)
                .then()
                .statusCode(201)
                .body("ok", is(true));

        // авторизуемся и сохраняем ID
        courierId = loginCourier(new CourierCredentials(minimalCourier.getLogin(), minimalCourier.getPassword()));
    }
}