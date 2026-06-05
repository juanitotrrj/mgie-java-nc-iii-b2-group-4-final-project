# Run Performance Tests

## Purpose

Macro SLA checks (`PaginationParamsPerfTest`) — 100k parses &lt; 2s.

## Command

```bash
mvn test -Pperf -pl inventory-server
```

## SLAs (documented targets)

| Area | Target |
|------|--------|
| Pagination parse (100k) | &lt; 2000 ms |
| `/api/health` p95 | &lt; 100 ms (manual/load) |
