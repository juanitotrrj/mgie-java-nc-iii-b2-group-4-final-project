package com.group4.inventoryclient.ui.components;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class PaginatedTable extends JPanel {

  private final DefaultTableModel tableModel;
  private final JTable table;
  private final JLabel pageLabel = new JLabel("Page 1 of 1");
  private final JButton prevBtn = new JButton("< Prev");
  private final JButton nextBtn = new JButton("Next >");
  private int currentPage = 1;
  private int totalPages = 1;
  private PageChangeListener pageChangeListener;

  public PaginatedTable(String[] columns) {
    super(new BorderLayout());
    tableModel =
        new DefaultTableModel(columns, 0) {
          @Override
          public boolean isCellEditable(int row, int column) {
            return false;
          }
        };
    table = new JTable(tableModel);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

    add(new JScrollPane(table), BorderLayout.CENTER);

    JPanel pagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
    prevBtn.addActionListener(
        e -> {
          if (currentPage > 1) {
            currentPage--;
            firePageChange();
          }
        });
    nextBtn.addActionListener(
        e -> {
          if (currentPage < totalPages) {
            currentPage++;
            firePageChange();
          }
        });
    pagePanel.add(prevBtn);
    pagePanel.add(pageLabel);
    pagePanel.add(nextBtn);
    add(pagePanel, BorderLayout.SOUTH);
  }

  public void setData(Object[][] rows, int page, int totalPagesCount) {
    tableModel.setRowCount(0);
    for (Object[] row : rows) {
      tableModel.addRow(row);
    }
    this.currentPage = page;
    this.totalPages = Math.max(1, totalPagesCount);
    pageLabel.setText("Page " + currentPage + " of " + totalPages);
    prevBtn.setEnabled(currentPage > 1);
    nextBtn.setEnabled(currentPage < totalPages);
  }

  public JTable getTable() {
    return table;
  }

  public DefaultTableModel getTableModel() {
    return tableModel;
  }

  public int getSelectedRow() {
    return table.getSelectedRow();
  }

  public int getCurrentPage() {
    return currentPage;
  }

  public void setPageChangeListener(PageChangeListener listener) {
    this.pageChangeListener = listener;
  }

  private void firePageChange() {
    pageLabel.setText("Page " + currentPage + " of " + totalPages);
    prevBtn.setEnabled(currentPage > 1);
    nextBtn.setEnabled(currentPage < totalPages);
    if (pageChangeListener != null) pageChangeListener.onPageChange(currentPage);
  }

  public interface PageChangeListener {
    void onPageChange(int page);
  }
}
