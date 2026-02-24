package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReplaceWarehouseUseCaseTest {

  private FakeStore store;
  private ReplaceWarehouseUseCase useCase;

  @BeforeEach
  void setup() {
    store = new FakeStore();
    useCase = new ReplaceWarehouseUseCase(store);
  }

  @Test
  void warehouseNotFound() {
    Warehouse newW = new Warehouse();
    newW.businessUnitCode = "MISSING";

    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> useCase.replace(newW));
    assertEquals(404, ex.getResponse().getStatus());
  }

  @Test
  void capacityCannotAccommodateStock() {
    Warehouse oldW = new Warehouse();
    oldW.businessUnitCode = "BU-1";
    oldW.stock = 50;
    store.persist(oldW);

    Warehouse newW = new Warehouse();
    newW.businessUnitCode = "BU-1";
    newW.capacity = 10; // less than old stock
    newW.stock = 50;

    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> useCase.replace(newW));
    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  void stockMustMatch() {
    Warehouse oldW = new Warehouse();
    oldW.businessUnitCode = "BU-2";
    oldW.stock = 10;
    store.persist(oldW);

    Warehouse newW = new Warehouse();
    newW.businessUnitCode = "BU-2";
    newW.capacity = 20;
    newW.stock = 5; // mismatch

    WebApplicationException ex = assertThrows(WebApplicationException.class, () -> useCase.replace(newW));
    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  void replaceSuccess() {
    Warehouse oldW = new Warehouse();
    oldW.businessUnitCode = "BU-3";
    oldW.stock = 10;
    oldW.capacity = 10;
    store.persist(oldW);

    Warehouse newW = new Warehouse();
    newW.businessUnitCode = "BU-3";
    newW.capacity = 20;
    newW.stock = 10; // matches

    useCase.replace(newW);

    assertEquals(1, store.updated.size());
    Warehouse updated = store.updated.get(0);
    assertNull(updated.archivedAt);
    assertEquals(20, updated.capacity);
  }

  static class FakeStore implements WarehouseStore {
    List<Warehouse> persisted = new ArrayList<>();
    List<Warehouse> updated = new ArrayList<>();

    void persist(Warehouse w) { persisted.add(w); }

    @Override
    public List<Warehouse> getAll() { return new ArrayList<>(persisted); }

    @Override
    public void create(Warehouse warehouse) { persist(warehouse); }

    @Override
    public void update(Warehouse warehouse) { updated.add(warehouse); }

    @Override
    public void remove(Warehouse warehouse) { persisted.removeIf(p -> p.businessUnitCode.equals(warehouse.businessUnitCode)); }

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) { return persisted.stream().filter(p -> buCode.equals(p.businessUnitCode)).findFirst().orElse(null); }
  }
}

