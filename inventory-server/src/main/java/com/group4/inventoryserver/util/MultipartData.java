package com.group4.inventoryserver.util;

import java.util.Map;

public class MultipartData {

  private final Map<String, String> fields;
  private final FileUpload file;

  public MultipartData(Map<String, String> fields, FileUpload file) {
    this.fields = fields;
    this.file = file;
  }

  public Map<String, String> getFields() {
    return fields;
  }

  public FileUpload getFile() {
    return file;
  }

  public boolean hasFile() {
    return file != null;
  }

  public String getField(String name) {
    return fields != null ? fields.get(name) : null;
  }
}
