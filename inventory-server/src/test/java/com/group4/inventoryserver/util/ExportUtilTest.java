package com.group4.inventoryserver.util;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class ExportUtilTest {

  @Test
  public void toCsv_generatesHeaderAndRows() {
    String[] headers = {"Code", "Name", "Qty"};
    byte[] csv =
        ExportUtil.toCsv(
            headers, Collections.singletonList(new String[] {"P001", "Keyboard", "10"}));

    String text = new String(csv, StandardCharsets.UTF_8);
    assertEquals("Code,Name,Qty\nP001,Keyboard,10\n", text);
  }

  @Test
  public void toCsv_escapesCommasAndQuotes() {
    String[] headers = {"Name"};
    byte[] csv =
        ExportUtil.toCsv(headers, Collections.singletonList(new String[] {"Product, \"Special\""}));

    String text = new String(csv, StandardCharsets.UTF_8);
    assertTrue(text.contains("\"Product, \"\"Special\"\"\""));
  }

  @Test
  public void toCsv_handlesNullFields() {
    String[] headers = {"A", "B"};
    byte[] csv = ExportUtil.toCsv(headers, Collections.singletonList(new String[] {null, "value"}));

    String text = new String(csv, StandardCharsets.UTF_8);
    assertEquals("A,B\n,value\n", text);
  }

  @Test
  public void toXlsx_generatesNonEmptyWorkbook() throws Exception {
    String[] headers = {"ID", "Amount"};
    byte[] xlsx =
        ExportUtil.toXlsx(
            "Sales",
            headers,
            Arrays.asList(new String[] {"1", "100.50"}, new String[] {"2", "text"}));

    assertNotNull(xlsx);
    assertTrue(xlsx.length > 0);
    assertEquals((byte) 0x50, xlsx[0]);
    assertEquals((byte) 0x4B, xlsx[1]);
  }
}
