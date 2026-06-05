package com.group4.inventoryserver.dto.sale;

import static org.junit.Assert.*;

import com.group4.inventoryserver.exception.ValidationException;
import com.group4.inventoryserver.util.JsonUtil;
import com.group4.inventoryserver.util.ValidationUtil;
import com.group4.inventoryserver.util.ValidationUtil.FieldError;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class SaleCreateRequestTest {

  private static final Set<String> VALID_PAYMENT_METHODS =
      new HashSet<>(Arrays.asList("Cash", "GCash", "Credit Card", "Bank Transfer", "E-Wallet"));

  private void validate(SaleCreateRequest request) {
    List<FieldError> errors = ValidationUtil.newErrorList();
    if (request.getPaymentMethod() == null || request.getPaymentMethod().trim().isEmpty()) {
      errors.add(new FieldError("paymentMethod", "Payment method is required."));
    } else if (!VALID_PAYMENT_METHODS.contains(request.getPaymentMethod())) {
      errors.add(new FieldError("paymentMethod", "Invalid payment method."));
    }
    if (request.getItems() == null || request.getItems().isEmpty()) {
      errors.add(new FieldError("items", "At least one item is required."));
    }
    ValidationUtil.throwIfErrors(errors);
  }

  private SaleCreateRequest validRequest() {
    SaleItemRequest item = new SaleItemRequest();
    item.setProductId(1L);
    item.setQuantity(2);

    SaleCreateRequest req = new SaleCreateRequest();
    req.setPaymentMethod("Cash");
    req.setAmountReceived(500.0);
    req.setItems(Collections.singletonList(item));
    return req;
  }

  @Test
  public void gettersAndSetters_workCorrectly() {
    SaleCreateRequest req = validRequest();
    req.setCustomerName("Walk-in");
    req.setStatus("Paid");

    assertEquals("Walk-in", req.getCustomerName());
    assertEquals("Cash", req.getPaymentMethod());
    assertEquals(1, req.getItems().size());
  }

  @Test
  public void roundTrip_jsonSerialization() {
    SaleCreateRequest parsed =
        JsonUtil.fromJson(JsonUtil.toJson(validRequest()), SaleCreateRequest.class);

    assertEquals("Cash", parsed.getPaymentMethod());
    assertEquals(Integer.valueOf(2), parsed.getItems().get(0).getQuantity());
  }

  @Test(expected = ValidationException.class)
  public void validation_failsWhenPaymentMethodMissing() {
    SaleCreateRequest req = validRequest();
    req.setPaymentMethod(null);
    validate(req);
  }

  @Test(expected = ValidationException.class)
  public void validation_failsWhenItemsEmpty() {
    SaleCreateRequest req = validRequest();
    req.setItems(Collections.<SaleItemRequest>emptyList());
    validate(req);
  }
}
