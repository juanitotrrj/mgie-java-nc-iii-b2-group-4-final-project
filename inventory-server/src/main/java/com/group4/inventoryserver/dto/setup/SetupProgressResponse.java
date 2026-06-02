package com.group4.inventoryserver.dto.setup;

import java.util.List;
import java.util.Map;

public class SetupProgressResponse {

  private String currentState;
  private List<String> completedSteps;
  private Map<String, Object> draftData;

  public SetupProgressResponse() {}

  public String getCurrentState() {
    return currentState;
  }

  public void setCurrentState(String currentState) {
    this.currentState = currentState;
  }

  public List<String> getCompletedSteps() {
    return completedSteps;
  }

  public void setCompletedSteps(List<String> completedSteps) {
    this.completedSteps = completedSteps;
  }

  public Map<String, Object> getDraftData() {
    return draftData;
  }

  public void setDraftData(Map<String, Object> draftData) {
    this.draftData = draftData;
  }
}
