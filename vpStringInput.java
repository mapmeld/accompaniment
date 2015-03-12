import java.awt.Component;
import java.awt.Font;

import javax.swing.AbstractCellEditor;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellEditor;

//vpStringInput is a general-use editor for input of a String (it is multi-line)
public class vpStringInput extends AbstractCellEditor implements TableCellEditor {
	protected static final Font editFont = new Font("Arial", Font.BOLD, 14);
	
	JTextArea editor = new JTextArea();
	JScrollPane scrollPane = new JScrollPane(editor);
	
	public vpStringInput(){
		editor.setFont(editFont);
		editor.setLineWrap(true);
		editor.setWrapStyleWord(true);
	}

	public Component getTableCellEditorComponent(JTable t, Object cellItem, boolean arg2, int row, int col) {
		editor.setText(t.getModel().getValueAt(row,col).toString());
		return scrollPane;
	}

	public Object getCellEditorValue() {
		return editor.getText();
	}
}