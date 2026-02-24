package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArchiveWarehouseUseCaseTest {

  private FakeStore store;
  private ArchiveWarehouseUseCase useCase;

  @BeforeEach
  void setup() {
    store = new FakeStore();
    useCase = new ArchiveWarehouseUseCase(store);
  }

  @Test
  void archiveSetsTimestampAndUpdates() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "BU-1";
    store.persist(w);

    useCase.archive(w);

    assertEquals(1, store.updated.size());
    Warehouse updated = store.updated.get(0);
    assertNotNull(updated.archivedAt);
    assertTrue(updated.archivedAt.isBefore(LocalDateTime.now().plusSeconds(1)));
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

