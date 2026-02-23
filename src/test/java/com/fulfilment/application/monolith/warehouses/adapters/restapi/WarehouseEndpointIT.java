package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse as DomainWarehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class WarehouseResourceImplTest {

    @Inject
    WarehouseResourceImpl resource;

    @Inject
    WarehouseRepository warehouseRepository;

    @Test
    void testCreateAndListWarehouses() {
        Warehouse apiBean = new Warehouse();
        apiBean.setBusinessUnitCode("BU1");
        apiBean.setLocation("Hyderabad");
        apiBean.setCapacity(100);
        apiBean.setStock(50);

        resource.createANewWarehouseUnit(apiBean);

        List<Warehouse> warehouses = resource.listAllWarehousesUnits();
        assertFalse(warehouses.isEmpty());
        assertEquals("BU1", warehouses.get(0).getBusinessUnitCode());
    }

    @Test
    void testGetWarehouseById_found() {
        Warehouse apiBean = new Warehouse();
        apiBean.setBusinessUnitCode("BU2");
        apiBean.setLocation("Delhi");
        apiBean.setCapacity(200);
        apiBean.setStock(80);

        resource.createANewWarehouseUnit(apiBean);

        Warehouse result = resource.getAWarehouseUnitByID("BU2");
        assertEquals("Delhi", result.getLocation());
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
        apiBean.setLocation("Mumbai");
        apiBean.setCapacity(300);
        apiBean.setStock(120);

        resource.createANewWarehouseUnit(apiBean);

        // Archive it
        resource.archiveAWarehouseUnitByID("BU3");

        // After archiving, depending on your domain logic, you can assert state
        DomainWarehouse domain = warehouseRepository.findByBusinessUnitCode("BU3");
        assertNotNull(domain);
        // For example, if archive sets a flag, check that flag here
        // assertTrue(domain.isArchived());
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
        apiBean.setLocation("Chennai");
        apiBean.setCapacity(400);
        apiBean.setStock(200);

        resource.createANewWarehouseUnit(apiBean);

        Warehouse newBean = new Warehouse();
        newBean.setBusinessUnitCode("IGNORED");
        newBean.setLocation("Bangalore");
        newBean.setCapacity(500);
        newBean.setStock(300);

        Warehouse result = resource.replaceTheCurrentActiveWarehouse("BU4", newBean);

        assertEquals("IGNORED", result.getBusinessUnitCode()); // returned object is same as input
        DomainWarehouse domain = warehouseRepository.findByBusinessUnitCode("BU4");
        assertEquals("Bangalore", domain.location);
        assertEquals(500, domain.capacity);
    }
}