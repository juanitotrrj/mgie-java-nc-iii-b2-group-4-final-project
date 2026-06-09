package com.group4.inventoryserver.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.group4.inventoryserver.dto.setup.SetupFinishResponse;
import com.group4.inventoryserver.dto.setup.SetupProgressResponse;
import com.group4.inventoryserver.dto.setup.SetupStatusResponse;
import com.group4.inventoryserver.repository.SetupSessionRepository;
import com.group4.inventoryserver.repository.SystemInstallationRepository;
import java.sql.Timestamp;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class SetupServiceTest {

  private SystemInstallationRepository installationRepo;
  private SetupSessionRepository sessionRepo;
  private SetupService setupService;

  @Before
  public void setUp() {
    installationRepo = mock(SystemInstallationRepository.class);
    sessionRepo = mock(SetupSessionRepository.class);
    setupService = new SetupService(installationRepo, sessionRepo);
  }

  @Test
  public void getStatus_returnsPendingState_whenInfraReady() {
    when(installationRepo.getSetupState()).thenReturn("INFRA_READY_APP_SETUP_PENDING");
    when(installationRepo.getServerVersion()).thenReturn("1.0.0");

    SetupStatusResponse status = setupService.getStatus();

    assertEquals("INFRA_READY_APP_SETUP_PENDING", status.getState());
    assertEquals("1.0.0", status.getServerVersion());
    assertTrue(status.isAppSetupRequired());
  }

  @Test
  public void getStatus_appSetupNotRequired_whenInitialized() {
    when(installationRepo.getSetupState()).thenReturn("INITIALIZED");
    when(installationRepo.getServerVersion()).thenReturn("1.0.0");

    SetupStatusResponse status = setupService.getStatus();

    assertFalse(status.isAppSetupRequired());
  }

  @Test
  public void getProgress_returnsCurrentState_withoutDbLookup() {
    when(installationRepo.getSetupState()).thenReturn("INFRA_READY_APP_SETUP_PENDING");

    SetupProgressResponse progress = setupService.getProgress();

    assertEquals("INFRA_READY_APP_SETUP_PENDING", progress.getCurrentState());
    assertNotNull(progress.getCompletedSteps());
    assertTrue(progress.getCompletedSteps().isEmpty());
    assertNotNull(progress.getDraftData());
  }

  @Test
  public void getProgress_skipsCompletedSteps_whenNotInProgress() {
    when(installationRepo.getSetupState()).thenReturn("UNCONFIGURED");

    SetupProgressResponse progress = setupService.getProgress();

    assertEquals("UNCONFIGURED", progress.getCurrentState());
    assertTrue(progress.getCompletedSteps().isEmpty());
  }

  @Test
  public void validateSetupToken_returnsFalse_whenNull() {
    assertFalse(setupService.validateSetupToken(null));
    verify(sessionRepo, never()).isValid(anyString());
  }

  @Test
  public void validateSetupToken_returnsFalse_whenEmpty() {
    assertFalse(setupService.validateSetupToken(""));
    verify(sessionRepo, never()).isValid(anyString());
  }

  @Test
  public void validateSetupToken_touchesActivity_whenValid() {
    when(sessionRepo.isValid(anyString())).thenReturn(true);

    assertTrue(setupService.validateSetupToken("setup-token-abc"));

    verify(sessionRepo).isValid(anyString());
    verify(sessionRepo).touchActivity(anyString());
  }

  @Test
  public void validateSetupToken_doesNotTouch_whenInvalid() {
    when(sessionRepo.isValid(anyString())).thenReturn(false);

    assertFalse(setupService.validateSetupToken("bad-token"));

    verify(sessionRepo, never()).touchActivity(anyString());
  }

  @Test
  public void createSetupSession_createsSessionAndReturnsToken() {
    String token = setupService.createSetupSession();

    assertNotNull(token);
    assertFalse(token.isEmpty());

    ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Timestamp> expiresCaptor = ArgumentCaptor.forClass(Timestamp.class);
    verify(sessionRepo).create(hashCaptor.capture(), expiresCaptor.capture());

    assertEquals(64, hashCaptor.getValue().length());
    assertTrue(expiresCaptor.getValue().getTime() > System.currentTimeMillis());
  }

  @Test
  public void finish_marksInitializedAndExpiresSessions() {
    Timestamp initializedAt = new Timestamp(System.currentTimeMillis());
    when(installationRepo.getInitializedAt()).thenReturn(initializedAt);

    SetupFinishResponse response = setupService.finish();

    verify(installationRepo).markInitialized();
    verify(sessionRepo).expireAll();
    assertEquals("INITIALIZED", response.getState());
    assertEquals(initializedAt.toString(), response.getInitializedAt());
  }
}
