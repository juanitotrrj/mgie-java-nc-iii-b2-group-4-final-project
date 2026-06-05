package com.group4.inventoryserver.dto.guest;

import static org.junit.Assert.*;

import com.group4.inventoryserver.exception.ValidationException;
import com.group4.inventoryserver.util.JsonUtil;
import com.group4.inventoryserver.util.ValidationUtil;
import com.group4.inventoryserver.util.ValidationUtil.FieldError;
import java.util.List;
import org.junit.Test;

public class InquiryRequestTest {

  private void validate(InquiryRequest req) {
    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireNonBlank(req.getName(), "name", errors);
    ValidationUtil.requireMaxLength(req.getName(), "name", 150, errors);
    ValidationUtil.requireNonBlank(req.getEmail(), "email", errors);
    ValidationUtil.requireMaxLength(req.getEmail(), "email", 150, errors);
    ValidationUtil.requireValidEmail(req.getEmail(), "email", errors);
    ValidationUtil.requireNonBlank(req.getSubject(), "subject", errors);
    ValidationUtil.requireMaxLength(req.getSubject(), "subject", 200, errors);
    ValidationUtil.requireNonBlank(req.getMessage(), "message", errors);
    ValidationUtil.throwIfErrors(errors);
  }

  private InquiryRequest validRequest() {
    InquiryRequest req = new InquiryRequest();
    req.setName("Jane Doe");
    req.setEmail("jane@example.com");
    req.setSubject("Product inquiry");
    req.setMessage("Do you have this item in stock?");
    return req;
  }

  @Test
  public void gettersAndSetters_workCorrectly() {
    InquiryRequest req = validRequest();

    assertEquals("Jane Doe", req.getName());
    assertEquals("jane@example.com", req.getEmail());
    assertEquals("Product inquiry", req.getSubject());
    assertTrue(req.getMessage().contains("stock"));
  }

  @Test
  public void roundTrip_jsonSerialization() {
    InquiryRequest parsed =
        JsonUtil.fromJson(JsonUtil.toJson(validRequest()), InquiryRequest.class);

    assertEquals("jane@example.com", parsed.getEmail());
  }

  @Test(expected = ValidationException.class)
  public void validation_failsWhenEmailInvalid() {
    InquiryRequest req = validRequest();
    req.setEmail("not-an-email");
    validate(req);
  }

  @Test(expected = ValidationException.class)
  public void validation_failsWhenMessageBlank() {
    InquiryRequest req = validRequest();
    req.setMessage("");
    validate(req);
  }
}
