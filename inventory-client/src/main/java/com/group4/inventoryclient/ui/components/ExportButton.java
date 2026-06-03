package com.group4.inventoryclient.ui.components;

import com.group4.inventoryclient.api.ApiClient;
import com.group4.inventoryclient.util.SwingUtil;
import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

public class ExportButton extends JButton {

  private final ApiClient apiClient;
  private final String exportPath;
  private final Component parent;

  public ExportButton(ApiClient apiClient, String exportPath, Component parent) {
    super("Export");
    this.apiClient = apiClient;
    this.exportPath = exportPath;
    this.parent = parent;
    addActionListener(e -> doExport());
  }

  private void doExport() {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Save Export");
    if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      new Thread(
              () -> {
                try {
                  URL url = new URL(apiClient.getBaseUrl() + exportPath);
                  HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                  conn.setRequestMethod("GET");
                  if (apiClient.getBearerToken() != null) {
                    conn.setRequestProperty(
                        "Authorization", "Bearer " + apiClient.getBearerToken());
                  }
                  conn.setConnectTimeout(10000);
                  conn.setReadTimeout(60000);

                  int code = conn.getResponseCode();
                  if (code == 200) {
                    try (InputStream in = conn.getInputStream();
                        FileOutputStream out = new FileOutputStream(file)) {
                      byte[] buf = new byte[4096];
                      int n;
                      while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
                    }
                    SwingUtilities.invokeLater(
                        () -> SwingUtil.showInfo(parent, "Exported to " + file.getName()));
                  } else {
                    SwingUtilities.invokeLater(
                        () -> SwingUtil.showError(parent, "Export failed: " + code));
                  }
                  conn.disconnect();
                } catch (Exception ex) {
                  SwingUtilities.invokeLater(() -> SwingUtil.showError(parent, ex.getMessage()));
                }
              })
          .start();
    }
  }
}
