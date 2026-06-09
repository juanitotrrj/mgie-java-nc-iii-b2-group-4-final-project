UPDATE system_installation
SET setup_state = 'INITIALIZED',
    initialized_at = COALESCE(initialized_at, UTC_TIMESTAMP()),
    installed_at = COALESCE(installed_at, UTC_TIMESTAMP())
WHERE installation_id = 1;
