package com.group4.inventoryserver.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.FileWriter;
import org.junit.After;
import org.junit.Test;

public class EnvConfigTest {

  @After
  public void tearDown() {
    DotEnvLoader.reset();
    System.clearProperty("g4ims.env.file");
  }

  @Test
  public void defaults_returnExpectedValues() {
    DotEnvLoader.reset();

    assertThat(EnvConfig.appName(), containsString("Inventory"));
    assertThat(EnvConfig.serverPort(), is(8080));
    assertThat(EnvConfig.dbPort(), is(3306));
    assertThat(EnvConfig.appContextPath(), is("/api"));
    assertThat(EnvConfig.corsEnabled(), is(true));
  }

  @Test
  public void readsCustomValues_fromEnvFile() throws Exception {
    File temp = File.createTempFile("g4ims-env", ".env");
    temp.deleteOnExit();
    try (FileWriter w = new FileWriter(temp)) {
      w.write("G4IMS_SERVER_SERVER_PORT=9090\n");
      w.write("G4IMS_SERVER_APP_ENV=test\n");
      w.write("G4IMS_SERVER_DB_NAME=custom_db\n");
    }
    System.setProperty("g4ims.env.file", temp.getAbsolutePath());
    DotEnvLoader.reset();
    DotEnvLoader.load();

    assertThat(EnvConfig.serverPort(), is(9090));
    assertThat(EnvConfig.appEnv(), is("test"));
    assertThat(EnvConfig.dbName(), is("custom_db"));
  }

  @Test
  public void dbUrl_usesExplicitValue_whenSet() throws Exception {
    File temp = File.createTempFile("g4ims-dburl", ".env");
    temp.deleteOnExit();
    try (FileWriter w = new FileWriter(temp)) {
      w.write("G4IMS_SERVER_DB_URL=jdbc:mysql://db.example.com:3306/prod\n");
    }
    System.setProperty("g4ims.env.file", temp.getAbsolutePath());
    DotEnvLoader.reset();
    DotEnvLoader.load();

    assertThat(EnvConfig.dbUrl(), is("jdbc:mysql://db.example.com:3306/prod"));
  }

  @Test
  public void corsAllowedOrigins_parsesCommaSeparatedList() throws Exception {
    File temp = File.createTempFile("g4ims-cors", ".env");
    temp.deleteOnExit();
    try (FileWriter w = new FileWriter(temp)) {
      w.write("G4IMS_SERVER_CORS_ALLOWED_ORIGINS=http://a.com,http://b.com\n");
    }
    System.setProperty("g4ims.env.file", temp.getAbsolutePath());
    DotEnvLoader.reset();
    DotEnvLoader.load();

    assertThat(EnvConfig.corsAllowedOrigins(), hasSize(2));
    assertThat(EnvConfig.corsAllowedOrigins().get(0), is("http://a.com"));
  }
}
