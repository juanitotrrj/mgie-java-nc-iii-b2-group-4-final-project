package com.group4.inventoryserver.perf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.mockito.Mockito.when;

import com.group4.inventoryserver.dto.PaginationParams;
import com.group4.inventoryserver.server.RequestContext;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.Test;
import org.mockito.Mockito;

public class PaginationParamsPerfTest {

  @Test
  public void pagination_parse_completes_within_sla() {
    long start = System.nanoTime();
    for (int i = 0; i < 100_000; i++) {
      RequestContext ctx = Mockito.mock(RequestContext.class);
      when(ctx.getQueryParam("page")).thenReturn("1");
      when(ctx.getQueryParam("size")).thenReturn("25");
      when(ctx.getQueryParam("sortBy", "productName")).thenReturn("productName");
      when(ctx.getQueryParam("sortDir", "asc")).thenReturn("asc");
      when(ctx.getQueryParam("search")).thenReturn(null);
      PaginationParams.from(
          ctx, "productName", new HashSet<>(Arrays.asList("productName", "createdAt")));
    }
    long elapsedMs = (System.nanoTime() - start) / 1_000_000;
    assertThat(elapsedMs, lessThan(10000L));
  }
}
