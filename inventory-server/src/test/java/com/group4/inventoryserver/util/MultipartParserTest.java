package com.group4.inventoryserver.util;

import static org.junit.Assert.*;

import com.group4.inventoryserver.exception.ApiException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

public class MultipartParserTest {

  private static final String BOUNDARY = "----Boundary123";

  private String buildMultipartBody(String fieldPart, String filePart) {
    StringBuilder sb = new StringBuilder();
    sb.append("--").append(BOUNDARY).append("\r\n");
    sb.append(fieldPart).append("\r\n");
    if (filePart != null) {
      sb.append("--").append(BOUNDARY).append("\r\n");
      sb.append(filePart).append("\r\n");
    }
    sb.append("--").append(BOUNDARY).append("--\r\n");
    return sb.toString();
  }

  @Test
  public void parse_extractsTextField() throws Exception {
    String field =
        "Content-Disposition: form-data; name=\"description\"\r\n\r\n" + "Updated stock level";
    String body = buildMultipartBody(field, null);
    String contentType = "multipart/form-data; boundary=" + BOUNDARY;

    MultipartData data =
        MultipartParser.parse(
            new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), contentType);

    assertEquals("Updated stock level", data.getField("description"));
    assertFalse(data.hasFile());
  }

  @Test
  public void parse_extractsFileUpload() throws Exception {
    String filePart =
        "Content-Disposition: form-data; name=\"file\"; filename=\"proof.png\"\r\n"
            + "Content-Type: image/png\r\n\r\n"
            + "PNGDATA";
    String body = buildMultipartBody("", filePart);
    String contentType = "multipart/form-data; boundary=" + BOUNDARY;

    MultipartData data =
        MultipartParser.parse(
            new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), contentType);

    assertTrue(data.hasFile());
    assertEquals("proof.png", data.getFile().getFilename());
    assertEquals("image/png", data.getFile().getContentType());
    assertEquals(7, data.getFile().getSize());
  }

  @Test(expected = ApiException.class)
  public void parse_throwsWhenBoundaryMissing() throws Exception {
    MultipartParser.parse(new ByteArrayInputStream(new byte[0]), "multipart/form-data");
  }

  @Test(expected = ApiException.class)
  public void parse_throwsOnInvalidBody() throws Exception {
    String contentType = "multipart/form-data; boundary=" + BOUNDARY;
    MultipartParser.parse(
        new ByteArrayInputStream("not multipart".getBytes(StandardCharsets.UTF_8)), contentType);
  }
}
