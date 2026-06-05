package com.group4.inventoryserver.dto.product;

import static org.junit.Assert.*;

import com.group4.inventoryserver.exception.ValidationException;
import com.group4.inventoryserver.util.JsonUtil;
import com.group4.inventoryserver.util.ValidationUtil;
import com.group4.inventoryserver.util.ValidationUtil.FieldError;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class ProductUpdateRequestTest {

  private static final Set<String> VALID_STATUSES =
      new HashSet<>(Arrays.asList("In Stock", "Low Stock", "Out of Stock", "Discontinued"));

  private void validate(ProductUpdateRequest request) {
    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireMaxLength(request.getProductName(), "productName", 150, errors);
    if (request.getReorderLevel() != null && request.getReorderLevel() < 0) {
      errors.add(new FieldError("reorderLevel", "Reorder level must not be negative."));
    }
    if (request.getUnitPrice() != null && request.getUnitPrice() < 0) {
      errors.add(new FieldError("unitPrice", "Unit price must not be negative."));
    }
    if (request.getStatus() != null && !VALID_STATUSES.contains(request.getStatus())) {
      errors.add(new FieldError("status", "Invalid status value."));
    }
    ValidationUtil.throwIfErrors(errors);
  }

  @Test
  public void gettersAndSetters_workCorrectly() {
    ProductUpdateRequest req = new ProductUpdateRequest();
    req.setProductName("Updated Name");
    req.setCategoryId(2L);
    req.setUnitPrice(120.0);

    assertEquals("Updated Name", req.getProductName());
    assertEquals(Long.valueOf(2L), req.getCategoryId());
    assertEquals(Double.valueOf(120.0), req.getUnitPrice());
  }

  @Test
  public void roundTrip_jsonSerialization() {
    ProductUpdateRequest original = new ProductUpdateRequest();
    original.setProductName("Mouse");
    original.setUnitPrice(45.0);

    ProductUpdateRequest parsed =
        JsonUtil.fromJson(JsonUtil.toJson(original), ProductUpdateRequest.class);

    assertEquals("Mouse", parsed.getProductName());
    assertEquals(Double.valueOf(45.0), parsed.getUnitPrice());
  }

  @Test(expected = ValidationException.class)
  public void validation_failsWhenUnitPriceNegative() {
    ProductUpdateRequest req = new ProductUpdateRequest();
    req.setUnitPrice(-5.0);
    validate(req);
  }
}
