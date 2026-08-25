package ui;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.IntConsumer;

/**
 * Wires a JButton into one column of a JTable, so each row shows a clickable
 * action (e.g. "View"). rowConsumer receives the *model* row index when clicked.
 */
public class TableButtonColumn extends AbstractCellEditor implements TableCellRenderer, javax.swing.table.TableCellEditor {

    private final JButton renderButton;
    private final JButton editButton;
    private final JTable table;
    private final IntConsumer onClick;
    private int editingRow;

    public TableButtonColumn(JTable table, String label, IntConsumer onClick) {
        this.table = table;
        this.onClick = onClick;

        this.renderButton = new JButton(label);
        this.editButton = new JButton(label);
        style(renderButton);
        style(editButton);

        editButton.addActionListener((ActionEvent e) -> {
            int modelRow = table.convertRowIndexToModel(editingRow);
            fireEditingStopped();
            onClick.accept(modelRow);
        });

        table.getColumnModel().getColumn(table.getColumnCount() - 1).setCellRenderer(this);
        table.getColumnModel().getColumn(table.getColumnCount() - 1).setCellEditor(this);
    }

    private void style(JButton b) {
        b.setFont(UITheme.FONT_SMALL);
        b.setBackground(UITheme.PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                     boolean hasFocus, int row, int column) {
        return renderButton;
    }

    @Override
    public Component getTableCellEditorComponent(JTable t, Object value, boolean isSelected, int row, int column) {
        this.editingRow = row;
        return editButton;
    }

    @Override
    public Object getCellEditorValue() { return ""; }
}
