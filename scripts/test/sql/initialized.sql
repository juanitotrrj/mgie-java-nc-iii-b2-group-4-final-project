-- Mark installation as complete for automated API/UI/E2E tests.
-- Assumes migrations (including V004 admin user) have already run.

UPDATE system_installation
SET setup_state = 'INITIALIZED',
    initialized_at = COALESCE(initialized_at, UTC_TIMESTAMP()),
    installed_at = COALESCE(installed_at, UTC_TIMESTAMP())
WHERE installation_id = 1;
