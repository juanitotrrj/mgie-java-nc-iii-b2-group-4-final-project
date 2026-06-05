package com.group4.inventoryserver.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class FileUploadTest {

  @Test
  public void getters_returnConstructorValues() {
    byte[] data = new byte[] {1, 2, 3, 4};
    FileUpload upload = new FileUpload("report.csv", "text/csv", data);

    assertEquals("report.csv", upload.getFilename());
    assertEquals("text/csv", upload.getContentType());
    assertArrayEquals(data, upload.getData());
    assertEquals(4L, upload.getSize());
  }

  @Test
  public void getSize_returnsZero_whenDataNull() {
    FileUpload upload = new FileUpload("empty.txt", "text/plain", null);

    assertEquals(0L, upload.getSize());
    assertNull(upload.getData());
  }
}
