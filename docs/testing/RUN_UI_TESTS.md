# Run UI Tests (AssertJ-Swing)

## Purpose

Swing smoke tests (`GuestFrameUiTest`). Wizard root-auth step is **not** tested.

## Prerequisites

Display server (Linux: `xvfb-run`), server optional for frame-only tests.

## Linux

```bash
xvfb-run mvn verify -Pui -pl inventory-client
```

## Windows

```powershell
mvn verify -Pui -pl inventory-client
```
