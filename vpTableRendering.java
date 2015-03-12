import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

//the TableRendering class determines the size and appearance of table cells
public class vpTableRendering extends DefaultTableCellRenderer{
	protected static final Font regularFont = new Font("Arial", Font.PLAIN, 15);

	protected int DEFAULT_ROW_HEIGHT = 30;
	protected int SELECTED_ROW_HEIGHT = 60;	
	int lastSelectedRow = -1;
	
	public vpTableRendering(int def, int selected){
		DEFAULT_ROW_HEIGHT = def;
		SELECTED_ROW_HEIGHT = selected;
	}
	
	public Component getTableCellRendererComponent(JTable t, Object item, boolean isSelected, boolean hasFocus, int row, int col) {
		Component c = super.getTableCellRendererComponent(t, item, isSelected, hasFocus, row, col);
		if((isSelected)&&(lastSelectedRow != row)){
			t.setRowHeight(row, SELECTED_ROW_HEIGHT);
			if(lastSelectedRow != -1){
				t.setRowHeight(lastSelectedRow, DEFAULT_ROW_HEIGHT);
			}
			lastSelectedRow = row;
		}
		c.setFont(regularFont);
		return c;
	}
}