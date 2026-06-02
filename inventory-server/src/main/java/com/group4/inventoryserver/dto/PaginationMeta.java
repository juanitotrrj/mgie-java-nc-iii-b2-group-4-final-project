package com.group4.inventoryserver.dto;

public class PaginationMeta {

  private final int page;
  private final int size;
  private final long totalRecords;
  private final int totalPages;
  private final String sortBy;
  private final String sortDir;

  public PaginationMeta(int page, int size, long totalRecords, String sortBy, String sortDir) {
    this.page = page;
    this.size = size;
    this.totalRecords = totalRecords;
    this.totalPages = size > 0 ? (int) Math.ceil((double) totalRecords / size) : 0;
    this.sortBy = sortBy;
    this.sortDir = sortDir;
  }

  public int getPage() {
    return page;
  }

  public int getSize() {
    return size;
  }

  public long getTotalRecords() {
    return totalRecords;
  }

  public int getTotalPages() {
    return totalPages;
  }

  public String getSortBy() {
    return sortBy;
  }

  public String getSortDir() {
    return sortDir;
  }
}
