package com.group4.inventoryserver.dto.purchase;

import static org.junit.Assert.*;

import com.group4.inventoryserver.exception.ValidationException;
import com.group4.inventoryserver.util.JsonUtil;
import com.group4.inventoryserver.util.ValidationUtil;
import com.group4.inventoryserver.util.ValidationUtil.FieldError;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class PurchaseCreateRequestTest {

  private void validate(PurchaseCreateRequest request) {
    List<FieldError> errors = ValidationUtil.newErrorList();
    if (request.getSupplierId() == null) {
      errors.add(new FieldError("supplierId", "Supplier is required."));
    }
    ValidationUtil.requireNonBlank(request.getOrderDate(), "orderDate", errors);
    if (request.getItems() == null || request.getItems().isEmpty()) {
      errors.add(new FieldError("items", "At least one item is required."));
    }
    ValidationUtil.throwIfErrors(errors);
  }

  private PurchaseCreateRequest validRequest() {
    PurchaseItemRequest item = new PurchaseItemRequest();
    item.setProductId(1L);
    item.setQuantity(10);
    item.setUnitCost(50.0);

    PurchaseCreateRequest req = new PurchaseCreateRequest();
    req.setSupplierId(1L);
    req.setOrderDate("2026-01-15");
    req.setItems(Collections.singletonList(item));
    return req;
  }

  @Test
  public void gettersAndSetters_workCorrectly() {
    PurchaseCreateRequest req = validRequest();
    req.setNotes("Rush order");

    assertEquals(Long.valueOf(1L), req.getSupplierId());
    assertEquals("2026-01-15", req.getOrderDate());
    assertEquals("Rush order", req.getNotes());
  }

  @Test
  public void roundTrip_jsonSerialization() {
    PurchaseCreateRequest parsed =
        JsonUtil.fromJson(JsonUtil.toJson(validRequest()), PurchaseCreateRequest.class);

    assertEquals(Long.valueOf(1L), parsed.getSupplierId());
    assertEquals("2026-01-15", parsed.getOrderDate());
  }

  @Test(expected = ValidationException.class)
  public void validation_failsWhenSupplierMissing() {
    PurchaseCreateRequest req = validRequest();
    req.setSupplierId(null);
    validate(req);
  }

  @Test(expected = ValidationException.class)
  public void validation_failsWhenOrderDateBlank() {
    PurchaseCreateRequest req = validRequest();
    req.setOrderDate("");
    validate(req);
  }
}
