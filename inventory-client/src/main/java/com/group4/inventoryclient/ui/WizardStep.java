package com.group4.inventoryclient.ui;

import javax.swing.JPanel;

public abstract class WizardStep {

  public abstract JPanel getPanel();

  public abstract String getTitle();

  public abstract boolean validate();

  public abstract void submit() throws Exception;

  public void onEnter() {}
}
