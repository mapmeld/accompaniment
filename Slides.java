import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

//Slides displays a series of pictures synchronized with a set of videos
//The user can move forward or backward in the slides to control the video
//A set of slides with different videos functions as a playlist

public class Slides extends Supplement implements CSSstyled {

	//CSS styles
	protected static final String basicStyle =
	"/* centering the image and caption */" +
	"#slidePic {\r\n" +
	"	width: 400px;\r\n" +
	"	height: 350px;\r\n" +
	"	/* centering the image-div */\r\n" +
	"	text-align:center;\r\n" +
	"}\r\n" +
	"#caption{\r\n" +
	"	font-family: arial;\r\n" +
	"	font-size: 14pt;\r\n" +
	"	padding-left: 8px;\r\n" +
	"	padding-right: 8px;\r\n" +
	"	height: 25pt;" +
	"}\r\n" +
	"#navButton{\r\n" +
	"	font-size: 16pt;\r\n" +
	"	font-weight: bold;\r\n" +
	"}\r\n";
	protected static final String blackStyle =
		"/* centering the image and caption */" +
		"#slidePic {\r\n" +
		"	width: 400px;\r\n" +
		"	height: 350px;\r\n" +
		"	border: 2px solid black;\r\n" +
		"	/* centering the image-div */\r\n" +
		"	text-align:center;\r\n" +
		"}\r\n" +
		"#caption{\r\n" +
		"	font-family: arial;\r\n" +
		"	font-size: 14pt;\r\n" +
		"	color: white;\r\n" +
		"	background-color: black;\r\n" +
		"	height: 25pt;" +
		"	border-left: 1px solid white;\r\n" +
		"	border-right: 1px solid white;\r\n" +
		"	border-bottom: 1px solid white;\r\n" +
		"	padding-left: 8px;\r\n" +
		"	padding-right: 8px;\r\n" +
		"}\r\n" +
		"#navButton{\r\n" +
		"	color: white;\r\n" +
		"	background-color: #000033;\r\n" +
		"	border 1px solid orange;\r\n" +
		"	font-size: 16pt;\r\n" +
		"	font-weight: bold;\r\n" +
		"}\r\n";
	protected static String[] stylenames = {"Basic", "Black" };
	protected static String[] styles = {basicStyle, blackStyle };
	
	//defining window elements
	protected JFrame myWindow;
	protected CSSEditor cssWindow;
	protected String myCSS = basicStyle;
	protected Vector<SlideItem> slideData = new Vector<SlideItem>();
	protected JLabel slidePreview = new JLabel();
	protected JTextField fileInput = new JTextField();
	protected JTextField caption = new JTextField();
	protected vpVideoSelector videoSelect = new vpVideoSelector();
	protected JTable timeTable;

	protected String numberRule = null;	// numberRule contains the general rule for slide pictures (such as picsite.com/slidepic##.jpg)
	protected int viewSlide = -1;		// viewSlide is the current slide

	public String getDescription() {
		return "<html><p>The <b>Slides</b> add-on puts a series of slides (currently images) alongside the video, set to show at the correct time</p><br><p>This is particularly useful for showing additional information or a series of related slides.</p></html>";
	}

	public String getHTML(String myID) {
		String buildString = "<style>\r\n" + myCSS.replace("slidePic", myID + "slidePic").replace("caption", myID + "slideBar").replace("#navButton", "input." + myID + "navButton") + "</style>\r\n";
		buildString += (
				//slidePic contains the slide's image
				"<img id=\"" + myID + "slidePic\"/>\r\n" +
				//slideBar contains the buttons for changing the slide and a caption
				"<div id=\"" + myID + "slideBar\">\r\n" +
				"	<input type=\"button\" style=\"float:left;\" class=\"" + myID + "navButton\" value=\"&larr;\" alt=\"previous\" onclick=\"" + myID + "_previousSlide();\"/>\r\n" +
				"	<input type=\"button\" style=\"float:right;\" class=\"" + myID + "navButton\" value=\"&rarr;\" alt=\"next\" onclick=\"" + myID + "_nextSlide();\"/>\r\n" +
				"	<span id=\"" + myID + "captiondiv\"></span>\r\n" +
				"</div>\r\n");
		return buildString;
	}
	
	public String getLoadScript(String myID){
		return "	" + myID + "_initSlides();\r\n";
	}
	
	//every quarter-second, each instance of Slides gets the update() call
	public int refreshType(){ return Supplement.EACH_REFRESHES; }
	
	public String getIndividualScripts(String myID){
		String buildString =
		"<script type=\"text/javascript\">\r\n" +
		"/* storing data about each slide */\r\n" +
		"var " + myID + "_slideVideos = new Array();\r\n" +		//contains the unique part of the video URL
		"var " + myID + "_slideStarts = new Array();\r\n" +		//contains the time where a slide begins to be displayed
		"var " + myID + "_slideEnds = new Array();\r\n" +		//if the video time is after this value, the next slide is called.  Usually null (automatically switching when the next slide's time starts)
		"var " + myID + "_slideCaptions = new Array();\r\n" +	//contains captions for each slide
		"var " + myID + "_slideFiles = new Array();\r\n" +		//contains the URL location of slide images
		"/* variables used in the slide-change algorithm */\r\n" +
		"var " + myID + "_currentSlide = 0;\r\n" +				//the currently-displayed slide
		"var " + myID + "_manualSlideChange = false;\r\n" +		//true when the slide should stay fixed (because this slide is selected to be the next one)
		//initSlides adds each slide
		"function " + myID + "_initSlides(){\r\n";
		for(int s=0; s<slideData.size(); s++){
			SlideItem thisSlide = slideData.elementAt(s);
			if((thisSlide.getVideo() != null)&&(thisSlide.getFile() != null)){
				String endValue = thisSlide.getEnd() + "";
				if(thisSlide.getEnd() < 0.1){ endValue = "null"; }
				String captionValue = thisSlide.getCaption();
				if(captionValue == null){
					captionValue = "";
				}
				//escape any double-quotes
				captionValue.replace("\"", "\\\"");
				buildString += ("	" + myID + "_addSlide(\"" + thisSlide.getVideo() + "\"," + thisSlide.getStart() + "," + endValue + ",\"" + captionValue.replace("\\", "&#92;").replace("\"", "&quot;") + "\",\"" + thisSlide.getFile().replace("\\", "&#92;").replace("\"", "&quot;") + "\");\r\n");
			}
		}
		buildString += ("}\r\n" +
		//addSlide stores a slide's data
		"/* placing slide information into arrays */\r\n" +
		"function " + myID + "_addSlide(video, start, end, caption, file){\r\n" +
		"	if(" + myID + "_slideVideos.length == 0){\r\n" +
		"		/* put the first image as the slide image */\r\n" +
		"		document.getElementById(\"" + myID + "slidePic\").src = file;\r\n" +
		"		cueYT(video, start);\r\n" +
		"		document.getElementById(\"" + myID + "captiondiv\").innerHTML = caption;\r\n" +
		"	}\r\n" +
		"	" + myID + "_slideVideos.push(video);\r\n" +
		"	" + myID + "_slideStarts.push(start);\r\n" +
		"	" + myID + "_slideEnds.push(end);\r\n" +
		"	" + myID + "_slideCaptions.push(caption);\r\n" +
		"	" + myID + "_slideFiles.push(file);\r\n" +
		"}\r\n" +
		//called to move to the previous slide
		"/* moves to previous slide */\r\n" +
		"function " + myID + "_previousSlide(){\r\n" +
		"	if(" + myID + "_currentSlide > 0){\r\n" +
		"		" + myID + "_currentSlide--;\r\n" +
		"		loadYT(" + myID + "_slideVideos[" + myID + "_currentSlide], " + myID + "_slideStarts[" + myID + "_currentSlide]);\r\n" +
		"		document.getElementById(\"" + myID + "slidePic\").src = " + myID + "_slideFiles[" + myID + "_currentSlide];\r\n" +
		"		document.getElementById(\"" + myID + "captiondiv\").innerHTML = " + myID + "_slideCaptions[" + myID + "_currentSlide];\r\n" +
		"	}\r\n" +
		"	" + myID + "_manualSlideChange = true;\r\n" +
		"}\r\n" +
		//called to move to the next slide
		"/* moves to next slide */\r\n" +
		"function " + myID + "_nextSlide(){\r\n" +
		"	if(" + myID + "_currentSlide < (" + myID + "_slideFiles.length - 1)){\r\n" +
		"		" + myID + "_currentSlide++;\r\n" +
		"		loadYT(" + myID + "_slideVideos[" + myID + "_currentSlide], " + myID + "_slideStarts[" + myID + "_currentSlide]);\r\n" +
		"		document.getElementById(\"" + myID + "slidePic\").src = " + myID + "_slideFiles[" + myID + "_currentSlide];\r\n" +
		"		document.getElementById(\"" + myID + "captiondiv\").innerHTML = " + myID + "_slideCaptions[" + myID + "_currentSlide];\r\n" +
		"	}\r\n" +
		"	" + myID + "_manualSlideChange = true;\r\n" +
		"}\r\n" +
		//called every quarter-second to decide which slide should be displayed
		"/* called to check for slide updates */\r\n" +
		"function " + myID + "_update(){\r\n" +
		"	var estimatedSlide = null;\r\n" +
		"	/* 'estimate' the slide expected for the current video and time */\r\n" +
		"	for(var s=0; s < " + myID + "_slideFiles.length; s++){\r\n" +
		"		if(" + myID + "_slideVideos[s] == nowVideoUrl){\r\n" +
		"			estimatedSlide = s;\r\n" +
		"			if(" + myID + "_slideStarts[s] < nowTime){\r\n" +
		"				/* a slide must end at the end of a video */\r\n" +
		"				var slideEnd = vidLength;\r\n" +
		"				if(" + myID + "_slideEnds[s] != null){\r\n" +
		"					/* if the slide's end is not null, use that value */\r\n" +
		"					slideEnd = " + "_slideEnd[s];\r\n" +
		"				}\r\n" +
		"				else{\r\n" +
		"					if(s < (" + myID + "_slideFiles.length - 1)){\r\n" +
		"						if(" + myID + "_slideVideos[s] == " + myID + "_slideVideos[s+1]){\r\n" +
		"							/* if the next slide is in the same video, this slide ends when the next slide starts */\r\n" +
		"							slideEnd = " + myID + "_slideStarts[s+1];\r\n" +
		"						}\r\n" +
		"					}\r\n" +
		"				}\r\n" +
		"				if(slideEnd > nowTime){\r\n" +
		"					/* estimate this slide if video time is within the slide's time ranges */\r\n" +
		"					estimatedSlide = s;\r\n" +
		"					break;\r\n" +
		"				}\r\n" +
		"			}\r\n" +
		"		}\r\n" +
		"	}\r\n" +
		"	if(" + myID + "_slideVideos[" + myID + "_currentSlide] == nowVideoUrl){\r\n" +
		"		if((nowVideoState == 0)&&(" + myID + "_currentSlide < (" + myID + "_slideFiles.length - 1))){\r\n" +
		"			/* at the end of a slide's video, jump to the next slide */\r\n" +
		"			" + myID + "_nextSlide();\r\n" +
		"		}\r\n" +
		"	}\r\n" +
		//switching the slide when the estimated one is different from the current one
		"	if(estimatedSlide != null){\r\n" +
		"		if(estimatedSlide != " + myID + "_currentSlide){\r\n" +
		"			/* the current slide does not match the estimated one */\r\n" +
		"			if(!" + myID + "_manualSlideChange){\r\n" +
		"				/* the difference is not due to a commanded switch, so update the slide */\r\n" +
		"				" + myID + "_currentSlide = estimatedSlide;\r\n" +
		"				document.getElementById(\"" + myID + "slidePic\").src = " + myID + "_slideFiles[estimatedSlide];\r\n" +
		"				document.getElementById(\"" + myID + "captiondiv\").innerHTML = " + myID + "_slideCaptions[estimatedSlide];\r\n" +
		"			}\r\n" +
		"			return;\r\n" +
		"		}\r\n" +
		"	}\r\n" +
		"	else{\r\n" +
		"		/* no slide could be estimated */\r\n" +
		"		return;\r\n" +
		"	}\r\n" +
		//if the estimated slide is the same as the current slide, it is no longer necessary to fix the current slide in place
		"	/* the current slide matches the estimated one - no longer commanded to show this particular slide */\r\n" +
		"	" + myID + "_manualSlideChange = false;\r\n" +
		"}\r\n");
		buildString += "</script>\r\n";
		return buildString;
	}

	public void initFromString(String saveData) {
		Document saveDoc = vpDocLoader.getXMLfromString(saveData);
		
		//load the saved CSS style
		NodeList myStyles = saveDoc.getElementsByTagName("style");
		if(myStyles.getLength() == 1){
			myCSS = vpDocLoader.reread(myStyles.item(0).getTextContent());
		}
		
		//generate each SlideItem from XML attributes
		NodeList slides = saveDoc.getElementsByTagName("slide");
		for(int s=0; s<slides.getLength(); s++){
			NamedNodeMap nnm = slides.item(s).getAttributes();
			
			String video = nnm.getNamedItem("video").getTextContent();
			float start = Float.parseFloat(nnm.getNamedItem("start").getTextContent());
			float end = Float.parseFloat(nnm.getNamedItem("end").getTextContent());
			String caption = vpDocLoader.reread(nnm.getNamedItem("caption").getTextContent());
			String fileAddress = vpDocLoader.rewrite(nnm.getNamedItem("file").getTextContent());
		
			//prevent empty values from being displayed as "null"
			if(video.equals("null")){ video = null; }
			if(caption.equals("")){ caption = null; }
			if(fileAddress.equals("")){ fileAddress = null; }
			
			slideData.add(new SlideItem(video, start, end, caption, fileAddress));
		}
	}

	//saveToString stores the slides in XML format
	public String saveToString() {
		//save the current CSS style
		String saveString = "<style>" + vpDocLoader.rewrite(myCSS) + "</style>";
		//save each slide's data
		for(int s=0; s<slideData.size(); s++){
			SlideItem thisSlide = slideData.elementAt(s);
			saveString += ("<slide video=\"" + thisSlide.getVideo() + "\" start=\"" + thisSlide.getStart() + "\" end=\"" + thisSlide.getEnd() + "\" caption=\"" + vpDocLoader.rewrite(thisSlide.getCaption()) + "\" file=\"" + vpDocLoader.rewrite(thisSlide.getFile()) + "\"/>");
		}
		return "<slideData>" + saveString + "</slideData>";
	}

	public void setWindow(JFrame j) {
		if(myWindow == null){
			//create the window elements
			myWindow = j;
			myWindow.setTitle("Slides: " + this.getName());
			myWindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			myWindow.setSize(500,450);
			myWindow.setVisible(true);
			myWindow.setLayout(new BorderLayout());
			
			//the top row (file address input and enter button)
			JPanel fileSetter = new JPanel();
			fileSetter.setLayout(new BoxLayout(fileSetter, BoxLayout.X_AXIS));
			fileInput.setText("http://");
			fileInput.setColumns(30);
			fileSetter.add(fileInput);
			
			JButton enterFile = new JButton("Enter");
			enterFile.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					slideData.elementAt(viewSlide).setFile(fileInput.getText().trim());
				}
			});
			fileSetter.add(enterFile);
			myWindow.add(fileSetter, BorderLayout.NORTH);
			
			JPanel midPanel = new JPanel();
			midPanel.setLayout(new FlowLayout());
			//deleting and styling slides
			JPanel modePanel = new JPanel();
			JButton deleteSlide = new JButton("Delete this Slide");
			deleteSlide.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					if(slideData.size() > 1){
						slideData.removeElementAt(viewSlide);
						viewSlide--;
						if(viewSlide == (slideData.size() - 1)){
							viewSlide--;
						}
						loadSlide(viewSlide+1);
					}
				}
			});
			modePanel.add(deleteSlide);
			//the css editor button
			cssWindow = new CSSEditor(this);
			JButton cssButton = new JButton("Edit Style");
			cssButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					cssWindow.editStyle();
				}
			});
			modePanel.add(cssButton);
			midPanel.add(modePanel);
			
			midPanel.add(Box.createVerticalStrut(10));
			
			//slide information input
			JPanel infoPanel = new JPanel();
			infoPanel.setLayout(new FlowLayout());
			//videoSelect is a drop-down list for selecting a video
			videoSelect.addFocusListener(new FocusListener(){
				public void focusGained(FocusEvent e) {
					videoSelect.refresh();
				}
				public void focusLost(FocusEvent e) {}
			});
			videoSelect.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					if(videoSelect.getSelectedItem() != null){
						if(viewSlide >= 0){
							slideData.elementAt(viewSlide).setVideo(((VideoListing)videoSelect.getSelectedItem()).getURL());
						}
					}
				}
			});
			videoSelect.refresh();
			infoPanel.add(videoSelect);
			//times sets the range of time in which the slide appears
			TableModel times = new AbstractTableModel(){
				public int getColumnCount() { return 2; }
				public int getRowCount() { return 1; }
				public boolean isCellEditable(int row, int col){ return true; }
				public Object getValueAt(int row, int col) {
					try{
						if(col == 0){
							return displayTime(slideData.elementAt(viewSlide).getStart());
						}
						if(col == 1){
							float end = slideData.elementAt(viewSlide).getEnd();
							if(end < 0.1){
								return "Auto";
							}
							else{
								return displayTime(end);
							}
						}
					}
					catch(Exception e){}
					return null;
				}
				public void setValueAt(Object value, int row, int col) {
					try{
						if(col == 0){
							slideData.elementAt(viewSlide).setStart(Float.parseFloat(value.toString()));
						}
						if(col == 1){
							slideData.elementAt(viewSlide).setEnd(Float.parseFloat(value.toString()));
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
			timeTable.getColumnModel().getColumn(0).setCellEditor(new SlideTimeInput(true));
			timeTable.getColumnModel().getColumn(1).setCellEditor(new SlideTimeInput(false));
			timeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			JPanel tablePanel = new JPanel();
			tablePanel.add(timeTable);
			infoPanel.add(tablePanel);
			midPanel.add(infoPanel);
			
			//the slide image preview (starts as informative text)
			midPanel.add(slidePreview);
			
			myWindow.add(midPanel, BorderLayout.CENTER);
			
			//the bottom row contains the slide-selection and caption-setting controls
			JPanel lowerOptions = new JPanel();
			lowerOptions.setLayout(new BoxLayout(lowerOptions,BoxLayout.X_AXIS));
			
			JButton previousSlide = new JButton("<html>&larr;</html>");
			previousSlide.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					loadSlide(viewSlide - 1);
				}
			});
			lowerOptions.add(previousSlide);
			
			caption.setColumns(20);
			caption.setHorizontalAlignment(SwingConstants.CENTER);
			caption.setEditable(true);
			caption.addKeyListener(new KeyListener(){
				public void keyReleased(KeyEvent e) {
					slideData.elementAt(viewSlide).setCaption(caption.getText());
				}
				public void keyPressed(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
			lowerOptions.add(caption);
			
			JButton nextSlide = new JButton("<html>&rarr;</html>");
			nextSlide.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					loadSlide(viewSlide + 1);
				}
			});
			lowerOptions.add(nextSlide);
			myWindow.add(lowerOptions, BorderLayout.SOUTH);
			
			//add the first slide, if no slides have been loaded
			if(slideData.size() == 0){
				slideData.add(new SlideItem(0));
			}
			//load the first slide
			loadSlide(0);
		}
		else{
			//restore the previous view
			myWindow.setVisible(true);
			j.dispose();
		}
	}
	
	//called by the Slides program to load a particular slide
	protected void loadSlide(int slidenum){
		timeTable.editingStopped(null);     //prevents the table from being edited across multiple slides
		if(slidenum < 0){
			//if less than zero, jump to the last slide
			slidenum = slideData.size() - 1;
		}
		if(slidenum >= slideData.size()){
			//if this slide does not yet exist, create a slide
			slidenum = slideData.size();
			float bookendTime = slideData.elementAt(slideData.size() - 1).getEnd();
			slideData.add(new SlideItem(bookendTime));
		}
		if(slidenum != viewSlide){
			//changing to a different slide
			String imagefile = slideData.elementAt(slidenum).getFile();
			if(imagefile != null){
				//set slidePreview to the specified image
				fileInput.setText(imagefile);
				slidePreview.setText("<html><img src=\"" + imagefile + "\" height=\"200\" width=\"240\"/><br/>Slide " + (slidenum+1) + " of " + slideData.size() + "</html>");
			}
			else{
				//set slidePreview to information on Slides
				fileInput.setText("");
				slidePreview.setText("<html>Enter an image filename<br/><ul><li>You may enter a file name with ## for<br/> the slide number</li><br/><li>Do not use other peoples' images. <br/>They may be copyrighted, or get replaced or removed</li><br/><li>Flickr photostreams and Picasa web albums<br/> can be imported</li></ul><br/>Slide " + (slidenum+1) + " of " + slideData.size() + "</html>");
			}
			//display caption for the slide
			String mycaption = slideData.elementAt(slidenum).getCaption();
			if(mycaption != null){
				caption.setText(mycaption);
			}
			else{
				caption.setText("");
			}
			viewSlide = slidenum;
			//display the selected video for the current slide
			String videoUrl = slideData.elementAt(slidenum).getVideo();
			if(videoUrl != null){
				for(int v=0; v < vpMain.videoData.size(); v++){
					if(vpMain.videoData.elementAt(v).getURL().equals(videoUrl)){
						videoSelect.setSelectedItem(vpMain.videoData.elementAt(v));
						break;
					}
				}
			}
			//order the timeTable to show this slide's start and end times
			timeTable.repaint();
		}
	}
	
	//SlideTimeInput handles the editor for the start and end times for a slide
	protected class SlideTimeInput extends vpTimeInput {
		public boolean isStartInput = true;
		
		public SlideTimeInput(boolean isStart){
			isStartInput = isStart;
		}
		
		public Component getTableCellEditorComponent(JTable t, Object value, boolean isSelected, int row, int col) {
			if(isStartInput){
				//start time component
				return super.getTableCellWithTime(slideData.elementAt(viewSlide).getStart());			
			}
			else{
				//end time component
				return super.getTableCellWithTime(slideData.elementAt(viewSlide).getEnd());	
			}
		}
	}
	
	protected class SlideItem {
		private String video;
		private float startTime = 0;
		private float endTime = 0;  //an endTime of less than 0.1 represents Automatic ending
		private String caption = null;
		private String fileAddress = null;
		
		public SlideItem(float start){
			startTime = start;
			if(numberRule != null){
				//pattern known for image files
				if(numberRule.indexOf("##") != -1){
					//slide number replaces ## in file address
					fileAddress = numberRule.replace("##", "" + (slideData.size() + 1));
				}
			}
			if(videoSelect.getSelectedItem() != null){
				//set the video to the last slide's video
				if(!videoSelect.getSelectedItem().equals(vpVideoSelector.blankVideo)){
					video = ((VideoListing)videoSelect.getSelectedItem()).getURL();
				}
			}
		}
		public SlideItem(String v, float s, float e, String c, String f){
			//build a slide from loaded information
			video = v;
			startTime = s;
			endTime = e;
			caption = c;
			fileAddress = f;
		}
		
		public String getVideo(){ return video; }
		public void setVideo(String v){ video = v; }
		public float getStart(){ return startTime; }
		public void setStart(float s){ startTime = s; }
		public float getEnd(){ return endTime; }
		public void setEnd(float e){ endTime = e; }
		public String getCaption(){ return caption; }
		public void setCaption(String c){ caption = c; }
		public String getFile(){ return fileAddress; }
		public void setFile(String f){
			fileAddress = null;
			if(f.indexOf("##") != -1){
				//replace ## with the slide number
				numberRule = f;
				for(int i=0; i<slideData.size(); i++){
					if(slideData.elementAt(i).getFile() == null){
						slideData.elementAt(i).setFile(f.replace("##", "" + (i+1)));
					}
				}
			}
			else{
				if(f.indexOf("flickr.com/") != -1){
					//Flickr API allows access to photostreams and photosets - this example uses RSS
					Document flickrPhotos = null;
					if((f.indexOf("@") != -1)&&(f.indexOf("/in/") == -1)){
						if(f.indexOf("api.flickr") == (f.indexOf("flickr") - 4)){
							//direct feed - these are needed for all URLs not accessed by content creator
							//easily acceptable through the RSS button on most web broswers
							flickrPhotos = vpDocLoader.getXMLfromURL(f);
						}
						else{
							if(f.indexOf("/sets/") != -1){
								//photoset
								String firstPart = f.split("@")[0];
								firstPart = firstPart.substring(firstPart.lastIndexOf("/") + 1);
								String secondPart = f.split("@")[1].split("/sets")[0];
								String thirdPart = f.split("@")[1].split("/sets")[1].substring(1);
								if(thirdPart.indexOf("/") != -1){
									thirdPart = thirdPart.substring(0, thirdPart.lastIndexOf("/"));
								}
								flickrPhotos = vpDocLoader.getXMLfromURL("http://api.flickr.com/services/feeds/photoset.gne?set=" + thirdPart + "&nsid=" + firstPart + "@" + secondPart + "&lang=en-us&format=rss_200");	
							}
							else{
								if(f.substring(f.indexOf("@")).indexOf("/") != f.lastIndexOf("/")){	
									//user's photostream
									String firstHalf = f.split("@")[0];
									firstHalf = firstHalf.substring(firstHalf.lastIndexOf("/") + 1);
									String secondHalf = f.split("@")[1];
									secondHalf = secondHalf.substring(0,secondHalf.indexOf("/"));
									String address = (firstHalf + "@" + secondHalf);
									flickrPhotos = vpDocLoader.getXMLfromURL("http://api.flickr.com/services/feeds/photos_public.gne?id=" + address + "&lang=en-us&format=rss_200");
								}
							}
						}
					}
					else{
						//single-photo link
						if(f.indexOf("farm") == -1){
							//linked to the photo page, not the photo itself
							fileAddress = "Right-click photo, or find 'Latest' subscription URL";
						}
					}
					if(flickrPhotos != null){
						NodeList photoFiles = flickrPhotos.getElementsByTagName("media:content");
						NodeList titleList = flickrPhotos.getElementsByTagName("media:title");
						for(int i=0; i<photoFiles.getLength(); i++){
							if(i != 0){
								//need to create slides
								if((viewSlide + i) == slideData.size()){
									//this slide is added to the end
									slideData.add(new SlideItem(0));
								}
								else{
									//this slide is inserted after the current slide
									slideData.insertElementAt(new SlideItem(0), viewSlide + i);
								}
							}
							slideData.elementAt(viewSlide + i).setFile(photoFiles.item(i).getAttributes().getNamedItem("url").getTextContent());
							slideData.elementAt(viewSlide + i).setCaption(titleList.item(i).getTextContent());
						}
						fileInput.setText("http://");
						flickrPhotos = null;
					}
				}
				if(f.indexOf("picasaweb.google") != -1){
					//Picasa web albums
					Document picasaPhotos = null;
					if((f.indexOf("alt=rss") != -1)&&(f.indexOf("kind=photo") != -1)){
						//Direct link to RSS of photos
						picasaPhotos = vpDocLoader.getXMLfromURL(f);
					}
					else{
						if(f.indexOf("#") != -1){
							String userName = f.split("picasaweb.google")[1];
							userName = userName.substring(userName.indexOf("/") + 1);
							userName = userName.substring(0, userName.indexOf("/"));
							Document picasaAlbums = vpDocLoader.getXMLfromURL("http://picasaweb.google.com/data/feed/api/user/" + userName + "?alt=rss&kind=album&hl=en_US");
							NodeList albums = picasaAlbums.getElementsByTagName("guid");
							NodeList albumTitles = picasaAlbums.getElementsByTagName("media:title");
							if(f.lastIndexOf("#") > (f.length() - 5)){
								//photo album
								String albumName = f.substring(f.lastIndexOf("/") + 1, f.indexOf("#"));
								for(int i=0; i<albums.getLength(); i++){
									if(albumTitles.item(i).getTextContent().replace(" ", "").equals(albumName)){
										picasaPhotos = vpDocLoader.getXMLfromURL(albums.item(i).getTextContent().replace("entry", "feed"));
										break;
									}
								}
							}
							else{
								//individual photo
								String albumName = f.substring(f.split("#")[0].lastIndexOf("/") + 1, f.indexOf("#"));
								String photoNum = f.substring(f.indexOf("#") + 1);
								Document thesePhotos = null;
								for(int i=0; i<albums.getLength(); i++){
									if(albumTitles.item(i).getTextContent().replace(" ", "").equals(albumName)){
										thesePhotos = vpDocLoader.getXMLfromURL(albums.item(i).getTextContent().replace("entry", "feed"));
										break;
									}
								}
								if(thesePhotos != null){
									NodeList ids = thesePhotos.getElementsByTagName("guid");
									NodeList photoFiles = thesePhotos.getElementsByTagName("enclosure");
									NodeList titleList = thesePhotos.getElementsByTagName("media:title");
									for(int i=0; i<ids.getLength(); i++){
										if(ids.item(i).getTextContent().indexOf(photoNum) != -1){
											slideData.elementAt(viewSlide).setFile(photoFiles.item(i).getAttributes().getNamedItem("url").getTextContent());
											slideData.elementAt(viewSlide).setCaption(titleList.item(i).getTextContent());
											break;
										}
									}
								}
							}
						}
					}
					if(picasaPhotos != null){
						NodeList photoFiles = picasaPhotos.getElementsByTagName("media:content");
						NodeList titleList = picasaPhotos.getElementsByTagName("media:title");
						for(int i=0; i<photoFiles.getLength(); i++){
							if(i != 0){
								//need to create slides
								if((viewSlide + i) == slideData.size()){
									//this slide is added to the end
									slideData.add(new SlideItem(0));
								}
								else{
									//this slide is inserted after the current slide
									slideData.insertElementAt(new SlideItem(0), viewSlide + i);
								}
							}
							slideData.elementAt(viewSlide + i).setFile(photoFiles.item(i).getAttributes().getNamedItem("url").getTextContent());
							slideData.elementAt(viewSlide + i).setCaption(titleList.item(i).getTextContent());
						}
						fileInput.setText("http://");
						picasaPhotos = null;
					}
				}
			}
			if(fileAddress == null){
				//no special settings - use the given file address
				fileAddress = f;
			}
			//reload the current slide, in case its image has changed
			viewSlide--;
			loadSlide(viewSlide+1);
		}
	}
	public String[] getCSSOptions() { return styles; }
	public String[] getOptionNames() { return stylenames; }
	public String getCurrentCSS() { return myCSS; }
	public void setCurrentCSS(String set) { myCSS = set; }
}