UPDATE system_installation
SET setup_state = 'INFRA_READY_APP_SETUP_PENDING',
    installed_at = COALESCE(installed_at, UTC_TIMESTAMP()),
    initialized_at = NULL
WHERE installation_id = 1;
