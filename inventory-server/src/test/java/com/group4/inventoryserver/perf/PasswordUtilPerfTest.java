package com.group4.inventoryserver.perf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

import com.group4.inventoryserver.security.PasswordUtil;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Benchmark)
public class PasswordUtilPerfTest {

  private static final String PLAINTEXT = "Admin@123";

  private String hashed;

  @Setup
  public void prepare() {
    hashed = PasswordUtil.hash(PLAINTEXT);
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.SECONDS)
  @Warmup(iterations = 1, time = 1)
  @Measurement(iterations = 2, time = 1)
  @Fork(1)
  public String hashPassword() {
    return PasswordUtil.hash(PLAINTEXT);
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.SECONDS)
  @Warmup(iterations = 2, time = 1)
  @Measurement(iterations = 3, time = 1)
  @Fork(1)
  public boolean verifyPassword() {
    return PasswordUtil.verify(PLAINTEXT, hashed);
  }

  @Test
  public void password_verify_100_within_sla() {
    String localHash = PasswordUtil.hash(PLAINTEXT);

    long start = System.nanoTime();
    for (int i = 0; i < 100; i++) {
      PasswordUtil.verify(PLAINTEXT, localHash);
    }
    long elapsedMs = (System.nanoTime() - start) / 1_000_000;
    assertThat(elapsedMs, lessThan(15_000L));
  }

  @Test
  public void password_hash_10_within_sla() {
    long start = System.nanoTime();
    for (int i = 0; i < 10; i++) {
      PasswordUtil.hash(PLAINTEXT);
    }
    long elapsedMs = (System.nanoTime() - start) / 1_000_000;
    assertThat(elapsedMs, lessThan(5000L));
  }
}
