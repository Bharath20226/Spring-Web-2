package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.warehouse.api.beans.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class WarehouseEndpointIT {

    @Inject
    WarehouseResourceImpl resource;

    @Inject
    WarehouseRepository warehouseRepository;

    @Test
    void testCreateAndListWarehouses() {
        Warehouse apiBean = new Warehouse();
        apiBean.setBusinessUnitCode("BU1");
        apiBean.setLocation("ZWOLLE-002");
        apiBean.setCapacity(40);
        apiBean.setStock(10);

        resource.createANewWarehouseUnit(apiBean);

        List<Warehouse> warehouses = resource.listAllWarehousesUnits();
        assertFalse(warehouses.isEmpty());
        assertTrue(warehouses.stream().anyMatch(w -> "BU1".equals(w.getBusinessUnitCode())));
    }

    @Test
    void testGetWarehouseById_found() {
        Warehouse apiBean = new Warehouse();
        apiBean.setBusinessUnitCode("BU2");
        apiBean.setLocation("AMSTERDAM-002");
        apiBean.setCapacity(50);
        apiBean.setStock(5);

        resource.createANewWarehouseUnit(apiBean);

        Warehouse result = resource.getAWarehouseUnitByID("BU2");
        assertEquals("AMSTERDAM-002", result.getLocation());
    }

    @Test
    void testGetWarehouseById_notFound() {
        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> resource.getAWarehouseUnitByID("UNKNOWN"));
        assertEquals(404, ex.getResponse().getStatus());
    }

    @Test
    void testArchiveWarehouse() {
        Warehouse apiBean = new Warehouse();
        apiBean.setBusinessUnitCode("BU3");
        apiBean.setLocation("EINDHOVEN-001");
        apiBean.setCapacity(60);
        apiBean.setStock(10);

        resource.createANewWarehouseUnit(apiBean);

        // Archive it
        resource.archiveAWarehouseUnitByID("BU3");

        com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domain = warehouseRepository.findByBusinessUnitCode("BU3");
        assertNotNull(domain);
        assertNotNull(domain.archivedAt);
    }

    @Test
    void testArchiveWarehouse_notFound() {
        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> resource.archiveAWarehouseUnitByID("UNKNOWN"));
        assertEquals(404, ex.getResponse().getStatus());
    }

    @Test
    void testReplaceWarehouse() {
        Warehouse apiBean = new Warehouse();
        apiBean.setBusinessUnitCode("BU4");
        apiBean.setLocation("AMSTERDAM-002");
        apiBean.setCapacity(30);
        apiBean.setStock(10);

        resource.createANewWarehouseUnit(apiBean);

        Warehouse newBean = new Warehouse();
        newBean.setBusinessUnitCode("IGNORED");
        newBean.setLocation("AMSTERDAM-002");
        newBean.setCapacity(50);
        newBean.setStock(10);

        Warehouse result = resource.replaceTheCurrentActiveWarehouse("BU4", newBean);

        assertEquals("IGNORED", result.getBusinessUnitCode()); // returned object is same as input
        com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domain = warehouseRepository.findByBusinessUnitCode("BU4");
        assertEquals("AMSTERDAM-002", domain.location);
        assertEquals(50, domain.capacity);
    }
}
