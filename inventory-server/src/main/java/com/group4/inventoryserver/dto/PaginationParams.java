package com.group4.inventoryserver.dto;

import com.group4.inventoryserver.server.RequestContext;
import java.util.Set;

public class PaginationParams {

  private static final int DEFAULT_PAGE = 1;
  private static final int DEFAULT_SIZE = 10;
  private static final int MAX_SIZE = 100;
  private static final String DEFAULT_SORT_DIR = "asc";

  private final int page;
  private final int size;
  private final String sortBy;
  private final String sortDir;
  private final String search;

  private PaginationParams(int page, int size, String sortBy, String sortDir, String search) {
    this.page = page;
    this.size = size;
    this.sortBy = sortBy;
    this.sortDir = sortDir;
    this.search = search;
  }

  public static PaginationParams from(
      RequestContext ctx, String defaultSortBy, Set<String> allowedSortFields) {
    int page = parseIntParam(ctx.getQueryParam("page"), DEFAULT_PAGE);
    int size = parseIntParam(ctx.getQueryParam("size"), DEFAULT_SIZE);
    if (page < 1) page = DEFAULT_PAGE;
    if (size < 1) size = DEFAULT_SIZE;
    if (size > MAX_SIZE) size = MAX_SIZE;

    String sortBy = ctx.getQueryParam("sortBy", defaultSortBy);
    if (allowedSortFields != null && !allowedSortFields.contains(sortBy)) {
      sortBy = defaultSortBy;
    }

    String sortDir = ctx.getQueryParam("sortDir", DEFAULT_SORT_DIR);
    if (!"asc".equalsIgnoreCase(sortDir) && !"desc".equalsIgnoreCase(sortDir)) {
      sortDir = DEFAULT_SORT_DIR;
    }

    String search = ctx.getQueryParam("search");

    return new PaginationParams(page, size, sortBy, sortDir.toLowerCase(), search);
  }

  public int getPage() {
    return page;
  }

  public int getSize() {
    return size;
  }

  public int getOffset() {
    return (page - 1) * size;
  }

  public String getSortBy() {
    return sortBy;
  }

  public String getSortDir() {
    return sortDir;
  }

  public String getSearch() {
    return search;
  }

  public boolean hasSearch() {
    return search != null && !search.trim().isEmpty();
  }

  private static int parseIntParam(String value, int defaultValue) {
    if (value == null || value.trim().isEmpty()) return defaultValue;
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }
}
