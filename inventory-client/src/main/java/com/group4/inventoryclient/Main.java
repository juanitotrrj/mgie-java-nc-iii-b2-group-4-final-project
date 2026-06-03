package com.group4.inventoryclient;

import com.group4.inventoryclient.config.ClientConfig;
import com.group4.inventoryclient.ui.WizardFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

  public static void main(String[] args) {
    String serverUrl = null;
    for (int i = 0; i < args.length - 1; i++) {
      if ("--server-url".equals(args[i])) {
        serverUrl = args[i + 1];
      }
    }

    ClientConfig config = new ClientConfig(serverUrl);

    SwingUtilities.invokeLater(
        () -> {
          try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
          } catch (Exception e) {
            // fallback to default
          }
          WizardFrame frame = new WizardFrame(config);
          frame.setVisible(true);
        });
  }
}
