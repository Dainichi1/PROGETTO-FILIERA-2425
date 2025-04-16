package unicam.filiera.view;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ComponentCellRenderer implements TableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        if (value instanceof Component comp) {
            return comp;
        } else {
            return new JLabel(value != null ? value.toString() : "");
        }
    }
}
