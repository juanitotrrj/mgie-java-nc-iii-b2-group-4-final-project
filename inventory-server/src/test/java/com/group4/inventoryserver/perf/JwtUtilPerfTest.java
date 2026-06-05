package com.group4.inventoryserver.perf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

import com.group4.inventoryserver.security.JwtUtil;
import io.jsonwebtoken.Claims;
import java.util.HashMap;
import java.util.Map;
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
public class JwtUtilPerfTest {

  private Map<String, Object> claims;
  private String token;

  @Setup
  public void prepare() {
    claims = new HashMap<String, Object>();
    claims.put("userId", 1L);
    claims.put("role", "Administrator");
    claims.put("username", "admin");
    token = JwtUtil.generateToken("1", claims);
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.SECONDS)
  @Warmup(iterations = 2, time = 1)
  @Measurement(iterations = 3, time = 1)
  @Fork(1)
  public String generateToken() {
    return JwtUtil.generateToken("1", claims);
  }

  @Benchmark
  @BenchmarkMode(Mode.Throughput)
  @OutputTimeUnit(TimeUnit.SECONDS)
  @Warmup(iterations = 2, time = 1)
  @Measurement(iterations = 3, time = 1)
  @Fork(1)
  public Claims parseToken() {
    return JwtUtil.validateToken(token);
  }

  @Test
  public void jwt_create_and_parse_10k_within_sla() {
    Map<String, Object> localClaims = new HashMap<String, Object>();
    localClaims.put("userId", 1L);
    localClaims.put("role", "Administrator");
    localClaims.put("username", "admin");

    long start = System.nanoTime();
    for (int i = 0; i < 10_000; i++) {
      String t = JwtUtil.generateToken("1", localClaims);
      JwtUtil.validateToken(t);
    }
    long elapsedMs = (System.nanoTime() - start) / 1_000_000;
    assertThat(elapsedMs, lessThan(5000L));
  }
}
