import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

//Storylines is a "Make-Your-Own Story" panel which gives users options of how to proceed
//It can be used for interactive storylines, tutorials, or teaching good decisions

public class Storylines extends Supplement implements CSSstyled {

	//CSS default options
	protected static String basicStyle =
		"#prompt{\r\n" +
		"	font-family: arial;\r\n" +
		"	font-size: 14pt;\r\n" +
		"}\r\n" +
		"#optionBox{\r\n" +
		"	padding: 3px;\r\n" +
		"	border: 1px solid black;\r\n" +
		"}\r\n" +
		"#option{\r\n" +
		"	margin-top: 0.2cm;\r\n" +
		"	margin-bottom: 0.2cm;\r\n" +
		"	text-decoration: none;\r\n" +
		"}\r\n" +
		"#option:hover{\r\n" +
		"	background-color: yellow;\r\n" +
		"	text-decoration: underline;\r\n" +
		"}\r\n" +
		"#returnButton{\r\n" +
		"	color:black;\r\n" +
		"	font-size: 10pt;\r\n" +
		"}\r\n";
	protected static String[] cssOptionNames = { "Basic" };
	protected static String[] cssOptions = { basicStyle };
	
	//page elements
	protected JFrame myWindow;
	protected vpVideoSelector videoSelect = new vpVideoSelector(false, false);
	protected JPanel optionsPanel;
	protected JTextArea promptText;
	protected JButton ret;
	
	//data used for moving through pages
	protected Vector<ChoicePage> pageData = new Vector<ChoicePage>();
	protected int viewPage = 0;
	protected Vector<Choice> openChoices = new Vector<Choice>();
	protected Vector<vpVideoSelector> vSelects = new Vector<vpVideoSelector>();
	protected Vector<JTextField> optFields = new Vector<JTextField>();
	protected String retOrder = "";
	
	//refresh list of video links when focus falls on selector
	protected FocusListener videoOptFocus = new FocusListener(){
		public void focusGained(FocusEvent e) {
			vpVideoSelector source = (vpVideoSelector) e.getSource();
			for(int v=0; v<vSelects.size(); v++){
				if(vSelects.elementAt(v).equals(source)){
					source.refresh();
					return;
				}
			}
		}
		public void focusLost(FocusEvent e) {}		
	};
	//change the property data when the property's video is changed
	protected ActionListener videoOptAction = new ActionListener(){
		public void actionPerformed(ActionEvent e) {
			vpVideoSelector source = (vpVideoSelector) e.getSource();
			if(source.getSelectedItem() != null){
				if(!((VideoListing) source.getSelectedItem()).equals(vpVideoSelector.blankVideo)){
					for(int v=0; v<vSelects.size(); v++){
						if(vSelects.elementAt(v).equals(source)){
							openChoices.elementAt(v).setVideo((VideoListing)source.getSelectedItem());
							return;
						}
					}
				}
			}			
		}
	};
	//change the property data when the property's text is changed
	protected KeyListener optEditListener = new KeyListener(){
		public void keyReleased(KeyEvent e) {
			JTextField source = (JTextField) e.getSource();
			for(int f=0; f<optFields.size(); f++){
				if(optFields.elementAt(f).equals(source)){
					openChoices.elementAt(f).setOption(source.getText());
					return;
				}
			}
		}
		public void keyPressed(KeyEvent e) {}
		public void keyTyped(KeyEvent e) {}		
	};
	
	//CSS variables
	protected CSSEditor cssWindow;
	protected String myCSS = basicStyle;
	
	public String getDescription() {
		return "<html><p>The <b>Storylines</b> add-on is similar to a make-your-own-story book.</p><br/><p>Each video comes with a set of options which load other videos.</p><br/><p>This could be used for video tutorials, interactive stories, or to show consequences of decisions.</p></html>";
	}

	public String getHTML(String myID) {
		String buildPage =
		"<style type=\"text/css\">\r\n" + myCSS.replace("#prompt", "#" + myID + "_prompt").replace("#optionBox", "#" + myID + "_prompt li").replace("#option", "#" + myID + "_prompt li a").replace("#returnButton", "input." + myID + "_returnButton") + "</style>\r\n" +
		"<div id=\"" + myID + "_prompt\" class=\"" + myID + "_prompt\"></div>\r\n";
		//the page texts are added to the document in alphabetic order by their associated video
		//this keeps the source from revealing the intended viewing order
		String[] sortArray = new String[pageData.size()];
		for(int s=0; s<pageData.size(); s++){
			sortArray[s] = pageData.elementAt(s).getVideo().getURL() + ":" + s;
		}
		Arrays.sort(sortArray);
		Vector<ChoicePage> randomizedPData = new Vector<ChoicePage>();
		for(int s=0; s<pageData.size(); s++){
			int fromIndex = Integer.parseInt(sortArray[s].substring(sortArray[s].lastIndexOf(":") + 1));
			randomizedPData.add(pageData.elementAt(fromIndex));
		}
		for(int s=0; s<randomizedPData.size(); s++){
			//build a hidden div for each choicePage
			buildPage += "<div id=\"" + myID + "_page_" + randomizedPData.elementAt(s).getVideo().getURL() + "\" style=\"display:none\">\r\n";
			buildPage += randomizedPData.elementAt(s).getPrompt();
			Vector<Choice> options = randomizedPData.elementAt(s).getChoices();
			buildPage += "<ul>\r\n";
			for(int c=0; c<options.size(); c++){
				if((options.elementAt(c).getVideo() != null)&&(options.elementAt(c).getOption() != null)){
					//the options' URLs are given some encoding to keep users from finding an obvious path in the page source
					buildPage += "<li onclick=\"" + myID + "_loadPage('" + encodeURL(options.elementAt(c).getVideo().getURL()) + "', true);\"><a href=\"javascript:void(0);\">" + options.elementAt(c).getOption().replace("\"", "&quot;").replace("\\","&#92") + "</a></li>\r\n";
				}
			}
			buildPage += "</ul>\r\n";
			buildPage += "</div>\r\n";
		}
		return buildPage;
	}
	
	public String encodeURL(String u){
		//so it isn't completely obvious how to navigate the story, the video URLs of choices are altered
		//this isn't secure, though.  It just makes it time-consuming to figure out
		String code = "";
		for(int i=0; i<u.length(); i++){
			code += (int)(u.charAt(i));
			code += "-";
		}
		return code;
	}
	
	public String getIndividualScripts(String myID){
		if(pageData.elementAt(0).getVideo() == null){
			return "First StoryLines point has no video set for it\r\n";
		}
		String buildScript =
		"<script type=\"text/javascript\">\r\n" +
		//pastPages keeps track of which pages the user visited, going back to the origin
		"var " + myID + "_pastPages = new Array();\r\n" +
		"function " + myID + "_loadStorylines(){\r\n";
		buildScript +=
		"	" + myID + "_loadPage(\"" + pageData.elementAt(0).getVideo().getURL() + "\", false);\r\n" +
		"}\r\n" +
		"/* loadPage is called to load information associated with the requested video */\r\n" +
		"function " + myID + "_loadPage(videoCode, needsDecode){\r\n" +
		"	if(needsDecode){\r\n" +
		"		decodedVideo = \"\";\r\n" +
		"		while(videoCode.length > 1){\r\n" +
		"			decodedVideo += String.fromCharCode(videoCode.substring(0, videoCode.indexOf(\"-\")));\r\n" +
		"			videoCode = videoCode.substring(videoCode.indexOf(\"-\") + 1);\r\n" +
		"		}\r\n" +
		"		loadYT(decodedVideo, 0);\r\n" +
		"	}\r\n" +
		"	else{\r\n" +
		"		decodedVideo = videoCode;\r\n" +
		"		cueYT(decodedVideo, 0);\r\n" +
		"	}\r\n" +
		"	" + myID + "_pastPages.push(decodedVideo);\r\n" +
		"	/* get the requested video's page */\r\n" +
		"	pageHTML = document.getElementById(\"" + myID + "_page_\" + decodedVideo).innerHTML;\r\n" +
		"	if(" + myID + "_pastPages.length > 1){\r\n" +
		"		/* add the return button unless this is the first page */\r\n" +
		"		pageHTML += \"<br/><input type='button' value='Return' class='" + myID + "_returnButton' onclick='" + myID + "_revertChoice();'/>\";\r\n"+
		"	}\r\n" +
		"	document.getElementById(\"" + myID + "_prompt\").innerHTML = pageHTML;\r\n" +
		"}\r\n" +
		"/* revertChoice() is called by the return button */\r\n" +
		"function " + myID + "_revertChoice(){\r\n" +
		"	" + myID + "_pastPages.pop();\r\n" +
		"	gotoPage = " + myID + "_pastPages[" + myID + "_pastPages.length - 1];\r\n" +
		"	" + myID + "_pastPages.pop();\r\n" +
		"	" + myID + "_loadPage(gotoPage,false);\r\n" +
		"}\r\n";
		buildScript += "</script>\r\n";
		return buildScript;
	}
	
	public String getLoadScript(String myID){
		return myID + "_loadStorylines();\r\n";
	}

	public void initFromString(String saveData) {
		Document loadDoc = vpDocLoader.getXMLfromString(saveData);
		//load the style
		myCSS = vpDocLoader.reread(loadDoc.getElementsByTagName("style").item(0).getTextContent());
		NodeList cpages = loadDoc.getElementsByTagName("choicePage");
		for(int p=0; p<cpages.getLength(); p++){
			//load each choicePage
			String url = cpages.item(p).getAttributes().getNamedItem("url").getTextContent();
			Vector<String> choices = new Vector<String>();
			Vector<String> vids = new Vector<String>();
			NodeList opts = cpages.item(p).getChildNodes();
			//the first child is the prompt on the page
			String prompt = vpDocLoader.reread(opts.item(0).getTextContent());
			for(int c=1; c<opts.getLength(); c++){
				//the remaining children are choices and the videos they link to
				choices.add(vpDocLoader.reread(opts.item(c).getTextContent()));
				vids.add(opts.item(c).getAttributes().getNamedItem("url").getTextContent());
			}
			pageData.add(new ChoicePage(url, choices, vids, prompt));
		}
	}

	public String saveToString() {
		//save a style
		String saveString = "<style>" + vpDocLoader.rewrite(myCSS) + "</style>";
		for(int p=0; p<pageData.size(); p++){
			//save each choicePage
			saveString += 
			"<choicePage url=\"" + pageData.elementAt(p).getVideo().getURL() + "\">" +
			//save the prompt text for the page
			"<prompt>" + vpDocLoader.rewrite(pageData.elementAt(p).getPrompt()) + "</prompt>";
			Vector<Choice> choices = pageData.elementAt(p).getChoices();
			for(int c=0; c<choices.size(); c++){
				//save the choices for each page, unless no video was selected
				if(choices.elementAt(c).getVideo() != null){
					saveString += "<option url=\"" + choices.elementAt(c).getVideo().getURL() + "\">" + vpDocLoader.rewrite(choices.elementAt(c).getOption()) + "</option>";
				}
			}
			saveString += "</choicePage>";
		}
		return "<storylines>" + saveString + "</storylines>";
	}

	public void setWindow(JFrame j) {
		if(myWindow == null){
			//create the window elements
			myWindow = j;
			myWindow.setTitle("Storylines: " + this.getName());
			myWindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			myWindow.setSize(500,450);
			myWindow.setVisible(true);
			myWindow.setLayout(new BorderLayout());
			cssWindow = new CSSEditor(this);
			
			JPanel topPanel = new JPanel();
				//videoSelect chooses this choicePage's associated video
				//changing this video does not fix links from others
				videoSelect.refresh();
				topPanel.add(videoSelect);
				videoSelect.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						if(viewPage == 0){
							vpVideoSelector vidS = (vpVideoSelector) e.getSource();
							if(vidS.getSelectedItem() != null){
								if(!((VideoListing) vidS.getSelectedItem()).equals(vpVideoSelector.blankVideo)){
									pageData.elementAt(viewPage).setVideo((VideoListing) vidS.getSelectedItem());
								}
							}
						}
					}
				});
				//cssButton edits the CSS style
				JButton cssButton = new JButton("Edit Style");
				cssButton.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						cssWindow.editStyle();
					}
				});
				topPanel.add(cssButton);
			topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
			myWindow.add(topPanel, BorderLayout.NORTH);
			
			JPanel centerPanel = new JPanel();
				//promptText is the prompt on the choicePage
				promptText = new JTextArea();
					promptText.setRows(3);
					promptText.setColumns(30);
					promptText.addKeyListener(new KeyListener(){
						public void keyReleased(KeyEvent e) {
							//when a key is pressed, store any changes
							pageData.elementAt(viewPage).setPrompt(promptText.getText());
						}
						public void keyPressed(KeyEvent e) {}
						public void keyTyped(KeyEvent e) {}
					});
				centerPanel.add(new JScrollPane(promptText));
				
				//optionsPanel is currently empty, but will be used to store all of the options
				optionsPanel = new JPanel();
				optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
				centerPanel.add(new JScrollPane(optionsPanel));
				//newOpt must exist even when there are no options
				JButton newOpt = new JButton("Add Option");
				newOpt.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						pageData.elementAt(viewPage).addChoice(new Choice());
						loadPage(viewPage);
					}
				});
				centerPanel.add(newOpt);
			myWindow.add(centerPanel, BorderLayout.CENTER);
			
			JPanel navPanel = new JPanel();
				//prev and nex review all items, cancel returnOrder
				JButton prev = new JButton("<html><span style='font-size: 16pt;'>&larr;</span></html>");
				prev.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						loadPage(viewPage - 1);
						ret.setEnabled(false);
						retOrder = "";
					}
				});
				navPanel.add(prev);
				
				//ret skips back through the chain of choices that were made
				ret = new JButton("Return");
				ret.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						//algorithm for storing changes, disabling when back at the original page
						retOrder = retOrder.substring(0, retOrder.lastIndexOf(","));
						loadPage(Integer.parseInt(retOrder.substring(retOrder.lastIndexOf(",") + 1)));
						if(retOrder.indexOf(",") == retOrder.lastIndexOf(",")){
							ret.setEnabled(false);
							retOrder = "";
						}
					}
				});
				ret.setEnabled(false);
				navPanel.add(ret);
				
				//prev and nex review all items, cancel returnOrder
				JButton nex = new JButton("<html><span style='font-size: 16pt;'>&rarr;</span></html>");
				nex.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						loadPage(viewPage + 1);
						ret.setEnabled(false);
						retOrder = "";
					}
				});
				navPanel.add(nex);
				navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.X_AXIS));
			myWindow.add(navPanel, BorderLayout.SOUTH);
			
			if(pageData.size() == 0){
				pageData.add(new ChoicePage(""));
			}
			loadPage(0);
		}
		else{
			//restore the previous view
			myWindow.setVisible(true);
			j.dispose();
		}
	}
	
	//load a page by its index in pageData
	protected void loadPage(int targetPage){
		//wrap around, so left of the first item is the last, and the converse
		if(targetPage < 0){
			targetPage = pageData.size() - 1;
		}
		else{
			if(targetPage >= pageData.size()){
				targetPage = 0;
			}
		}
		viewPage = targetPage;
		
		//set window elements to match this choicePage
		VideoListing selectVid = pageData.elementAt(viewPage).getVideo();
		videoSelect.refresh();
		videoSelect.setSelectedItem(selectVid);
		promptText.setText(pageData.elementAt(viewPage).getPrompt());
		drawOptions();
	}
	protected void drawOptions(){
		//redraw the options panel
		Vector<Choice> options = pageData.elementAt(viewPage).getChoices();
		optionsPanel.removeAll();
		openChoices.removeAllElements();
		vSelects.removeAllElements();
		optFields.removeAllElements();
		for(int i=0; i<options.size(); i++){
			//build an nPanel with option editing tools for each choice C
			Choice c = options.elementAt(i);
			openChoices.add(c);
			JPanel nPanel = new JPanel();
			final vpVideoSelector videoOpt = new vpVideoSelector();
				videoOpt.addFocusListener(videoOptFocus);
				videoOpt.addActionListener(videoOptAction);
				videoOpt.refresh();
				VideoListing selectVid = c.getVideo();
				videoOpt.setSelectedItem(selectVid);
				vSelects.add(videoOpt);
			nPanel.add(videoOpt);
			JTextField choiceOpt = new JTextField();
				choiceOpt.setColumns(20);
				choiceOpt.addKeyListener(optEditListener);
				choiceOpt.setText(c.getOption());
				optFields.add(choiceOpt);
			nPanel.add(choiceOpt);
			
			//choiceGo adds a page to its order and jumps to it
			JButton choiceGo = new JButton("Go");
				choiceGo.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						for(int a=0; a<pageData.size(); a++){
							if(pageData.elementAt(a).getVideo() != null){
								if(pageData.elementAt(a).getVideo().equals((VideoListing)videoOpt.getSelectedItem())){
									ret.setEnabled(true);
									if(retOrder.equals("")){
										retOrder = "," + viewPage + "," + a;
									}
									else{
										retOrder += "," + a;
									}
									loadPage(a);
									return;
								}
							}
						}
						pageData.add(new ChoicePage((VideoListing) videoOpt.getSelectedItem()));
						ret.setEnabled(true);
						loadPage(-1);
					}
				});
			nPanel.add(choiceGo);
			nPanel.setLayout(new BoxLayout(nPanel, BoxLayout.LINE_AXIS));
			optionsPanel.add(nPanel);
		}
	}
	
	//a choicePage represents a video to play, a text prompt, and a list of options which jump to more choicePages
	protected class ChoicePage {
		protected VideoListing myVideo;
		protected String prompt = "";
		protected Vector<Choice> options;
		
		public ChoicePage(String url){
			//newly created choicePage
			for(int v=0; v<vpMain.videoData.size(); v++){
				if(vpMain.videoData.elementAt(v).getURL().equals(url)){
					myVideo = vpMain.videoData.elementAt(v);
					break;
				}
			}
			options = new Vector<Choice>();
		}
		public ChoicePage(VideoListing v){
			//newly created choicePage
			myVideo = v;
			options = new Vector<Choice>();
		}
		public ChoicePage(String url, Vector<String> o, Vector<String> u, String p){
			//created from project file
			for(int v=0; v<vpMain.videoData.size(); v++){
				if(vpMain.videoData.elementAt(v).getURL().equals(url)){
					myVideo = vpMain.videoData.elementAt(v);
					break;
				}
			}
			options = new Vector<Choice>();
			for(int i=0; i<u.size(); i++){
				options.add(new Choice(o.elementAt(i), u.elementAt(i)));
			}
			prompt = p;
		}
		public void addChoice(Choice c){
			options.add(c);
		}
		public VideoListing getVideo(){ return myVideo; }
		public void setVideo(VideoListing vl){ myVideo = vl; }
		public String getPrompt(){ return prompt; }
		public void setPrompt(String p){ prompt = p; }
		public Vector<Choice> getChoices(){ return options; }
	}
	
	//a Choice represents an option and the video link for the outcome
	protected class Choice{
		protected String option;
		protected VideoListing myVideo;
		
		public Choice(){
			option = "";
			myVideo = null;
		}
		public Choice(String opt, String url){
			option = opt;
			for(int v=0; v<vpMain.videoData.size(); v++){
				if(vpMain.videoData.elementAt(v).getURL().equals(url)){
					myVideo = vpMain.videoData.elementAt(v);
					break;
				}
			}
		}
		public void setOption(String o){ option = o; }
		public String getOption(){ return option; }
		public void setVideo(VideoListing u){ myVideo = u; }
		public VideoListing getVideo(){ return myVideo; }
	}

	//CSS variables
	public String[] getCSSOptions() {
		return cssOptions;
	}
	public String[] getOptionNames() {
		return cssOptionNames;
	}
	public String getCurrentCSS() {
		return myCSS;
	}
	public void setCurrentCSS(String set) {
		myCSS = set;
	}
}