import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//The Profiles add-on displays a list of key terms / names at specified times in videos
//Clicking a word pauses the video and opens a profile window with information; any links open in a new window
//Best uses: profile people in video, explain terms when they are mentioned, link to more detailed information

public class Profiles extends Supplement {
	
	//font used for input
	protected static Font editFont = new Font("Arial", Font.PLAIN, 14);
	
	//define window elements
	protected JFrame myWindow;
	protected JTabbedPane modeTabs;
	protected CSSEditor tagCSSWindow;
	protected CSSEditor profileCSSWindow;
	protected JPanel profilePanel;
	protected JTextField profileNameField;
	protected JPanel profileListPanel;
	protected JList profileList;
	protected JTextArea profileHTML;
	protected JComboBox tagStyleSelect = new JComboBox();
	protected JTable timesTable;
	
	//profile data
	protected Vector<ProfileRecord> profileData = new Vector<ProfileRecord>();
	
	//information on the profile being edited
	protected int openProfileIndex = 0;
	protected Vector<String[]> timeData = new Vector<String[]>();	

	//CSS styles
	protected static String basicTagCSS =
		"#tag{\r\n" +
		"	/* draw a small box around the profile name */\r\n" +
		"	font-family: arial;\r\n" +
		"	padding: 5px;\r\n" +
		"	border: 1px solid black;\r\n" +
		"}\r\n" +
		"#tag a{\r\n" +
		"	/* keeps tag link from being blue and underlined */\r\n" +
		"	text-decoration: none;\r\n" +
		"}\r\n";
	protected ProfileTagStyle basicTagStyle = new ProfileTagStyle("Basic Style",
			basicTagCSS);
	
	public String getDescription() {
		return "<html><p>Profiles helps you place a list of relevant people, places, topics, and items while they appear in your videos.  Clicking on a tag displays a profile over your page.</p><br/><p>You can use this to have detailed bios for people in the videos, define words, or link to relevant pages.</p></html>";
	}
	public int refreshType(){ return EACH_REFRESHES; }
	
	public String getClassScripts(){
		return
		//Use YUI libraries to make modal (pop-up) windows
		"<!-- The Yahoo! UI Library does the difficult work behind the centered (modal) windows -->\r\n" +
		"<link rel=\"stylesheet\" type=\"text/css\" href=\"http://yui.yahooapis.com/2.6.0/build/container/assets/skins/sam/container.css\" />\r\n" +
		"<script type=\"text/javascript\" src=\"http://yui.yahooapis.com/2.6.0/build/utilities/utilities.js\"></script>\r\n" + 
		"<script type=\"text/javascript\" src=\"http://yui.yahooapis.com/2.6.0/build/container/container-min.js\"></script>\r\n";
	}
	
	public String getLoadScript(String myID){
		//the page must set to accept YUI layouts
		//(I don't understand how this works, but it is necessary for the pop-up effect)
		return myID + "_initProfiles();\r\n" +
			   "/* the page's body is set to accept YUI layouts used by Profiles */\r\n" +
			   "document.body.className = \" yui-skin-sam\";\r\n";
	}
		
	public String getIndividualScripts(String myID){
		String buildScript =
		"<script type=\"text/javascript\">\r\n" +
		"/* for pop-up window effect, use YUI namespace */\r\n" +
		"YAHOO.namespace(\"" + myID + ".container\");\r\n" +
		"/* init arrays with information about the profiles */\r\n" +
		"/* note: times are stored in this form: start_1,end_1,start_2,end_2... */\r\n" +
		"var " + myID + "_profileName = new Array();\r\n" +
		"var " + myID + "_tagStyleName = new Array();\r\n" +
		"var " + myID + "_profileText = new Array();\r\n" +
		"var " + myID + "_times = new Array();\r\n" +
		"var " + myID + "_shownProfiles = new Array();\r\n" +
		"function " + myID + "_initProfiles(){\r\n";
		for(int p=0; p<profileData.size(); p++){
			try{
				String profileName = profileData.elementAt(p).getName();
				String styleName = profileData.elementAt(p).getTagStyle().getName().replace(" ", "");
				Vector<String[]> savetimes = profileData.elementAt(p).getTimeData();
				String currentVid = savetimes.elementAt(0)[0];
				String timeArray = "\"v:" + currentVid + "\"";
				for(int t=0; t<savetimes.size(); t++){
					int startTime = Integer.parseInt(savetimes.elementAt(t)[1]);
					int endTime = Integer.parseInt(savetimes.elementAt(t)[2]);
					if(startTime < endTime){
						if(!currentVid.equals(savetimes.elementAt(t)[0])){
							//reading times from the next video
							currentVid = savetimes.elementAt(t)[0];
							timeArray += ",-1,\"v:" + currentVid + "\""; 
						}
						timeArray += "," + savetimes.elementAt(t)[1] + "," + savetimes.elementAt(t)[2];
					}
				}
 				buildScript += "	" + myID + "_addProfile(\"" + profileName.replace("\\", "&#92;").replace("\"", "&quot;") + "\", \"" + styleName.replace("\\", "&#92;").replace("\"", "&quot;") + "\", new Array(" + timeArray + "));\r\n";
			}catch(Exception e){}
		}
		buildScript +=
		"}\r\n" +
		"function " + myID + "_addProfile(name, styleName, sortedTimes){\r\n" +
		"	" + myID + "_profileName.push(name);\r\n" +
		"	" + myID + "_tagStyleName.push(styleName);\r\n" +
		"	" + myID + "_times.push(sortedTimes);\r\n" +
		"}\r\n" +
		"/* every quarter-second, update the list of profiles */\r\n" +
		"function " + myID + "_update(){\r\n" +
		"	/* make a list of profiles that should be visible */\r\n" +
		"	visibleProfiles = new Array();\r\n" +
		"	for(i=0; i<" + myID + "_profileName.length; i++){\r\n" +
		"		videoMatch = false;\r\n" +
		"		for(j=0; j<(" + myID + "_times[i]).length; j++){\r\n" +
		"			if(!videoMatch){\r\n" +
		"				/* waiting to read times from the current video */\r\n" +
		"				if((" + myID + "_times[i])[j] == \"v:\" + nowVideoUrl){" +
		"					/* start reading from next entry */\r\n" +
		"					videoMatch = true;\r\n" +
		"				}\r\n" +
		"			}\r\n" +
		"			else{\r\n" +
		"				if((" + myID + "_times[i])[j] > nowTime){\r\n" +
		"					if((j%2) == 0){\r\n" +
		"						/* the next change will be to end, so this profile is listed */\r\n" +
		"						visibleProfiles[visibleProfiles.length] = ((" + myID + "_times[i])[j]*1000) + i; \r\n" +
		"					}\r\n" +
		"					/* profile added, end checks on this profile */\r\n" +
		"					break;\r\n" +
		"				}\r\n" +
		"				else{\r\n" +
		"					if((" + myID + "_times[i])[j] == -1){\r\n" +
		"						/* at end of times, go to next profile */\r\n" +
		"						break;\r\n" +
		"					}\r\n" +
		"				}\r\n" +
		"			}\r\n" +
		"		}\r\n" +
		"	}\r\n" +
		"	visibleProfiles = visibleProfiles.sort(function(a,b){return b-a;});\r\n" +
		"	if(visibleProfiles.length == " + myID + "_shownProfiles.length){\r\n" +
		"		if(visibleProfiles.length == 0){ return; }\r\n" +
		"		for(t=0; t<visibleProfiles.length; t++){\r\n" +
		"			if(visibleProfiles[t] != " + myID + "_shownProfiles[t]){\r\n" +
		"				/* tag list has changed */\r\n" +
		"				" + myID + "_buildTagList(visibleProfiles);\r\n" +
		"			}\r\n" +
		"		}\r\n" +
		"	}\r\n" +
		"	else{\r\n" +
		"		/* tag list length has changed */\r\n" +
		"		" + myID + "_buildTagList(visibleProfiles);\r\n" +
		"	}\r\n" +
		"}\r\n" +
		"function " + myID + "_buildTagList(visibleProfiles){\r\n" +
		"	/* write and display the given (updated) list of tags to display */\r\n" +
		"	buildPage = \"\";\r\n" +
		"	for(i=0; i<visibleProfiles.length; i++){\r\n" +
		"		buildProfile = 1*((visibleProfiles[i]+\"\").substring((visibleProfiles[i]+\"\").length - 3));\r\n" +
		"		buildPage += \"<div class='" + myID + "_\" + " + myID + "_tagStyleName[buildProfile] + \"_tag'><a href='javascript:void(0);' onclick='" + myID + "_showProfile(\" + buildProfile + \");'>\" + " + myID + "_profileName[buildProfile] + \"</a></div>\";\r\n" +
		"	}\r\n" +
		"	document.getElementById(\"" + myID + "_profileTags\").innerHTML = buildPage;\r\n" +
		"	" + myID + "_shownProfiles = visibleProfiles;\r\n" +
		"}\r\n" +
		"/* clicking on a tag calls showProfile */\r\n" +
		"function " + myID + "_showProfile(profilenum){\r\n" +
		"	if(player){\r\n" +
		"		/* pause the video so a profile window can be read */\r\n" +
		"		player.pauseVideo();\r\n" +
		"	}\r\n" +
		"	/* use YUI to build the pop-up window for the profile */\r\n" +
		"	" + myID + "_profile = new YAHOO.widget.Panel(\"" + myID + "_profile\",\r\n" +
		"		{ width: \"400px\",\r\n" +
		"		  close: true,\r\n" +
		"		  draggable: false,\r\n" +
		"		  modal: true,\r\n" +
		"		  visible: true\r\n" +
		"		});\r\n" +
		"	" + myID + "_profile.setHeader(" + myID + "_profileName[profilenum]);\r\n" +
		"	" + myID + "_profile.setBody(document.getElementById(\"" + myID + "_profile_\" + profilenum).innerHTML);\r\n" +
		"	/* the box will appear on the popupPlacement div element above the tag list */\r\n" +
		"	" + myID + "_profile.render(document.getElementById(\"" + myID + "_popupPlacement\"));\r\n" +
		"}\r\n" +
		"</script>\r\n";
		return buildScript;
	}

	public String getHTML(String myID) {
		//make an anchor for popups and open a div element to store all of the profile tags
		String buildString = "<div id=\"" + myID + "_popupPlacement\"></div>\r\n<div id=\"" + myID + "_profileTags\">\r\n</div>\r\n";
		//store tag styles
		buildString += "<style type=\"text/css\">\r\n";
		for(int s=0; s<tagStyleSelect.getItemCount(); s++){
			ProfileTagStyle thisTagStyle = (ProfileTagStyle) tagStyleSelect.getItemAt(s);
			buildString += thisTagStyle.getCurrentCSS().replace("#tag", "div." + myID + "_" + thisTagStyle.toString().replace(" ", "") + "_tag");
		}
		buildString += "</style>\r\n";
		int profileIndex = 0;
		for(int profile=0; profile < profileData.size(); profile++){
			try{
				ProfileRecord thisProfile = profileData.elementAt(profile);
				String profileName = thisProfile.getName();
				//the profile text is stored in an invisible page div element
				buildString += "<!--Hidden profile for " + profileName + "-->\r\n";
				buildString += "<div id=\"" + myID + "_profile_" + profileIndex + "\" style=\"display:none;\">\r\n";
				//using target="_blank" makes all links open in a new page
				buildString += thisProfile.getProfileText().replace("<a", "<a target=\"_blank\"");
				buildString += "\r\n</div>\r\n";
				profileIndex++;
			}
			catch(Exception e){}
		}
		return buildString;
	}

	public void initFromString(String saveData) {
		Document loadDoc = vpDocLoader.getXMLfromString(saveData);
		NodeList tagStyles = loadDoc.getElementsByTagName("tagStyle");
		if(tagStyles.getLength() > 0){
			tagStyleSelect.removeAllItems();
		}
		for(int s=0; s < tagStyles.getLength(); s++){
			//load the tag styles
			String name = vpDocLoader.reread(tagStyles.item(s).getAttributes().getNamedItem("name").getTextContent());
			String css = vpDocLoader.reread(tagStyles.item(s).getTextContent());
			new ProfileTagStyle(name,css);
		}
		NodeList profileList = loadDoc.getElementsByTagName("profile");
		for(int p=0; p < profileList.getLength(); p++){
			//load all of the profiles
			Node currentProfile = profileList.item(p);
			String profileName = vpDocLoader.reread(currentProfile.getAttributes().getNamedItem("name").getTextContent());
			String tagStyleName = vpDocLoader.reread(currentProfile.getAttributes().getNamedItem("tagStyle").getTextContent());
			NodeList profileData = currentProfile.getChildNodes();
			String profileContent = vpDocLoader.reread(profileData.item(0).getTextContent());
			NodeList showTimes = profileData.item(1).getChildNodes();
			Vector<String[]> times = new Vector<String[]>();
			for(int t=0; t<showTimes.getLength(); t++){
				//load the display times (video, start, and end)
				String[] newArray = { showTimes.item(t).getAttributes().getNamedItem("video").getTextContent(), showTimes.item(t).getAttributes().getNamedItem("start").getTextContent(), showTimes.item(t).getAttributes().getNamedItem("end").getTextContent()};
				times.add(newArray);
			}
			new ProfileRecord(profileName, profileContent, tagStyleName, times);
		}
	}

	public String saveToString() {
		String saveString = "<tagStyleList>";
		//the tag styles are collected and stored for use in any of the profiles
		for(int style=0; style < tagStyleSelect.getItemCount(); style++){
			saveString += "<tagStyle name=\"" + vpDocLoader.rewrite(((ProfileTagStyle) tagStyleSelect.getItemAt(style)).toString()) + "\">";
			saveString += vpDocLoader.rewrite(((ProfileTagStyle) tagStyleSelect.getItemAt(style)).getCurrentCSS());
			saveString += "</tagStyle>";
		}
		saveString += "</tagStyleList>";
		saveString += "<profiles>";
		for(int item=0; item < profileData.size(); item++){
			//save each profile's information
			ProfileRecord currentProfile = profileData.elementAt(item);
			saveString += "<profile name=\"" + vpDocLoader.rewrite(currentProfile.getName()) + "\" tagStyle=\"" + vpDocLoader.rewrite(currentProfile.getTagStyle().toString()) + "\">";
			saveString += "<profileContent>" + vpDocLoader.rewrite(currentProfile.getProfileText()) + "</profileContent>";
			saveString += "<showTimes>";
			Vector<String[]> currentTimeData = currentProfile.getTimeData();
			for(int timeset = 0; timeset < currentTimeData.size(); timeset++){
				//each display time is stored as a "timeset" with video, start, and end attributes
				String[] currentTimes = currentTimeData.elementAt(timeset);
				saveString += "<timeset video=\"" + currentTimes[0] + "\" start=\"" + currentTimes[1] + "\" end=\"" + currentTimes[2] + "\"/>";
			}
			saveString += "</showTimes>";
			saveString += "</profile>";
		}
		saveString += "</profiles>";
		return "<profilesData>" + saveString + "</profilesData>";
	}

	public void setWindow(JFrame j) {
		if(myWindow == null){
			myWindow = j;
			myWindow.setTitle("Profiles: " + this.getName());
			myWindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			myWindow.setSize(600,550);
			myWindow.setVisible(true);
			myWindow.getContentPane().setLayout(new BoxLayout(myWindow.getContentPane(), BoxLayout.Y_AXIS));
					
			JPanel searchPanel = new JPanel();
				//the profileNameField allows the user to switch between multiple profiles and get help on selecting one
				//when changing profiles, the view will switch to the list of all profiles
				//after choosing a profile, the user is allowed to edit the profile
				searchPanel.add(new JLabel("Profile for:"));
				profileNameField = new JTextField();
					profileNameField.setColumns(20);
					profileNameField.setFont(editFont);
				final JButton gotoProfile = new JButton("Create");
					gotoProfile.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							if(gotoProfile.getText().equals("Create")){
								profileData.add(new ProfileRecord(profileNameField.getText().trim()));
								profileList.setListData(profileData);
								loadProfile(profileData.size() - 1);
								gotoProfile.setText("Current");
								gotoProfile.setEnabled(false);
								modeTabs.setEnabled(true);
								modeTabs.setSelectedIndex(1);
							}
							else{
								if(gotoProfile.getText().equals("View")){
									String searchTerm = profileNameField.getText().trim().toLowerCase();
									for(int i=0; i<profileData.size(); i++){
										if(profileData.elementAt(i).getName().toLowerCase().equals(searchTerm)){
											loadProfile(i);
											gotoProfile.setText("Current");
											gotoProfile.setEnabled(false);
											modeTabs.setSelectedIndex(1);
											return;
										}
									}
								}
							}
						}
					});
				profileNameField.addKeyListener(new KeyListener(){
					public void keyReleased(KeyEvent e) {
						String nameCheck = profileNameField.getText().trim().toLowerCase();
						for(int i=(profileData.size() - 1); i >= 0; i--){
							if(profileData.elementAt(i).getName().toLowerCase().equals(nameCheck)){
								if(i == openProfileIndex){
									gotoProfile.setText("Current");
									gotoProfile.setEnabled(false);
									modeTabs.setSelectedIndex(1);
								}
								else{
									gotoProfile.setEnabled(true);
									gotoProfile.setText("View");
									profileList.setListData(profileData);
									modeTabs.setSelectedIndex(0);
								}
								return;
							}
						}
						gotoProfile.setEnabled(true);
						gotoProfile.setText("Create");
						profileList.setListData(profileData);
						modeTabs.setSelectedIndex(0);
					}
					public void keyPressed(KeyEvent e) {}
					public void keyTyped(KeyEvent e) {}	
				});
				searchPanel.add(profileNameField);	
				searchPanel.add(gotoProfile);
				searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.LINE_AXIS));
			myWindow.add(searchPanel);
			
			//tab listing
			modeTabs = new JTabbedPane();
			modeTabs.addChangeListener(new ChangeListener(){
				public void stateChanged(ChangeEvent e) {
					if(modeTabs.getSelectedIndex() == 1){
						//when switching to edit, make sure the name shown matches the open profile
						profileNameField.setText(profileData.elementAt(openProfileIndex).getName());
					}
				}
			});
				
			//listing of all profiles
			profileListPanel = new JPanel();
				profileListPanel.setLayout(new BoxLayout(profileListPanel, BoxLayout.Y_AXIS));
				profileList = new JList(profileData);
				profileList.addListSelectionListener(new ListSelectionListener(){
					public void valueChanged(ListSelectionEvent e) {
						if(profileList.getSelectedIndex() > -1){
							profileNameField.setText(profileList.getSelectedValue().toString());
							loadProfile(profileList.getSelectedIndex());
							gotoProfile.setEnabled(true);
							gotoProfile.setText("View");
						}
					}
				});
				profileList.setVisibleRowCount(15);
				profileListPanel.add(new JScrollPane(profileList));
			modeTabs.add("List Profiles", profileListPanel);
			
			profilePanel = new JPanel();
			profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
				//profile text editor
				JPanel itemPanel = new JPanel();
					itemPanel.add(new JLabel("Profile text (HTML allowed):"));
					profileHTML = new JTextArea();
						profileHTML.setRows(7);
						profileHTML.setColumns(35);
						profileHTML.setFont(editFont);
						profileHTML.addKeyListener(new KeyListener(){
							public void keyReleased(KeyEvent e) {
								profileData.elementAt(openProfileIndex).setProfileText(profileHTML.getText().trim());
							}
							public void keyPressed(KeyEvent e) {}
							public void keyTyped(KeyEvent e) {}
						});
					itemPanel.add(new JScrollPane(profileHTML));
					itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
				profilePanel.add(itemPanel);
				
				JPanel timePanel = new JPanel();
				timePanel.setLayout(new BorderLayout());
				JPanel buttons = new JPanel();				
				//adding an item
				JButton branch = new JButton("Add Times");
				branch.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						String[] starterArray = {"", "0", "0"};
						timeData.add(starterArray);
						timesTable.tableChanged(new TableModelEvent(timesTable.getModel()));
					}
				});
				buttons.add(branch);
				buttons.add(Box.createVerticalGlue());
				
				//delete the selected item
				JButton cutbranch = new JButton("Delete");
				cutbranch.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						if(timesTable.getSelectedRow() != -1){
							timeData.removeElementAt(timesTable.getSelectedRow());
							timesTable.tableChanged(new TableModelEvent(timesTable.getModel()));
						}
					}				
				});
				buttons.add(cutbranch);
				buttons.add(Box.createVerticalGlue());
				
				//move an item up on the list
				JButton moveUp = new JButton("<html><span style='font-size: 16pt;'>&uarr;</span></html>");
				moveUp.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						int selection = timesTable.getSelectedRow();
						//the zero-th item cannot be moved up
						if(selection > 0){
							String[] moveItem = timeData.elementAt(selection);
							timeData.removeElementAt(selection);
							timeData.insertElementAt(moveItem, selection-1);
							//update row numbers for selected and in-editing items
							timesTable.setRowSelectionInterval(selection-1, selection-1);
							timesTable.setEditingRow(selection-1);
							timesTable.repaint();
						}
					}
				});
				buttons.add(moveUp);
				
				//moving an item down on the list
				JButton moveDown = new JButton("<html><span style='font-size: 16pt;'>&darr;</span></html>");
				moveDown.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						int selection = timesTable.getSelectedRow();
						//the last item cannot be moved up
						if(selection < timeData.size() - 1){
							String[] moveItem = timeData.elementAt(selection);
							timeData.removeElementAt(selection);
							timeData.insertElementAt(moveItem, selection+1);
							//update row numbers for selected and in-editing items
							timesTable.setRowSelectionInterval(selection+1, selection+1);
							timesTable.setEditingRow(selection+1);
							timesTable.repaint();
						}
					}
				});
				buttons.add(moveDown);					
				buttons.setLayout(new BoxLayout(buttons, BoxLayout.PAGE_AXIS));	
				timePanel.add(buttons, BorderLayout.WEST);
					
				//videoEdit is a ComboBox that allows the user to select videos
				final vpVideoSelector videoEdit = new vpVideoSelector();
				//Setting the appearance and workings of the display-time table
				TableModel times = new AbstractTableModel(){
					//define table appearance
					public int getColumnCount() { return 3; }
					public int getRowCount() { return timeData.size(); }
					public String getColumnName(int col){
						if(col == 0){ return "During"; }
						if(col == 1){ return "Show at"; }
						return "Hide at";
					}
					public boolean isCellEditable(int row, int col){ return true; }
					public Object getValueAt(int row, int col) {
						if(col > 0){
								//return the stored time
								return displayTime(Float.parseFloat(timeData.elementAt(row)[col]));
							}
							else{
								//return the stored video
								videoEdit.refresh();
								String videoURL = timeData.elementAt(row)[col];
								for(int v=0; v < vpMain.videoData.size(); v++){
									if(vpMain.videoData.elementAt(v).getURL().equals(videoURL)){
										return vpMain.videoData.elementAt(v);
									}
								}
								return null;
							}
						}
						public void setValueAt(Object value, int row, int col) {
							try{
								if(col > 0){
									//store the start or end time
									timeData.elementAt(row)[col] = "" + ((int) Float.parseFloat(value.toString()));
								}
								else{
									//store the URL of the selected video
									VideoListing selectVideo = (VideoListing) value;
									if(!selectVideo.equals(vpVideoSelector.blankVideo)){
										timeData.elementAt(row)[col] = selectVideo.getURL();
									}
								}
							}
							catch(Exception e){}
							finally{
								profileData.elementAt(openProfileIndex).setTimeData(timeData);
							}
						}
						//this converts the stored time (a float value in seconds) to the displayed hours, minutes, and seconds
						//format is h:mm:ss or hh:mm:ss
						protected String displayTime(float time){
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
					};
					timesTable = new JTable(times);
					//set table controls and operation
					timesTable.setDefaultRenderer(Object.class, new vpTableRendering(30,30));
					timesTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(videoEdit));
					timesTable.getColumnModel().getColumn(1).setCellEditor(new ProfilesTimeInput());
					timesTable.getColumnModel().getColumn(2).setCellEditor(new ProfilesTimeInput());
					timesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					timePanel.add(new JScrollPane(timesTable), BorderLayout.CENTER);
				profilePanel.add(timePanel);	
				
				//style-editor panel
				JPanel stylePanel = new JPanel();
				stylePanel.setLayout(new GridLayout(2,1,10,10));
					JPanel tagStylePanel = new JPanel();
					//label explaining the selection
					tagStylePanel.add(new JLabel("Tag style:"));
					//ComboBox allowing a style selection
					tagStyleSelect.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							tagCSSWindow = null;
							profileData.elementAt(openProfileIndex).setTagStyle((ProfileTagStyle) tagStyleSelect.getSelectedItem());
						}
					});
					tagStylePanel.add(tagStyleSelect);
					//button editing the style
					JButton styleButton = new JButton("Edit Style");
					styleButton.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							if(tagCSSWindow == null){
								if(tagStyleSelect.getSelectedItem() != null){
									tagCSSWindow = new CSSEditor((ProfileTagStyle) tagStyleSelect.getSelectedItem());
									tagCSSWindow.editStyle();
								}
							}
							else{
								tagCSSWindow.editStyle();
							}
						}
					});
					tagStylePanel.add(styleButton);
					tagStylePanel.setLayout(new BoxLayout(tagStylePanel, BoxLayout.LINE_AXIS));
					stylePanel.add(tagStylePanel);
				profilePanel.add(stylePanel);
			modeTabs.add("Editor", profilePanel);
			
			JPanel styleEditPanel = new JPanel();
				styleEditPanel.add(new JLabel("<html>" +
						"These styles set the appearance of the 'tags' which appear with profile names.<br/>" +
						"A profile's appearance should be set in the Edit tab.<br/><br/>" +
						"Tag styles could make links to people, vocabulary, and other categories look different." +
						"</html>"));
				styleEditPanel.add(new JLabel("Add a style named:"));
				final JTextField styleNameField = new JTextField();
				final JButton addStyle = new JButton("Add Style");
					styleNameField.setColumns(20);
					styleNameField.setFont(editFont);
					styleNameField.addKeyListener(new KeyListener(){
						public void keyReleased(KeyEvent e) {
							String nStyle = styleNameField.getText().trim().toLowerCase();
							for(int s=0; s<tagStyleSelect.getItemCount(); s++){
								if(nStyle.equals(((ProfileTagStyle)tagStyleSelect.getItemAt(s)).toString().toLowerCase())){
									//style names cannot repeat
									styleNameField.setBackground(vpMain.failColor);
									addStyle.setEnabled(false);
									return;
								}
							}
							if(nStyle.length() < 2){
								//style names must be at least two letters
								styleNameField.setBackground(vpMain.failColor);
								addStyle.setEnabled(false);
								return;
							}
							styleNameField.setBackground(vpMain.validColor);
							addStyle.setEnabled(true);
						}
						public void keyPressed(KeyEvent e) {}
						public void keyTyped(KeyEvent e) {}		
					});
				styleEditPanel.add(styleNameField);
					addStyle.setEnabled(false);
					addStyle.setFont(editFont);
					addStyle.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							String nStyle = styleNameField.getText().trim();
							new ProfileTagStyle(nStyle, basicTagCSS);
							addStyle.setEnabled(false);
						}
					});
				styleEditPanel.add(addStyle);
				styleEditPanel.setLayout(new BoxLayout(styleEditPanel, BoxLayout.PAGE_AXIS));
			modeTabs.add("Add Styles", styleEditPanel);
			
			myWindow.add(modeTabs);
			
			if(profileData.size() == 0){
				//no profiles yet, view the list panel
				modeTabs.setSelectedIndex(0);
				modeTabs.setEnabled(false);
			}
			else{
				//profiles have been created, show the first one
				loadProfile(0);
				modeTabs.setSelectedIndex(1);
			}
		}
		else{
			//restore the last window
			myWindow.setVisible(true);
			j.dispose();
		}
	}
	
	//switch to a specific profile
	public void loadProfile(int index){
		//prevent table from making any changes while switching
		timesTable.editingStopped(null);
		//set displayed values to those of the selected profile
		ProfileRecord openProfile = profileData.elementAt(index);
		profileNameField.setText(openProfile.getName());
		profileHTML.setText(openProfile.getProfileText());
		openProfileIndex = index;
		tagStyleSelect.setSelectedItem(openProfile.getTagStyle());
		timeData = openProfile.getTimeData();
		//redraw the table of display times
		timesTable.tableChanged(new TableModelEvent(timesTable.getModel()));
	}
	
	//record of a profile and its display times
	protected class ProfileRecord{
		private String myName;
		private String profileText;
		private ProfileTagStyle myTagStyle;
		//timeData is stored as an array of Strings, each in form: { videoURL, startTime, endTime }
		private Vector<String[]> timeData = new Vector<String[]>();
		
		public ProfileRecord(String name){
			//run when making a new profile
			myName = name;
			myTagStyle = basicTagStyle;
			String[] starterArray = {"", "0", "0"};
			timeData.add(starterArray);
		}
		
		public ProfileRecord(String profileName, String profileContent,
			//loading from saved project data
			String tagStyleName, Vector<String[]> times) {
			myName = profileName;
			profileText = profileContent;
			timeData = times;
			for(int s=0; s<tagStyleSelect.getItemCount(); s++){
				if(((ProfileTagStyle) tagStyleSelect.getItemAt(s)).toString().equals(tagStyleName)){
					myTagStyle = (ProfileTagStyle) tagStyleSelect.getItemAt(s);
					break;
				}
			}
			profileData.add(this);
		}
		public String getName(){
			return myName;
		}
		public String toString(){
			return myName;
		}
		public String getProfileText(){
			return profileText;
		}
		public void setProfileText(String p){ profileText = p; }
		public Vector<String[]> getTimeData(){
			return timeData;
		}
		public void setTimeData(Vector<String[]>td){ timeData = td; }
		public ProfileTagStyle getTagStyle(){ return myTagStyle; }
		public void setTagStyle(ProfileTagStyle p){ myTagStyle = p; }
	}
	
	//this class is used to give the time editor access to the start and end times
	protected class ProfilesTimeInput extends vpTimeInput{		
		public Component getTableCellEditorComponent(JTable t, Object value, boolean isSelected, int row, int col) {
			return super.getTableCellWithTime(Integer.parseInt(timeData.elementAt(row)[col]));
		}
	}
	
	//styles can be made for different categories (People, Places)
	protected class ProfileTagStyle implements CSSstyled{
		protected String myName;
		protected String myCSS;
		
		public ProfileTagStyle(String name){
			//run when making a brand new tag style
			myName = name;
			tagStyleSelect.addItem(this);
		}
		public ProfileTagStyle(String name, String setCSS){
			//run when making a tag style with some CSS
			myName = name;
			myCSS = setCSS;
			tagStyleSelect.addItem(this);
		}
		public String toString(){ return myName; }
		public String getName() { return myName; }
		public String[] getCSSOptions() {
			return null;
		}
		public String[] getOptionNames() {
			return null;
		}
		public String getCurrentCSS() { return myCSS; }
		public void setCurrentCSS(String set) { myCSS = set; }
	}
}