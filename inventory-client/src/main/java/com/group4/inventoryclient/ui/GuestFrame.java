package com.group4.inventoryclient.ui;

import com.google.gson.JsonObject;
import com.group4.inventoryclient.api.ApiClient;
import com.group4.inventoryclient.api.AuthApiClient;
import com.group4.inventoryclient.session.SessionManager;
import com.group4.inventoryclient.util.SwingUtil;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class GuestFrame extends JFrame {

  private static final String[] LOGIN_ROLES = {
    "Administrator", "Manager", "Inventory Clerk", "Cashier"
  };

  private final ApiClient apiClient;
  private final CardLayout cardLayout = new CardLayout();
  private final JPanel cardPanel = new JPanel(cardLayout);

  public GuestFrame(ApiClient apiClient) {
    super("G4 Inventory Management System");
    this.apiClient = apiClient;
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setMinimumSize(new Dimension(700, 500));
    setLocationRelativeTo(null);
    initUI();
  }

  private void initUI() {
    cardPanel.add(buildWelcomePanel(), "welcome");
    cardPanel.add(buildAboutPanel(), "about");
    cardPanel.add(buildContactPanel(), "contact");
    cardPanel.add(buildLoginPanel(), "login");

    JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
    JButton welcomeBtn = new JButton("Welcome");
    JButton aboutBtn = new JButton("About");
    JButton contactBtn = new JButton("Contact");
    JButton loginBtn = new JButton("Login");
    JButton testBtn = new JButton("Test Connection");

    welcomeBtn.addActionListener(e -> cardLayout.show(cardPanel, "welcome"));
    aboutBtn.addActionListener(e -> cardLayout.show(cardPanel, "about"));
    contactBtn.addActionListener(e -> cardLayout.show(cardPanel, "contact"));
    loginBtn.addActionListener(e -> cardLayout.show(cardPanel, "login"));
    testBtn.addActionListener(e -> testConnection());

    navPanel.add(welcomeBtn);
    navPanel.add(aboutBtn);
    navPanel.add(contactBtn);
    navPanel.add(testBtn);
    navPanel.add(loginBtn);

    setLayout(new BorderLayout());
    add(navPanel, BorderLayout.NORTH);
    add(cardPanel, BorderLayout.CENTER);
  }

  private JPanel buildWelcomePanel() {
    JPanel p = new JPanel(new BorderLayout(10, 10));
    p.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
    JLabel title = new JLabel("Welcome to the Inventory Management System");
    title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
    p.add(title, BorderLayout.NORTH);
    JTextArea desc =
        new JTextArea(
            "This system manages products, categories, suppliers, purchases, "
                + "sales, and inventory for your business.\n\n"
                + "Use the navigation above to learn more or login to start working.");
    desc.setEditable(false);
    desc.setLineWrap(true);
    desc.setWrapStyleWord(true);
    desc.setOpaque(false);
    p.add(desc, BorderLayout.CENTER);
    return p;
  }

  private JPanel buildAboutPanel() {
    JPanel p = new JPanel(new BorderLayout(10, 10));
    p.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
    JLabel title = new JLabel("About the System");
    title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
    p.add(title, BorderLayout.NORTH);
    JTextArea info =
        new JTextArea(
            "G4 Inventory Management System\n\n"
                + "Core Modules:\n"
                + "  - Products, Categories, Suppliers\n"
                + "  - Purchase Orders & Receiving\n"
                + "  - Sales & Point-of-Sale\n"
                + "  - Inventory Change Requests\n"
                + "  - Reports & Data Export\n"
                + "  - User & Role Management\n"
                + "  - System Settings & Backups\n\n"
                + "Architecture: Java Swing client + Java SE 8 REST API server");
    info.setEditable(false);
    info.setLineWrap(true);
    info.setWrapStyleWord(true);
    info.setOpaque(false);
    p.add(info, BorderLayout.CENTER);
    return p;
  }

  private JPanel buildContactPanel() {
    JPanel p = new JPanel(new BorderLayout(10, 10));
    p.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
    JLabel title = new JLabel("Contact / Submit Inquiry");
    title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
    p.add(title, BorderLayout.NORTH);

    JPanel form = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    JTextField nameField = new JTextField(20);
    JTextField emailField = new JTextField(20);
    JTextField subjectField = new JTextField(20);
    JTextArea messageArea = new JTextArea(4, 20);

    int row = 0;
    gbc.gridx = 0;
    gbc.gridy = row;
    form.add(new JLabel("Name:"), gbc);
    gbc.gridx = 1;
    form.add(nameField, gbc);

    row++;
    gbc.gridx = 0;
    gbc.gridy = row;
    form.add(new JLabel("Email:"), gbc);
    gbc.gridx = 1;
    form.add(emailField, gbc);

    row++;
    gbc.gridx = 0;
    gbc.gridy = row;
    form.add(new JLabel("Subject:"), gbc);
    gbc.gridx = 1;
    form.add(subjectField, gbc);

    row++;
    gbc.gridx = 0;
    gbc.gridy = row;
    form.add(new JLabel("Message:"), gbc);
    gbc.gridx = 1;
    form.add(new javax.swing.JScrollPane(messageArea), gbc);

    row++;
    gbc.gridx = 1;
    gbc.gridy = row;
    JButton submitBtn = new JButton("Submit Inquiry");
    submitBtn.addActionListener(
        e -> {
          new Thread(
                  () -> {
                    try {
                      java.util.Map<String, String> body = new java.util.HashMap<>();
                      body.put("name", nameField.getText().trim());
                      body.put("email", emailField.getText().trim());
                      body.put("subject", subjectField.getText().trim());
                      body.put("message", messageArea.getText().trim());
                      ApiClient.ApiResponse resp = apiClient.post("/public/inquiries", body);
                      if (resp.isSuccess()) {
                        SwingUtilities.invokeLater(
                            () -> {
                              nameField.setText("");
                              emailField.setText("");
                              subjectField.setText("");
                              messageArea.setText("");
                            });
                        SwingUtil.showInfo(GuestFrame.this, "Inquiry submitted successfully.");
                      } else {
                        SwingUtil.showError(GuestFrame.this, resp.getErrorMessage());
                      }
                    } catch (Exception ex) {
                      SwingUtil.showError(GuestFrame.this, ex.getMessage());
                    }
                  })
              .start();
        });
    form.add(submitBtn, gbc);

    p.add(form, BorderLayout.CENTER);
    return p;
  }

  private JPanel buildLoginPanel() {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(8, 8, 8, 8);
    gbc.anchor = GridBagConstraints.WEST;

    JTextField usernameField = new JTextField(20);
    JPasswordField passwordField = new JPasswordField(20);
    JComboBox<String> roleField = new JComboBox<>(LOGIN_ROLES);

    int row = 0;
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 2;
    JLabel title = new JLabel("Login");
    title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
    p.add(title, gbc);
    gbc.gridwidth = 1;

    row++;
    gbc.gridx = 0;
    gbc.gridy = row;
    p.add(new JLabel("Username:"), gbc);
    gbc.gridx = 1;
    p.add(usernameField, gbc);

    row++;
    gbc.gridx = 0;
    gbc.gridy = row;
    p.add(new JLabel("Password:"), gbc);
    gbc.gridx = 1;
    p.add(passwordField, gbc);

    row++;
    gbc.gridx = 0;
    gbc.gridy = row;
    p.add(new JLabel("Role:"), gbc);
    gbc.gridx = 1;
    p.add(roleField, gbc);

    row++;
    gbc.gridx = 1;
    gbc.gridy = row;
    JButton loginBtn = new JButton("Login");
    loginBtn.addActionListener(
        e -> performLogin(usernameField, passwordField, (String) roleField.getSelectedItem()));
    p.add(loginBtn, gbc);

    return p;
  }

  private void performLogin(JTextField usernameField, JPasswordField passwordField, String role) {
    String username = usernameField.getText().trim();
    String password = new String(passwordField.getPassword());
    if (username.isEmpty() || password.isEmpty()) {
      SwingUtil.showError(this, "Username and password are required.");
      return;
    }
    if (role == null || role.trim().isEmpty()) {
      SwingUtil.showError(this, "Role is required.");
      return;
    }
    new Thread(
            () -> {
              try {
                AuthApiClient auth = new AuthApiClient(apiClient);
                JsonObject data = auth.login(username, password, role);
                apiClient.setBearerToken(data.get("token").getAsString());
                SessionManager.getInstance().login(data);
                SwingUtilities.invokeLater(
                    () -> {
                      dispose();
                      MainFrame mainFrame = new MainFrame(apiClient);
                      mainFrame.setVisible(true);
                    });
              } catch (Exception ex) {
                SwingUtil.showError(GuestFrame.this, ex.getMessage());
              }
            })
        .start();
  }

  private void testConnection() {
    new Thread(
            () -> {
              try {
                ApiClient.ApiResponse resp = apiClient.get("/health");
                if (resp.isSuccess()) {
                  SwingUtil.showInfo(GuestFrame.this, "Server connection successful!");
                } else {
                  SwingUtil.showError(GuestFrame.this, "Server returned: " + resp.getStatusCode());
                }
              } catch (Exception ex) {
                SwingUtil.showError(GuestFrame.this, "Cannot connect: " + ex.getMessage());
              }
            })
        .start();
  }
}
