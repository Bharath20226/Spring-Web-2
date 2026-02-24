package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class StoreResourceIT {

  @Test
  void testListAllStores() {
    given()
        .when()
        .get("/store")
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));
  }

  @Test
  void testGetStoreById() {
    given()
        .when()
        .get("/store/1")
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"));
  }

  @Test
  void testGetStoreById_NotFound() {
    given()
        .when()
        .get("/store/9999")
        .then()
        .statusCode(404)
        .body(containsString("does not exist"));
  }

  @Test
  void testCreateStore() {
    String payload = """
        {
          "name": "NEW-STORE",
          "quantityProductsInStock": 50
        }
        """;

    given()
        .contentType("application/json")
        .body(payload)
        .when()
        .post("/store")
        .then()
        .statusCode(201)
        .body(containsString("NEW-STORE"));
  }

  @Test
  void testCreateStore_InvalidId() {
    String payload = """
        {
          "id": 999,
          "name": "INVALID-STORE",
          "quantityProductsInStock": 20
        }
        """;

    given()
        .contentType("application/json")
        .body(payload)
        .when()
        .post("/store")
        .then()
        .statusCode(422)
        .body(containsString("Id was invalidly set on request"));
  }

  @Test
  void testUpdateStore() {
    String payload = """
        {
          "name": "UPDATED-TONSTAD",
          "quantityProductsInStock": 25
        }
        """;

    given()
        .contentType("application/json")
        .body(payload)
        .when()
        .put("/store/1")
        .then()
        .statusCode(200)
        .body(containsString("UPDATED-TONSTAD"));
  }

  @Test
  void testUpdateStore_NoName() {
    String payload = """
        {
          "quantityProductsInStock": 20
        }
        """;

    given()
        .contentType("application/json")
        .body(payload)
        .when()
        .put("/store/1")
        .then()
        .statusCode(422)
        .body(containsString("Store Name was not set on request"));
  }

  @Test
  void testUpdateStore_NotFound() {
    String payload = """
        {
          "name": "GHOST-STORE",
          "quantityProductsInStock": 30
        }
        """;

    given()
        .contentType("application/json")
        .body(payload)
        .when()
        .put("/store/9999")
        .then()
        .statusCode(404)
        .body(containsString("does not exist"));
  }

  @Test
  void testPatchStore() {
    String payload = """
        {
          "name": "PATCHED-KALLAX"
        }
        """;

    given()
        .contentType("application/json")
        .body(payload)
        .when()
        .patch("/store/2")
        .then()
        .statusCode(200)
        .body(containsString("PATCHED-KALLAX"));
  }

  @Test
  void testPatchStore_NotFound() {
    String payload = """
        {
          "name": "GHOST-PATCH"
        }
        """;

    given()
        .contentType("application/json")
        .body(payload)
        .when()
        .patch("/store/9999")
        .then()
        .statusCode(404)
        .body(containsString("does not exist"));
  }

  @Test
  void testDeleteStore() {
    given()
        .when()
        .delete("/store/3")
        .then()
        .statusCode(204);

    // Verify it's deleted
    given()
        .when()
        .get("/store/3")
        .then()
        .statusCode(404);
  }

  @Test
  void testDeleteStore_NotFound() {
    given()
        .when()
        .delete("/store/9999")
        .then()
        .statusCode(404)
        .body(containsString("does not exist"));
  }

  @Test
  void testErrorMapperInvalidJson() {
    given()
        .contentType("application/json")
        .body("{invalid json")
        .when()
        .post("/store")
        .then()
        .statusCode(400);
  }
}
