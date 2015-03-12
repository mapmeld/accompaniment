import java.awt.Component;
import java.awt.Font;

import javax.swing.AbstractCellEditor;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

//vpTimeInput is a general-use cell-editor
//it allows hours (integer), minutes (integer), and seconds (double) to be input
public abstract class vpTimeInput extends AbstractCellEditor implements TableCellEditor {
	
	//the font used in the editor
	protected static final Font editFont = new Font("Arial", Font.BOLD, 15);
	
	//defining the editor
	protected JPanel valuesPanel = new JPanel();
	protected JTextField hours = new JTextField();
	protected JTextField minutes = new JTextField();
	protected JTextField seconds = new JTextField();
	
	public vpTimeInput(){
		//set up the editor
		hours.setFont(editFont);
		minutes.setFont(editFont);
		seconds.setFont(editFont);
		seconds.selectAll();
		valuesPanel.add(hours);
		valuesPanel.add(minutes);
		valuesPanel.add(seconds);
		valuesPanel.setLayout(new BoxLayout(valuesPanel, BoxLayout.X_AXIS));
	}
	
	//vpTimeInput can create a display for a given time (double)
	public Component getTableCellWithTime(float mytime){
		int myhours = (int)(mytime / 3600);
		int myminutes = (int)((mytime - 3600 * myhours) / 60);
		float myseconds = mytime - 3600 * myhours - 60 * myminutes;
		if(myhours > 0){
			hours.setVisible(true);
			hours.setText("" + myhours);
		}
		else{
			//if start time is < 1 hour, do not display its textbox
			hours.setVisible(false);
			hours.setText("0");
		}
		minutes.setText("" + myminutes);
		seconds.setText("" + myseconds);
		return valuesPanel;
	}
	
	//vpTimeInput cannot generate the editor by itself
	//you should use an editor which extends this class by overriding this method:
	public abstract Component getTableCellEditorComponent(JTable t, Object value, boolean isSelected, int row, int col);
	//Here is an example of how to override this method in a class:
	/**
	  protected class MapTimeSetter extends vpTimeInput{
		public Component getTableCellEditorComponent(JTable t, Object value, boolean isSelected, int row, int col) {
			double myTime = [Time for the cell at (row,col) ]
			return super.getTableCellWithTime(myTime);
		}
	  }
	*/

	public Object getCellEditorValue() {
		if(hours.getText().trim() == ""){ hours.setText("0"); }
		if(minutes.getText().trim() == ""){ minutes.setText("0"); }
		if(seconds.getText().trim() == ""){ seconds.setText("0"); }
		
		try{
			//the editor allows any number of hours, minutes, and seconds
			//seconds can have double values (such as 10.0405)
			//more than 60 minutes or seconds will be internally converted and displayed
			return (Float.parseFloat(seconds.getText().trim()) + 60 * Integer.parseInt(minutes.getText().trim()) + 3600 * Integer.parseInt(hours.getText().trim()));
		}
		catch(Exception e){ return null; }
	}
}