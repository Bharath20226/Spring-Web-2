package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CreateWarehouseUseCaseTest {

  private FakeStore store;
  private FakeLocationResolver resolver;
  private CreateWarehouseUseCase useCase;

  @BeforeEach
  void setup() {
    store = new FakeStore();
    resolver = new FakeLocationResolver();
    useCase = new CreateWarehouseUseCase(store, resolver);
  }

  @Test
  void createSuccess() {
    resolver.location = new Location("LOC-1", 2, 100);

    Warehouse w = new Warehouse();
    w.businessUnitCode = "BU-1";
    w.location = "LOC-1";
    w.capacity = 50;
    w.stock = 10;

    useCase.create(w);

    assertEquals(1, store.created.size());
    Warehouse created = store.created.get(0);
    assertNotNull(created.createdAt);
    assertEquals("BU-1", created.businessUnitCode);
  }

  @Test
  void duplicateBusinessUnitCode() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "BU-1";
    store.persist(existing);

    Warehouse w = new Warehouse();
    w.businessUnitCode = "BU-1";
    w.location = "LOC-1";
    w.capacity = 10;
    w.stock = 1;

    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> useCase.create(w));
    assertEquals(409, ex.getResponse().getStatus());
  }

  @Test
  void invalidLocation() {
    resolver.location = null;
    Warehouse w = new Warehouse();
    w.businessUnitCode = "BU-2";
    w.location = "UNKNOWN";
    w.capacity = 10;
    w.stock = 1;

    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> useCase.create(w));
    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  void maxWarehousesReached() {
    resolver.location = new Location("LOC-1", 1, 100);
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "BU-EX";
    existing.location = "LOC-1";
    store.persist(existing);

    Warehouse w = new Warehouse();
    w.businessUnitCode = "BU-NEW";
    w.location = "LOC-1";
    w.capacity = 10;
    w.stock = 1;

    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> useCase.create(w));
    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  void capacityExceedsLocationMax() {
    resolver.location = new Location("LOC-1", 2, 20);
    Warehouse w = new Warehouse();
    w.businessUnitCode = "BU-3";
    w.location = "LOC-1";
    w.capacity = 30; // exceeds 20
    w.stock = 0;

    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> useCase.create(w));
    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  void stockExceedsCapacity() {
    resolver.location = new Location("LOC-1", 2, 100);
    Warehouse w = new Warehouse();
    w.businessUnitCode = "BU-4";
    w.location = "LOC-1";
    w.capacity = 10;
    w.stock = 20; // exceeds capacity

    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> useCase.create(w));
    assertEquals(400, ex.getResponse().getStatus());
  }

  // --- Test fakes ---
  static class FakeStore implements WarehouseStore {
    List<Warehouse> created = new ArrayList<>();
    List<Warehouse> persisted = new ArrayList<>();

    void persist(Warehouse w) { persisted.add(w); }

    @Override
    public List<Warehouse> getAll() {
      return new ArrayList<>(persisted);
    }

    @Override
    public void create(Warehouse warehouse) {
      created.add(warehouse);
    }

    @Override
    public void update(Warehouse warehouse) {
      // not needed for these tests
    }

    @Override
    public void remove(Warehouse warehouse) {
      persisted.removeIf(p -> p.businessUnitCode.equals(warehouse.businessUnitCode));
    }

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) {
      return persisted.stream().filter(p -> buCode.equals(p.businessUnitCode)).findFirst().orElse(null);
    }
  }

  static class FakeLocationResolver implements LocationResolver {
    Location location;

    @Override
    public Location resolveByIdentifier(String identifier) {
      return location;
    }
  }
}

