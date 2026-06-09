package com.group4.inventoryclient.ui.steps;

import com.group4.inventoryclient.api.SetupApiClient;
import com.group4.inventoryclient.ui.WizardStep;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

public class RoleSeedStep extends WizardStep {

  private final SetupApiClient api;
  private final JPanel panel;
  private final DefaultListModel<String> listModel = new DefaultListModel<>();
  private final JList<String> roleList = new JList<>(listModel);

  public RoleSeedStep(SetupApiClient api) {
    this.api = api;
    this.panel = buildPanel();
  }

  private JPanel buildPanel() {
    JPanel p = new JPanel(new BorderLayout(10, 10));
    p.setBorder(new TitledBorder("Seed Roles"));

    listModel.addElement("Admin");
    listModel.addElement("Manager");
    listModel.addElement("Inventory Clerk");
    listModel.addElement("Cashier");

    roleList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    roleList.setSelectionInterval(0, listModel.getSize() - 1);

    p.add(new JLabel("Select roles to create:"), BorderLayout.NORTH);
    p.add(new JScrollPane(roleList), BorderLayout.CENTER);
    p.add(
        new JLabel(
            "<html><i>All selected roles will be seeded. Existing roles won't be duplicated.</i></html>"),
        BorderLayout.SOUTH);

    return p;
  }

  @Override
  public JPanel getPanel() {
    return panel;
  }

  @Override
  public String getTitle() {
    return "Seed Roles";
  }

  @Override
  public boolean validate() {
    return roleList.getSelectedValuesList().size() > 0;
  }

  @Override
  public void submit() throws Exception {
    List<String> selected = new ArrayList<>(roleList.getSelectedValuesList());
    api.seedRoles(selected);
  }
}
