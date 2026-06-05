package com.group4.inventoryserver.perf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;

import com.group4.inventoryserver.util.ExportUtil;
import java.util.ArrayList;
import java.util.List;
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
public class ExportUtilPerfTest {

  private static final String[] HEADERS = {"Code", "Name", "Qty", "Price"};

  private List<String[]> rows;

  @Setup
  public void prepare() {
    rows = buildRows(1000);
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Warmup(iterations = 2, time = 1)
  @Measurement(iterations = 3, time = 1)
  @Fork(1)
  public byte[] exportCsv1000Rows() {
    return ExportUtil.toCsv(HEADERS, rows);
  }

  @Test
  public void csv_export_1000_rows_within_sla() {
    List<String[]> localRows = buildRows(1000);

    long start = System.nanoTime();
    byte[] csv = ExportUtil.toCsv(HEADERS, localRows);
    long elapsedMs = (System.nanoTime() - start) / 1_000_000;

    assertThat(csv.length, greaterThan(0));
    assertThat(elapsedMs, lessThan(500L));
  }

  private static List<String[]> buildRows(int count) {
    List<String[]> data = new ArrayList<String[]>(count);
    for (int i = 0; i < count; i++) {
      data.add(
          new String[] {
            "P" + String.format("%04d", i),
            "Product " + i,
            String.valueOf(i % 100),
            String.valueOf(10.0 + (i % 50))
          });
    }
    return data;
  }
}
