package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ProductResourceIT {

  @Test
  void testListAllProducts() {
    given()
        .when()
        .get("/product")
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));
  }

  @Test
  void testGetProductById() {
    given()
        .when()
        .get("/product/1")
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"));
  }

  @Test
  void testGetProductById_NotFound() {
    given()
        .when()
        .get("/product/9999")
        .then()
        .statusCode(404)
        .body(containsString("does not exist"));
  }

  @Test
  void testCreateProduct() {
    String payload = """
        {
          "name": "NEW-PRODUCT",
          "description": "New test product",
          "price": 99.99,
          "stock": 100
        }
        """;

    given()
        .contentType("application/json")
        .body(payload)
        .when()
        .post("/product")
        .then()
        .statusCode(201)
        .body(containsString("NEW-PRODUCT"));
  }

  @Test
  void testCreateProduct_InvalidId() {
    String payload = """
        {
          "id": 999,
          "name": "INVALID-PRODUCT",
          "price": 10.0,
          "stock": 50
        }
        """;

    given()
        .contentType("application/json")
        .body(payload)
        .when()
        .post("/product")
        .then()
        .statusCode(422)
        .body(containsString("Id was invalidly set on request"));
  }

  @Test
  void testUpdateProduct() {
    String payload = """
        {
          "name": "UPDATED-TONSTAD",
          "description": "Updated description",
          "price": 45.99,
          "stock": 20
        }
        """;

    given()
        .contentType("application/json")
        .body(payload)
        .when()
        .put("/product/1")
        .then()
        .statusCode(200)
        .body(containsString("UPDATED-TONSTAD"));
  }

  @Test
  void testUpdateProduct_NoName() {
    String payload = """
        {
          "description": "Missing name",
          "price": 50.0,
          "stock": 30
        }
        """;

    given()
        .contentType("application/json")
        .body(payload)
        .when()
        .put("/product/1")
        .then()
        .statusCode(422)
        .body(containsString("Product Name was not set on request"));
  }

  @Test
  void testUpdateProduct_NotFound() {
    String payload = """
        {
          "name": "GHOST-PRODUCT",
          "price": 10.0,
          "stock": 50
        }
        """;

    given()
        .contentType("application/json")
        .body(payload)
        .when()
        .put("/product/9999")
        .then()
        .statusCode(404)
        .body(containsString("does not exist"));
  }

  @Test
  void testDeleteProduct() {
    // First create a product to delete
    String payload = """
        {
          "name": "PRODUCT-TO-DELETE",
          "price": 5.0,
          "stock": 10
        }
        """;

    Long productId = given()
        .contentType("application/json")
        .body(payload)
        .when()
        .post("/product")
        .then()
        .statusCode(201)
        .extract()
        .jsonPath()
        .getLong("id");

    // Delete it
    given()
        .when()
        .delete("/product/" + productId)
        .then()
        .statusCode(204);

    // Verify it's deleted
    given()
        .when()
        .get("/product/" + productId)
        .then()
        .statusCode(404);
  }

  @Test
  void testDeleteProduct_NotFound() {
    given()
        .when()
        .delete("/product/9999")
        .then()
        .statusCode(404)
        .body(containsString("does not exist"));
  }

  @Test
  void testListProductsSorted() {
    given()
        .when()
        .get("/product")
        .then()
        .statusCode(200)
        .body(containsString("BESTÅ")); // Should be sorted by name
  }

  @Test
  void testErrorMapperInvalidJson() {
    given()
        .contentType("application/json")
        .body("{invalid json")
        .when()
        .post("/product")
        .then()
        .statusCode(400);
  }
}
