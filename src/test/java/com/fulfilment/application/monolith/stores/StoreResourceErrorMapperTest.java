package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

class StoreResourceErrorMapperTest {

  @Test
  void mapsWebApplicationException() {
    StoreResource.ErrorMapper mapper = new StoreResource.ErrorMapper();
    mapper.objectMapper = new ObjectMapper();

    WebApplicationException ex = new WebApplicationException("Not found", 404);
    Response resp = mapper.toResponse(ex);
    assertEquals(404, resp.getStatus());
    assertTrue(resp.getEntity() instanceof ObjectNode);
    ObjectNode body = (ObjectNode) resp.getEntity();
    assertEquals("jakarta.ws.rs.WebApplicationException", body.get("exceptionType").asText());
    assertEquals(404, body.get("code").asInt());
    assertEquals("Not found", body.get("error").asText());
  }

  @Test
  void mapsGenericExceptionTo500() {
    StoreResource.ErrorMapper mapper = new StoreResource.ErrorMapper();
    mapper.objectMapper = new ObjectMapper();

    RuntimeException ex = new RuntimeException("boom");
    Response resp = mapper.toResponse(ex);
    assertEquals(500, resp.getStatus());
    ObjectNode body = (ObjectNode) resp.getEntity();
    assertEquals("java.lang.RuntimeException", body.get("exceptionType").asText());
    assertEquals(500, body.get("code").asInt());
    assertEquals("boom", body.get("error").asText());
  }
}
