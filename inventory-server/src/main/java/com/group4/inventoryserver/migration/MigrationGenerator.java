package com.group4.inventoryserver.migration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MigrationGenerator {

  private static final Logger log = LoggerFactory.getLogger(MigrationGenerator.class);
  private static final String MIGRATIONS_DIR = "src/main/resources/db/migrations/";

  public void generate(String description) {
    int nextVersion = findNextVersion();
    String sanitized = description.toLowerCase().replaceAll("[^a-z0-9]+", "_");
    String filename = String.format("V%03d__%s.sql", nextVersion, sanitized);
    String downFilename = String.format("V%03d__%s.down.sql", nextVersion, sanitized);

    File dir = new File(MIGRATIONS_DIR);
    if (!dir.exists() && !dir.mkdirs()) {
      log.error("Failed to create migrations directory: {}", dir.getAbsolutePath());
      return;
    }

    writeTemplate(new File(dir, filename), "-- Migration: " + description);
    writeTemplate(new File(dir, downFilename), "-- Rollback: " + description);
    updateIndex(dir, filename);

    System.out.printf(
        "Created migration files:%n  %s%s%n  %s%s%n",
        MIGRATIONS_DIR, filename, MIGRATIONS_DIR, downFilename);
  }

  private int findNextVersion() {
    File dir = new File(MIGRATIONS_DIR);
    if (!dir.exists()) return 1;

    int maxVersion = 0;
    File[] files = dir.listFiles();
    if (files == null) return 1;

    for (File f : files) {
      String name = f.getName();
      if (name.startsWith("V") && name.endsWith(".sql") && !name.contains(".down.")) {
        try {
          int version = Integer.parseInt(name.substring(1, name.indexOf("__")));
          maxVersion = Math.max(maxVersion, version);
        } catch (NumberFormatException | StringIndexOutOfBoundsException ignored) {
        }
      }
    }
    return maxVersion + 1;
  }

  private void writeTemplate(File file, String header) {
    try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
      pw.println(header);
      pw.println();
      pw.println("-- Write your SQL here");
      pw.println();
    } catch (IOException e) {
      log.error("Failed to create migration file: {}", file.getAbsolutePath(), e);
    }
  }

  private void updateIndex(File dir, String filename) {
    File indexFile = new File(dir, "migrations.index");
    try (PrintWriter pw = new PrintWriter(new FileWriter(indexFile, true))) {
      pw.println(filename);
    } catch (IOException e) {
      log.error("Failed to update migrations.index: {}", e.getMessage());
    }
  }
}
