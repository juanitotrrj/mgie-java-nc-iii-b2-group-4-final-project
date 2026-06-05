package com.group4.inventoryserver.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.FileWriter;
import org.junit.After;
import org.junit.Test;

public class DotEnvLoaderTest {

  @After
  public void tearDown() {
    DotEnvLoader.reset();
    System.clearProperty("g4ims.env.file");
  }

  @Test
  public void resolve_reads_from_env_file() throws Exception {
    File temp = File.createTempFile("g4ims-test", ".env");
    temp.deleteOnExit();
    try (FileWriter w = new FileWriter(temp)) {
      w.write("G4IMS_SERVER_APP_NAME=TestApp\n");
    }
    System.setProperty("g4ims.env.file", temp.getAbsolutePath());
    DotEnvLoader.reset();
    DotEnvLoader.load();
    assertThat(DotEnvLoader.resolve("G4IMS_SERVER_APP_NAME", "default"), is("TestApp"));
  }
}
