package com.group4.inventoryserver.perf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

import com.group4.inventoryserver.dto.auth.LoginRequest;
import com.group4.inventoryserver.util.JsonUtil;
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
public class JsonUtilPerfTest {

  private LoginRequest request;
  private String json;

  @Setup
  public void prepare() {
    request = new LoginRequest();
    request.setUsername("admin");
    request.setPassword("Admin@123");
    request.setRole("Administrator");
    json = JsonUtil.toJson(request);
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.SECONDS)
  @Warmup(iterations = 2, time = 1)
  @Measurement(iterations = 3, time = 1)
  @Fork(1)
  public String serializeLoginRequest() {
    return JsonUtil.toJson(request);
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.SECONDS)
  @Warmup(iterations = 2, time = 1)
  @Measurement(iterations = 3, time = 1)
  @Fork(1)
  public LoginRequest deserializeLoginRequest() {
    return JsonUtil.fromJson(json, LoginRequest.class);
  }

  @Test
  public void json_serialize_deserialize_10k_within_sla() {
    LoginRequest localRequest = new LoginRequest();
    localRequest.setUsername("admin");
    localRequest.setPassword("Admin@123");
    localRequest.setRole("Administrator");

    long start = System.nanoTime();
    for (int i = 0; i < 10_000; i++) {
      String payload = JsonUtil.toJson(localRequest);
      JsonUtil.fromJson(payload, LoginRequest.class);
    }
    long elapsedMs = (System.nanoTime() - start) / 1_000_000;
    assertThat(elapsedMs, lessThan(3000L));
  }
}
