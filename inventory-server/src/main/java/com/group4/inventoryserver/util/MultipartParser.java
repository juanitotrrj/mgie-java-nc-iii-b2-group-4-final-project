package com.group4.inventoryserver.util;

import com.group4.inventoryserver.exception.ApiException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class MultipartParser {

  private static final int MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

  private MultipartParser() {}

  public static MultipartData parse(InputStream inputStream, String contentType)
      throws IOException {
    String boundary = extractBoundary(contentType);
    if (boundary == null) {
      throw new ApiException(400, "Missing multipart boundary.");
    }

    byte[] bodyBytes = readAll(inputStream);
    byte[] boundaryBytes = ("--" + boundary).getBytes(StandardCharsets.UTF_8);

    Map<String, String> fields = new HashMap<>();
    FileUpload fileUpload = null;

    int pos = indexOf(bodyBytes, boundaryBytes, 0);
    if (pos == -1) {
      throw new ApiException(400, "Invalid multipart body.");
    }

    while (true) {
      int start = pos + boundaryBytes.length;
      if (start + 2 > bodyBytes.length) break;

      // check for closing boundary --
      if (bodyBytes[start] == '-' && bodyBytes[start + 1] == '-') break;

      // skip CRLF after boundary
      if (bodyBytes[start] == '\r' && bodyBytes[start + 1] == '\n') {
        start += 2;
      }

      int nextBoundary = indexOf(bodyBytes, boundaryBytes, start);
      if (nextBoundary == -1) break;

      // Part content is between start and nextBoundary (minus trailing CRLF)
      int partEnd = nextBoundary;
      if (partEnd >= 2 && bodyBytes[partEnd - 2] == '\r' && bodyBytes[partEnd - 1] == '\n') {
        partEnd -= 2;
      }

      byte[] partBytes = Arrays.copyOfRange(bodyBytes, start, partEnd);
      parsePart(partBytes, fields, fileUpload == null ? null : "skip");

      if (fileUpload == null) {
        fileUpload = extractFilePart(partBytes);
      }

      pos = nextBoundary;
    }

    return new MultipartData(fields, fileUpload);
  }

  private static void parsePart(byte[] partBytes, Map<String, String> fields, String skipFile) {
    int headerEnd = findHeaderEnd(partBytes);
    if (headerEnd == -1) return;

    String headers = new String(partBytes, 0, headerEnd, StandardCharsets.UTF_8);
    if (headers.contains("filename=\"")) {
      return; // file part handled separately
    }

    String name = extractFieldName(headers);
    if (name == null) return;

    int bodyStart = headerEnd + 4; // skip \r\n\r\n
    if (bodyStart >= partBytes.length) {
      fields.put(name, "");
      return;
    }
    String value =
        new String(partBytes, bodyStart, partBytes.length - bodyStart, StandardCharsets.UTF_8);
    fields.put(name, value.trim());
  }

  private static FileUpload extractFilePart(byte[] partBytes) {
    int headerEnd = findHeaderEnd(partBytes);
    if (headerEnd == -1) return null;

    String headers = new String(partBytes, 0, headerEnd, StandardCharsets.UTF_8);
    if (!headers.contains("filename=\"")) return null;

    String filename = extractFilename(headers);
    if (filename == null || filename.isEmpty()) return null;

    String partContentType = extractPartContentType(headers);
    if (partContentType == null) partContentType = "application/octet-stream";

    int bodyStart = headerEnd + 4;
    if (bodyStart >= partBytes.length) return null;

    byte[] fileData = Arrays.copyOfRange(partBytes, bodyStart, partBytes.length);
    if (fileData.length > MAX_FILE_SIZE) {
      throw new ApiException(400, "File exceeds maximum size of 5MB.");
    }

    return new FileUpload(filename, partContentType, fileData);
  }

  private static String extractBoundary(String contentType) {
    if (contentType == null) return null;
    for (String part : contentType.split(";")) {
      String trimmed = part.trim();
      if (trimmed.startsWith("boundary=")) {
        String boundary = trimmed.substring("boundary=".length());
        if (boundary.startsWith("\"") && boundary.endsWith("\"")) {
          boundary = boundary.substring(1, boundary.length() - 1);
        }
        return boundary;
      }
    }
    return null;
  }

  private static String extractFieldName(String headers) {
    int nameStart = headers.indexOf("name=\"");
    if (nameStart == -1) return null;
    nameStart += 6;
    int nameEnd = headers.indexOf("\"", nameStart);
    if (nameEnd == -1) return null;
    return headers.substring(nameStart, nameEnd);
  }

  private static String extractFilename(String headers) {
    int nameStart = headers.indexOf("filename=\"");
    if (nameStart == -1) return null;
    nameStart += 10;
    int nameEnd = headers.indexOf("\"", nameStart);
    if (nameEnd == -1) return null;
    return headers.substring(nameStart, nameEnd);
  }

  private static String extractPartContentType(String headers) {
    for (String line : headers.split("\r\n")) {
      if (line.toLowerCase().startsWith("content-type:")) {
        return line.substring("content-type:".length()).trim();
      }
    }
    return null;
  }

  private static int findHeaderEnd(byte[] data) {
    byte[] separator = "\r\n\r\n".getBytes(StandardCharsets.UTF_8);
    return indexOf(data, separator, 0);
  }

  private static int indexOf(byte[] source, byte[] target, int fromIndex) {
    outer:
    for (int i = fromIndex; i <= source.length - target.length; i++) {
      for (int j = 0; j < target.length; j++) {
        if (source[i + j] != target[j]) continue outer;
      }
      return i;
    }
    return -1;
  }

  private static byte[] readAll(InputStream is) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    byte[] chunk = new byte[8192];
    int bytesRead;
    while ((bytesRead = is.read(chunk)) != -1) {
      buffer.write(chunk, 0, bytesRead);
    }
    return buffer.toByteArray();
  }
}
