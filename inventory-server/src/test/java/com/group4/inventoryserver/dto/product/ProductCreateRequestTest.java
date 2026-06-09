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

public class ProductCreateRequestTest {

  private static final Set<String> VALID_STATUSES =
      new HashSet<>(Arrays.asList("In Stock", "Low Stock", "Out of Stock", "Discontinued"));

  private void validate(ProductCreateRequest request) {
    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireNonBlank(request.getProductCode(), "productCode", errors);
    ValidationUtil.requireNonBlank(request.getProductName(), "productName", errors);
    ValidationUtil.requireMaxLength(request.getProductCode(), "productCode", 30, errors);
    ValidationUtil.requireMaxLength(request.getProductName(), "productName", 150, errors);
    if (request.getCategoryId() == null) {
      errors.add(new FieldError("categoryId", "Category is required."));
    }
    if (request.getQuantity() == null) {
      errors.add(new FieldError("quantity", "Quantity is required."));
    } else if (request.getQuantity() < 0) {
      errors.add(new FieldError("quantity", "Quantity must not be negative."));
    }
    if (request.getUnitPrice() == null) {
      errors.add(new FieldError("unitPrice", "Unit price is required."));
    } else if (request.getUnitPrice() < 0) {
      errors.add(new FieldError("unitPrice", "Unit price must not be negative."));
    }
    if (request.getStatus() != null && !VALID_STATUSES.contains(request.getStatus())) {
      errors.add(new FieldError("status", "Invalid status value."));
    }
    ValidationUtil.throwIfErrors(errors);
  }

  private ProductCreateRequest validRequest() {
    ProductCreateRequest req = new ProductCreateRequest();
    req.setProductCode("P001");
    req.setProductName("Keyboard");
    req.setCategoryId(1L);
    req.setQuantity(10);
    req.setUnitPrice(99.99);
    return req;
  }

  @Test
  public void gettersAndSetters_workCorrectly() {
    ProductCreateRequest req = validRequest();
    req.setSupplierId(2L);
    req.setReorderLevel(5);
    req.setStatus("In Stock");

    assertEquals("P001", req.getProductCode());
    assertEquals("Keyboard", req.getProductName());
    assertEquals(Long.valueOf(1L), req.getCategoryId());
    assertEquals(Integer.valueOf(10), req.getQuantity());
    assertEquals("In Stock", req.getStatus());
  }

  @Test
  public void roundTrip_jsonSerialization() {
    ProductCreateRequest parsed =
        JsonUtil.fromJson(JsonUtil.toJson(validRequest()), ProductCreateRequest.class);

    assertEquals("P001", parsed.getProductCode());
    assertEquals("Keyboard", parsed.getProductName());
  }

  @Test(expected = ValidationException.class)
  public void validation_failsWhenProductCodeBlank() {
    ProductCreateRequest req = validRequest();
    req.setProductCode("");
    validate(req);
  }

  @Test(expected = ValidationException.class)
  public void validation_failsWhenCategoryMissing() {
    ProductCreateRequest req = validRequest();
    req.setCategoryId(null);
    validate(req);
  }

  @Test(expected = ValidationException.class)
  public void validation_failsWhenQuantityNegative() {
    ProductCreateRequest req = validRequest();
    req.setQuantity(-1);
    validate(req);
  }
}
