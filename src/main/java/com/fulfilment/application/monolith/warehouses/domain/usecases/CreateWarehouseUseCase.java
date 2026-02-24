package com.fulfilment.application.monolith.warehouses.domain.usecases;

import java.time.LocalDateTime;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  @jakarta.inject.Inject
  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    // 1. Business Unit Code Verification
    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
        throw new WebApplicationException("Business Unit Code already exists", 409);
    }

    // 2. Location Validation
    var location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
        throw new WebApplicationException("Invalid location", 400);
    }

    // 3. Feasibility Check (Max warehouses per location)
    long currentCount = warehouseStore.getAll().stream()
            .filter(w -> w.location.equals(warehouse.location) && w.archivedAt == null)
            .count();
    if (currentCount >= location.maxNumberOfWarehouses) {
        throw new WebApplicationException("Maximum warehouses reached for this location", 400);
    }

    // 4. Capacity and Stock Validation
    if (warehouse.capacity > location.maxCapacity) {
        throw new WebApplicationException("Capacity exceeds location maximum", 400);
    }
    if (warehouse.stock > warehouse.capacity) {
        throw new WebApplicationException("Stock exceeds capacity", 400);
    }

    warehouse.createdAt = LocalDateTime.now();
    warehouseStore.create(warehouse);
  }
}

