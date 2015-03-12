import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

public class Outline extends Supplement implements CSSstyled {

	protected JFrame myWindow = null;
	protected CSSEditor cssWindow;
	
	//Defining the table of OutlineItems
	protected static final int DEFAULT_ROW_HEIGHT = 30;
	protected static final int SELECTED_ROW_HEIGHT = 60;
	protected static final String childHeading = "--) ";
	
	protected Vector<OutlineItem> outlineData = new Vector<OutlineItem>();
	protected JTable outlineList;
	protected JScrollPane outlineContainer;
	
	//Defining appearance	
	protected static final String[] CSSdefaultLabels = {"Plain", "Highlight", "3D Highlight"};
	protected static final String plainCSS =
		"/* Plain Style */\r\n" +
		"#outline{\r\n" +
		"	font-family: arial;\r\n" +
		"	border-top: 1px solid gray;\r\n" +
		"	border-left: 1px solid gray;\r\n" +
		"	border-right: 1px solid gray;\r\n" +
		"	-moz-user-select: none;\r\n" +
		"	-khtml-user-select: none;\r\n" +
		"	user-select: none;\r\n" +
		"}\r\n" +
		"#outline li.firstlevel{\r\n" +
		"	list-style-type: square;\r\n" +
		"	padding: 0.35em;\r\n" +
		"	border-bottom: 1px solid gray;\r\n" +
		"}\r\n" +
		"#outline li.secondlevel{\r\n" +
		"	list-style-type: square;\r\n" +
		"	padding: 0.35em;\r\n" +
		"	border-bottom: 1px solid gray;\r\n" +
		"}\r\n" +
		"#outline li a { text-decoration: none; }";
	protected static final String highlightCSS =
		"/* Highlight Style */\r\n" +
		"#outline{\r\n" +
		"	list-style-type: none;\r\n" +
		"	font-family: arial;\r\n" +
		"	-moz-user-select: none;\r\n" +
		"	-khtml-user-select: none;\r\n" +
		"	user-select: none;\r\n" +
		"}\r\n" +
		"#outline a{\r\n" +
		"	display: block;\r\n" +
		"	padding: 3px;\r\n" +
		"	background-color: #036;\r\n" +
		"	border-bottom: 1px solid #eee;\r\n" +
		"}\r\n" +
		"#outline a:link, #outline a:visited{\r\n" +
		"	color: #EEE;\r\n" +
		"	text-decoration: none;\r\n" +
		"}\r\n" +
		"#outline a:hover{\r\n" +
		"	background-color: orange;\r\n" +
		"	color: white;\r\n" +
		"}\r\n" +
		"#outline li{\r\n" +
		"	list-style-type: none;\r\n" +
		"}\r\n";
	protected static final String texturedhighlightCSS =
		"/* 3D highlight style */\r\n" +
		"#outline{\r\n" +
		"	color: white;\r\n" +
		"	background: #17a;\r\n" +
		"	border-bottom: 0.2em solid #17a;\r\n" +
		"	border-right: 0.2em solid #17a;\r\n" +
		"	padding: 0 1px;\r\n" +
		"	width: 16em;\r\n" +
		"}\r\n" +
		"#outline li{\r\n" +
		"	list-style: none;\r\n" +
		"	font-size: 1em;\r\n" +
		"}\r\n" +
		"#outline a{\r\n" +
		"	display: block;\r\n" +
		"	text-decoration: none;\r\n" +
		"	margin: 0.5em;\r\n" +
		"	color: white;\r\n" +
		"	background: #39c;\r\n" +
		"	border-width: 1px;\r\n" +
		"	border-style: solid;\r\n" +
		"	border-color: #5bd #035 #068 #6cf;\r\n" +
		"	border-left: 1em solid #fc0;\r\n" +
		"	padding: 0.25em 0.5em 0.4em 0.75em;\r\n" +
		"}\r\n" +
		"#outline a#current { border-color: #5bd #035 #068 #f30; }\r\n" +
		"#outline a:hover, #outline a#current:hover{\r\n" +
		"	background: #28b;\r\n" +
		"	border-color: #069 #6cf #5bd #fc0;\r\n" +
		"	padding: 0.4em 0.35em 0.25em 0.9em;\r\n" +
		"}\r\n" +
		"#outline a:active, #outline a#current:active{\r\n" +
		"	background: #17a;\r\n" +
		"	border-color: #069 #6cf #5bd white;\r\n" +
		"	padding: 0.4em 0.35em 0.25em 0.9em;\r\n" +
		"}\r\n";
	
	protected String myCSS = plainCSS;
	
	//Defining the outline controls
	protected final VideoSetter videoSelect = new VideoSetter();
	protected VideoListing defaultVideo = vpVideoSelector.blankVideo;
	protected static final String defaultItem = "[ New Item ]";
	
	public String getDescription() {
		return "<html><p>The <b>Outline</b> add-on organizes the video with a structured outline.  Each outline item is a link to a point in a video.</p><br><p>An outline is a useful addition to long videos or for jumping to important information.</p></html>";
	}

	//Will send the HTML for setting up this AddOn
	public String getHTML(String myID) {
		boolean inFamily = false;
		String outputHTML = "<div id=\"" + myID + "Outline\">\r\n";
		outputHTML += "<style type=\"text/css\">\r\n" + myCSS.replace("#outline", "#" + myID + "outline").replace("firstlevel", myID + "_firstlevel").replace("secondlevel", myID + "_secondlevel") + "\r\n</style>\r\n";
		outputHTML += "<ul id=\"" + myID + "outline\">\r\n";
		for(int i=0; i<outlineData.size(); i++){
			OutlineItem v = outlineData.elementAt(i);
			if(!defaultItem.equals(v.getLabel())){
				if(inFamily){
					if(!v.isChild()){
						outputHTML += "</ul>\r\n<li class=\"" + myID + "_firstlevel\">";
						inFamily = false;
					}
					else{
						outputHTML += "<li class=\"" + myID + "_secondlevel\">";
					}
				}
				else{
					if(v.isChild()){
						outputHTML += "<ul>\r\n<li class=\"" + myID + "_secondlevel\">";
						inFamily = true;
					}
					else{
						outputHTML += "<li class=\"" + myID + "_firstlevel\">";
					}
				}
				if(!v.getVideo().equals(vpVideoSelector.blankVideo)){
					outputHTML += "<a href=\"javascript:void(0);\" onclick=\"loadYT('" + v.getVideo().getURL() + "', " + v.getStart() + ");\">" + v.getLabel() + "</a></li>\r\n";
				}
				else{
					outputHTML += v.getLabel() + "</li>\r\n";
				}
			}
		}
		if(inFamily){ outputHTML += "</ul>\r\n"; }
		outputHTML += "</ul>\r\n</div>";
		return outputHTML;
	}

	//This defines the preview in the layout window
	public String getMiniView() {
		return "<ul><li>" + this.getName() + "<ul><li>links<li>etc</ul></ul>";
	}

	public void setWindow(JFrame j) {
		//Generates the window if it is not already stored
		if(myWindow == null){
			myWindow = j;
			j.setTitle("Outline: " + this.getName());
			j.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			myWindow.setSize(600,400);
			myWindow.setVisible(true);
			BorderLayout myLayout = new BorderLayout();
			myWindow.setLayout(myLayout);
			
			cssWindow = new CSSEditor(this);
			
			//This should be replaced by more options or descriptive text
			myWindow.add(new Label("Create Outline"), BorderLayout.NORTH);
			
			JPanel buttons = new JPanel();
			//branch adds a new first-level OutlineItem and asks the table to update
			final JButton branch = new JButton("New Item");
			branch.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					//the default video for a new item is the same video as the last item
					videoSelect.refresh();
					outlineData.add(new OutlineItem());
					outlineList.tableChanged(new TableModelEvent(outlineList.getModel()));
				}
			});
			buttons.add(branch);
			
			//delete the selected item
			JButton cutbranch = new JButton("Delete");
			cutbranch.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					if(outlineList.getSelectedRow() != -1){
						outlineData.removeElementAt(outlineList.getSelectedRow());
						outlineList.tableChanged(new TableModelEvent(outlineList.getModel()));
					}
				}				
			});
			buttons.add(cutbranch);
			
			//a set of arrows that move items up or down, or left/right (parent/child)
			JPanel moveItemPanel = new JPanel();
			moveItemPanel.setLayout(new BorderLayout(5,5));
			
			//move the item up on the list
			JButton moveUp = new JButton("<html><span style='font-size: 16pt;'>&uarr;</span></html>");
			moveUp.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					int selection = outlineList.getSelectedRow();
					//the zero-th item cannot be moved up
					if(selection > 0){
						OutlineItem moveItem = outlineData.elementAt(selection);
						outlineData.removeElementAt(selection);
						outlineData.insertElementAt(moveItem, selection-1);
						//update row numbers for selected and in-editing items
						outlineList.setRowSelectionInterval(selection-1, selection-1);
						outlineList.setEditingRow(selection-1);
						outlineList.repaint();
					}
				}
			});
			moveItemPanel.add(moveUp, BorderLayout.NORTH);
			
			//child makes the selected item a child of the first regular item above it
			JButton child = new JButton("<html><span style='font-size: 16pt;'>&rarr;</span></html>");
			child.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					int selection = outlineList.getSelectedRow();
					//the zero-th item cannot be selected; there would be no parent
					if(selection > 0){
						outlineData.elementAt(selection).setChildFlag(true);
						outlineList.editingStopped(new ChangeEvent(""));
						//if the item has no video set, default to the parent's video
						if(outlineData.elementAt(selection).getVideo().equals(vpVideoSelector.blankVideo)){
							//traveling upward from the selected item, the first non-child is a parent
							for(int i = (selection - 1); i>=0; i--){
								if(!outlineData.elementAt(i).isChild()){
									//now set this child item's video to its parent item
									outlineData.elementAt(selection).setVideo(outlineData.elementAt(i).getVideo());
									break;
								}
							}
						}
						outlineList.editingStopped(new ChangeEvent(""));
						outlineList.repaint();
					}
				}
			});
			moveItemPanel.add(child, BorderLayout.EAST);
			
			//turning a child item into a regular item
			JButton makeParent = new JButton("<html><span style='font-size: 16pt;'>&larr;</span></html>");
			makeParent.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					int selection = outlineList.getSelectedRow();
					//the item must be a child
					if(outlineData.elementAt(selection).isChild()){
						outlineData.elementAt(selection).setChildFlag(false);
						outlineList.editingStopped(new ChangeEvent(""));
						outlineList.repaint();
					}
				}
			});
			moveItemPanel.add(makeParent, BorderLayout.WEST);
			
			//moving an item down on the list
			JButton moveDown = new JButton("<html><span style='font-size: 16pt;'>&darr;</span></html>");
			moveDown.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					int selection = outlineList.getSelectedRow();
					//the last item cannot be moved up
					if(selection < outlineData.size() - 1){
						OutlineItem moveItem = outlineData.elementAt(selection);
						outlineData.removeElementAt(selection);
						outlineData.insertElementAt(moveItem, selection+1);
						//update row numbers for selected and in-editing items
						outlineList.setRowSelectionInterval(selection+1, selection+1);
						outlineList.setEditingRow(selection+1);
						outlineList.repaint();
					}
				}
			});
			moveItemPanel.add(moveDown, BorderLayout.SOUTH);			
			buttons.add(moveItemPanel);
			
			//open a CSS editing window
			JButton cssButton = new JButton("Edit Style");
			cssButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					cssWindow.editStyle();
				}
			});
			buttons.add(cssButton);
			
			buttons.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 20));
			myWindow.add(buttons, BorderLayout.CENTER);
			
			//defining the outline-editing table
			outlineList = new JTable();
			outlineList.setFillsViewportHeight(true);
			outlineList.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			outlineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			outlineList.setRowHeight(DEFAULT_ROW_HEIGHT);
			outlineList.setModel(new TableRegulator());
			outlineList.setDefaultRenderer(Object.class, new vpTableRendering(DEFAULT_ROW_HEIGHT, SELECTED_ROW_HEIGHT));
			outlineList.getColumnModel().getColumn(0).setCellEditor(new OutlineStringInput());
			outlineList.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(videoSelect));
			outlineList.getColumnModel().getColumn(2).setCellEditor(new TimeSetter());
			outlineContainer = new JScrollPane(outlineList);
			myWindow.add(outlineContainer, BorderLayout.EAST);
		}
		else{
			//restoring a saved window
			myWindow.setVisible(true);
			j.dispose();
		}
	}

	//this saves the add-on to the project file in XML format
	public String saveToString(){
		String saveString = "<style>" + vpDocLoader.rewrite(myCSS) + "</style>";
		boolean inFamily = false;
		
		for(int i=0; i<outlineData.size(); i++){
			OutlineItem currentItem = outlineData.elementAt(i);
			String vidURL = "blank";
			VideoListing currentVid = currentItem.getVideo();
			if(currentVid != null){
				if(currentVid != vpVideoSelector.blankVideo){
					vidURL = currentVid.getURL();
				}
			}
			String currentLabel = currentItem.getLabel();
			//store values in XML
			currentLabel = vpDocLoader.rewrite(currentLabel);
			if((i < outlineData.size() - 1)&&(!inFamily)){
				if(outlineData.elementAt(i+1).isChild()){
					//a parent item has no special attributes; instead it is simply not an empty element
					saveString += ("<item label=\"" + currentLabel + "\" video=\"" + vidURL + "\" start=\"" + currentItem.getStart() + "\">");
					inFamily = true;
				}
				else{
					//items that have no children are represented as empty elements
					saveString += ("<item label=\"" + currentLabel + "\" video=\"" + vidURL + "\" start=\"" + currentItem.getStart() + "\"/>");	
				}
			}
			else
			{
				if(outlineData.elementAt(i).isChild()){
					//all children are represented as empty elements
					saveString += ("<item label=\"" + currentLabel + "\" video=\"" + vidURL + "\" start=\"" + currentItem.getStart() + "\"/>");	
					if(i == outlineData.size() - 1){
						//there are no more elements, so the items list must be closed here
						saveString += "</item>";
					}
				}
				else{
					if(inFamily){
						//child elements have been listed and this marks the first item after this list
						saveString += "</item>";
						inFamily = false;
					}
					
					//now add the non-parent item as an empty element
					if(i < outlineData.size() - 1){
						if(outlineData.elementAt(i+1).isChild()){
							//a parent item has no special attributes; instead it is simply not an empty element
							saveString += ("<item label=\"" + currentLabel + "\" video=\"" + vidURL + "\" start=\"" + currentItem.getStart() + "\">");
							inFamily = true;
						}
						else{
							//items that have no children are represented as empty elements
							saveString += ("<item label=\"" + currentLabel + "\" video=\"" + vidURL + "\" start=\"" + currentItem.getStart() + "\"/>");	
						}
					}
					else{
						//items that have no children are represented as empty elements
						saveString += ("<item label=\"" + currentLabel + "\" video=\"" + vidURL + "\" start=\"" + currentItem.getStart() + "\"/>");
					}
				}
			}			
		}
		//place the information in an outlineData element
		return ("<outlineData>" + saveString + "</outlineData>");
	}
	
	//this loads the add-on from the project file in XML format 
	public void initFromString(String saveData){
		Document saveDoc = vpDocLoader.getXMLfromString(saveData);

		NodeList myStyles = saveDoc.getElementsByTagName("style");
		if(myStyles.getLength() == 1){
			myCSS = vpDocLoader.reread(myStyles.item(0).getTextContent());
		}
		
		NodeList myBullets = saveDoc.getElementsByTagName("item");
		if(myBullets.getLength() > 0){
			boolean isChild = false;
			int childCountoff = 0;
			for(int i=0; i<myBullets.getLength(); i++){
				if(childCountoff > 0){
					//if still counting children, this is a child node
					isChild = true;
					childCountoff--;
				}
				else{
					//regular, possibly parent node
					isChild = false;
					if(myBullets.item(i).hasChildNodes()){
						//parent node, trigger child countoff
						childCountoff = myBullets.item(i).getChildNodes().getLength();
					}
				}
				//Send attributes as strings to the OutlineItem constructor
				//this allows OutlineItem to parse any time format it can recognize
				NamedNodeMap nnm = myBullets.item(i).getAttributes();
				String name = nnm.getNamedItem("label").getNodeValue();
				//restore values from XML
				name = vpDocLoader.reread(name);
				if(name.equals("null")){ name = ""; }
				String video = nnm.getNamedItem("video").getNodeValue();
				String time = nnm.getNamedItem("start").getNodeValue();
				outlineData.add(new OutlineItem(name, video, time, isChild));
			}
		}
	}
	
	//the video is selected from a combobox list of names
	protected class VideoSetter extends vpVideoSelector{
		
		public VideoSetter(){
			//include both (same) and null options
			super(true, true);
		}
		
		//refresh populates the item list with video names
		public void refresh(){
			super.refresh();
			//the first option will make the same selection
			if(vpMain.videoData.size() > 0){
				if(vpMain.videoData.size() == 1){
					//if there is only one video, the default video is that video
					defaultVideo = vpMain.videoData.firstElement();
				}
				else{
					if(outlineList.getSelectedRow() != -1){
						//if there is a selected item, the default video is that item's video
						VideoListing v = outlineData.elementAt(outlineList.getSelectedRow()).getVideo();
						defaultVideo = v;
					}
					else{
						if(outlineData.size() > 0){
							//no item is selected, so use the default video is that of the item before this
							for(int i=outlineData.size() - 1; i>=0; i--){
								if(!outlineData.elementAt(i).getVideo().equals(blankVideo)){
									defaultVideo = outlineData.elementAt(i).getVideo();
									break;
								}
							}
						}
					}
				}
			}
			else{
				this.setSelectedItem(null);
				this.setEnabled(false);
			}
		}
	}
	
	protected class TimeSetter extends vpTimeInput{
		public Component getTableCellEditorComponent(JTable t, Object value,
				boolean isSelected, int row, int col) {
			float mytime = outlineData.elementAt(row).getStart();
			return super.getTableCellWithTime(mytime);
		}
	}
	
	//the TableRegulator class defines the table's functionality
	protected class TableRegulator extends AbstractTableModel{
		
		//adding columns:  set the length of categories and add more names to the categories array
		private String[] categories = new String[3];
		public TableRegulator(){
			categories[0] = "Item Name";
			categories[1] = "Video Name";
			categories[2] = "Start Time";
		}
		public boolean isCellEditable(int row, int col)
		{
			//all columns are editable
			if(col == 1){
				//the VideoSetter should refresh videos from the list
				videoSelect.refresh();
			}
			return true;
		}
		//stores a cell editor's value
		public void setValueAt(Object value, int row, int col) {
			//allows direct changes in the name and video columns
			if(value != null){
				try{
				if(col == 0){
					outlineData.elementAt(row).setLabel(value.toString());
				}
				if(col == 1){
					if(value.toString().equals(" ")){
						//selected null
						outlineData.elementAt(row).setVideo(null);
					}
					else{
						VideoListing v = (VideoListing) value;
						if(v != vpVideoSelector.blankVideo){
							//changing to a different video
							outlineData.elementAt(row).setVideo(v);
						}
					}
				}
				if(col == 2){
					outlineData.elementAt(row).setStart(Float.parseFloat(value.toString()));
				}
				fireTableCellUpdated(row, col);
				}
				catch(Exception e){}
			}
		}
		
		//using OutlineItem's methods to get ready-to-display values
		public Object getValueAt(int row, int col) {
			if(col == 0){
				return outlineData.elementAt(row).toString();
			}
			if(col == 1){
				VideoListing v = outlineData.elementAt(row).getVideo();
				if(v == null){
					return "";
				}
				else{
					return v.getName();
				}
			}
			if(col == 2){
				return outlineData.elementAt(row).displayTime();
			}
			return null;
		}
		
	    public String getColumnName(int col) {
	        return categories[col];
	    }
		public int getColumnCount() {
			return categories.length;
		}
		public int getRowCount() {
			return outlineData.size();
		}
	}
	
	protected class OutlineStringInput extends vpStringInput {
		public Component getTableCellEditorComponent(JTable t, Object cellItem, boolean arg2, int row, int col) {
			editor.setText(t.getModel().getValueAt(row,col).toString().replace(childHeading, ""));
			return scrollPane;
		}
	}
	
	protected class OutlineItem {
		protected String label;  //label stores the item text, without display notations
		protected VideoListing myVideo;  //the videoURL is currently the key in YouTube.com/watch?v=KEY
		protected float startTime; //startTime stores the time in seconds, without display notations
		protected boolean isChild;  //child items have a parent item above them
		
		//the default constructor is called by the New Branch button
		public OutlineItem(){
			label = defaultItem; //the label of an unedited item
			myVideo = defaultVideo;  //set by the program if the item is likely to be from a certain video
			startTime = 0;
			isChild = false;
		}
		
		//this constructor is used when loading from the project file
		public OutlineItem(String name, String url, String time, boolean child){
			this.setLabel(name);
			VideoListing foundVideo = vpVideoSelector.blankVideo;
			for(int i=0; i<vpMain.videoData.size(); i++){
				if(vpMain.videoData.elementAt(i).getURL().equals(url)){
					foundVideo = vpMain.videoData.elementAt(i);
					break;
				}
			}
			this.setVideo(foundVideo);
			this.setStart(Float.parseFloat(time));
			this.setChildFlag(child);
		}
		
		//this is the value that displays in the Name column
		public String toString(){
			String cellText = this.getLabel();
			if(isChild){
				//child labels have a special notation
				cellText = childHeading + cellText;
			}
			/**Other values could be added to the cell text:
			 * cellText = cellText + " (" + displayTime() + ")";
			 * 
			 * this sample makes the cell display the item label followed by the display time in parentheses
			 * ItemName (0:05:00)
			 * --) ChildItem (0:08:33)
			 */
			return cellText;
		}
		
		//this converts the stored time (a float value in seconds) to the displayed hours, minutes, and seconds
		//format is h:mm:ss or hh:mm:ss
		protected String displayTime(){
			float time = getStart();
			int hours = (int)(time / 3600);
			int minutes = (int)((time - 3600 * hours) / 60);
			int seconds = (int)(time - 3600 * hours - 60 * minutes);
			String printString = "";
			if(hours > 0){ printString = hours + ":"; }
			if(minutes < 10){ printString += "0"; }
			printString += (minutes + ":");
			if(seconds < 10){ printString += "0"; }
			printString += seconds;
			return printString;
		}
		
		public String getLabel(){ return label; }
		public void setLabel(String l){ label = l; }
		public VideoListing getVideo(){ return myVideo; }
		public void setVideo(VideoListing v){ myVideo = v; }
		public float getStart(){ return startTime; }
		public void setStart(float s){ startTime = s; }
		public boolean isChild(){ return isChild; }
		public void setChildFlag(boolean c){ isChild = c; }
	}

	public String[] getCSSOptions() {
		String[] opts = {plainCSS, highlightCSS, texturedhighlightCSS};
		return opts;
	}

	public String[] getOptionNames() {
		return CSSdefaultLabels;
	}

	public void setCurrentCSS(String set) { myCSS = set; }

	public String getCurrentCSS() { return myCSS; }
}