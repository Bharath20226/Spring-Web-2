package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

    @Inject WarehouseRepository warehouseRepository;
    
    // Inject the Use Case Operations
    @Inject CreateWarehouseOperation createOperation;
    @Inject ReplaceWarehouseOperation replaceOperation;
    @Inject ArchiveWarehouseOperation archiveOperation;

    @Override
    public List<Warehouse> listAllWarehousesUnits() {
        return warehouseRepository.getAll().stream()
                .map(this::toWarehouseResponse)
                .toList();
    }

    @Override
    @Transactional
    public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
        // Delegate to Use Case
        createOperation.create(toDomain(data));
        return data;
    }

    @Override
    public Warehouse getAWarehouseUnitByID(String id) {
        var warehouse = warehouseRepository.findByBusinessUnitCode(id);
        if (warehouse == null) {
            throw new WebApplicationException("Warehouse not found", 404);
        }
        return toWarehouseResponse(warehouse);
    }

    @Override
    @Transactional
    public void archiveAWarehouseUnitByID(String id) {
        var warehouse = warehouseRepository.findByBusinessUnitCode(id);
        if (warehouse == null) {
            throw new WebApplicationException("Warehouse not found", 404);
        }
        // Delegate to Use Case
        archiveOperation.archive(warehouse);
    }

    @Override
    @Transactional
    public Warehouse replaceTheCurrentActiveWarehouse(String businessUnitCode, @NotNull Warehouse data) {
        // Ensure the domain model has the business code from the URL path
        var domainModel = toDomain(data);
        domainModel.businessUnitCode = businessUnitCode;
        
        // Delegate to Use Case
        replaceOperation.replace(domainModel);
        
        return data;
    }

    // --- Mapping Helpers ---

    private Warehouse toWarehouseResponse(com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
        var response = new Warehouse();
        response.setBusinessUnitCode(warehouse.businessUnitCode);
        response.setLocation(warehouse.location);
        response.setCapacity(warehouse.capacity);
        response.setStock(warehouse.stock);
        return response;
    }

    private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomain(Warehouse apiBean) {
        var model = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
        model.businessUnitCode = apiBean.getBusinessUnitCode();
        model.location = apiBean.getLocation();
        model.capacity = apiBean.getCapacity();
        model.stock = apiBean.getStock();
        return model;
    }
}