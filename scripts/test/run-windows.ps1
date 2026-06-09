param(
    [string]$Profile = "full"
)

$Root = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
Set-Location $Root

if ($env:SKIP_DOCKER -ne "1") {
    docker compose -f docker-compose.test.yml up -d
    Start-Sleep -Seconds 8
}

$envTest = Join-Path $Root "inventory-server\.env.test"
if (-not (Test-Path $envTest)) {
    Copy-Item (Join-Path $Root "inventory-server\.env.test.example") $envTest
}

$env:G4IMS_ENV_FILE = $envTest

Write-Host "=== Unit tests ==="
mvn -q test -pl inventory-server,inventory-client

switch ($Profile) {
    "unit-only" { exit 0 }
    "integration" {
        mvn -q verify -Pintegration -pl inventory-server "-Dg4ims.env.file=.env.test"
        exit 0
    }
    "api" {
        mvn -q verify -Papi -pl inventory-server "-Dg4ims.env.file=.env.test"
        exit 0
    }
    "e2e" {
        mvn -q test -Pe2e -pl test-automation "-Dg4ims.env.file=../inventory-server/.env.test"
        exit 0
    }
    "uat" {
        mvn -q test -Puat -pl test-automation "-Dtest=CucumberUatRunner" "-Dg4ims.env.file=../inventory-server/.env.test"
        exit 0
    }
    "perf" {
        mvn -q test -Pperf -pl inventory-server
        exit 0
    }
    default {
        mvn -q verify -Pintegration -pl inventory-server "-Dg4ims.env.file=.env.test"
        mvn -q verify -Papi -pl inventory-server "-Dg4ims.env.file=.env.test"
        mvn -q test -Pe2e -pl test-automation "-Dg4ims.env.file=../inventory-server/.env.test"
        mvn -q test -Pperf -pl inventory-server
    }
}

Write-Host "Done (profile=$Profile)."
