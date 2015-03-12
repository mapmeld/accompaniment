//Passport displays a message and cues a video, based on how the user got to the page
//For example, responses can be set for people arriving from your NewUsers page, or from a particular topic's page

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class Passport extends Supplement {

	protected JFrame myWindow;
	protected JTabbedPane siteTabs = new JTabbedPane();
	
	//CSS defaults
	protected static String[] styleNames = {"Basic", "Urgent", "Two-Tone"};
	protected static String basicStyle =
		"#message{\r\n" +
		"	text-align: left;\r\n" +
		"	font-family: arial;\r\n" +
		"	font-size: 18pt;\r\n" +
		"	padding-left: 40px;\r\n" +
		"	padding-right: 40px;\r\n" +
		"}\r\n" +
		"input.closebutton{\r\n" +
		"	float:right;\r\n" +
		"}\r\n";
	protected static String urgentStyle =
		"#message{\r\n" +
		"	color: black;\r\n" +
		"	background-color: yellow;\r\n" +
		"	text-align: center;\r\n" +
		"	font-family: arial;\r\n" +
		"	font-weight: bold;\r\n" +
		"	font-size: 20pt;\r\n" +
		"}\r\n" +
		"input.closebutton{\r\n" +
		"	float:right;\r\n" +
		"	background-color: red;\r\n" +
		"	color: white;\r\n" +
		"}\r\n";
	protected static String twotoneStyle =
		"/* Make sure your message has two divs: classes leftSide and rightSide */\r\n" +
		"/* sample: <div class='leftSide'>Left</div><div class='rightSide'>Right</div>*/\r\n" +
		"#message{\r\n" +
		"	border: 2px black solid;\r\n" +
		"	font-family: arial;\r\n" +
		"	font-size: 18pt;\r\n" +
		"	font-weight: bold;\r\n" +
		"}\r\n" +
		"div.leftSide{\r\n" +
		"	display: inline;\r\n" +
		"	background-color: darkgreen;\r\n" +
		"	color: white;\r\n" +
		"	padding-left: 10px;\r\n" +
		"	padding-right: 30px;\r\n" +
		"}\r\n" +
		"div.rightSide{\r\n" +
		"	display: inline;\r\n" +
		"	background-color: white;\r\n" +
		"	color: black;\r\n" +
		"	padding-left: 30px;\r\n" +
		"}\r\n" +
		"input.closebutton{\r\n" +
		"	float:right;\r\n" +
		"	background-color:white;\r\n" +
		"	color:black;\r\n" +
		"}\r\n";
	protected static String[] styles = { basicStyle, urgentStyle, twotoneStyle };
	
	public String getDescription() {
		return "<html><p>Passport displays a welcome message on the top of the page and cues a video.</p><br/><p>Custom messages can be displayed for vistors following links from different websites.</p></html>";
	}

	public String getLoadScript(String myID){
		String loadScript =
			"	/* Passport checks for messages based on what site referred to this page */\r\n" +
			"	if (document.referrer && document.referrer != \"\"){\r\n" +
			"		" + myID + "_checkReferrer(document.referrer);\r\n" +
			"	}\r\n";
		return loadScript;
	}

	public String getIndividualScripts(String myID){
		//writes methods for displaying the welcome message and setting the cookie
		String welcomeScript =
			"<script type=\"text/javascript\">\r\n" +
			"function " + myID + "_checkReferrer(origin){\r\n" +
			"	/* tries to find match */\r\n";
		for(int tab = 0; tab < (siteTabs.getTabCount() -1); tab++){
			sitesTab thisTab = (sitesTab) siteTabs.getComponentAt(tab);
			Vector<String> terms = thisTab.getSiteList();
			if(terms.size() > 0){
				welcomeScript += "	if(";
				for(int site=0; site<terms.size(); site++){
					if(site!=0){ welcomeScript += "||"; }
					welcomeScript += "(origin.indexOf(\"" + terms.elementAt(site).replace("\\", "&#92;").replace("\"", "&quot;") + "\") != -1)\r\n";
				}
				welcomeScript += "){\r\n";
				if(thisTab.isMessageShown()){
					welcomeScript += ("		document.getElementById(\"headerMessage\").innerHTML += (\"");
					//style for the message
					welcomeScript += ("<style type='text/css'>" + thisTab.getCurrentCSS().replace("\n", "\\n").replace("\r", "\\n").replace("#message", "#" + myID + "_messageDiv").replace("\"", "&quote;").replace("input.closebutton", "input." + myID + "_closebutton") + "</style>");
					//HTML written message
					welcomeScript += ("<div id='" + myID + "_messageDiv'>\\n<input type='button' class='" + myID + "_closebutton' value='X' onclick=\\\"document.getElementById('" + myID + "_messageDiv').innerHTML='';\\\"/>\\n" + thisTab.getMessage().replace("\n", "<br/>").replace("\r", "<br/>").replace("\"","\\\"") + "</div>\");\r\n");
				}
				if(thisTab.isVideoCued()){
					if(thisTab.cueListing != null){
						if(!thisTab.cueListing.equals(vpVideoSelector.blankVideo)){
							welcomeScript += ("		cueYT(\"" + thisTab.getCueVideo() + "\",0);\r\n");
						}
					}
				}
				welcomeScript += "	}\r\n";
			}
		}
		welcomeScript +=
			"}\r\n" +
			"</script>";
		return welcomeScript;
	}
	
	public String getHTML(String myID) {
		//the Passport Add-On does not fill its cell
		return "";
	}

	public void initFromString(String saveData) {
		Document loadData = vpDocLoader.getXMLfromString(saveData);
		NodeList tabs = loadData.getElementsByTagName("siteGroup");
		NodeList styles = loadData.getElementsByTagName("style");
		NodeList siteLists = loadData.getElementsByTagName("siteList");
		NodeList messages = loadData.getElementsByTagName("message");
		NodeList videos = loadData.getElementsByTagName("cueVideo");
		for(int tab=0; tab<tabs.getLength(); tab++){
			String style = vpDocLoader.reread(styles.item(tab).getTextContent());
			Vector<String> sites = new Vector<String>();
			if(siteLists.item(tab).hasChildNodes()){
				for(int site=0; site < siteLists.item(tab).getChildNodes().getLength(); site++){
					sites.add(vpDocLoader.reread(siteLists.item(tab).getChildNodes().item(site).getAttributes().getNamedItem("urlpart").getTextContent()));
				}
			}
			String realMessage = vpDocLoader.reread(messages.item(tab).getTextContent());
			boolean showMessage = messages.item(tab).getAttributes().getNamedItem("show").getTextContent().equals("true");
			String vidURL = videos.item(tab).getAttributes().getNamedItem("url").getTextContent().toString();
			boolean cueVideo = videos.item(tab).getAttributes().getNamedItem("cue").getTextContent().equals("true");
			siteTabs.addTab(tabs.item(tab).getAttributes().getNamedItem("name").getTextContent().toString(), new sitesTab(style, sites, realMessage, showMessage, vidURL, cueVideo));
		}
		JButton newTab = new JButton("Add Video Tab");
		newTab.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				//when someone clicks to add the new tab
				siteTabs.insertTab("Group " + siteTabs.getTabCount(), null, new sitesTab(), null, siteTabs.getTabCount() - 1);
			}
		});
		siteTabs.addTab("New", newTab);
	}

	public String saveToString() {
		String saveString = "";
		for(int tab=0; tab < (siteTabs.getTabCount() - 1); tab++){
			sitesTab thisTab = (sitesTab) siteTabs.getComponentAt(tab);
			saveString += "<siteGroup name=\"" + siteTabs.getTitleAt(tab) + "\">";
			saveString += "<style>" + vpDocLoader.rewrite(thisTab.getCurrentCSS()) + "</style>";
			saveString += "<siteList>";
			Vector<String> listedSites = thisTab.getSiteList();
			for(int site=0; site < listedSites.size(); site++){
				saveString += "<site urlpart=\"" + vpDocLoader.rewrite(listedSites.elementAt(site)) + "\"/>";
			}
			saveString += "</siteList>";
			saveString += ("<message show=\"" + thisTab.isMessageShown() + "\">" + vpDocLoader.rewrite(thisTab.getMessage()) + "</message>");
			saveString += ("<cueVideo cue=\"" + thisTab.isVideoCued() + "\" url=\"" + thisTab.getCueVideo() + "\"/>");
			saveString += "</siteGroup>";
		}
		return "<passportData>" + saveString + "</passportData>";
	}

	public void setWindow(JFrame j) {
		if(myWindow == null){
			//define the window's appearance
			myWindow = j;
			myWindow.setTitle("FirstVisit: " + this.getName());
			myWindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			myWindow.setSize(350,400);
			myWindow.setVisible(true);
			
			//add the tabbed pane
			myWindow.add(siteTabs);
			if(siteTabs.getTabCount() == 0){
				siteTabs.add("Group 1", new sitesTab());
				JButton newTab = new JButton("Add Video Tab");
				newTab.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						//when someone clicks to add the new tab
						siteTabs.insertTab("Group " + siteTabs.getTabCount(), null, new sitesTab(), null, siteTabs.getTabCount() - 1);
					}
				});
				siteTabs.addTab("New", newTab);
			}
		}
		else{
			myWindow.setVisible(true);
			j.dispose();
		}
	}
	
	protected class sitesTab extends JPanel implements CSSstyled {
		//window elements
		protected final JTextField siteEntry = new JTextField("");
		protected Vector<String> siteData = new Vector<String>();
		protected JList siteList = new JList(siteData);
		protected JCheckBox displayAMessage = new JCheckBox();
		protected JTextArea myMessage = new JTextArea();
		protected JCheckBox cueAVideo = new JCheckBox();
		protected vpVideoSelector myVideoSelect = new vpVideoSelector();
		//CSS variables
		protected String myCSS = basicStyle;
		protected CSSEditor messageCSSEditor = new CSSEditor(this);
		//load information
		protected String welcomeMessage = null;
		protected VideoListing cueListing = vpVideoSelector.blankVideo;
		
		//loads a completely-new tab
		public sitesTab(){
			drawPanel();
		}
		//load a tab from saved data
		public sitesTab(String style, Vector<String> sites, String realMessage,
				boolean showMessage, String vidURL, boolean cueVideo) {
			drawPanel();
			myCSS = style;
			siteData = sites;
			siteList.setListData(siteData);
			welcomeMessage = realMessage;
			myMessage.setText(welcomeMessage);
			displayAMessage.setSelected(showMessage);
			for(int v=0; v<vpMain.videoData.size(); v++){
				if(vpMain.videoData.elementAt(v).getURL().equals(vidURL)){
					cueListing = vpMain.videoData.elementAt(v);
					myVideoSelect.setSelectedItem(cueListing);
					break;
				}
			}
			cueAVideo.setSelected(cueVideo);
		}

		//draws this tab's elements
		private void drawPanel(){
			//style editing
			JPanel stylePanel = new JPanel();
			stylePanel.setLayout(new GridLayout(3,1));
			messageCSSEditor = new CSSEditor(this);
			JButton styleButton = new JButton("Edit Style");
			styleButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					messageCSSEditor.editStyle();
				}
			});
			stylePanel.add(styleButton);

			JPanel entryPanel = new JPanel();
			siteEntry.setColumns(25);
			entryPanel.add(siteEntry);
			JButton addSite = new JButton("Add");
			addSite.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					siteData.add(siteEntry.getText());
					siteList.setListData(siteData);
					siteEntry.setText("");
				}
			});
			entryPanel.add(addSite);
			stylePanel.add(entryPanel);
			
			JPanel sitePanel = new JPanel();
	    	sitePanel.add(Box.createVerticalStrut(10));
			siteList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	    	siteList.setLayoutOrientation(JList.VERTICAL_WRAP);
	    	siteList.setVisibleRowCount(3);
	    	sitePanel.add(siteList);
	    	stylePanel.add(sitePanel);
			this.add(stylePanel);
	    	
			JPanel videoSelectPanel = new JPanel();
			displayAMessage.setText("Display the following message");
			videoSelectPanel.add(displayAMessage);
			
			//message text
			myMessage = new JTextArea();
			welcomeMessage = "Welcome to the site!  Please watch our welcome video.";
			myMessage.setText(welcomeMessage);
			myMessage.addKeyListener(new KeyListener(){
				public void keyReleased(KeyEvent e) {
					welcomeMessage = myMessage.getText();
				}
				public void keyPressed(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
			myMessage.setRows(4);
			myMessage.setEditable(true);
			videoSelectPanel.add(new JScrollPane(myMessage));
			
			//cue-video select
			myVideoSelect.addFocusListener(new FocusListener(){
				public void focusGained(FocusEvent e) {
					myVideoSelect.refresh();
				}
				public void focusLost(FocusEvent e) {
					myVideoSelect.setSelectedItem(cueListing);
				}
			});
			myVideoSelect.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					if(myVideoSelect.getSelectedItem() != null){
						if(!myVideoSelect.getSelectedItem().equals(vpVideoSelector.blankVideo)){
							cueListing = (VideoListing) myVideoSelect.getSelectedItem();
						}
					}
				}
			});
			myVideoSelect.setMaximumRowCount(7);
			myVideoSelect.refresh();
			videoSelectPanel.add(myVideoSelect);
			
			cueAVideo.setText("Cue this video");
			videoSelectPanel.add(cueAVideo);
			this.add(videoSelectPanel);
			
			this.setLayout(new GridLayout(2,1));
			displayAMessage.setSelected(true);
			cueAVideo.setSelected(true);
		}
		
		//methods for retrieving project data
		public boolean isVideoCued() { return (cueAVideo.getSelectedObjects() != null); }
		public String getCueVideo() { return cueListing.getURL(); }
		public boolean isMessageShown() { return (displayAMessage.getSelectedObjects() != null); }
		public String getMessage() { return welcomeMessage; }
		public Vector<String> getSiteList() { return siteData; }
		
		//CSS data for this group
		public String[] getCSSOptions() { return styles; }
		public String[] getOptionNames() { return styleNames; }
		public String getCurrentCSS() { return myCSS; }
		public void setCurrentCSS(String set) { myCSS = set; }
	}
}