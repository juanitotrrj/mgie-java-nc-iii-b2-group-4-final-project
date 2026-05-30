package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.dto.guest.InquiryData;
import com.group4.inventoryserver.dto.guest.InquiryRequest;
import com.group4.inventoryserver.repository.InquiryRepository;
import com.group4.inventoryserver.server.RequestContext;
import com.group4.inventoryserver.util.ValidationUtil;
import com.group4.inventoryserver.util.ValidationUtil.FieldError;
import java.io.IOException;
import java.util.List;

public class InquiryHandler extends BaseHandler {

  private final InquiryRepository repository = new InquiryRepository();

  @Override
  protected void handlePost(RequestContext ctx) throws IOException {
    InquiryRequest req = parseBody(ctx, InquiryRequest.class);
    validate(req);
    InquiryData result = repository.save(req);
    sendCreated(ctx, result);
  }

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
}
