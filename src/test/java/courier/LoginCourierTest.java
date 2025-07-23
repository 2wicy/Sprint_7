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

public class LoginCourierTest {

    private final String BASE_URL = "https://qa-scooter.praktikum-services.ru";
    private Courier courier;
    private int courierId;

    @Before
    public void setUp() {
        baseURI = BASE_URL;
        // создаём курьера для тестов
        courier = new Courier("twicy_login_test", "1234", "Екатерина");
        createCourier(courier);
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
                .post("/api/v1/courier");
    }

    @Step("Удаление курьера по ID")
    private void deleteCourier(int id) {
        given()
                .header("Content-type", "application/json")
                .delete("/api/v1/courier/" + id);
    }

    @Step("Логин под курьером")
    private Response loginCourier(CourierCredentials credentials) {
        return given()
                .header("Content-type", "application/json")
                .body(credentials)
                .post("/api/v1/courier/login");
    }

    @Test
    public void testLoginCourierSuccess() {
        CourierCredentials creds = new CourierCredentials(courier.getLogin(), courier.getPassword());
        Response response = loginCourier(creds);

        response.then().statusCode(200).body("id", notNullValue());

        courierId = response.then().extract().path("id");
    }

    @Test
    public void testLoginWithWrongPassword() {
        CourierCredentials creds = new CourierCredentials(courier.getLogin(), "wrong_password");
        loginCourier(creds)
                .then()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    public void testLoginWithWrongLogin() {
        CourierCredentials creds = new CourierCredentials("wrong_login", courier.getPassword());
        loginCourier(creds)
                .then()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    public void testLoginWithoutLogin() {
        CourierCredentials creds = new CourierCredentials(null, courier.getPassword());
        loginCourier(creds)
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    public void testLoginWithoutPassword() {
        Courier courier = new Courier("loginOnly", null);

        given()
                .header("Content-type", "application/json")
                .and()
                .body(courier)
                .when()
                .post(BASE_URL + "/api/v1/courier/login")
                .then()
                .assertThat()
                .statusCode(anyOf(equalTo(400), equalTo(504)))
                .body("message", notNullValue());
    }
    @Test
    public void testLoginWithNonExistentUser() {
        CourierCredentials creds = new CourierCredentials("ghost_user_404", "no_pass");
        loginCourier(creds)
                .then()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }
}