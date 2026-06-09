# Run Load Tests (JMeter)

## Plan

`test-automation/jmeter/g4ims-smoke.jmx` — 5 threads × 3 loops on `GET /api/health`.

## Prerequisites

Server running on port 18080 with INITIALIZED DB.

## Command

```bash
mvn verify -Pload -pl test-automation
```

Nightly / manual only — not required on every PR.
