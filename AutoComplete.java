import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
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
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

public class AutoComplete extends Supplement implements CSSstyled {
	//preset font
	protected static Font editFont = new Font("Arial", Font.PLAIN, 14);
	//preset CSS styles
	protected static String basicStyle =
		"#searchBox{\r\n" +
		"	/* the textbox where the user enters search terms */\r\n" +
		"	font-family: arial;\r\n" +
		"	font-size: 14pt;\r\n" +
		"	border: 2px solid blue;\r\n" +
		"	padding: 3px;\r\n" +
		"	width: 100%;\r\n" +
		"}\r\n" +
		"#result{\r\n" +
		"	/* set the style of the box containing each result */\r\n" +
		"	font-family: arial;\r\n" +
		"	font-size: 10pt;\r\n" +
		"	border: 1px solid black;\r\n" +
		"	padding: 0.2cm;\r\n" +
		"	/* also set the style of properties here */\r\n" +
		"	color: black;\r\n" +
		"}\r\n" +
		"#result a{\r\n" +
		"	/* the matching result (text matches will be bolded) */\r\n" +
		"	font-size: 12pt;\r\n" +
		"	text-decoration: underline;\r\n" +
		"	color: blue;\r\n" +
		"}\r\n";
	protected static String greenStyle =
		"#searchBox{\r\n" +
		"	/* the textbox where the user enters search terms */\r\n" +
		"	font-family: arial;\r\n" +
		"	font-size: 12pt;" +
		"	color: green;\r\n" +
		"	background-color: #66FF33;\r\n" +
		"	padding: 3px;\r\n" +
		"	width: 100%;\r\n" +
		"}\r\n" +
		"#result{\r\n" +
		"	/* set the style of the box containing each result */\r\n" +
		"	font-family: arial;\r\n" +
		"	background-color: #66FF33;\r\n" +
		"	border: 1px solid green;\r\n" +
		"	padding: 0.2cm;\r\n" +
		"	/* also set the style of properties here */\r\n" +
		"	color: #003300;\r\n" +
		"}\r\n" +
		"#result a{\r\n" +
		"	/* the matching result (text matches will be bolded) */\r\n" +
		"	text-decoration: underline;\r\n" +
		"	color: #000066;\r\n" +
		"}\r\n";
	protected static String techStyle =
		"#searchBox{\r\n" +
		"	/* the textbox where the user enters search terms */\r\n" +
		"	font-family: courier;\r\n" +
		"	border: 1px solid black;\r\n" +
		"	padding: 3px;\r\n" +
		"	width: 100%;\r\n" +
		"}\r\n" +
		"#result{\r\n" +
		"	/* set the style of the box containing each result */\r\n" +
		"	font-family: courier;\r\n" +
		"	background-color: black;\r\n" +
		"	padding: 0.2cm;\r\n" +
		"	/* also set the style of properties here */\r\n" +
		"	color: #00FF00;\r\n" +
		"}\r\n" +
		"#result a{\r\n" +
		"	/* the matching result (text matches will be bolded) */\r\n" +
		"	text-decoration: underline;\r\n" +
		"	color: #0066CC;\r\n" +
		"}\r\n";
	
	//window elements
	protected JFrame myWindow;
	protected JTabbedPane modeTabs;
	protected JTextField resultNameField;
	protected Vector<Result> resultData = new Vector<Result>();
	protected Vector<String> properties = new Vector<String>();
	protected Vector<JTextField> propertyfields = new Vector<JTextField>();
	protected int openResultIndex = -1;
	protected JTable timeTable;
	protected JPanel propPanel;
	protected JLabel propertiesStatus;
	protected JTextArea resultFormatter;
	protected String loadedResultFormat;
	
	//order of elements
	protected String orderType = "Alphabetic";
	
	//event handler for updating properties' values
	protected KeyListener propertyEditListener;
	//event handler for saving changes to the timeTable
	protected FocusListener endEditListener = new FocusListener(){
		public void focusGained(FocusEvent e) {
			timeTable.editingStopped(null);
		}
		public void focusLost(FocusEvent e) {}
	};
	
	//CSS variables
	protected CSSEditor myCSSeditor;
	protected String myCSS = basicStyle;
	protected String[] cssStyleNames = {"Basic", "Green", "Tech" };
	protected String[] cssStyles = { basicStyle, greenStyle, techStyle };
	
	public String getDescription() {
		return "<html><p>The <b>Auto-Complete</b> add-on adds an intelligent textbox.  Like other auto-completes, it will suggest options that match what the user has begun to type.</p><br><p>You will be able to display custom information about each result.  This is useful for supporting a lot of content through a tool similar to 'search'.</p></html>";
	}

	public String getClassScripts(){
		//this script allows the AutoComplete add-on to convert the DATE property into a date
		return
		"<script type='text/javascript'>\r\n" +
		"	/* converts numerical dates (20080415) into Strings (April 15, 2008) */\r\n" +
		"	function AutoComplete_writeDate(qnum){\r\n" +
		"		qtime = \"\" + qnum;\r\n" +
		"		suffix = qtime.substring(6) + \"&#44 \" + qtime.substring(0,4);\r\n" +
		"		switch(qtime.substring(4,6)){\r\n" +
		"			case \"01\": return (\"January \" + suffix);\r\n" +
		"			case \"02\": return (\"February \" + suffix);\r\n" +
		"			case \"03\": return (\"March \" + suffix);\r\n" +
		"			case \"04\": return (\"April \" + suffix);\r\n" +
		"			case \"05\": return (\"May \" + suffix);\r\n" + 
		"			case \"06\": return (\"June \" + suffix);\r\n" + 
		"			case \"07\": return (\"July \" + suffix);\r\n" + 
		"			case \"08\": return (\"August \" + suffix);\r\n" + 
		"			case \"09\": return (\"September \" + suffix);\r\n" +
		"			case \"10\": return (\"October \" + suffix);\r\n" +
		"			case \"11\": return (\"November \" + suffix);\r\n" +
		"			case \"12\": return (\"December \" + suffix);\r\n" +
		"		}\r\n" +
		"	}\r\n" +
		"</script>\r\n";
	}
	
	public String getHTML(String myID) {
		String buildString =
		"<!--Styles for AutoComplete SearchBox and results -->\r\n" +
		"<style type=\"text/css\">\r\n" + myCSS.replace("#searchBox", "input." + myID + "_searchBox").replace("#result", "div." + myID + "_resultDiv") + "</style>\r\n" +
		"<!--AutoComplete SearchBox calls getResults() when its value changes-->\r\n" +
		"<input id=\"" + myID + "_queryInput\" class=\"" + myID + "_searchBox\" onkeyup=\"" + myID + "_getResults();\" type=\"text\"><br/>\r\n" +
		"<!--Results are stored in the queryResults div-->\r\n" +
		"<div id=\"" + myID + "_queryResults\" class=\"" + myID + "_resultDiv\">No Results</div>\r\n\r\n" +
		"<!--The standardResult div is a template which tells AutoComplete where to put properties' values-->\r\n";
		if(resultFormatter != null){
			buildString += "<div id=\"" + myID + "_standardResult\" style=\"display:none;\">\r\n" + resultFormatter.getText().trim().replace("[", "&#91;").replace("]", "&#93;") + "</div>\r\n";
		}
		else{
			buildString += "<div id=\"" + myID + "_standardResult\" style=\"display:none;\">\r\n" + loadedResultFormat.replace("[", "&#91;").replace("]", "&#93;") + "</div>\r\n";
		}
		return buildString;
	}
	
	public String getLoadScript(String myID){
		return myID + "_initAutoComplete();\r\n";
	}
	
	public String getIndividualScripts(String myID){
		String buildScript = "<script type=\"text/javascript\">\r\n";
		buildScript +=
		//use arrays to store the results and the video/times they link to
		"var " + myID + "_primaryResults = new Array();\r\n" +
		"var " + myID + "_videoURLs = new Array();\r\n" +
		"var " + myID + "_times = new Array();\r\n";
		//make an array to store values of each property
		for(int p=0; p<properties.size(); p++){
			buildScript+=
		"var " + myID + "_p_" + properties.elementAt(p).replace(" ", "") + " = new Array();\r\n";
		}
		
		//this method sets the data at the program start
		buildScript +=
		"function " + myID + "_initAutoComplete(){\r\n";
		//make a copy of the results in the order specified by the user
		Vector<Result> resultDataClone = new Vector<Result>();
		if(orderType.equals("Alphabetic")){
			//sort all in resultDataClone alphabetically
			String[] resultTitles = new String[resultData.size()];
			for(int r=0; r<resultData.size(); r++){
				resultTitles[r] = resultData.elementAt(r).getName() + ":" + r;
			}
			Arrays.sort(resultTitles);
			for(int r=0; r<resultData.size(); r++){
				int fromIndex = Integer.parseInt(resultTitles[r].substring(resultTitles[r].lastIndexOf(":") + 1));
				resultDataClone.add(resultData.elementAt(fromIndex));
			}
		}
		else{
			if(orderType.equals("Date")){
				//sort all in resultDataClone by date
				String dateProp = "";
				for(int p=0; p<properties.size(); p++){
					if(properties.elementAt(p).toUpperCase().equals("DATE")){
						dateProp = properties.elementAt(p);
						break;
					}
				}
				String[] resultDates = new String[resultData.size()];
				for(int r=0; r<resultData.size(); r++){
					String d = resultData.elementAt(r).getProperty(dateProp);
					if(d.equals("")){ d = "0"; }
					resultDates[r] = d + ":" + r;
				}
				Arrays.sort(resultDates);
				for(int r=(resultDates.length - 1); r>=0; r--){
					int fromIndex = Integer.parseInt(resultDates[r].substring(resultDates[r].lastIndexOf(":") + 1));
					resultDataClone.add(resultData.elementAt(fromIndex));
				}
			}
		}
		//now that results are sorted, write the commands to add them
		for(int r=0; r<resultDataClone.size(); r++){
			buildScript +=
			//each result follows the form: myID_addResult("name", "videoURL", time, new Array("property1", "property2"));
			//if the date property is used: myID_addResult("name", "videoURL", time, new Array("property1", date));
		"	" + myID + "_addResult(\"" + resultDataClone.elementAt(r).getName().replace("\\", "&#92;").replace("\"", "&quot;") + "\",\"" + resultDataClone.elementAt(r).getVideoURL() + "\"," + resultDataClone.elementAt(r).getStart() + ",new Array(";
			for(int p=0; p<properties.size(); p++){
				if(properties.elementAt(p).toUpperCase().equals("DATE")){
					//add the date property as a numeric value
					buildScript += resultDataClone.elementAt(r).getProperty(properties.elementAt(p)) + ",";
				}
				else{
					//add any non-date properties as Strings
					buildScript += "\"" + resultDataClone.elementAt(r).getProperty(properties.elementAt(p)).replace("\\", "&#92;").replace("\"", "&quot;") + "\",";
				}
			}
			buildScript = buildScript.substring(0, buildScript.length() - 1) +  "));\r\n";
		}
		buildScript +=
		"}\r\n" +
		
		//each result is stored by being added to the arrays
		"function " + myID + "_addResult(name, url, time, propArray){\r\n" +
		"	" + myID + "_primaryResults.push(name);\r\n" +
		"	" + myID + "_videoURLs.push(url);\r\n" +
		"	" + myID + "_times.push(time);\r\n";
		for(int p=0; p<properties.size(); p++){
			//add properties from propArray into their specific arrays
			buildScript +=
		"	" + myID + "_p_" + properties.elementAt(p).replace(" ", "") + ".push(propArray[" + p + "]);\r\n";
		}
		buildScript +=
		"}\r\n" +
		
		//getResults() is the key method that adds results to the list
		"/* getResults() is called when searchBox is changed */\r\n" +
		"function " + myID + "_getResults(){\r\n" +
		"	searchInput = document.getElementById(\"" + myID + "_queryInput\").value;\r\n" +
		"	/* searchItems is an array of each 'word' in searchItems */\r\n" +
		"	searchItems = searchInput.split(\" \");\r\n" +
		"	/* resultsBox will be used to rewrite the results list */\r\n" +
		"	resultsBox = \"\";\r\n" +
		"	/* review each possible result */\r\n" +
		"	for(q=0; q<" + myID + "_primaryResults.length; q++){\r\n" +
		"		/* review each possible search term */\r\n" +
		"		for(i=0; i<searchItems.length; i++){\r\n" +
		"			if(searchItems[i] != null){\r\n" +
		"				if(searchItems[i].length > 2){\r\n" +
		"					/* these search terms have at least 3 letters */\r\n" +
		"					/* convert Strings to lower-case so that they can be compared */\r\n" +
		"					searchItems[i] = searchItems[i].toLowerCase();\r\n" +
		
		//a "property search" such as "Date: August September" to show all results with these properties
		"					if(searchItems[i].indexOf(\":\") != -1){\r\n" +
		"						/* if there is a : try to run a property search */\r\n" +
		"						foundSomething = -1;\r\n" +
		"						/* extract the propertyToSearch from the query */\r\n" +
		"						propertyToSearch = searchItems[i].split(\":\")[0].replace(\" \",\"\");\r\n";
			for(int p=0; p<properties.size(); p++){
				buildScript +=
		"						if(propertyToSearch ==\"" + properties.elementAt(p).replace(" ", "").toLowerCase() + "\"){\r\n" +
		"							/* the user's query matches this property */\r\n" +
		"							/* begin with the first term in case there is no space in property:value */\r\n" +
		"							propSearchItem = searchItems[i].substring(searchItems[i].indexOf(\":\") + 1);\r\n" +
		"							propI = i;\r\n" +
		"							/* the program checks the rest of the terms until it finds a match  */\r\n" +
		"							while(propI<searchItems.length){\r\n" +
		"								if(propSearchItem.length > 0){\r\n";
				if(properties.elementAt(p).replace(" ","").toUpperCase().equals("DATE")){
					buildScript +=
					//DATE property searches the written date
		"									/* check if this result's written date matches the term */\r\n" +
		"									if(AutoComplete_writeDate(" + myID + "_p_" + properties.elementAt(p).replace(" ", "") + "[q]).toLowerCase().indexOf(propSearchItem) != -1){\r\n";
				}else{
					//all other properties look for a match in the property
					buildScript +=
		"									/* check if this result's property matches the term */\r\n" +
		"									if(" + myID + "_p_" + properties.elementAt(p).replace(" ", "") + "[q].toLowerCase().indexOf(propSearchItem) != -1){\r\n";
				}
				buildScript+=
		"										/* success: write the result to the resultsBox, then end property search */\r\n" +
		"										resultsBox += (\"<div class=\\\"" + myID + "_resultDiv\\\"><a href=\\\"javascript:void(0);\\\" onclick=\\\"loadYT('\" + " + myID + "_videoURLs[q] + \"',\" + " + myID + "_times[q] + \");\\\">\" + " + myID + "_boldMatches(" + myID + "_primaryResults[q],searchItems[i]) + \"</a><br/>\");\r\n" +
		"										/* specify that this property should appear bolded */\r\n" +
		"										resultsBox += (" + myID + "_writeProperties(q,\"" + properties.elementAt(p).replace(" ", "") + "\") + \"</div>\");\r\n" +
		"										foundSomething = propI;\r\n" +
		"										break;\r\n" +
		"									}\r\n" +
		"								}\r\n" +
		"								/* this term didn't match, try searching with the next term */\r\n" +
		"								propI++;\r\n" +
		"								if(propI < searchItems.length){\r\n" +
		"									propSearchItem = searchItems[propI];\r\n" +
		"								}\r\n" +
		"								if(propSearchItem.indexOf(\":\") != -1){\r\n" +
		"									/* consider the : to be the start of a new property search */\r\n" +
		"									break;\r\n" +
		"								}\r\n" +
		"							}\r\n" +
		"						}\r\n" +
		"						if(foundSomething != -1){\r\n" +
		"							/* the property search was successful, now jump to the next result */\r\n" +
		"							break;\r\n" +
		"						}\r\n";
		}
		buildScript +=
		"					}\r\n" +		
		"					/* property search was not done or had no results - do standard search */\r\n" +
		"					if(" + myID + "_primaryResults[q].toLowerCase().indexOf(searchItems[i]) != -1){\r\n" +
		"						/* this result matches the term, add it to the resultsBox and jump to next result */\r\n" +
		"						/* the match in the results is bolded, but no properties are bolded */\r\n" +
		"						resultsBox += (\"<div class=\\\"" + myID + "_resultDiv\\\"><a href=\\\"javascript:void(0);\\\" onclick=\\\"loadYT('\" + " + myID + "_videoURLs[q] + \"',\" + " + myID + "_times[q] + \");\\\">\" + " + myID + "_boldMatches(" + myID + "_primaryResults[q],searchItems[i]) + \"</a><br/>\");\r\n" +
		"						resultsBox += (" + myID + "_writeProperties(q,\"\") + \"</div>\");\r\n" +
		"						break;\r\n" +
		"					}\r\n" +
		"				}\r\n" +
		"			}\r\n" +
		"		}\r\n" +
		"	}\r\n" +
		"	if(resultsBox == \"\"){\r\n" +
		"		/* running a search did not find any results */\r\n" +
		"		resultsBox = \"No Results\";\r\n" +
		"	}\r\n" +
		"	/* set the queryResults div to the generated results */\r\n" +
		"	document.getElementById(\"" + myID + "_queryResults\").innerHTML = resultsBox;\r\n" +
		"}\r\n" +
		
		//boldMatches bolds matches in the search result without changing capitalization
		"/* boldMatches bolds the search term wherever it appears in the result */\r\n" +
		"function " + myID + "_boldMatches(result, searchTerm){\r\n" +
		"	resultCheck = result.toLowerCase();\r\n" +
		"	while(resultCheck.lastIndexOf(searchTerm) != -1){\r\n" +
		"		/* indices are used because resultCheck and searchTerm are matched in lower-case, but the printed result should have the same capitalization as the stored result */\r\n" +
		"		lastMark = resultCheck.lastIndexOf(searchTerm);\r\n" +
		"		lastMarkEnd = lastMark + searchTerm.length;\r\n" +
		"		result = result.substring(0, lastMark) + \"<b>\" + result.substring(lastMark, lastMarkEnd) + \"</b>\" + result.substring(lastMarkEnd);\r\n" +
		"		resultCheck = resultCheck.substring(0, lastMark);\r\n" +
		"	}\r\n" +
		"	return result;\r\n" +
		"}\r\n" +
		
		//writeProperties copies the template, except it bolds a property if this result was found in a property search
		"/* writeProperties copies the resultForm and replaces each [PROPERTY] with the result's value */\r\n" +
		"function " + myID + "_writeProperties(index,keyProperty){\r\n" +
		"	resultForm = document.getElementById(\"" + myID + "_standardResult\").innerHTML;\r\n";
		for(int p=0; p < properties.size(); p++){
			buildScript+=
		"	if(keyProperty == \"" + properties.elementAt(p) + "\"){\r\n";
			if(properties.elementAt(p).toUpperCase().equals("DATE")){
				buildScript +=
		"		/* DATE property was selected in a search, bold DATE */\r\n" +
		"		resultForm = resultForm.replace(\"[" + properties.elementAt(p) + "]\", \"<b>\" + AutoComplete_writeDate(" + myID + "_p_" + properties.elementAt(p).replace(" ", "") + "[index] + \"</b>\"));\r\n" +
		"	}\r\n" +
		"	else{\r\n" +
		"		/* DATE property was not selected, do simple replacement */\r\n" +
		"		resultForm = resultForm.replace(\"[" + properties.elementAt(p) + "]\", AutoComplete_writeDate(" + myID + "_p_" + properties.elementAt(p).replace(" ", "") + "[index]));\r\n" +
		"	}\r\n";
			}
			else{
				buildScript +=
		"		/* this property was selected in a search, bold its value */\r\n" +
		"		resultForm = resultForm.replace(\"[" + properties.elementAt(p) + "]\", \"<b>\" + " + myID + "_p_" + properties.elementAt(p).replace(" ", "") + "[index] + \"</b>\");\r\n" +
		"	}\r\n" +
		"	else{\r\n" +
		"		/* this property was not selected, do simple replacement */\r\n" +
		"		resultForm = resultForm.replace(\"[" + properties.elementAt(p) + "]\", " + myID + "_p_" + properties.elementAt(p).replace(" ", "") + "[index]);\r\n" +
		"	}\r\n";
			}
		}
		buildScript +=
		"	return resultForm;\r\n" +
		"}\r\n" +
		"</script>\r\n";
		return buildScript;
	}

	public void setWindow(JFrame j) {
		if(myWindow == null){
			//set up the window
			myWindow = j;
			myWindow.setTitle("AutoComplete: " + this.getName());
			myWindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			myWindow.setSize(600,550);
			myWindow.setVisible(true);
			myWindow.getContentPane().setLayout(new BoxLayout(myWindow.getContentPane(), BoxLayout.Y_AXIS));
			
			//when the window is clicked, end edits on the timeTable
			myWindow.addFocusListener(endEditListener);
			//create a CSSEditor for this AutoComplete
			myCSSeditor = new CSSEditor(this);
			
			//tabs allow the user to view the result list, editor, or settings
			modeTabs = new JTabbedPane();
			modeTabs.addChangeListener(new ChangeListener(){
				public void stateChanged(ChangeEvent e) {
					if(timeTable != null){
						//when the tab changes, stop any edits on timeTable
						timeTable.editingStopped(null);
					}
					if(modeTabs.getSelectedIndex() == 1){
						//if moving to the editor, redraw properties and make sure the resultNameField matches the edited content
						resultNameField.setText(resultData.elementAt(openResultIndex).getName());
						drawProperties();
					}
				}
			});
			modeTabs.addFocusListener(endEditListener);
			
			//more window elements
			final JPanel resultListPanel = new JPanel();
			final JList resultList = new JList(resultData);
			final JPanel resultEditPanel = new JPanel();
			
			//the searchPanel is always visible, allowing the user to view any result
			JPanel searchPanel = new JPanel();
				searchPanel.add(new JLabel("Result for:"));
				resultNameField = new JTextField();
					resultNameField.setColumns(20);
					resultNameField.setFont(editFont);
				final JButton gotoResult = new JButton("Create");
					gotoResult.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							if(gotoResult.getText().equals("Create")){
								resultData.add(new Result(resultNameField.getText().trim()));
								resultList.setListData(resultData);
								loadResult(resultData.size() - 1);
								gotoResult.setText("Current");
								gotoResult.setEnabled(false);
								modeTabs.setEnabled(true);
								modeTabs.setSelectedIndex(1);
							}
							else{
								if(gotoResult.getText().equals("View")){
									for(int i=0; i< resultData.size(); i++){
										if(resultData.elementAt(i).getName().equals(resultNameField.getText().trim())){
											loadResult(i);
											gotoResult.setText("Current");
											gotoResult.setEnabled(false);
											modeTabs.setSelectedIndex(1);
											return;
										}
									}
								}
							}
						}
					});
				resultNameField.addKeyListener(new KeyListener(){
					public void keyReleased(KeyEvent e) {
						timeTable.editingStopped(null);
						String nameCheck = resultNameField.getText().trim().toLowerCase();
						for(int i=(resultData.size() - 1); i >= 0; i--){
							if(resultData.elementAt(i).getName().toLowerCase().equals(nameCheck)){
								if(i == openResultIndex){
									gotoResult.setText("Current");
									gotoResult.setEnabled(false);
									modeTabs.setSelectedIndex(1);
								}
								else{
									gotoResult.setEnabled(true);
									gotoResult.setText("View");
									resultList.setListData(resultData);
									modeTabs.setSelectedIndex(0);
								}
								return;
							}
						}
						gotoResult.setEnabled(true);
						gotoResult.setText("Create");
						resultList.setListData(resultData);
						modeTabs.setSelectedIndex(0);
					}
					public void keyPressed(KeyEvent e) {}
					public void keyTyped(KeyEvent e) {}	
				});
				searchPanel.add(resultNameField);	
				searchPanel.add(gotoResult);
				searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.LINE_AXIS));
			myWindow.add(searchPanel);
			
			//the resultListPanel contains the resultList
			resultList.setVisibleRowCount(15);
			resultList.addListSelectionListener(new ListSelectionListener(){
				public void valueChanged(ListSelectionEvent e) {
					//when a result is selected, display it in the name field and offer to view it
					if(resultList.getSelectedIndex() > -1){
						resultNameField.setText(resultList.getSelectedValue().toString());
						loadResult(resultList.getSelectedIndex());
						gotoResult.setEnabled(true);
						gotoResult.setText("View");
					}
				}
			});
			resultListPanel.add(new JScrollPane(resultList));
			resultListPanel.setLayout(new BoxLayout(resultListPanel, BoxLayout.Y_AXIS));
			modeTabs.add("Result List", resultListPanel);
			
			//resultEditPanel contains all of the controls for editing a result's values
			resultEditPanel.setLayout(new BoxLayout(resultEditPanel, BoxLayout.Y_AXIS));

			//selecting the video and time to jump to when the result is clicked
			final vpVideoSelector videoEdit = new vpVideoSelector();
			TableModel times = new AbstractTableModel(){
				public int getColumnCount() { return 2; }
				public int getRowCount() { return 1; }
				public boolean isCellEditable(int row, int col){ return true; }
				public Object getValueAt(int row, int col) {
					if(col == 0){
						//retrieve video
						videoEdit.refresh();
						String videoURL = resultData.elementAt(openResultIndex).getVideoURL();
						for(int v=0; v < vpMain.videoData.size(); v++){
							if(vpMain.videoData.elementAt(v).getURL().equals(videoURL)){
								return vpMain.videoData.elementAt(v);
							}
						}
						return null;
					}
					else{
						//retrieve start time
						return displayTime(resultData.elementAt(openResultIndex).getStart());
					}
				}
				public void setValueAt(Object value, int row, int col) {
					try{
						if(col == 0){
							//video setting
							VideoListing selectVideo = (VideoListing) value;
							if(!selectVideo.equals(vpVideoSelector.blankVideo)){
								resultData.elementAt(openResultIndex).setVideo(selectVideo);
							}
						}
						else{
							//start time
							resultData.elementAt(openResultIndex).setStart((int) Float.parseFloat(value.toString()));
						}
					}
					catch(Exception e){}
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
			timeTable = new JTable(times);
			timeTable.setDefaultRenderer(Object.class, new vpTableRendering(30,30));
			timeTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(videoEdit));
			timeTable.getColumnModel().getColumn(1).setCellEditor(new AutoCompleteTimeInput());
			timeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			resultEditPanel.add(timeTable, BorderLayout.NORTH);
			
			//propPanel contains all property names and values
			propPanel = new JPanel();
			propertiesStatus = new JLabel();
			propPanel.add(propertiesStatus);
			propertyEditListener = new KeyListener(){
				public void keyReleased(KeyEvent e) {
					//when the property's value is changed, update the data
					JTextField editedProperty = (JTextField) e.getSource();
					int place = Integer.parseInt(editedProperty.getName());
					resultData.elementAt(openResultIndex).setProperty(properties.elementAt(place), editedProperty.getText());
					timeTable.editingStopped(null);
				}
				public void keyPressed(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			};
			propertiesStatus.setText("Add properties in result tab.");
			propPanel.setLayout(new BoxLayout(propPanel, BoxLayout.PAGE_AXIS));
			resultEditPanel.add(propPanel, BorderLayout.CENTER);
			modeTabs.add("Editor", resultEditPanel);
			
			//the controls / settings panel sets the form and style of results
			JPanel controlsPanel = new JPanel();
			controlsPanel.setLayout(new BorderLayout());
			JPanel topPanel = new JPanel();
			topPanel.setLayout(new BorderLayout());
			final CSSEditor cssWindow = new CSSEditor(this);
			JButton cssButton = new JButton("Edit Style");
			cssButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					cssWindow.editStyle();
				}
			});
			topPanel.add(cssButton, BorderLayout.NORTH);
			JLabel propertiesPrompt = new JLabel("<html><div style='font-size: 14pt; text-align: center;'>Write a sample result <i>example:</i>Recorded [PLACE] by [PERSON]<br/>(use [DATE] to convert 20080415&rarr;April 15, 2008)<br/></div><div style='text-align: left;'>[RESULT]&lt;br/&gt;</div></html>");
			topPanel.add(propertiesPrompt, BorderLayout.CENTER);
			controlsPanel.add(topPanel, BorderLayout.NORTH);			
			resultFormatter = new JTextArea();
			if(loadedResultFormat != null){ resultFormatter.setText(loadedResultFormat); }
			resultFormatter.setFont(editFont);
			resultFormatter.setRows(4);
			resultFormatter.setColumns(45);
			resultFormatter.setLineWrap(true);
			resultFormatter.addKeyListener(new KeyListener(){
				public void keyReleased(KeyEvent e) {
					String props = resultFormatter.getText();
					Vector<String> proposedProps = new Vector<String>();
					while((props.indexOf("[") != -1)&&(props.substring(props.indexOf("[")).indexOf("]") != -1)){
						proposedProps.add(props.substring(props.indexOf("[") + 1, props.indexOf("[") + props.substring(props.indexOf("[") + 1).indexOf("]") + 1));
						props = props.substring(props.indexOf("["));
						props = props.substring(props.indexOf("]"));
					}
					if(proposedProps.size() != properties.size()){
						properties = proposedProps;
					}
					else{
						for(int p=0; p<proposedProps.size(); p++){
							if(!properties.elementAt(p).equals(proposedProps.elementAt(p))){
								properties.setElementAt(proposedProps.elementAt(p), p);
							}
						}
					}
				}
				public void keyPressed(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
			controlsPanel.add(resultFormatter, BorderLayout.CENTER);
			//sorting options
			JPanel sortByBox = new JPanel();
			sortByBox.add(new JLabel("Sort by:"));
			ButtonGroup b = new ButtonGroup();
			final JRadioButton alpha = new JRadioButton("Alphabetic");
			alpha.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					if(alpha.getSelectedObjects() != null){
						//select an alphabetic sort
						orderType = "Alphabetic";
					}
				}
			});
			b.add(alpha);
			sortByBox.add(alpha);
			final JRadioButton date = new JRadioButton("[DATE] property");
			date.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					if(date.getSelectedObjects() != null){
						//select a date sort
						orderType = "Date";
					}
				}
			});
			b.add(date);
			sortByBox.add(date);
			if(orderType.equals("Alphabetic")){
				alpha.setSelected(true);
			}
			else{
				if(orderType.equals("Date")){
					date.setSelected(true);
				}
			}
			sortByBox.setLayout(new BoxLayout(sortByBox, BoxLayout.PAGE_AXIS));
			controlsPanel.add(sortByBox, BorderLayout.SOUTH);
			modeTabs.add("Controls", controlsPanel);
			
			myWindow.add(modeTabs);
			if(resultData.size() == 0){
				//this is a new AutoComplete, show the list and wait for results to be added
				modeTabs.setSelectedIndex(0);
				modeTabs.setEnabled(false);
			}
			else{
				//results have been loaded, load the first and show the results list
				loadResult(0);
				modeTabs.setSelectedIndex(0);
			}
		}
		else{
			myWindow.setVisible(true);
			j.dispose();
		}
	}
	
	public void loadResult(int index){
		Result openResult = resultData.elementAt(index);
		resultNameField.setText(openResult.getName());
		openResultIndex = index;
	}
	
	public void drawProperties(){
		//redraw properties to make sure they always reflect the form
		propPanel.removeAll();
		propertyfields.removeAllElements();
		propertiesStatus.setText("Properties (add/remove in result tab):");
		propPanel.add(propertiesStatus);
		for(int i=0; i < properties.size(); i++){
			JPanel builtProp = new JPanel();
			builtProp.setLayout(new BoxLayout(builtProp, BoxLayout.LINE_AXIS));
			builtProp.add(new JLabel(properties.elementAt(i) + ": "));
			JTextField propText = new JTextField();
			propText.setName(i + "");
			propText.addKeyListener(propertyEditListener);
			propText.addFocusListener(endEditListener);
			propText.setFont(editFont);
			propertyfields.add(propText);
			builtProp.add(propText);
			propPanel.add(builtProp);
		}
		for(int p=0; p<propertyfields.size(); p++){
			propertyfields.elementAt(p).setText(resultData.elementAt(openResultIndex).getProperty(properties.elementAt(p)));
		}
	}
	
	public String saveToString(){
		//store style
		String saveString = "<style>" + vpDocLoader.rewrite(myCSS) + "</style>";
		//store properties that are being viewed / added to results
		if(resultFormatter != null){
			saveString += "<propText>" + vpDocLoader.rewrite(resultFormatter.getText()) + "</propText>";
		}
		else{
			saveString += "<propText>" + vpDocLoader.rewrite(loadedResultFormat) + "</propText>";			
		}
		saveString += "<propertyList>";
		for(int p=0; p<properties.size(); p++){
			saveString += "<property name=\"" + vpDocLoader.rewrite(properties.elementAt(p)) + "\"/>";
		}
		saveString += "</propertyList>";
		//store all possible results
		saveString += "<resultList preferredOrder=\"" + orderType + "\">";
		for(int r=0; r<resultData.size(); r++){
			Result saveResult = resultData.elementAt(r);
			//each result has attributes for its name, video, start time, and end time
			saveString += "<result name=\"" + vpDocLoader.rewrite(saveResult.getName()) + "\" video=\"" + saveResult.getVideoURL() + "\" start=\"" + saveResult.getStart() + "\">";
			Vector<String[]> allProps = saveResult.getAllProperties();
			for(int p=0; p<allProps.size(); p++){
				//include all of the properties ever set for the result
				saveString += "<property name=\"" + vpDocLoader.rewrite(allProps.elementAt(p)[0]) + "\" value=\"" + vpDocLoader.rewrite(allProps.elementAt(p)[1]) + "\"/>";
			}
			saveString += "</result>";
		}
		saveString += "</resultList>";
		return "<autoComplete>" + saveString + "</autoComplete>";
	}
	
	public void initFromString(String saveData){
		Document loadDoc = vpDocLoader.getXMLfromString(saveData);
		myCSS = vpDocLoader.reread(loadDoc.getElementsByTagName("style").item(0).getTextContent());
		loadedResultFormat = vpDocLoader.reread(loadDoc.getElementsByTagName("propText").item(0).getTextContent());
		NodeList mandatoryProps = loadDoc.getElementsByTagName("propertyList").item(0).getChildNodes();
		for(int c=0; c<mandatoryProps.getLength(); c++){
			//in-use properties of results
			properties.add(vpDocLoader.reread(mandatoryProps.item(c).getAttributes().getNamedItem("name").getTextContent()));
		}
		orderType = loadDoc.getElementsByTagName("resultList").item(0).getAttributes().getNamedItem("preferredOrder").getTextContent();
		NodeList results = loadDoc.getElementsByTagName("result");
		for(int r=0; r<results.getLength(); r++){
			//load a result
			NamedNodeMap attributes = results.item(r).getAttributes();
			String resultName = vpDocLoader.reread(attributes.getNamedItem("name").getTextContent());
			String resultURL = attributes.getNamedItem("video").getTextContent();
			float start = Float.parseFloat(attributes.getNamedItem("start").getTextContent());
			NodeList props = results.item(r).getChildNodes();
			Vector<String> propNames = new Vector<String>();
			Vector<String> propValues = new Vector<String>();
			for(int c=0; c<props.getLength(); c++){
				propNames.add(vpDocLoader.reread(props.item(c).getAttributes().getNamedItem("name").getTextContent()));
				propValues.add(vpDocLoader.reread(props.item(c).getAttributes().getNamedItem("value").getTextContent()));
			}
			resultData.add(new Result(resultName, resultURL, start, propNames, propValues));
		}
	}
	
	//used to control the time to point each result to
	protected class AutoCompleteTimeInput extends vpTimeInput{
		public Component getTableCellEditorComponent(JTable t, Object value,
				boolean isSelected, int row, int col) {
			return super.getTableCellWithTime(resultData.elementAt(openResultIndex).getStart());
		}
	}
	
	protected class Result {
		protected String myName;
		protected String videoURL;
		protected float start = 0;
		protected Vector<String[]> myProperties = new Vector<String[]>();
		
		public Result(String n){
			//starting a new Result
			myName = n;
			for(int p=0; p<properties.size(); p++){
				String[] newProp = {properties.elementAt(p), ""};
				myProperties.add(newProp);
			}
		}		
		public Result(String resultName, String resultURL, float s, Vector<String> propNames, Vector<String> propValues) {
			// loading a Result from a project file
			myName = resultName;
			videoURL = resultURL;
			start = s;
			for(int i=0; i<propNames.size(); i++){
				String[] newPair = { propNames.elementAt(i), propValues.elementAt(i) };
				myProperties.add(newPair);
			}
		}
		//each result stores all of the properties it has ever had
		//this allows results to keep their values, even if a property is removed and added again
		public String getProperty(String propName){
			int index = -1;
			for(int p=0; p<myProperties.size(); p++){
				if(propName.equals(myProperties.elementAt(p)[0])){
					index = p;
					break;
				}
			}
			if(index == -1){
				String[] newProp = {propName, ""};
				myProperties.add(newProp);
				return "";
			}
			return myProperties.elementAt(index)[1];
		}
		public void setProperty(String propName, String value){
			int index = -1;
			for(int p=0; p<myProperties.size(); p++){
				if(propName.equals(myProperties.elementAt(p)[0])){
					index = p;
					break;
				}
			}
			if(index == -1){
				String[] newProp = {propName, value};
				myProperties.add(newProp);
			}
			else{
				myProperties.elementAt(index)[1] = value;
			}
		}
		public Vector<String[]> getAllProperties(){ return myProperties; }
		
		public String toString(){ return myName; }
		public String getVideoURL(){ return videoURL; }
		public void setVideo(VideoListing v){ videoURL = v.getURL(); }
		public void setVideoURL(String vURL){ videoURL = vURL; } 
		public String getName(){ return myName; }
		public float getStart(){ return start; }
		public void setStart(float s){ start = s; }
	}

	//CSS methods
	public String[] getCSSOptions() {
		return cssStyles;
	}
	public String[] getOptionNames() {
		return cssStyleNames;
	}
	public String getCurrentCSS() {
		return myCSS;
	}
	public void setCurrentCSS(String set) {
		myCSS = set;
	}
}