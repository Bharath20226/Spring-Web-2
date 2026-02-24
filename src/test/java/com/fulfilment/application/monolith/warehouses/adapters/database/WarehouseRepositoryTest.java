package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class WarehouseRepositoryTest {

  @Inject WarehouseRepository repository;

  @Test
  @Transactional
  void testCreateWarehouse() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "TEST-CREATE-001";
    w.location = "AMSTERDAM-002";
    w.capacity = 50;
    w.stock = 10;
    w.createdAt = LocalDateTime.now();
    w.archivedAt = null;

    repository.create(w);

    Warehouse found = repository.findByBusinessUnitCode("TEST-CREATE-001");
    assertNotNull(found);
    assertEquals("TEST-CREATE-001", found.businessUnitCode);
    assertEquals("AMSTERDAM-002", found.location);
    assertEquals(50, found.capacity);
    assertEquals(10, found.stock);
  }

  @Test
  @Transactional
  void testFindByBusinessUnitCode_NotFound() {
    Warehouse found = repository.findByBusinessUnitCode("NON-EXISTENT-001");
    assertNull(found);
  }

  @Test
  @Transactional
  void testUpdateWarehouse() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "TEST-UPDATE-001";
    w.location = "ZWOLLE-002";
    w.capacity = 50;
    w.stock = 10;
    w.createdAt = LocalDateTime.now();
    w.archivedAt = null;

    repository.create(w);

    // Update the warehouse
    Warehouse toUpdate = repository.findByBusinessUnitCode("TEST-UPDATE-001");
    toUpdate.capacity = 75;
    toUpdate.stock = 20;
    repository.update(toUpdate);

    Warehouse updated = repository.findByBusinessUnitCode("TEST-UPDATE-001");
    assertEquals(75, updated.capacity);
    assertEquals(20, updated.stock);
  }

  @Test
  @Transactional
  void testArchiveWarehouse() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "TEST-ARCHIVE-001";
    w.location = "EINDHOVEN-001";
    w.capacity = 60;
    w.stock = 15;
    w.createdAt = LocalDateTime.now();
    w.archivedAt = null;

    repository.create(w);

    // Archive it
    Warehouse toArchive = repository.findByBusinessUnitCode("TEST-ARCHIVE-001");
    toArchive.archivedAt = LocalDateTime.now();
    repository.update(toArchive);

    Warehouse archived = repository.findByBusinessUnitCode("TEST-ARCHIVE-001");
    assertNotNull(archived.archivedAt);
  }

  @Test
  @Transactional
  void testGetAll_FiltersArchived() {
    Warehouse active = new Warehouse();
    active.businessUnitCode = "TEST-ACTIVE-001";
    active.location = "TILBURG-001";
    active.capacity = 40;
    active.stock = 5;
    active.createdAt = LocalDateTime.now();
    active.archivedAt = null;

    Warehouse archived = new Warehouse();
    archived.businessUnitCode = "TEST-ARCHIVED-001";
    archived.location = "HELMOND-001";
    archived.capacity = 45;
    archived.stock = 3;
    archived.createdAt = LocalDateTime.now();
    archived.archivedAt = LocalDateTime.now();

    repository.create(active);
    repository.create(archived);

    var allWarehouses = repository.getAll();

    // Check active is present
    assertTrue(allWarehouses.stream()
        .anyMatch(w -> "TEST-ACTIVE-001".equals(w.businessUnitCode)));

    // Check archived is NOT present (filtered out)
    assertFalse(allWarehouses.stream()
        .anyMatch(w -> "TEST-ARCHIVED-001".equals(w.businessUnitCode)));
  }

  @Test
  @Transactional
  void testRemoveWarehouse() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "TEST-REMOVE-001";
    w.location = "VETSBY-001";
    w.capacity = 90;
    w.stock = 50;
    w.createdAt = LocalDateTime.now();
    w.archivedAt = null;

    repository.create(w);

    Warehouse found = repository.findByBusinessUnitCode("TEST-REMOVE-001");
    assertNotNull(found);

    // Remove it
    repository.remove(w);

    Warehouse notFound = repository.findByBusinessUnitCode("TEST-REMOVE-001");
    assertNull(notFound);
  }

  @Test
  @Transactional
  void testGetAll_EmptyWhenAllArchived() {
    Warehouse w1 = new Warehouse();
    w1.businessUnitCode = "TEST-ALL-ARCHIVED-001";
    w1.location = "AMSTERDAM-001";
    w1.capacity = 100;
    w1.stock = 50;
    w1.createdAt = LocalDateTime.now();
    w1.archivedAt = LocalDateTime.now(); // Already archived

    repository.create(w1);

    var allWarehouses = repository.getAll();

    assertFalse(allWarehouses.stream()
        .anyMatch(w -> "TEST-ALL-ARCHIVED-001".equals(w.businessUnitCode)));
  }

  @Test
  @Transactional
  void testMultipleWarehouses() {
    Warehouse w1 = new Warehouse();
    w1.businessUnitCode = "TEST-MULTI-001";
    w1.location = "ZWOLLE-001";
    w1.capacity = 40;
    w1.stock = 10;
    w1.createdAt = LocalDateTime.now();
    w1.archivedAt = null;

    Warehouse w2 = new Warehouse();
    w2.businessUnitCode = "TEST-MULTI-002";
    w2.location = "AMSTERDAM-002";
    w2.capacity = 75;
    w2.stock = 20;
    w2.createdAt = LocalDateTime.now();
    w2.archivedAt = null;

    repository.create(w1);
    repository.create(w2);

    Warehouse found1 = repository.findByBusinessUnitCode("TEST-MULTI-001");
    Warehouse found2 = repository.findByBusinessUnitCode("TEST-MULTI-002");

    assertNotNull(found1);
    assertNotNull(found2);
    assertEquals("TEST-MULTI-001", found1.businessUnitCode);
    assertEquals("TEST-MULTI-002", found2.businessUnitCode);
  }
}
