package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;

  @jakarta.inject.Inject
  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    var oldWarehouse = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (oldWarehouse == null) {
        throw new WebApplicationException("Warehouse to replace not found", 404);
    }

    // Validation: Capacity Accommodation
    if (newWarehouse.capacity < oldWarehouse.stock) {
        throw new WebApplicationException("New capacity cannot accommodate existing stock", 400);
    }
    
    // Validation: Stock Matching
    if (!newWarehouse.stock.equals(oldWarehouse.stock)) {
        throw new WebApplicationException("Stock must match the previous warehouse stock", 400);
    }

    // New warehouse should not be archived
    newWarehouse.archivedAt = null;
    warehouseStore.update(newWarehouse);
  }
}
