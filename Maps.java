import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

//Maps uses the freely-available Microsoft Virtual Earth Map Control SDK
//Official Terms of Use ( http://www.microsoft.com/virtualearth/product/terms.html )
//This limits you to 50,000 requests per day

//You may wish to set up an alternative program using the Google Maps API or some other service
//Virtual Earth was chosen because it does not (as of December 2008) require an API key
//Make sure to edit doTaskNum(j) and getClassScripts()

public class Maps extends Supplement implements CSSstyled {
	
	protected static final String basicStyle =
		"/* Basic Style has a 400x400 map with centered black-on-white captions inside a 3D border */\r\n\r\n" +
		"#map {\r\n" +
		"	/* this directly sets the height and width of the map */\r\n" +
		"	height: 400px;\r\n" +
		"	width: 400px;\r\n" +
		"	/* remove the show-controls property to hide dashboard */\r\n" +
		"	show-controls: true;\r\n" +
		"}\r\n\r\n" +
		"#caption{\r\n" +
		"	font-family: arial;\r\n" +
		"	font-size: 14pt;\r\n" +
		"	font-style: bold;\r\n" +
		"	text-align: center;\r\n" +
		"	border-left-style: outset;\r\n" +
		"	border-right-style: outset;\r\n" +
		"	border-bottom-style: outset;\r\n" +
		"	padding: 3px;\r\n" +
		"}\r\n";
	protected static final String borderlessStyle =
		"/* Borderless Style has a 400x400 map with centered black-on-white captions */\r\n\r\n" +
		"#map {\r\n" +
		"	/* this directly sets the height and width of the map */\r\n" +
		"	height: 400px;\r\n" +
		"	width: 400px;\r\n" +
		"	/* remove the show-controls property to hide dashboard */\r\n" +
		"	show-controls: true;\r\n" +
		"}\r\n\r\n" +
		"#caption{\r\n" +
		"	font-family: verdana;\r\n" +
		"	font-size: 14pt;\r\n" +
		"	font-style: bold;\r\n" +
		"	text-align: center;\r\n" +
		"}\r\n";
	protected static final String alternativeStyle =
		"/* Alternative Style has a 550x550 map with green-on-black left-aligned captions */\r\n\r\n" +
		"#map {\r\n" +
		"	/* this directly sets the height and width of the map */\r\n" +
		"	height: 550px;\r\n" +
		"	width: 550px;\r\n" +
		"	/* this style does not show the map controls; check other styles for property  */\r\n" +
		"}\r\n\r\n" +
		"#caption{\r\n" +
		"	/* font style */\r\n" +
		"	font-family: trebuchet ms;\r\n" +
		"	font-size: 12pt;\r\n" +
		"	color: green;\r\n\r\n" +
		"	/* background style */\r\n" +
		"	background-color: black;\r\n" +
		"	text-align: left;\r\n" +
		"	padding-top: 5px;\r\n" +
		"	padding-bottom: 5px;\r\n" +
		"	padding-left: 10px;\r\n" +
		"}\r\n";
	protected static String[] stylenames = {"Basic", "Borderless", "Alternative"};
	protected static String[] styles = {basicStyle, borderlessStyle, alternativeStyle};
	
	//defining window elements and data
	protected JFrame myWindow;
	protected CSSEditor cssWindow;
	protected Vector<MapEvent> mapData = new Vector<MapEvent>();
	protected JTabbedPane myVideoTabs;
	protected Vector<MapTableTab> myTabs = new Vector<MapTableTab>();
	protected JButton newTab;
	protected MapTimeSetter timeInput = new MapTimeSetter();
	protected vpStringInput stringInput = new vpStringInput();
	protected MapTableRendering myTableRendering = new MapTableRendering(30,45);;
	protected MapTableModel myTableModel = new MapTableModel();
	protected JTable mapList;
	protected String myCSS = basicStyle;
	
	public String getDescription() {
		return "<html><p>The <b>Maps</b> add-on puts interactive maps alongside your video.  You will be able to control the region and content shown on the map.</p><br><p>This is useful not just for showing location, but also for showing region-specific information to the user.</p></html>";
	}
	
	public String getLoadScript(String myID){
		//when the YouTube window is loaded, the API requests a map load
		return (myID + "_loadMap();\r\n");
	}

	public String getHTML(String myID) {
		String outputHTML = "<style>" + myCSS.replace("#map", "#" + myID + "mapdiv").replace("#caption", "#" + myID + "mapcaption") + "</style>";
		//MapDiv will contain the map window
		outputHTML += ("<div id=\"" + myID + "mapdiv\" class=\"" + myID + "mapdiv\" style=\"position: relative;\"></div>\r\n");
		//MapCaption is blank by default, but can contain styled text captions
		outputHTML += "<div id=\"" + myID + "mapcaption\"></div>\r\n";
		return outputHTML;
	}

	//every quarter-second, each instance of Maps gets the update() call
	public int refreshType(){ return Supplement.EACH_REFRESHES; }

	//getClassScripts loads the JavaScript for the specific Map API being used
	public String getClassScripts() {
		return "<script charset=\"UTF-8\" type=\"text/javascript\" src=\"http://dev.virtualearth.net/mapcontrol/mapcontrol.ashx?v=6.2&mkt=en-us\"></script>\r\n";
	}
	
	//this writes the JavaScript functions for each map instance
	public String getIndividualScripts(String myID){
		String script =
			"<script type=\"text/javascript\">\r\n" +
			"var " + myID + "_map;\r\n" + 							//myID_map[] contains the map object
			"var " + myID + "_supportedVideos = new Array();\r\n" +	//myID_supportedVideos[] contains a list of video URLs that are connected to map events
			"var " + myID + "_videoStarts = new Array();\r\n" +		//myID_videoStarts[] contains the first index to use for each of the supported video URLs
			"var " + myID + "_videoURL = \"\";\r\n" +				//myID_videoURL is the current video's URL
			"var " + myID + "_tasks = new Array();\r\n" +			//myID_tasks[] contains each command name
			"var " + myID + "_taskData = new Array();\r\n" +		//myID_taskData[] contains parameters for each command
			"var " + myID + "_startTimes = new Array();\r\n" +		//myID_startTimes[] contains the fire-time of each command
			"var " + myID + "_captions = new Array();\r\n" +		//myID_captions[] contains the captions each command sets
			"var " + myID + "_currentcaption = \"\";\r\n" +			//myID_currentcaption allows the program to know whether the caption needs an update
			"var " + myID + "_isOn = new Array(); \r\n" +			//myID_isOn[] is whether the command has been run (0 = off, 1 = on, 2 = off, but content still not cleared)
			"/* loadMap() creates the map once the YouTube player is loaded */\r\n" +

			//initializing the map (Seattle, Washington ; 2D Map in Shaded style)
			"function " + myID + "_loadMap(){\r\n" +
			"	" + myID + "_map = new VEMap(\"" + myID + "mapdiv\");\r\n";
			if(myCSS.indexOf("show-controls") != -1){
				//style requests map controls: use small version
				script += "	" + myID + "_map.SetDashboardSize(VEDashboardSize.Small);\r\n";
			}
		    script +=
			"	/* This map begins as a Hybrid (satellite / political / roads) view in Seattle */\r\n" +
			"	/* Change the latitude/longitude, or original map style (try Road, Aerial, or Shaded) here*/\r\n" +
			"	" + myID + "_map.LoadMap(new VELatLong(47.6, -122.33, 0, VEAltitudeMode.RelativeToGround), 10, VEMapStyle.Hybrid, false, VEMapMode.Mode2D, true, 1);\r\n";
			if(myCSS.indexOf("show-controls") == -1){
				//style does not request map controls, so hide them
				script += "	" + myID + "_map.HideDashboard();\r\n";
			}
			
			//add each map events
			script += "\r\n/* Add map commands */\r\n";
			for(int tab=0; tab<myVideoTabs.getTabCount() - 1; tab++){
				MapTableTab thisTab = (MapTableTab) myVideoTabs.getComponentAt(tab);
				if(thisTab.getVideo() != null){
					Vector<MapEvent> saveMapData = thisTab.getMapData();
					float lastTime = -1;
					script += ("	" + myID + "_setInputVideo(\"" + thisTab.getVideo().getURL() + "\");\r\n");
					for(int i=0; i<saveMapData.size(); i++){
						MapEvent currentEvent = saveMapData.elementAt(i);
						String command = currentEvent.getCommand().trim();
						if(command != null){
							//checking all commands to make sure they are complete (parameter fits command)
							command = command.toLowerCase();
							String parameter = currentEvent.getParameter();
							if(command.equals("")){ continue; }
							if(command.equals("goto")||command.equals("pushpin")){
								//these commands will not be written without a written parameter
								if(parameter == null){ continue; }
								parameter = parameter.trim();						
								if(parameter == ""){ continue; }
							}
							if(command.equals("goto+pin")){
								//this command will not be written without a written parameter
								if(parameter == null){ continue; }
								parameter = parameter.trim();						
								if(parameter == ""){ continue; }
								command = "gotopin";
							}
							if(command.equals("load kml")){
								//this command will not be written without a written parameter
								if(parameter == null){ continue; }
								parameter = parameter.trim();						
								if(parameter == ""){ continue; }
								command = "gotoloadkml";
							}
							if(command.equals("clear")){
								//clear does not use a parameter
								parameter = "";
							}
							//commands should be separated by 0.3 seconds or more
							//sending commands too quickly WILL result in errors
							if(currentEvent.getTime() < lastTime + 0.3){
								lastTime = (((int)(lastTime + 0.3) * 1000))/1000;
							}
							else{
								lastTime = currentEvent.getTime();
							}
							//The caption can be unchanged (null), characters, or the code [blank]
							//To make this possible, double-quotes must be added in this stage using the code \"
							String caption = currentEvent.getCaption();
							if(caption != null){
								caption = caption.replace("\"", "\\\"");
								caption = "\"" + caption.trim() + "\"";
								if(caption.equals("\"\"")){
									caption = null;
								}
								else{
									if(caption.equals("\"[blank]\"")){ caption = "\"\""; }							
								}
							}
							//writing the critical command-creating line
							script += ("	" + myID + "_addTask(\"" + command + "\",\"" + parameter + "\"," + lastTime + "," + caption + ");\r\n");
						}
					}
				}
			}
		script+= ("}\r\n" +
			//supporting multiple videoURLs
			"/* set the video to support for the next MapEvents */\r\n" +
			"function " + myID + "_setInputVideo(vid){\r\n" +
			"	" + myID + "_supportedVideos[" + myID + "_supportedVideos.length] = vid;\r\n" +
			"	" + myID + "_videoStarts[" + myID + "_videoStarts.length] = " + myID + "_tasks.length;\r\n" +
			"}\r\n" +
			//storing the command information
			"/* addTask stores information about commands in arrays */\r\n" +
			"function " + myID + "_addTask(tasktype, taskinfo, start, caption){\r\n" +
			"	" + myID + "_tasks.push(tasktype);\r\n" +
			"	" + myID + "_taskData.push(taskinfo);\r\n" +
			"	" + myID + "_startTimes.push(start);\r\n" +
			"	" + myID + "_captions.push(caption);\r\n" +
			"	" + myID + "_isOn.push(0);\r\n" +
			"}\r\n");
		script += (
			//running a command-check if the video is supported by Maps
			"/* update is called every quarter-second - maps decides when and how to update */\r\n" +
			"function " + myID + "_update(){\r\n" +
			"	for(var v=0; v<" + myID + "_supportedVideos.length; v++){\r\n" +
			"		if(nowVideoUrl == " + myID + "_supportedVideos[v]){\r\n" +
			"			if(nowVideoUrl != " + myID + "_videoURL){\r\n" +
			"				if(" + myID + "_videoURL != \"\"){\r\n" +
			"					" + myID + "_map.Clear();\r\n" +
			"					document.getElementById(\"" + myID + "mapcaption\").innerHTML = \"\";\r\n" +
			"				}\r\n" +
			"				" + myID + "_videoURL = nowVideoUrl;\r\n" +
			"			}\r\n" +
			"			" + myID + "_runCommands(v);\r\n" +
			"		}\r\n" +
			"	}\r\n" +
			"}\r\n" +
			//checking when and how to run commands
			"function " + myID + "_runCommands(vidIndex){\r\n" +
			"	var hasSetCaption = false;\r\n" +
			"	var checkedRecentClear = false;\r\n" +
			"	var recentCommand = -1;\r\n" +
			"	var checkedRecentGoto = false;\r\n" +
			"	var recentGoto = -1;\r\n" +
			"	var startSearch;\r\n" +
			"	if(vidIndex == (" + myID + "_supportedVideos.length - 1)){\r\n" +
			"		/* last video */\r\n" +
			"		startSearch = (" + myID + "_tasks.length - 1);\r\n" +
			"	}\r\n" +
			"	else{\r\n" +
			"		/* start search at last command set for this video */\r\n" +
			"		startSearch = (" + myID + "_videoStarts[vidIndex + 1] - 1);\r\n" +
			"	}\r\n" +
			"	for(var i=startSearch; i>=" + myID + "_videoStarts[vidIndex]; i--){\r\n" +
			"		if(" + myID + "_startTimes[i] <= nowTime){\r\n" +
			"			if((!checkedRecentClear)&&(" + myID + "_tasks[i] == \"clear\")){\r\n" +
			"				/* this is the most recent clear point */\r\n" +
			"				checkedRecentClear = true;\r\n" +
			"				if(" + myID + "_isOn[i] == 0){\r\n" +
			"					/* execute this clear command */\r\n" +
			"					" + myID + "_isOn[i] = 1;\r\n" +
			"					" + myID + "_map.Clear();\r\n" +
			"					for(var j=" + myID + "_videoStarts[vidIndex]; j < i; j++){\r\n" +
			"						/* all previous commands are undone */\r\n" +
			"						" + myID + "_isOn[j] = 0;\r\n" +
			"					}\r\n" +
			"				}\r\n" +
			"			}\r\n" +
			"			if(" + myID + "_isOn[i] == 0){\r\n" +
			"				if((!checkedRecentClear)&&(recentCommand == -1)&&(" + myID + "_tasks[i].indexOf(\"goto\") == -1)){\r\n" +
			"					/* after the clear is executed, this un-run command is the first to run */\r\n" +
			"					recentCommand = i;\r\n" +
			"				}\r\n" +
			"			}\r\n" +
			"			if((!hasSetCaption)&&(" + myID + "_captions[i] != null)){\r\n" +
			"				/* set the caption */\r\n" +
			"				hasSetCaption = true;\r\n" +
			"				if(" + myID + "_currentcaption != " + myID + "_captions[i]){\r\n" +
			"					document.getElementById(\"" + myID + "mapcaption\").innerHTML = " + myID + "_captions[i];\r\n" +
			"					" + myID + "_currentcaption = " + myID + "_captions[i];\r\n" +
			"				}\r\n" +
			"			}\r\n" +
			"			if(!checkedRecentGoto){\r\n" +
			"				if(" + myID + "_tasks[i].indexOf(\"goto\") != -1){\r\n" +
			"					/* this is the most recent goto */\r\n" +
			"					checkedRecentGoto = true;\r\n" +
			"					if(" + myID + "_isOn[i] != 1){\r\n" +
			"						recentGoto = i;\r\n" +
			"						if(checkedRecentClear){\r\n" +
			"							/* clear has been made, execute goto now */\r\n" +
			"							doTaskNum(i);\r\n" +
			"						}\r\n" +
			"					}\r\n" +
			"					else{\r\n" +
			"						recentGoto = -1 * (i+1);\r\n" +
			"					}\r\n" +
			"				}\r\n" +
			"			}\r\n" +
			"			if(i == " + myID + "_videoStarts[vidIndex]){\r\n" +
			"				/* arrived at beginning without finding any clear commands to execute */\r\n" +
			"				if((checkedRecentGoto)&&(recentGoto >= 0)){\r\n" +
			"					doTaskNum(recentGoto);\r\n" +
			"				}\r\n" +
			"				else{\r\n" +
			"					if(recentCommand != -1){\r\n" +
			"						doTaskNum(recentCommand);\r\n" +
			"					}\r\n" +
			"				}\r\n" +
			"			}\r\n" +
			"			if((checkedRecentClear)&&(hasSetCaption)&&(checkedRecentGoto)){\r\n" +
			"				/* stop loop to continue updates on the next call */\r\n" +
			"				if(recentGoto >= 0){\r\n" +
			"					doTaskNum(recentGoto);\r\n" +
			"				}\r\n" +
			"				else{\r\n" +
			"					if(recentCommand != -1){\r\n" +
			"						doTaskNum(recentCommand);\r\n" +
			"					}\r\n" +
			"				}\r\n" +
			"				i = 0;\r\n" +
			"			}\r\n" +
			"		}\r\n" +
			"		else{\r\n" +
			"			/* these commands haven't run yet, so should be off */\r\n" +
			"			" + myID + "_isOn[i] = 0;\r\n" +
			"		}\r\n" +
			"	}\r\n" +
			"}\r\n" +
		
			//running the given command
			"/* doTaskNum executes the command at the given index */\r\n" +
			"function doTaskNum(j){\r\n" +
			"	var task = " + myID + "_tasks[j];\r\n" +
			"	if(task.indexOf(\"goto\") != -1){\r\n" +
			"		for(var k=0; k < j; k++){\r\n" +
			"			if(" + myID + "_tasks[k].indexOf(\"goto\") != -1){\r\n" +
			"				" + myID + "_isOn[k] = 2;\r\n" +
			"			}\r\n" +
			"		}\r\n" +
			"		if(" + myID + "_isOn[j] == 2){\r\n" +
			"			task = \"goto\";\r\n" +
			"		}\r\n" +
			"	}\r\n" +
			"	" + myID + "_isOn[j] = 1;\r\n" +
			"	if(task == \"goto\"){\r\n" +
			"		/* goto uses Microsoft Virtual Earth's Find method to move to a location */\r\n" +
			"		" + myID + "_map.Find(null, " + myID + "_taskData[j], null, null, 0, 1, false, false, false, true, null);\r\n" +
			"	}\r\n" +
			"	if(task == \"pushpin\"){\r\n" +
			"		/* pushpin uses Microsoft Virtual Earth's Find method to add a pushpin at a location (but not move) */\r\n" +
			"		" + myID + "_map.Find(null, " + myID + "_taskData[j], null, null, 0, 1, false, false, false, false, " + myID + "_MakePin);\r\n" +
			"	}\r\n" +
			"	if(task == \"gotopin\"){\r\n" +
			"		/* gotopin uses Microsoft Virtual Earth's Find method to move to a location and add a pushpin */\r\n" +
			"		" + myID + "_map.Find(null, " + myID + "_taskData[j], null, null, 0, 1, false, false, false, true, " + myID + "_MakePin);\r\n" +
			"	}\r\n" +
			"	if(task == \"gotoloadkml\"){\r\n" +
			"		/* loadkml uses Microsoft Virtual Earth's ImportShapeLayerData method to show maps saved as KML files */\r\n" +
			"		var shapeLayer = new VEShapeLayer();\r\n" +
			"		var layerSpec = new VEShapeSourceSpecification(VEDataType.ImportXML," + myID + "_taskData[j], shapeLayer);\r\n" +
			"		" + myID + "_map.ImportShapeLayerData(layerSpec,null,true);\r\n" +
			"	}\r\n" +
			"}\r\n" +
			
			//helper script to add a pin to the found location
			"/* MakePin uses Microsoft Virtual Earth's AddShape method to load a Pushpin shape */\r\n" +
			"function " + myID + "_MakePin(a,b,c,d,e){\r\n" +
			"	" + myID + "_map.AddShape(new VEShape(VEShapeType.Pushpin, c[0].LatLong));\r\n" +
			"}\r\n" +
			"</script>\r\n");
		return script;
	}

	public String getMiniView() {
		return "[Map: " + this.getName() + "]";
	}

	public void setWindow(JFrame j) {
		if(myWindow == null){
			//generating the window
			myWindow = j;
			j.setTitle("Maps: " + this.getName());
			j.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			myWindow.setSize(600,550);
			myWindow.setVisible(true);
			BorderLayout myLayout = new BorderLayout();
			myWindow.setLayout(myLayout);
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(Box.createRigidArea(new Dimension(10, 30)));
			JPanel buttons = new JPanel();
			buttons.add(Box.createVerticalGlue());
			
			//branch adds a new MapEvent and asks the table to update
			JButton branch = new JButton("Add Event");
			branch.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					mapData.add(new MapEvent());
					mapList.tableChanged(new TableModelEvent(mapList.getModel()));
				}
			});
			buttons.add(branch);
			buttons.add(Box.createVerticalGlue());
			
			//delete the selected item
			JButton cutbranch = new JButton("Delete");
			cutbranch.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					if(mapList.getSelectedRow() != -1){
						mapData.removeElementAt(mapList.getSelectedRow());
						mapList.tableChanged(new TableModelEvent(mapList.getModel()));
					}
				}				
			});
			buttons.add(cutbranch);
			buttons.add(Box.createVerticalGlue());
			
			//move an item up on the list
			JButton moveUp = new JButton("<html><span style='font-size: 16pt;'>&uarr;</span></html>");
			moveUp.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					int selection = mapList.getSelectedRow();
					//the zero-th item cannot be moved up
					if(selection > 0){
						MapEvent moveItem = mapData.elementAt(selection);
						mapData.removeElementAt(selection);
						mapData.insertElementAt(moveItem, selection-1);
						//update row numbers for selected and in-editing items
						mapList.setRowSelectionInterval(selection-1, selection-1);
						mapList.setEditingRow(selection-1);
						mapList.repaint();
					}
				}
			});
			buttons.add(moveUp);
			
			//moving an item down on the list
			JButton moveDown = new JButton("<html><span style='font-size: 16pt;'>&darr;</span></html>");
			moveDown.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					int selection = mapList.getSelectedRow();
					//the last item cannot be moved up
					if(selection < mapData.size() - 1){
						MapEvent moveItem = mapData.elementAt(selection);
						mapData.removeElementAt(selection);
						mapData.insertElementAt(moveItem, selection+1);
						//update row numbers for selected and in-editing items
						mapList.setRowSelectionInterval(selection+1, selection+1);
						mapList.setEditingRow(selection+1);
						mapList.repaint();
					}
				}
			});
			buttons.add(moveDown);
			
			buttons.add(Box.createVerticalGlue());
			
			//open a CSS editing window
			JButton cssButton = new JButton("Edit Style");
			cssButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					cssWindow.editStyle();
				}
			});
			buttons.add(cssButton);
			buttons.add(Box.createVerticalGlue());

			buttons.setLayout(new BoxLayout(buttons, BoxLayout.PAGE_AXIS));
			buttons.setBorder(vpMain.labelBorder);
			
			buttonPanel.add(buttons);			
			buttonPanel.add(Box.createRigidArea(new Dimension(10, 250)));
			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
			
			myWindow.add(buttonPanel, BorderLayout.CENTER);
			
			if(myVideoTabs == null){
				myVideoTabs = new JTabbedPane();
				
				MapTableTab firstVideoTab = new MapTableTab();
				mapData = firstVideoTab.getMapData();
				mapList = firstVideoTab.getTable();
				myTabs.add(firstVideoTab);

				myVideoTabs.addTab("Set Video...", firstVideoTab);
				newTab = new JButton("Add Video Tab");
				newTab.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						//when someone clicks to add the new tab
						myVideoTabs.insertTab("Set Video...", null, new MapTableTab(), null, myVideoTabs.getTabCount() - 1);
					}
				});
				myVideoTabs.addTab("New", newTab);
			}
			else{
				mapData = ((MapTableTab)myVideoTabs.getComponentAt(0)).getMapData();
				mapList = ((MapTableTab)myVideoTabs.getComponentAt(0)).getTable();
			}

			myVideoTabs.addChangeListener(new ChangeListener(){
				public void stateChanged(ChangeEvent e) {
					if(myVideoTabs.getSelectedIndex() < myVideoTabs.getTabCount() - 1){
						mapData = ((MapTableTab)myVideoTabs.getSelectedComponent()).getMapData();					
						mapList = ((MapTableTab)myVideoTabs.getSelectedComponent()).getTable();
					}
				}
			});
			
			myWindow.add(myVideoTabs, BorderLayout.EAST);
		}
		else{
			//restoring the previous window when possible
			j.dispose();
			myWindow.setVisible(true);
		}
		if(cssWindow == null) { cssWindow = new CSSEditor(this); }
	}
	
	//storing each MapEvent in a project file
	public String saveToString(){
		String saveData = "<style>" + vpDocLoader.rewrite(myCSS) + "</style>";
		for(int tab=0; tab < (myVideoTabs.getTabCount()-1); tab++){
			MapTableTab thisTab = (MapTableTab) myVideoTabs.getComponentAt(tab);
			Vector<MapEvent> saveMapTabData = thisTab.getMapData();
			if(thisTab.getVideo() != null){
				saveData += ("<tab vidUrl=\"" + thisTab.getVideo().getURL() + "\">");
			}
			else{
				saveData += ("<tab>");
			}
			for(int i=0; i<saveMapTabData.size(); i++){
				MapEvent currentEvent = saveMapTabData.elementAt(i);
				//store values in XML
				saveData += ("<mapEvent time=\"" + currentEvent.getTime() + "\" command=\"" + currentEvent.getCommand() + "\" parameter=\""+ currentEvent.getParameter() + "\" caption=\"" + vpDocLoader.rewrite(currentEvent.getCaption()) + "\"/>");
			}
			saveData += "</tab>";
		}
		return "<mapData>" + saveData + "</mapData>";
	}
	
	//loading MapEvents from the project file
	public void initFromString(String saveData){
		Document saveDoc = vpDocLoader.getXMLfromString(saveData);
		
		//load the saved CSS style
		NodeList myStyles = saveDoc.getElementsByTagName("style");
		if(myStyles.getLength() == 1){
			myCSS = vpDocLoader.reread(myStyles.item(0).getTextContent());
		}
		
		//generate each MapEvent from XML attributes
		NodeList tabs = saveDoc.getElementsByTagName("tab");
		if(tabs.getLength() > 0){ myVideoTabs = new JTabbedPane(); }
		for(int i=0; i<tabs.getLength(); i++){
			NodeList events = null;
			if(tabs.item(i).hasChildNodes()){
				events = tabs.item(i).getChildNodes();
			}
			Vector<MapEvent> loadMapData = new Vector<MapEvent>();
			for(int e=0; e<events.getLength(); e++){
				NamedNodeMap nnm = events.item(e).getAttributes();
				float time = Float.parseFloat(nnm.getNamedItem("time").getTextContent());
				String command = nnm.getNamedItem("command").getTextContent();
				String parameter = nnm.getNamedItem("parameter").getTextContent();
				String caption = vpDocLoader.reread(nnm.getNamedItem("caption").getTextContent());
			
				//prevent empty values from being displayed as "null"
				if(command.equals("null")){ command = null; }
				if(parameter.equals("null")){ parameter = null; }
				//restore values from XML
				if(caption.equals("")){ caption = null; }
				loadMapData.add(new MapEvent(time, command, parameter, caption));
			}
			NamedNodeMap nnm = tabs.item(i).getAttributes();
			String vidUrl;
			try{
				vidUrl = nnm.getNamedItem("vidUrl").getNodeValue();
			}
			catch(Exception e){ vidUrl = null; }
			MapTableTab addTab = new MapTableTab(vidUrl, loadMapData);
			if(addTab.getVideo() == null){
				myVideoTabs.add("Set Video...", addTab);
			}
			else{
				myVideoTabs.add(addTab.getVideo().getName(), addTab);
			}
		}
		newTab = new JButton("Add Video Tab");
		newTab.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				//when someone clicks to add the new tab
				myVideoTabs.insertTab("Set Video...", null, new MapTableTab(), null, myVideoTabs.getTabCount() - 1);
			}
		});
		myVideoTabs.addTab("New", newTab);
	}
	
	//defining tab data
	protected class MapTableTab extends JPanel{
		private Vector<MapEvent> myEvents = new Vector<MapEvent>();
		private JTable mapTable = new JTable();
		private JComboBox commandOptions = new JComboBox();
		private vpVideoSelector videoSelect = new vpVideoSelector();
		private VideoListing myVideo = null;
		
		//the default constructor is the one called by making a new tab
		public MapTableTab(){
			buildMapTable();
		}
		//this constructor is used when loading a project file
		public MapTableTab(String vid, Vector<MapEvent> events){
			buildMapTable();
			myEvents = events;
			for(int i=0; i < vpMain.videoData.size(); i++){
				if(vpMain.videoData.elementAt(i).getURL().equals(vid)){
					myVideo = vpMain.videoData.elementAt(i);
					break;
				}
			}
		}
		
		public void buildMapTable(){			
			//defining the table
			mapTable.setFillsViewportHeight(true);
			mapTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			mapTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			mapTable.setModel(myTableModel);
			mapTable.setDefaultRenderer(Object.class, myTableRendering);
			mapTable.setRowHeight(30);
			mapTable.getColumnModel().getColumn(0).setCellEditor(timeInput);
			//commands are selected from a dropdown list
			commandOptions.addItem("Goto");
			commandOptions.addItem("Pushpin");
			commandOptions.addItem("Load KML");
			commandOptions.addItem("Goto+Pin");
			commandOptions.addItem("Clear");
			mapTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(commandOptions));			
			mapTable.getColumnModel().getColumn(2).setCellEditor(stringInput);
			mapTable.getColumnModel().getColumn(3).setCellEditor(stringInput);
			
			//the list of MapEvents is scrollable
			JScrollPane mapContainer = new JScrollPane(mapTable);
			
			videoSelect.refresh();
			videoSelect.addFocusListener(new FocusListener(){
				public void focusGained(FocusEvent e) {
					videoSelect.refresh();
				}
				public void focusLost(FocusEvent e) {}
			});
			videoSelect.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					VideoListing select = (VideoListing) videoSelect.getSelectedItem();
					if(select != null){
						if(!select.equals(vpVideoSelector.blankVideo)){
							myVideoTabs.setTitleAt(myVideoTabs.getSelectedIndex(), select.getName());
							myVideo = select;
						}
					}
				}
			});
			
			//defining the tabbox
			this.add(Box.createRigidArea(new Dimension(10,15)));
			this.add(videoSelect);
			this.add(mapContainer);
			this.add(Box.createRigidArea(new Dimension(10,15)));
			this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		}
		
		public JTable getTable(){ return mapTable; }
		public Vector<MapEvent> getMapData(){ return myEvents; }
		public VideoListing getVideo(){ return myVideo; }
	}
	
	//defining the table functionality
	protected class MapTableModel extends AbstractTableModel{

		//the categories array sets up the table columns
		private String[] categories = new String[4];
		public MapTableModel(){
			categories[0] = "Time";
			categories[1] = "Command";
			categories[2] = "Parameter";
			categories[3] = "Caption";
		}
		
		public boolean isCellEditable(int row, int col){
			return true;
		}
		
		//getValueAt determines what value is displayed
		public Object getValueAt(int row, int col) {
			if(col == 0){
				return mapData.elementAt(row).displayTime();
			}
			if(col == 1){
				String command = mapData.elementAt(row).getCommand();
				if(command == null){
					return "";
				}
				else{
					return command;
				}
			}
			if(col == 2){
				String parameter = mapData.elementAt(row).getParameter();
				if(parameter == null){
					return "";
				}
				else{
					return parameter;
				}			}
			if(col == 3){
				String caption = mapData.elementAt(row).getCaption();
				if(caption == null){
					return "";
				}
				else{
					return caption;
				}
			}
			//more columns would go here
			return null;
		}
		
		//setValueAt receives a value to be stored at (row,col)
		public void setValueAt(Object value, int row, int col){
			try{
				if(col == 0){
					//time is stored as a float
					mapData.elementAt(row).setTime(Float.parseFloat(value.toString()));
				}
				if(col == 1){
					mapData.elementAt(row).setCommand(value.toString());
				}
				if(col == 2){
					mapData.elementAt(row).setParameter(value.toString());
				}
				if(col == 3){
					mapData.elementAt(row).setCaption(value.toString());
				}
				fireTableCellUpdated(row, col);
			}catch(Exception e){}
		}
		
		//defining table details
	    public String getColumnName(int col) {
	        return categories[col];
	    }
		public int getColumnCount() {
			return categories.length;
		}
		public int getRowCount() {
			return mapData.size();
		}
	}
	
	//MapTableRenderer makes a row's Time cell's background red when it is not in order
	protected class MapTableRendering extends vpTableRendering{
		public MapTableRendering(int def, int selected) {
			super(def, selected);
		}
		public Component getTableCellRendererComponent(JTable t, Object item, boolean isSelected, boolean hasFocus, int row, int col) {
			Component c = super.getTableCellRendererComponent(t, item, isSelected, hasFocus, row, col);
			if((col == 0)&&(row > 0)){
				if(mapData.elementAt(row).getTime() < mapData.elementAt(row-1).getTime()){
					c.setBackground(vpMain.failColor);
				}
				else{
					if(!isSelected) { c.setBackground(Color.WHITE); }
				}
			}
			else{
				if(!isSelected) { c.setBackground(Color.WHITE); }
			}
			return c;
		}
	}
	
	//MapTimeSetter uses the standard vpTimeInput editor
	//This code only connects the editor with the MapEvent data
	protected class MapTimeSetter extends vpTimeInput{
		public Component getTableCellEditorComponent(JTable t, Object value, boolean isSelected, int row, int col) {
			float myTime = mapData.elementAt(row).getTime();
			return super.getTableCellWithTime(myTime);
		}
	}
	
	//a MapEvent stores information on how to command the map
	protected class MapEvent {
		private float myTime = 0;
		private String command = null;
		private String parameter = null;
		private String caption = null;
		
		//the basic new MapEvent has no set properties
		public MapEvent(){}
		
		//this constructor is used when loading from the project file
		public MapEvent(float t, String com, String p, String cap){
			myTime = t;
			command = com;
			parameter = p;
			caption = cap;
		}
		
		public void setTime(float t){ myTime = t; }
		public float getTime(){ return myTime; }
		public void setCommand(String c){ command = c; }
		public String getCommand(){ return command; }
		public void setParameter(String p){ parameter = p; }
		public String getParameter(){ return parameter; }
		public void setCaption(String c){ caption = c; }
		public String getCaption(){ return caption; }
		//this converts the stored time (a float value in seconds) to the displayed hours, minutes, and seconds
		//format is h:mm:ss or hh:mm:ss
		protected String displayTime(){
			float time = getTime();
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
	}
	
	//CSS editor methods
	public String[] getCSSOptions() {
		return styles;
	}
	public String[] getOptionNames() {
		return stylenames;
	}
	public String getCurrentCSS() {
		return myCSS;
	}
	public void setCurrentCSS(String set) {
		myCSS = set;
	}
}