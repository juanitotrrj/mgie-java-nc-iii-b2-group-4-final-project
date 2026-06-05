package com.group4.inventoryserver.dto;

import static org.junit.Assert.*;

import org.junit.Test;

public class PaginationMetaTest {

  @Test
  public void calculatesTotalPages() {
    PaginationMeta meta = new PaginationMeta(1, 10, 25, "createdAt", "desc");

    assertEquals(1, meta.getPage());
    assertEquals(10, meta.getSize());
    assertEquals(25L, meta.getTotalRecords());
    assertEquals(3, meta.getTotalPages());
    assertEquals("createdAt", meta.getSortBy());
    assertEquals("desc", meta.getSortDir());
  }

  @Test
  public void totalPages_zeroWhenSizeZero() {
    PaginationMeta meta = new PaginationMeta(1, 0, 10, "id", "asc");

    assertEquals(0, meta.getTotalPages());
  }

  @Test
  public void totalPages_roundsUp() {
    PaginationMeta meta = new PaginationMeta(2, 7, 15, "name", "asc");

    assertEquals(3, meta.getTotalPages());
  }
}
