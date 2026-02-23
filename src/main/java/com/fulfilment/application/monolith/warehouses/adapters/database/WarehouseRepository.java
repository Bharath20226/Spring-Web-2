package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

    @Override
    public List<Warehouse> getAll() {
        return this.listAll().stream()
                .filter(dw -> dw.archivedAt == null) // Only active warehouses
                .map(DbWarehouse::toWarehouse)
                .toList();
    }

    @Override
    public void create(Warehouse warehouse) {
        DbWarehouse dw = fromDomain(warehouse);
        this.persist(dw);
    }

    @Override
    public void update(Warehouse warehouse) {
        DbWarehouse existing = this.find("businessUnitCode", warehouse.businessUnitCode).firstResult();
        if (existing != null) {
            existing.capacity = warehouse.capacity;
            existing.stock = warehouse.stock;
            existing.location = warehouse.location;
            existing.archivedAt = warehouse.archivedAt;
            // Hibernate updates this automatically at end of transaction
        }
    }

    @Override
    public void remove(Warehouse warehouse) {
        this.delete("businessUnitCode", warehouse.businessUnitCode);
    }

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) {
        return this.find("businessUnitCode", buCode)
                .firstResultOptional()
                .map(DbWarehouse::toWarehouse)
                .orElse(null);
    }

    private DbWarehouse fromDomain(Warehouse w) {
        DbWarehouse dw = new DbWarehouse();
        dw.businessUnitCode = w.businessUnitCode;
        dw.location = w.location;
        dw.capacity = w.capacity;
        dw.stock = w.stock;
        dw.createdAt = w.createdAt;
        dw.archivedAt = w.archivedAt;
        return dw;
    }
}