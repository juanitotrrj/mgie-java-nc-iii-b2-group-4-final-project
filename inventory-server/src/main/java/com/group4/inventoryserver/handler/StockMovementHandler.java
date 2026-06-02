package com.group4.inventoryserver.handler;

import com.group4.inventoryserver.config.EnvConfig;
import com.group4.inventoryserver.dto.PaginatedResponse;
import com.group4.inventoryserver.dto.PaginationMeta;
import com.group4.inventoryserver.dto.PaginationParams;
import com.group4.inventoryserver.dto.product.StockMovementData;
import com.group4.inventoryserver.exception.ApiException;
import com.group4.inventoryserver.repository.StockMovementRepository;
import com.group4.inventoryserver.server.JsonResponse;
import com.group4.inventoryserver.server.RequestContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StockMovementHandler extends BaseHandler {

  private static final Set<String> ALLOWED_SORT_FIELDS =
      new HashSet<>(
          Arrays.asList(
              "createdAt", "movementType", "productName", "quantityChange", "referenceType"));

  private static final String BASE_PATH = EnvConfig.appContextPath() + "/stock-movements";
  private static final String PERMISSION = "STOCK_MOVEMENT_READ";

  private final StockMovementRepository stockMovementRepository = new StockMovementRepository();

  @Override
  public void handle(RequestContext ctx) throws IOException {
    requirePermission(ctx, PERMISSION);

    String pathParam = ctx.getPathParam(BASE_PATH);
    if (pathParam != null && !pathParam.isEmpty()) {
      throw new ApiException(404, "Not found.");
    }

    super.handle(ctx);
  }

  @Override
  protected void handleGet(RequestContext ctx) throws IOException {
    PaginationParams params = PaginationParams.from(ctx, "createdAt", ALLOWED_SORT_FIELDS);
    Long productId = parseLongOrNull(ctx.getQueryParam("productId"));
    String movementType = ctx.getQueryParam("movementType");
    String referenceType = ctx.getQueryParam("referenceType");
    String dateFrom = ctx.getQueryParam("dateFrom");
    String dateTo = ctx.getQueryParam("dateTo");

    List<StockMovementData> data =
        stockMovementRepository.findAll(
            params.getOffset(),
            params.getSize(),
            params.getSortBy(),
            params.getSortDir(),
            params.getSearch(),
            productId,
            movementType,
            referenceType,
            dateFrom,
            dateTo);
    long total =
        stockMovementRepository.count(
            params.getSearch(), productId, movementType, referenceType, dateFrom, dateTo);
    PaginationMeta meta =
        new PaginationMeta(
            params.getPage(), params.getSize(), total, params.getSortBy(), params.getSortDir());

    PaginatedResponse<StockMovementData> response = new PaginatedResponse<>(data, meta);
    JsonResponse.send(ctx.getExchange(), 200, response);
  }

  private Long parseLongOrNull(String value) {
    if (value == null || value.trim().isEmpty()) return null;
    try {
      return Long.parseLong(value.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
