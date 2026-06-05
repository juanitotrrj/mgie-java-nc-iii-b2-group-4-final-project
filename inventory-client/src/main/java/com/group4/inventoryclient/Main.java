package com.group4.inventoryclient;

import com.group4.inventoryclient.api.ApiClient;
import com.group4.inventoryclient.config.ClientConfig;
import com.group4.inventoryclient.ui.GuestFrame;
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
          launch(config);
        });
  }

  private static void launch(ClientConfig config) {
    ApiClient apiClient = new ApiClient(config.getServerUrl());
    new Thread(
            () -> {
              try {
                ApiClient.ApiResponse response = apiClient.get("/setup/status");
                if (response.isSuccess()) {
                  String state =
                      response.getDataAsObject().has("state")
                          ? response.getDataAsObject().get("state").getAsString()
                          : "UNKNOWN";
                  SwingUtilities.invokeLater(
                      () -> {
                        if ("INITIALIZED".equals(state)) {
                          GuestFrame guest = new GuestFrame(apiClient);
                          guest.setVisible(true);
                        } else {
                          WizardFrame wizard = new WizardFrame(config);
                          wizard.setVisible(true);
                        }
                      });
                } else {
                  SwingUtilities.invokeLater(
                      () -> {
                        GuestFrame guest = new GuestFrame(apiClient);
                        guest.setVisible(true);
                      });
                }
              } catch (Exception e) {
                SwingUtilities.invokeLater(
                    () -> {
                      GuestFrame guest = new GuestFrame(apiClient);
                      guest.setVisible(true);
                    });
              }
            })
        .start();
  }
}
