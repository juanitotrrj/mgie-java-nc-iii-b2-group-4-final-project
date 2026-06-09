package com.group4.inventoryserver.dto.category;

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

public class CategoryCreateRequestTest {

  private static final Set<String> VALID_STATUSES =
      new HashSet<>(Arrays.asList("Active", "Inactive"));

  private void validate(CategoryCreateRequest request) {
    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireNonBlank(request.getCategoryName(), "categoryName", errors);
    ValidationUtil.requireMaxLength(request.getCategoryName(), "categoryName", 120, errors);
    ValidationUtil.requireMaxLength(request.getDescription(), "description", 500, errors);
    ValidationUtil.requireMaxLength(request.getType(), "type", 80, errors);
    if (request.getStatus() != null && !VALID_STATUSES.contains(request.getStatus())) {
      errors.add(new FieldError("status", "Status must be Active or Inactive."));
    }
    ValidationUtil.throwIfErrors(errors);
  }

  @Test
  public void gettersAndSetters_workCorrectly() {
    CategoryCreateRequest req = new CategoryCreateRequest();
    req.setCategoryName("Electronics");
    req.setDescription("Gadgets");
    req.setType("Product");
    req.setStatus("Active");

    assertEquals("Electronics", req.getCategoryName());
    assertEquals("Active", req.getStatus());
  }

  @Test
  public void roundTrip_jsonSerialization() {
    CategoryCreateRequest original = new CategoryCreateRequest();
    original.setCategoryName("Office");

    CategoryCreateRequest parsed =
        JsonUtil.fromJson(JsonUtil.toJson(original), CategoryCreateRequest.class);

    assertEquals("Office", parsed.getCategoryName());
  }

  @Test(expected = ValidationException.class)
  public void validation_failsWhenNameBlank() {
    CategoryCreateRequest req = new CategoryCreateRequest();
    req.setCategoryName("");
    validate(req);
  }

  @Test(expected = ValidationException.class)
  public void validation_failsWhenStatusInvalid() {
    CategoryCreateRequest req = new CategoryCreateRequest();
    req.setCategoryName("Valid");
    req.setStatus("Deleted");
    validate(req);
  }
}
