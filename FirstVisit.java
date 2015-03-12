import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.w3c.dom.Document;

//FirstVisit displays a custom message at the top of the page when the user has not previously visited the page
//Note: this message will display every time by some user's browsers (cookies must be enabled)

public class FirstVisit extends Supplement implements CSSstyled {

	//window elements
	protected JFrame myWindow;
	protected JCheckBox displayAMessage = new JCheckBox();
	protected JTextArea myMessage = new JTextArea();
	protected JCheckBox cueAVideo = new JCheckBox();
	protected vpVideoSelector myVideoSelect = new vpVideoSelector();

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

	//CSS settings
	protected String messageCSS = basicStyle;
	protected CSSEditor messageCSSEditor;
	
	//other data
	protected boolean fromSavedData = false;
	protected String welcomeMessage = null;
	protected VideoListing cueListing = vpVideoSelector.blankVideo;
	
	public String getDescription() {
		return "<html><p><b>FirstVisit</b> helps your page recognize first-time visitors and present certain messages or videos.</p><p>For the users to be remembered, their browser must accept cookies.</p></html>";
	}
	
	public String getClassScripts(){
		return (
		"<script type=\"text/javascript\">\r\n" +
		"function findFirstVisit(cookieName){\r\n" +
		"	if (document.cookie.length > 0){\r\n" +
		"		cookieIndex = document.cookie.indexOf(cookieName + \"=\");\r\n" +
		"		if (cookieIndex != -1){\r\n" +
		"			cookieIndex = cookieIndex + cookieName.length + 1;\r\n" +
		"			cookieEnd = document.cookie.indexOf(\";\", cookieIndex);\r\n" +
		"			if (cookieEnd == -1){\r\n" +
		"				cookieEnd = document.cookie.length;\r\n" +
		"			}\r\n" +
		"			return document.cookie.substring(cookieStart, cookieEnd);\r\n" +
		"		}\r\n" +
		"	}\r\n" +
		"	return null;\r\n" +
		"}\r\n" +
		"</script>\r\n");
	}
	
	public String getLoadScript(String myID){
		String loadScript =
			"	/* decides whether to display the FirstVisit message */\r\n" +
			"	/* this message will always display for users with cookies blocked */\r\n" +
			"	" + myID + "_FirstVisit = findFirstVisit('" + myID + "_FirstVisit');\r\n" +
			"	if (" + myID + "_FirstVisit == null){\r\n" +
			"		/* this is the user's first visit to the page */\r\n" +
			"		" + myID + "_welcome();\r\n" +
			"	}\r\n" +
			"	else{\r\n" +
			"		/* already visited; update cookie */\r\n" +
			"		" + myID + "_updateCookie();\r\n" +
			"	}\r\n";
		return loadScript;
	}
	
	public String getIndividualScripts(String myID){
		//writes methods for displaying the welcome message and setting the cookie
		String cookieScript =
			"<script type=\"text/javascript\">\r\n" +
			"function " + myID + "_welcome(){\r\n" +
			"	/* displays the welcome message */\r\n";
			if(displayAMessage.getSelectedObjects() != null){
				cookieScript += ("	document.getElementById(\"headerMessage\").innerHTML += (\"");
				//style for the message
				cookieScript += ("<style type='text/css'>" + messageCSS.replace("\n", "\\n").replace("\r", "\\n").replace("#message", "#" + myID + "_messageDiv").replace("\"", "&quote;").replace("input.closebutton", "input." + myID + "_closebutton") + "</style>");
				//HTML / written message
				cookieScript += ("<div id='" + myID + "_messageDiv'>\\n<input type='button' class='" + myID + "_closebutton' value='X' onclick=\\\"document.getElementById('" + myID + "_messageDiv').innerHTML='';\\\"/>\\n" + welcomeMessage.replace("\n", "\\n").replace("\r", "\\n").replace("\"","\\\"") + "</div>\");\r\n");
			}
			if(cueAVideo.getSelectedObjects() != null){
				if(cueListing != null){
					if(!cueListing.equals(vpVideoSelector.blankVideo)){
						cookieScript += ("	cueYT(\"" + cueListing.getURL() + "\",0);\r\n");
					}
				}
			}
		cookieScript +=(
			"	" + myID + "_updateCookie();\r\n" +
			"}\r\n" +
			"function " + myID + "_updateCookie(){\r\n" +
			"	/* stores the date of this visit until the site is visited again, or 100 days pass */\r\n" +
			"	var endDate = new Date();\r\n" +
			"	endDate.setDate(endDate.getDate() + 100);\r\n" +
			"	document.cookie = (\"" + myID + "_FirstVisit=false;expires=\" + endDate.toGMTString());\r\n" +
			"}\r\n" +
			"</script>\r\n");
		return cookieScript;
	}
	
	public void setWindow(JFrame j) {
		if(myWindow == null){
			//define the window's appearance
			myWindow = j;
			myWindow.setTitle("FirstVisit: " + this.getName());
			myWindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			myWindow.setSize(400,300);
			myWindow.setVisible(true);
			
			JPanel mainPanel = new JPanel();
			
			//style editing
			JPanel stylePanel = new JPanel();
			messageCSSEditor = new CSSEditor(this);
			JButton styleButton = new JButton("Edit Style");
			styleButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					messageCSSEditor.editStyle();
				}
			});
			stylePanel.add(styleButton);
			stylePanel.add(Box.createVerticalStrut(30));
			displayAMessage.setText("Display the following message");
			stylePanel.add(displayAMessage);
			mainPanel.add(stylePanel);
			
			//message text
			myMessage = new JTextArea();
			if(welcomeMessage == null){
				myMessage.setText("Welcome to the site!  Please watch our welcome video.");
			}
			else{
				myMessage.setText(welcomeMessage);
			}
			myMessage.addKeyListener(new KeyListener(){
				public void keyReleased(KeyEvent e) {
					welcomeMessage = myMessage.getText();
				}
				public void keyPressed(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
			myMessage.setRows(4);
			myMessage.setEditable(true);
			mainPanel.add(new JScrollPane(myMessage));
			
			//cue-video select
			JPanel videoSelectPanel = new JPanel();
			cueAVideo.setText("Cue this video");
			videoSelectPanel.add(cueAVideo);
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
			mainPanel.add(videoSelectPanel);
			
			GridLayout mainLayout = new GridLayout(3,1);
			mainPanel.setLayout(mainLayout);
			myWindow.add(mainPanel);
			if(!fromSavedData){
				displayAMessage.setSelected(true);
				cueAVideo.setSelected(true);
			}
		}
		else{
			myWindow.setVisible(true);
			j.dispose();
		}
	}
	
	//save and init preserve the style and message information
	public String saveToString(){
		String saveString = "<style>" + vpDocLoader.rewrite(messageCSS) + "</style>";
		saveString += ("<message show=\"" + (displayAMessage.getSelectedObjects() != null) + "\">" + vpDocLoader.rewrite(welcomeMessage) + "</message>");
		saveString += ("<cueVideo cue=\"" + (cueAVideo.getSelectedObjects() != null) + "\" url=\"" + cueListing.getURL() + "\"/>");
		return "<firstVisit>" + saveString + "</firstVisit>";
	}
	public void initFromString(String saveData){
		fromSavedData = true;
		Document myData = vpDocLoader.getXMLfromString(saveData);
		welcomeMessage = vpDocLoader.reread(myData.getElementsByTagName("message").item(0).getTextContent());
		displayAMessage.setSelected((myData.getElementsByTagName("message").item(0).getAttributes().getNamedItem("show").getTextContent().equals("true")));
		
		String vidURL = myData.getElementsByTagName("cueVideo").item(0).getAttributes().getNamedItem("url").getTextContent();
		cueListing = null;
		if(vidURL != null){
			if(!vidURL.equals("")){
				for(int v=0; v<vpMain.videoData.size(); v++){
					if(vpMain.videoData.elementAt(v).getURL().equals(vidURL)){
						cueListing = vpMain.videoData.elementAt(v);
						myVideoSelect.setSelectedItem(cueListing);
						break;
					}
				}
			}
		}
		if(cueListing == null){
			cueListing = vpVideoSelector.blankVideo;
		}
		cueAVideo.setSelected((myData.getElementsByTagName("cueVideo").item(0).getAttributes().getNamedItem("cue").getTextContent().equals("true")));

		messageCSS = myData.getElementsByTagName("style").item(0).getTextContent();
	}
	
	public String getHTML(String myID) {
		//the FirstVisit Add-On does not fill its cell
		return "";
	}

	public String[] getCSSOptions() {
		return styles;
	}
	public String[] getOptionNames() {
		return styleNames;
	}
	public String getCurrentCSS() {
		return messageCSS;
	}
	public void setCurrentCSS(String set) {
		messageCSS = set;
	}
}