package com.group4.inventoryclient.api;

import java.io.IOException;

class TestableApiClient extends ApiClient {

  ApiResponse nextResponse = new ApiResponse(200, "{\"data\":{}}");
  String lastMethod;
  String lastPath;
  Object lastBody;

  TestableApiClient() {
    super("http://localhost:18080/api");
  }

  @Override
  protected ApiResponse executeRequest(String method, String path, Object body) throws IOException {
    lastMethod = method;
    lastPath = path;
    lastBody = body;
    return nextResponse;
  }
}
