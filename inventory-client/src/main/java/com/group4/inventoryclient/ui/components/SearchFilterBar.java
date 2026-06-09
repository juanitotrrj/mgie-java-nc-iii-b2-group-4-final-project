package com.group4.inventoryclient.ui.components;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

public class SearchFilterBar extends JPanel {

  private final JTextField searchField = new JTextField(18);
  private final JButton searchBtn = new JButton("Search");
  private final JButton clearBtn = new JButton("Clear");
  private final List<JComboBox<String>> filterCombos = new ArrayList<>();
  private SearchListener listener;
  private Timer debounceTimer;

  public SearchFilterBar() {
    super(new FlowLayout(FlowLayout.LEFT, 8, 4));
    add(new JLabel("Search:"));
    add(searchField);
    add(searchBtn);
    add(clearBtn);

    searchBtn.addActionListener(e -> fireSearch());
    clearBtn.addActionListener(
        e -> {
          searchField.setText("");
          for (JComboBox<String> combo : filterCombos) combo.setSelectedIndex(0);
          fireSearch();
        });

    debounceTimer = new Timer(400, e -> fireSearch());
    debounceTimer.setRepeats(false);
    searchField
        .getDocument()
        .addDocumentListener(
            new javax.swing.event.DocumentListener() {
              public void insertUpdate(javax.swing.event.DocumentEvent e) {
                debounceTimer.restart();
              }

              public void removeUpdate(javax.swing.event.DocumentEvent e) {
                debounceTimer.restart();
              }

              public void changedUpdate(javax.swing.event.DocumentEvent e) {
                debounceTimer.restart();
              }
            });
  }

  public JComboBox<String> addFilter(String label, String[] options) {
    add(new JLabel(label + ":"));
    JComboBox<String> combo = new JComboBox<>(options);
    combo.addActionListener(e -> fireSearch());
    add(combo);
    filterCombos.add(combo);
    return combo;
  }

  public String getSearchText() {
    return searchField.getText().trim();
  }

  public String getFilterValue(int index) {
    if (index < filterCombos.size()) {
      Object selected = filterCombos.get(index).getSelectedItem();
      return selected != null ? selected.toString() : "";
    }
    return "";
  }

  public void setSearchListener(SearchListener listener) {
    this.listener = listener;
  }

  private void fireSearch() {
    if (listener != null) listener.onSearch(getSearchText());
  }

  public interface SearchListener {
    void onSearch(String query);
  }
}
