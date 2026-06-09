package com.group4.inventoryserver.perf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

import com.group4.inventoryserver.util.ValidationUtil;
import com.group4.inventoryserver.util.ValidationUtil.FieldError;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

public class ValidationUtilPerfTest {

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.SECONDS)
  @Warmup(iterations = 2, time = 1)
  @Measurement(iterations = 3, time = 1)
  @Fork(1)
  public int validateProductFields() {
    List<FieldError> errors = ValidationUtil.newErrorList();
    ValidationUtil.requireNonBlank("P001", "productCode", errors);
    ValidationUtil.requireMaxLength("P001", "productCode", 20, errors);
    ValidationUtil.requireNonBlank("Keyboard", "productName", errors);
    ValidationUtil.requireMaxLength("Keyboard", "productName", 100, errors);
    ValidationUtil.requireValidEmail("buyer@example.com", "email", errors);
    return errors.size();
  }

  @Test
  public void field_validation_10k_within_sla() {
    long start = System.nanoTime();
    for (int i = 0; i < 10_000; i++) {
      List<FieldError> errors = ValidationUtil.newErrorList();
      ValidationUtil.requireNonBlank("P001", "productCode", errors);
      ValidationUtil.requireMaxLength("P001", "productCode", 20, errors);
      ValidationUtil.requireNonBlank("Keyboard", "productName", errors);
      ValidationUtil.requireMaxLength("Keyboard", "productName", 100, errors);
      ValidationUtil.requireValidEmail("buyer@example.com", "email", errors);
    }
    long elapsedMs = (System.nanoTime() - start) / 1_000_000;
    assertThat(elapsedMs, lessThan(1500L));
  }
}
