//This is the main file for Accompaniment / VideoPresenter (see vpAboutTab.java)
//Accompaniment writes video mash-up pages from components such as interactive slides and maps
//Currently, the only video service used is the YouTube API

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

public class vpMain extends JFrame implements ActionListener {

	//Set these values if YouTube changes the length of the code after 'watch?v='
	static final int MIN_YOUTUBE_CHARS = 10;
	static final int MAX_YOUTUBE_CHARS = 15;
	
	//suggested reusable styles
    protected static final Font messageFont = new Font("Arial", Font.PLAIN, 16);
    protected static final Font urlFont = new Font("Arial", Font.PLAIN, 12);
    protected static final Font buttonFont = new Font("Arial", Font.BOLD, 16);
    //these colors display to show invalid or potentially valid input
    protected static final Color failColor = new Color(16744272);
    protected static final Color validColor = new Color(16766720);
    
    protected static final Border labelBorder = new EtchedBorder();
	
	//The application represents each stage as a tab
	protected JTabbedPane tabPanel = new JTabbedPane(JTabbedPane.LEFT);
	
	//File tab
	protected JPanel fileFrame = new JPanel();
	protected JLabel fileInfo = new JLabel();
	protected Vector<String> fileOps = new Vector<String>();
	protected String fileInfoFormat = "<html><span style='font-size: 14pt;'>TEXT_HERE</span><br><br></html>";
	protected final JButton openTemplate = new JButton("Import Template");
	protected final JButton saveTemplate = new JButton("Save Template");
	protected final JButton buildPresentation = new JButton("Build Presentation");
	protected JList fileHistory = new JList(fileOps);
	protected final JFileChooser fileWindows = new JFileChooser();
	protected File currentFile = null;
	protected File buildFile = null;

	//Videos tab
	protected vpVideosTab videosFrame = new vpVideosTab();
	public static Vector<VideoListing> videoData = new Vector<VideoListing>();

	//Add-Ons tab
	protected vpAddOnsTab addonsFrame = new vpAddOnsTab();
	public static Vector<Supplement> addonData = new Vector<Supplement>();

	//Page tab
	protected vpPageTab pageFrame = new vpPageTab();
	
	//About tab
	protected vpAboutTab aboutFrame = new vpAboutTab();	
    
    public vpMain(){
        //set up the window
        super("Accompaniment");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	Container c = getContentPane();

    	c.add(tabPanel);

    	//File menu;
    	JLabel fileTitle = new JLabel("<html><span style='font-size: 16pt;'><b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Welcome to Accompaniment&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</b></span><br><br></html>", SwingConstants.CENTER);
    	fileTitle.setBorder(labelBorder);
    	fileFrame.add(fileTitle);
    	fileInfo.setText(fileInfoFormat.replace("TEXT_HERE", "You are editing a new Presentation or Template"));
    	fileInfo.setHorizontalAlignment(SwingConstants.CENTER);
    	fileFrame.add(fileInfo);
    	
    	JPanel fileOptions = new JPanel();
    	fileOptions.setLayout(new FlowLayout());
    	fileOptions.setBorder(new TitledBorder("File Options"));
    	openTemplate.addActionListener(this);
    	fileOptions.add(openTemplate);
    	saveTemplate.addActionListener(this);
    	fileOptions.add(saveTemplate);
    	buildPresentation.addActionListener(this);
    	fileOptions.add(buildPresentation);
    	fileFrame.add(fileOptions);    	
    	fileFrame.add(fileHistory);
    	tabPanel.add(fileFrame);
    	tabPanel.setTabComponentAt(tabPanel.getTabCount()-1,new JLabel("<html><span style='font-size: 16pt;'>File</span><br><br></html>"));
    	fileWindows.setDragEnabled(false);
    	
    	//videos tab
    	tabPanel.add(videosFrame);
    	tabPanel.setTabComponentAt(tabPanel.getTabCount()-1,new JLabel("<html><span style='font-size: 16pt;'>Videos</span><br><br></html>"));

    	//add-ons tab
    	tabPanel.add(addonsFrame);
    	tabPanel.setTabComponentAt(tabPanel.getTabCount()-1,new JLabel("<html><span style='font-size: 16pt;'>Add-Ons</span><br><br></html>"));

    	//Page tab    	
    	tabPanel.add(pageFrame);
    	tabPanel.setTabComponentAt(tabPanel.getTabCount()-1,new JLabel("<html><span style='font-size: 16pt;'>Page</span><br><br></html>"));

    	//About tab
    	tabPanel.add(aboutFrame);
    	tabPanel.setTabComponentAt(tabPanel.getTabCount()-1,new JLabel("<html><span style='font-size: 16pt;'>About</span><br><br></html>"));
    	
    	//set the window's appearance
    	setSize(new Dimension(550,500));
    	setVisible(true);
    }
    
    public static void main(String args[]){
    	//show Nimbus look-and-feel
    	try{
    		UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
    	}
    	catch(Exception e){}
        //start the application by initializing the main window
        new vpMain();
    }

	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(saveTemplate)){
			//save file
			OutputStream savefile = null;
			OutputStreamWriter filewriter = null;
			try{
				try{
					if(currentFile != null){
						if(currentFile.getAbsoluteFile().toString().endsWith(".html")){
							currentFile = new File(currentFile.getAbsoluteFile().toString().substring(0, currentFile.getAbsoluteFile().toString().length() - 5));
						}
						fileWindows.setSelectedFile(currentFile);
					}
					if(fileWindows.getSelectedFile().getName().endsWith(".html")){
						fileWindows.setSelectedFile(new File(fileWindows.getSelectedFile().getName().replace(".html", "")));
					}
				} catch(Exception junk){}
				int returnVal = fileWindows.showSaveDialog(fileHistory);
				if(returnVal == JFileChooser.APPROVE_OPTION){
		            currentFile = fileWindows.getSelectedFile();
		            savefile = new FileOutputStream(currentFile);
		            filewriter = new OutputStreamWriter(savefile);
		            filewriter.write("<?xml version=\"1.0\"?>\r\n");
		            filewriter.write("<!--This XML file is a template generated by Accompaniment/VideoPresenter-->\r\n");
		            filewriter.write("<!--What is Accompaniment?\r\n" +
		            		"Why is this just a template?\r\n" +
		            		"Can I add information to this file?" +
		            		"Find out here: http://advocatesforscience.wiki.zoho.com/VideoPresenter-Project.html -->\r\n");

		            //root element
		            filewriter.write("\r\n<videotemplate>\r\n");
				
		            //writing page template data
		            filewriter.write("\r\n<!--Page Elements and Layout-->\r\n");
		            String pageLayout = pageFrame.getProjectXML();
		            if(pageLayout == null){
		            	filewriter.write("<page/>\r\n");
		            }
		            else{
		            	filewriter.write("<page>\r\n" + pageLayout.replace("<", "&lt;").replace(">", "&gt;") + "</page>\r\n");
		            }
				
		            //writing a video list
		            if(videoData.size() > 0){
		            	filewriter.write("\r\n<!--Importable Videos-->\r\n");
		            	filewriter.write("<!--Use the video's url to fill in the blank: http://youtube.com/watch?v=_____ -->\r\n");
		            	filewriter.write("<videolist>\r\n");
		            	for(int i=0; i<videoData.size(); i++){
		            		VideoListing v = videoData.elementAt(i);
		            		filewriter.write("<video name=\"" + vpDocLoader.rewrite(v.getName()) + "\" url=\"" + v.getURL() + "\"/>\r\n");
		            	}
		            	filewriter.write("</videolist>\r\n");
		            }
		            else{
		            	//no videos have been added
		            	filewriter.write("\r\n<!--This template has no videos-->\r\n");
		            	filewriter.write("<videolist/>\r\n");
		            }
				
		            //writing add-ons data
		            if(addonData.size() > 0){
		            	filewriter.write("\r\n<!--Importable Add-Ons-->\r\n");
		            	filewriter.write("<addonlist>");
		            	for(int i=0; i<addonData.size(); i++){
		            		Supplement a = addonData.elementAt(i);
			            	filewriter.write("\r\n<!--An Add-On of type " + a.getType() + " -->\r\n");
		            		filewriter.write("<addon name=\"" + vpDocLoader.rewrite(a.getName()) + "\" type=\"" + a.getType() + "\">\r\n");
		            		filewriter.write(a.saveToString().replace("<", "&lt;").replace(">", "&gt;"));
		            		filewriter.write("\r\n</addon>\r\n");
		            	}
		            	filewriter.write("</addonlist>\r\n");
		            }
		            else{
		            	filewriter.write("\r\n<!--This template has no Add-Ons-->\r\n");
		            	filewriter.write("<addonlist/>\r\n");
		            }
			
		            //finish the file
		            filewriter.write("\r\n</videotemplate>");
		        	fileInfo.setText(fileInfoFormat.replace("TEXT_HERE", "You are editing template " + currentFile.getName()));
		        	fileOps.add(0,"Project saved: " + currentFile.getName());
		            filewriter.close();
		            savefile.close();
				}
			}
			catch(IOException io){
				//file output failed
				fileOps.add(0,"Failed to save");
	        	fileInfo.setText(fileInfoFormat.replace("TEXT_HERE", "An error occurred while saving the file"));
			}
			finally{
				//nullify all file i/o objects
				filewriter = null;
				savefile = null;
			}
            fileHistory.setListData(fileOps);
			return;
		}
		if(e.getSource().equals(openTemplate)){
			try{
				if(currentFile != null){ fileWindows.setSelectedFile(currentFile); }
				int returnVal = fileWindows.showOpenDialog(fileHistory);
				if(returnVal == JFileChooser.APPROVE_OPTION){
		            File myFile = fileWindows.getSelectedFile();
		            Document doc = vpDocLoader.getXMLfromFile(myFile);
	            
		            //load videos
			        NodeList videos = doc.getElementsByTagName("video");
			        if(videos.getLength() > 0){
			            //videos included
			           	for(int i=0; i<videos.getLength(); i++){
			           		NamedNodeMap nnm = videos.item(i).getAttributes();
			           		videosFrame.addToVideoList(vpDocLoader.reread(nnm.getNamedItem("name").getTextContent()), nnm.getNamedItem("url").getTextContent());
			           	}
			        }
			     
			        //load add-ons
			        NodeList addons = doc.getElementsByTagName("addon");
			        int addonOffset = addonData.size();
			        if(addons.getLength() > 0){
			            //addons included
			           	for(int i=0; i<addons.getLength(); i++){
			           		NamedNodeMap nnm = addons.item(i).getAttributes();
			           		Supplement loader = addonsFrame.initSupplement(nnm.getNamedItem("type").getNodeValue());
			           		loader.initFromString(addons.item(i).getTextContent().replace("&lt;", "<").replace("&gt;", ">"));
			           		loader.setName(vpDocLoader.reread(nnm.getNamedItem("name").getTextContent()));
			           		addonsFrame.addSupplement(loader);
			           	}
			        }
			        
			        NodeList page = doc.getElementsByTagName("page");
			        if(page != null){
			        	if(page.item(0).getTextContent() != null){
			        		if(pageFrame.getProjectXML() == null){
			        			//no current page layout, so use imported one
			        			pageFrame.useProjectXML(page.item(0).getTextContent().replace("&lt;", "<").replace("&gt;", ">"), addonOffset);
			        		}
			        	}
			        }
			        
			        fileOps.add(0, "Imported: " + myFile.getName());
			        fileInfo.setText(fileInfoFormat.replace("TEXT_HERE", "Data imported from " + myFile.getName()));
				}
			}
			catch(Exception io){				
				//file input failed
	        	fileInfo.setText(fileInfoFormat.replace("TEXT_HERE", "File failed to load; may not exist"));
			}
            fileHistory.setListData(fileOps);
			return;
		}
		if(e.getSource().equals(buildPresentation)){
			//make an HTML webpage of the project
			OutputStream savefile = null;
			OutputStreamWriter filewriter = null;
			if((buildFile == null)&&(currentFile != null)){
				if(!currentFile.getAbsoluteFile().toString().endsWith(".html")){
					buildFile = new File(currentFile.getAbsoluteFile() + ".html");
				}
			}
			else{
				if(fileWindows.getSelectedFile() != null){
					if(!fileWindows.getSelectedFile().getName().endsWith(".html")){
						buildFile = new File(fileWindows.getSelectedFile().getName() + ".html");
					}
				}
			}
			try{
				try{
					if(buildFile != null){ fileWindows.setSelectedFile(buildFile); }
				} catch(Exception junk){}
				int returnVal = fileWindows.showSaveDialog(fileHistory);
				if(returnVal == JFileChooser.APPROVE_OPTION){
		            buildFile = fileWindows.getSelectedFile();
		            savefile = new FileOutputStream(buildFile);
		            filewriter = new OutputStreamWriter(savefile);
		            //headers and file Q&A
		            filewriter.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\r\n");
		            filewriter.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\r\n");
		            filewriter.write("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\r\n\r\n");
		            filewriter.write(
		            "<!--This XHTML file was written with Accompaniment, a free resource from Student Advocates for Science-->\r\n"+
		            "<!--DISCLAIMER: Student Advocates for Science is NOT responsible for content, users, or applications-->\r\n" +
		            "<!--DISCLAIMER2: YouTube is not affiliated with Student Advocates for Science or this website-->\r\n" +
		            "<!--What is Accompaniment?\r\n" +
		            "-Can I add information to this file?\r\n" +
		            "-Why are the videos not loading?\r\n" +
		            ">Find out here: http://advocatesforscience.wiki.zoho.com/VideoPresenter-Page.html -->\r\n");
		            //write header
		            filewriter.write(
		            "<head>\r\n" +
		            "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\r\n\r\n" +
		            //write title
		            "<title>" + buildFile.getName() + "</title>\r\n\r\n");

		            //write scripts to import (starting with the YouTube API's Chromeless Player controls)
		            filewriter.write(
		            "<!--Importing the YouTube JavaScript API-->\r\n" +
		            "<script type=\"text/javascript\" src=\"http://swfobject.googlecode.com/svn/tags/rc3/swfobject/src/swfobject.js\"></script>\r\n\r\n" +
		            //write YUI scripts to import (for slider)
		            "<!--Importing the Slider control from YUI-->\r\n" +
		            "<script src=\"http://yui.yahooapis.com/2.5.2/build/yahoo-dom-event/yahoo-dom-event.js\" ></script>\r\n" +
		            "<script src = \"http://yui.yahooapis.com/2.5.2/build/dragdrop/dragdrop-min.js\" ></script>\r\n" +
		            "<script src = \"http://yui.yahooapis.com/2.5.2/build/slider/slider-min.js\" ></script>\r\n\r\n");
		            
		            //load top-level scripts for add-ons
		            //each type can have a scripted version and a null version, but multiple scripted versions are not allowed 
		            Vector<String> repeatAddons = new Vector<String>();
		            for(int i=0; i<addonData.size(); i++){
		            	String scripts = addonData.elementAt(i).getClassScripts();
		            	if(scripts != null){
		            		//make any security checks here
		            		if(!repeatAddons.contains(addonData.elementAt(i).getType())){
		            			repeatAddons.add(addonData.elementAt(i).getType());
		            			filewriter.write("\r\n<!--Importing JavaScript for all " + addonData.elementAt(i).getType() + " add-ons-->\r\n");
		            			filewriter.write(scripts + "\r\n");
		            		}
		            	}
		            }
		            
		            filewriter.write(
		            "<script type=\"text/javascript\">\r\n" + 
		            "var autoslide = true;\r\n" +
		            "var vidLength;\r\n" +
		            "var nowTime;\r\n" +
		            "var nowVideoUrl;\r\n" +
		            "var nowVideoState;\r\n");
		            
		            //storing the player when ready
		            filewriter.write(
		            "/* Readying page for use once the video player is loaded */\r\n" +
		            "function onYouTubePlayerReady(playerid){\r\n"+
		            "	player = document.getElementById(playerid);\r\n"+
		            "	player.addEventListener(\"onStateChange\", \"onplayerStateChange\");\r\n"+
		            "	setInterval(adjustTime, 250);\r\n" +
		            "	loadAddons();\r\n" +
		            "}\r\n");
		            
		            //command for jumping to a YouTube video URL and start time
		            filewriter.write(
				    "/* load and play a YouTube video */\r\n" +
		            "function loadYT(url, startSeconds){\r\n"+
		            "	if (player) {\r\n"+
		            "		if(url == getVideoUrl()){\r\n"+
		            "			seekTo(startSeconds);\r\n"+
		            "		}\r\n"+
		            "		else{\r\n"+
		            "			nowVideoUrl = url;\r\n" +
		            "			player.loadVideoById(url, startSeconds);\r\n"+
		            "		}\r\n"+
		            "	}\r\n"+
		            "}\r\n");
		            
		            //command for cueing a YouTube video URL and start time
		            filewriter.write(
				    "/* load and cue a YouTube video */\r\n" +
				    "function cueYT(url, startSeconds){\r\n"+
				    "	if (player) {\r\n"+
				    "		nowVideoUrl = url;\r\n" +
				    "		player.cueVideoById(url, startSeconds);\r\n"+
				    "	}\r\n"+
				    "}\r\n");		            
		            
		            //command for jumping to a time within the YouTube video
		            filewriter.write(
		            "/* jump to a seconds-value in a YouTube video */\r\n" +
		            "function seekTo(seconds) {\r\n"+
		            "    if (player) {\r\n"+
		            "		player.seekTo(seconds, true);\r\n"+
		            "    }\r\n"+
		            "}\r\n"+
		            
		            //command for returning the video's URL
		           	"function getVideoUrl() {\r\n" +
		           	"    if (player) {\r\n" +
		           	"        return player.getVideoUrl();\r\n" +
		           	"    }\r\n" +
		           	"}\r\n" +
		           	
		           	//commands that control playback and video controls
				    "/* switch between Pause and Play buttons */\r\n" +
		           	"function onplayerStateChange(newState){\r\n" +
		           	"	nowVideoState = newState;\r\n" +
		           	"	if(newState == 1){\r\n" +
		           	"		document.getElementById(\"playpausebutton\").src = \"http://sugo-katta.appspot.com/pauseButton.png\";\r\n" +
		           	"		return;\r\n" +
		           	"	}\r\n" +
		           	"	else{\r\n" +
		           	"		document.getElementById(\"playpausebutton\").src = \"http://sugo-katta.appspot.com/playButton.png\";\r\n" +
		           	"		return;\r\n" +
		           	"	}\r\n" +
		           	"}\r\n" +
		           	"function playpause(){\r\n" +
		           	"	if (player.getPlayerState() == 1){\r\n" +
		           	"		player.pauseVideo();\r\n" +
		           	"		return;\r\n" +
		           	"	}\r\n" +
		           	"	if(player.getPlayerState() == 2){\r\n" +
		           	"		player.playVideo();\r\n" +
		           	"		return;\r\n" +
		           	"	}\r\n" +
		           	"}\r\n" +
		           	//moving the time slider to match current time
		           	"/* interacting with the time slider */\r\n" +
		           	"function adjustTime(){\r\n" +
		           	"	if(player && autoslide){\r\n" +
		           	"		if (nowVideoState == 1){\r\n" +
		           	"			/* executes while playing */\r\n" +
		           	"			vidLength = player.getDuration();\r\n" +
		           	"			nowTime = player.getCurrentTime();\r\n" +
		           	"			nowPoint = 400 * (nowTime / vidLength);\r\n" +
		           	"			slider.setValue(nowPoint, true, true, true);\r\n" +
		           	"			refreshPage();\r\n" +
		           	"		}\r\n" +
		           	"		else{\r\n" +
		           	"			if(nowVideoState == 0){\r\n" +
		           	"				/* keep updates going after end of video */\r\n" +
		           	"				refreshPage();\r\n" +
		           	"			}\r\n" +
		           	"		}\r\n" +
		           	"	}\r\n" +
		           	"}\r\n" +
		           	//setting time to match user's movement of the slider
		           	"function setVidTime(){\r\n" +
		           	"	autoslide = true;\r\n" +
		           	"	if(player){\r\n" +
		           	"		var setPoint = (slider.getValue() / 400) * vidLength;\r\n" +
		          	"		player.seekTo(setPoint,true);\r\n" +
		          	"	}\r\n" +
		          	"}\r\n" +
		          	"function disableAutoSlide(){\r\n" +
		           	"	autoslide = false;\r\n" +
		           	"}\r\n" +
		           	"</script>\r\n" + 
		           	"<!--Pagewide Styles-->\r\n" +
		           	"<style type=\"text/css\">\r\n" + pageFrame.getPageCSS() + "</style>\r\n" +
		           	"</head>\r\n" +
		           	"<body>\r\n");
		            
		            //write add-ons
			        filewriter.write(pageFrame.getHTML());
			        //YouTube requests that you include a "Powered by YouTube" badge that goes to www.youtube.com
			        //See full guidelines here: http://code.google.com/apis/youtube/branding.html
		            filewriter.write("\r\n" +
		            "<!--This website is powered by the YouTube API (see DISCLAIMER2 at page top) (http://code.google.com/apis/youtube/)-->\r\n" +
		            "<!--A \"Powered by YouTube\" badge appear on all pages using the YouTube API-->\r\n" +
		            "<!--See full guidelines and other styles here: http://code.google.com/apis/youtube/branding.html-->\r\n" +
		            "<a href=\"http://www.youtube.com/\"><img src=\"http://sugo-katta.appspot.com/YouTubePower.jpg\" style=\"float: right;\"/></a>\r\n" +
		            "</body>\r\n" +
		            "</html>");
		            filewriter.close();
		            savefile.close();
		            fileOps.add(0, "Built: " + buildFile.getName());
				}
			}
			catch(IOException io){
				//file output failed
				fileOps.add(0, "Build failed");
	        	fileInfo.setText(fileInfoFormat.replace("TEXT_HERE", "An error occurred while building the file"));
			}
			finally{
				//nullify all file i/o objects
				filewriter = null;
				savefile = null;
			}
            fileHistory.setListData(fileOps);
			return;
		}
	}
}