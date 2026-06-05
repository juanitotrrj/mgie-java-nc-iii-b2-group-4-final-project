package com.group4.inventoryserver.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

public class AuditLogsApiIT extends ApiITBase {

  @Test
  public void list_audit_logs_returns_200() throws Exception {
    assertThat(adminClient.get("/audit-logs?page=1&pageSize=10").status, is(200));
  }
}
