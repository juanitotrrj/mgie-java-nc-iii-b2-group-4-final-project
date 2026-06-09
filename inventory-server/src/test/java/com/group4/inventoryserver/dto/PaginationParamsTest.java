package com.group4.inventoryserver.dto;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.group4.inventoryserver.server.RequestContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class PaginationParamsTest {

  private RequestContext mockCtx(
      String page, String size, String sortBy, String sortDir, String search) {
    RequestContext ctx = mock(RequestContext.class);
    when(ctx.getQueryParam("page")).thenReturn(page);
    when(ctx.getQueryParam("size")).thenReturn(size);
    when(ctx.getQueryParam("sortBy", "productName"))
        .thenReturn(sortBy != null ? sortBy : "productName");
    when(ctx.getQueryParam("sortDir", "asc")).thenReturn(sortDir != null ? sortDir : "asc");
    when(ctx.getQueryParam("search")).thenReturn(search);
    return ctx;
  }

  @Test
  public void defaultValues_whenNoParams() {
    RequestContext ctx = mockCtx(null, null, null, null, null);
    Set<String> allowed = new HashSet<>(Arrays.asList("productName", "createdAt"));
    PaginationParams params = PaginationParams.from(ctx, "productName", allowed);
    assertEquals(1, params.getPage());
    assertEquals(10, params.getSize());
    assertEquals(0, params.getOffset());
    assertEquals("productName", params.getSortBy());
    assertEquals("asc", params.getSortDir());
    assertNull(params.getSearch());
    assertFalse(params.hasSearch());
  }

  @Test
  public void parsesValidParams() {
    RequestContext ctx = mockCtx("3", "25", "createdAt", "desc", "keyboard");
    Set<String> allowed = new HashSet<>(Arrays.asList("productName", "createdAt"));
    PaginationParams params = PaginationParams.from(ctx, "productName", allowed);
    assertEquals(3, params.getPage());
    assertEquals(25, params.getSize());
    assertEquals(50, params.getOffset());
    assertEquals("createdAt", params.getSortBy());
    assertEquals("desc", params.getSortDir());
    assertEquals("keyboard", params.getSearch());
    assertTrue(params.hasSearch());
  }

  @Test
  public void invalidPage_defaultsToOne() {
    RequestContext ctx = mockCtx("-1", "10", null, null, null);
    PaginationParams params = PaginationParams.from(ctx, "productName", null);
    assertEquals(1, params.getPage());
  }

  @Test
  public void invalidSize_defaultsToTen() {
    RequestContext ctx = mockCtx("1", "0", null, null, null);
    PaginationParams params = PaginationParams.from(ctx, "productName", null);
    assertEquals(10, params.getSize());
  }

  @Test
  public void sizeExceedingMax_cappedAt100() {
    RequestContext ctx = mockCtx("1", "999", null, null, null);
    PaginationParams params = PaginationParams.from(ctx, "productName", null);
    assertEquals(100, params.getSize());
  }

  @Test
  public void invalidSortField_fallsBackToDefault() {
    RequestContext ctx = mockCtx("1", "10", "hackerField", null, null);
    when(ctx.getQueryParam("sortBy", "productName")).thenReturn("hackerField");
    Set<String> allowed = new HashSet<>(Arrays.asList("productName", "createdAt"));
    PaginationParams params = PaginationParams.from(ctx, "productName", allowed);
    assertEquals("productName", params.getSortBy());
  }

  @Test
  public void invalidSortDir_defaultsToAsc() {
    RequestContext ctx = mockCtx("1", "10", null, "invalid", null);
    when(ctx.getQueryParam("sortDir", "asc")).thenReturn("invalid");
    PaginationParams params = PaginationParams.from(ctx, "productName", null);
    assertEquals("asc", params.getSortDir());
  }

  @Test
  public void nonNumericPage_defaultsToOne() {
    RequestContext ctx = mockCtx("abc", "10", null, null, null);
    PaginationParams params = PaginationParams.from(ctx, "productName", null);
    assertEquals(1, params.getPage());
  }
}
