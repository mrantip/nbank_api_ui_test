package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class TransferMoneyTest {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }

    @ParameterizedTest
    @ValueSource(doubles = {10000, 9999.99, 0.01, 500})
    public void transferValidSumToOwnAccountTest(double transferSum) {
        String username = ("siroza" + UUID.randomUUID().toString().substring(0, 8)).substring(0, 14);

        // создание пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(String.format("""
                        {
                          "username": "%s",
                          "password": "Kate2000#",
                          "role": "USER"
                        }
                        """, username))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        // получаем токен юзера
        String userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                          "username": "%s",
                          "password": "Kate2000#"
                        }
                        """, username))
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // создаем 1 аккаунт(счет)
        int createdAccount1 = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        // вносим депозит
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format(""" 
                         {
                           "id": %d,
                           "balance": %s
                         }
                        """, createdAccount1, 5000))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // вносим депозит еще раз для достаточной суммы перевода
        double balance1 = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format(""" 
                         {
                           "id": %d,
                           "balance": %s
                         }
                        """, createdAccount1, 5000))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getDouble("balance");

        assertTrue(balance1 >= 10000.0);

        // создаем 2 аккаунт(счет)
        int createdAccount2 = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        //получаем текущий баланс 2 аккаунта
        double balance2 = given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getDouble("find { it.id == " + createdAccount2 + " }.balance");

        // переводим средства с 1 аккаунта на 2
        String messageResult = given()
                .headers("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format(""" 
                         {
                           "senderAccountId": %d,
                           "receiverAccountId": %d,
                           "amount": %s
                         }
                        """, createdAccount1, createdAccount2, transferSum))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getString("message");

        assertEquals("Transfer successful", messageResult);

        //получаем текущий баланс 1 аккаунта
        double balanceResult = given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getDouble("find { it.id == " + createdAccount1 + " }.balance");

        //получаем текущий баланс 2 аккаунта
        double balanceResult2 = given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getDouble("find { it.id == " + createdAccount2 + " }.balance");

        assertNotEquals(balance1, balanceResult);
        assertNotEquals(balance2, balanceResult2);
    }

    @ParameterizedTest
    @ValueSource(doubles = {10000, 9999.99, 0.01, 500})
    public void transferValidSumToStrangersAccountTest(double transferSum) {
        String username = ("siroza" + UUID.randomUUID().toString().substring(0, 8)).substring(0, 14);
        String usernameStranger = ("Sashaa" + UUID.randomUUID().toString().substring(0, 8)).substring(0, 14);

        // создание пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(String.format("""
                        {
                          "username": "%s",
                          "password": "Kate2000#",
                          "role": "USER"
                        }
                        """, username))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        // получаем токен юзера
        String userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                          "username": "%s",
                          "password": "Kate2000#"
                        }
                        """, username))
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // создаем 1 аккаунт(счет)
        int createdAccount1 = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        // вносим депозит
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format(""" 
                         {
                           "id": %d,
                           "balance": %s
                         }
                        """, createdAccount1, 5000))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // вносим депозит еще раз для достаточной суммы перевода
        double balance = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format(""" 
                         {
                           "id": %d,
                           "balance": %s
                         }
                        """, createdAccount1, 5000))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getDouble("balance");

        assertTrue(balance >= 10000.0);

        // создание 2 пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(String.format("""
                        {
                          "username": "%s",
                          "password": "Kate2000#",
                          "role": "USER"
                        }
                        """, usernameStranger))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        // получаем токен юзера
        String userAuthHeaderStranger = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                          "username": "%s",
                          "password": "Kate2000#"
                        }
                        """, usernameStranger))
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // создаем аккаунт(счет)
        int createdAccountStranger = given()
                .header("Authorization", userAuthHeaderStranger)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        //получаем баланс 2 пользователя
        Double balanceStranger = given()
                .header("Authorization", userAuthHeaderStranger)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getDouble("find { it.id == " + createdAccountStranger + " }.balance");

        // переводим средства с 1 аккаунта на 2
        String messageResult = given()
                .headers("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format(""" 
                         {
                           "senderAccountId": %d,
                           "receiverAccountId": %d,
                           "amount": %s
                         }
                        """, createdAccount1, createdAccountStranger, transferSum))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getString("message");

        assertEquals("Transfer successful", messageResult);

        //получаем текущий баланс 1 пользователя
        Double balanceResult = given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getDouble("find { it.id == " + createdAccount1 + " }.balance");

        //получаем текущий баланс 2 пользователя
        Double balanceStrangerResult = given()
                .header("Authorization", userAuthHeaderStranger)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getDouble("find { it.id == " + createdAccountStranger + " }.balance");

        assertNotEquals(balance, balanceResult);
        assertNotEquals(balanceStranger, balanceStrangerResult);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -0.01, 10000.01})
    public void transferInvalidSumToOwnAccountTest(double transferSum) {
        String username = ("siroza" + UUID.randomUUID().toString().substring(0, 8)).substring(0, 14);

        // создание пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(String.format("""
                        {
                          "username": "%s",
                          "password": "Kate2000#",
                          "role": "USER"
                        }
                        """, username))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        // получаем токен юзера
        String userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                          "username": "%s",
                          "password": "Kate2000#"
                        }
                        """, username))
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // создаем 1 аккаунт(счет)
        int createdAccount1 = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        // вносим депозит
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format(""" 
                         {
                           "id": %d,
                           "balance": %s
                         }
                        """, createdAccount1, 5000))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // вносим депозит еще 2 раза для достаточной суммы перевода (для кеса с переводом более 10000)
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format(""" 
                         {
                           "id": %d,
                           "balance": %s
                         }
                        """, createdAccount1, 5000))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        double balance = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format(""" 
                         {
                           "id": %d,
                           "balance": %s
                         }
                        """, createdAccount1, 5000))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getDouble("balance");

        assertTrue(balance >= 10000.01);

        // создаем 2 аккаунт(счет)
        int createdAccount2 = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        //получаем баланс 2 аккаунта
        double balance2 = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format(""" 
                         {
                           "id": %d,
                           "balance": %s
                         }
                        """, createdAccount2, 5000))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getDouble("balance");

        // переводим средства с 1 аккаунта на 2
        given()
                .headers("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format(""" 
                         {
                           "senderAccountId": %d,
                           "receiverAccountId": %d,
                           "amount": %s
                         }
                        """, createdAccount1, createdAccount2, transferSum))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        //получаем текущий баланс 1 аккаунта
        Double balanceResult = given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getDouble("find { it.id == " + createdAccount1 + " }.balance");

        //получаем текущий баланс 2 аккаунта
        Double balanceResult2 = given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getDouble("find { it.id == " + createdAccount2 + " }.balance");


        assertEquals(balance, balanceResult);
        assertEquals(balance2, balanceResult2);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -0.01, 10000.01})
    public void transferInvalidSumToStrangersAccountTest(double transferSum) {
        String username = ("siroza" + UUID.randomUUID().toString().substring(0, 8)).substring(0, 14);
        String usernameStranger = ("Sashaa" + UUID.randomUUID().toString().substring(0, 8)).substring(0, 14);

        // создание пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(String.format("""
                        {
                          "username": "%s",
                          "password": "Kate2000#",
                          "role": "USER"
                        }
                        """, username))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        // получаем токен юзера
        String userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                          "username": "%s",
                          "password": "Kate2000#"
                        }
                        """, username))
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // создаем 1 аккаунт(счет)
        int createdAccount1 = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        // вносим депозит
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format(""" 
                         {
                           "id": %d,
                           "balance": %s
                         }
                        """, createdAccount1, 5000))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        // вносим депозит еще 2 раза для достаточной суммы перевода (для кеса с переводом более 10000)
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format(""" 
                         {
                           "id": %d,
                           "balance": %s
                         }
                        """, createdAccount1, 5000))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        double balance = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format(""" 
                         {
                           "id": %d,
                           "balance": %s
                         }
                        """, createdAccount1, 5000))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getDouble("balance");

        assertTrue(balance >= 10000.01);

        /// создание 2 пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(String.format("""
                        {
                          "username": "%s",
                          "password": "Kate2000#",
                          "role": "USER"
                        }
                        """, usernameStranger))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        // получаем токен юзера
        String userAuthHeaderStranger = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                          "username": "%s",
                          "password": "Kate2000#"
                        }
                        """, usernameStranger))
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // создаем аккаунт(счет)
        int createdAccountStranger = given()
                .header("Authorization", userAuthHeaderStranger)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        //получаем текущий баланс 2 пользователя
        Double balance2 = given()
                .header("Authorization", userAuthHeaderStranger)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getDouble("find { it.id == " + createdAccountStranger + " }.balance");


        // переводим средства с 1 аккаунта на 2
        given()
                .headers("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format(""" 
                         {
                           "senderAccountId": %d,
                           "receiverAccountId": %d,
                           "amount": %s
                         }
                        """, createdAccount1, createdAccountStranger, transferSum))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        //получаем текущий баланс
        Double balanceResult = given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getDouble("find { it.id == " + createdAccount1 + " }.balance");

        //получаем текущий баланс 2 пользователя
        Double balanceResult2 = given()
                .header("Authorization", userAuthHeaderStranger)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getDouble("find { it.id == " + createdAccountStranger + " }.balance");

        assertEquals(balance, balanceResult);
        assertEquals(balance2, balanceResult2);
    }

    @Test
    public void transferSumHigherThanBalanceTest() {
        String username = ("siroza" + UUID.randomUUID().toString().substring(0, 8)).substring(0, 14);

        // создание пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(String.format("""
                        {
                          "username": "%s",
                          "password": "Kate2000#",
                          "role": "USER"
                        }
                        """, username))
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        // получаем токен юзера
        String userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format("""
                        {
                          "username": "%s",
                          "password": "Kate2000#"
                        }
                        """, username))
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // создаем 1 аккаунт(счет)
        int createdAccount1 = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        // вносим депозит
        double balance = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format(""" 
                         {
                           "id": %d,
                           "balance": %s
                         }
                        """, createdAccount1, 5000))
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getDouble("balance");

        assertTrue(balance < 5000.01);

        // создаем 2 аккаунт(счет)
        int createdAccount2 = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath().getInt("id");

        //получаем текущий баланс 2 frrfeynf
        Double balance2 = given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getDouble("find { it.id == " + createdAccount2 + " }.balance");

        // переводим средства с 1 аккаунта на 2
        given()
                .headers("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(String.format(""" 
                         {
                           "senderAccountId": %d,
                           "receiverAccountId": %d,
                           "amount": 5000.01
                         }
                        """, createdAccount1, createdAccount2))
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        //получаем текущий баланс
        Double balanceResult = given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getDouble("find { it.id == " + createdAccount1 + " }.balance");

        //получаем текущий баланс 2 frrfeynf
        Double balanceResult2 = given()
                .header("Authorization", userAuthHeader)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getDouble("find { it.id == " + createdAccount2 + " }.balance");

        assertEquals(balance, balanceResult);
        assertEquals(balance2, balanceResult2);
    }
}