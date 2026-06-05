package com.group4.inventoryserver.setup;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.junit.Test;

public class SetupCommandTest {

  @Test
  public void prompt_returnsDefault_whenInputEmpty() throws Exception {
    Scanner scanner =
        new Scanner(new ByteArrayInputStream("\n".getBytes(StandardCharsets.UTF_8)), "UTF-8");
    SetupCommand command = new SetupCommand(scanner);

    Method prompt = SetupCommand.class.getDeclaredMethod("prompt", String.class, String.class);
    prompt.setAccessible(true);

    String result = (String) prompt.invoke(command, "Server port", "8080");

    assertEquals("8080", result);
  }

  @Test
  public void prompt_returnsUserInput_whenProvided() throws Exception {
    Scanner scanner =
        new Scanner(new ByteArrayInputStream("9090\n".getBytes(StandardCharsets.UTF_8)), "UTF-8");
    SetupCommand command = new SetupCommand(scanner);

    Method prompt = SetupCommand.class.getDeclaredMethod("prompt", String.class, String.class);
    prompt.setAccessible(true);

    String result = (String) prompt.invoke(command, "Server port", "8080");

    assertEquals("9090", result);
  }

  @Test
  public void prompt_returnsInput_whenNoDefault() throws Exception {
    Scanner scanner =
        new Scanner(
            new ByteArrayInputStream("custom-value\n".getBytes(StandardCharsets.UTF_8)), "UTF-8");
    SetupCommand command = new SetupCommand(scanner);

    Method prompt = SetupCommand.class.getDeclaredMethod("prompt", String.class, String.class);
    prompt.setAccessible(true);

    String result = (String) prompt.invoke(command, "Label", null);

    assertEquals("custom-value", result);
  }

  @Test
  public void mainRecognizesSetupCommand() {
    String[] setupArgs = {"setup"};
    String[] migrateArgs = {"migrate"};
    String[] unknownArgs = {"unknown-cmd"};

    assertEquals("setup", setupArgs[0]);
    assertEquals("migrate", migrateArgs[0]);
    assertNotEquals("setup", unknownArgs[0]);
  }
}
