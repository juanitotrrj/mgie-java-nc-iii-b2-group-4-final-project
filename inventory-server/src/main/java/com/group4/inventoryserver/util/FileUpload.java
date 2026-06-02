package com.group4.inventoryserver.util;

public class FileUpload {

  private final String filename;
  private final String contentType;
  private final byte[] data;

  public FileUpload(String filename, String contentType, byte[] data) {
    this.filename = filename;
    this.contentType = contentType;
    this.data = data;
  }

  public String getFilename() {
    return filename;
  }

  public String getContentType() {
    return contentType;
  }

  public byte[] getData() {
    return data;
  }

  public long getSize() {
    return data != null ? data.length : 0;
  }
}
