import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//vpPageTab allows the user to set up page layout
public class vpPageTab extends JPanel implements MouseListener, CSSstyled {

	//playerOption represents the video player
	protected static PlayerOption playerOption = new PlayerOption();
	
	//CSS defaults
	protected static String basicStyle =
    	"body{\r\n" +
    	"	/* page's base background */\r\n" +
    	"	background-color: #D3D3D3;\r\n" +
    	"}\r\n" +
    	"div.pageTableDiv{\r\n" +
    	"	/* content table setup */\r\n" +
    	"	background-color: white;\r\n" +
    	"	border: 2px solid #808080;\r\n\r\n" +
    	"	margin-top: 30px;\r\n" +
    	"	margin-bottom: 30px;\r\n" +
    	"	margin-left: 5%;\r\n" +
    	"	margin-right: 5%;\r\n\r\n" +
    	"	padding-top: 50px;\r\n" +
    	"	padding-bottom:50px;\r\n" +
    	"	padding-left: 4%;\r\n" +
    	"	padding-right: 4%;\r\n" +
    	"}\r\n";
	protected static String[] cssOptions = { basicStyle };
	protected static String[] cssOptionNames = { "Basic" };
	
	//defining the Page tab
    protected Container layoutPanel = new JPanel();
    protected PageFrame[][] pageFrames = new PageFrame[2][2];
    protected PageFrame videoFrame;
    protected PageFrame selectFrame;
    protected int rows = 2;
    protected int columns = 2;
    protected JLabel pageLabel = new JLabel();
    protected JLabel elementName = new JLabel();
    protected JPanel elementEdits = new JPanel();
    protected JTextArea headerName = new JTextArea();
	protected AddOnSelect addonSelect = new AddOnSelect();
	protected boolean changedSelect = false;
	protected String myCSS = basicStyle;
	protected CSSEditor cssWindow = new CSSEditor(this);
    
    public vpPageTab(){
    	this.setLayout(new GridLayout(2,1,0,20));
    	
    	//boxes to represent layout
    	layoutPanel.setLayout(new GridLayout(2, 2));
    	setPageFrame(layoutPanel);
    	JScrollPane pageScroll = new JScrollPane(layoutPanel);

    	this.add(pageScroll);
    	
    	//setting element details
    	JPanel elementPanel = new JPanel();
    	
    	//addonSelect is a drop-down list of AddOns in the project
    	addonSelect.setMaximumRowCount(7);
    	addonSelect.setEnabled(false);
    	addonSelect.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if((selectFrame != null)&&(!changedSelect)){
					//have the selected frame use this AddOn
					if(selectFrame.equals(playerOption.getPlace())){
						playerOption.setPlaced(false);
					}
					selectFrame.setSupplement(addonSelect.getSelectedItem());
					return;
				}
				changedSelect = false;
			}
    	});
    	addonSelect.addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent e) {
				//when the list is being clicked, make sure the list contains all unused AddOns 
				addonSelect.refresh();
			}
			public void focusLost(FocusEvent e) {}
    	});
    	elementPanel.add(addonSelect);
    	
    	//headerName puts an <h3> element above the section
    	headerName.setEditable(true);
    	headerName.setText("Type header text here");
    	headerName.setFont(vpMain.messageFont);
    	headerName.addKeyListener(new KeyListener(){
			public void keyReleased(KeyEvent e) {
				if(selectFrame != null){
					//when the header text is edited, set the header of the selected box
					selectFrame.setHeader(headerName.getText());
				}
			}
			public void keyPressed(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
    	});
    	elementPanel.add(headerName);
    	
    	//elementEdits manages the CSS of the selected section
    	elementEdits.setBorder(vpMain.labelBorder);
    	JButton pCSS = new JButton("Page Style");
    	pCSS.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				cssWindow.editStyle();
			}
    	});
    	elementEdits.add(pCSS);
    	JButton cssEditButton = new JButton("Edit Cell Style");
    	cssEditButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(selectFrame != null){
					CSSEditor css = new CSSEditor(selectFrame);
					css.editStyle();
				}
			}
    	});
    	elementEdits.add(cssEditButton);
    	
    	elementPanel.add(elementEdits);
    	
      	elementPanel.setLayout(new BoxLayout(elementPanel, BoxLayout.PAGE_AXIS));
    	this.add(elementPanel);
    }
    
    //sets up the grid in the given container
    public void setPageFrame(Container page){
    	page.removeAll();
    	GridLayout myLayout = ((GridLayout) page.getLayout());
    	rows = myLayout.getRows();
    	columns = myLayout.getColumns();
    	for(int r=0; r<rows; r++){
    		for(int c=0; c<columns; c++){
    			if(pageFrames[r][c] == null){
    				pageFrames[r][c] = new PageFrame();
    				pageFrames[r][c].addMouseListener(this);
    			}
    			page.add(pageFrames[r][c]);
    		}
    	}
    }
    
    //Putting the mouse over a frame highlights it
    public void mouseEntered(MouseEvent e) {
        for(int r=0; r<rows; r++){
        	for(int c=0; c<columns; c++){
        		if(e.getSource().equals(pageFrames[r][c])){
        			if(!e.getSource().equals(selectFrame)){
        				pageFrames[r][c].setHoverState(true);
        			}
        			return;
        		}
        	}
        }
    }
    
    //A mouse click makes a frame the selected frame
    public void mouseClicked(MouseEvent e){
        for(int r=0; r<rows; r++){
        	for(int c=0; c<columns; c++){
        		if(e.getSource().equals(pageFrames[r][c])){
        			changedSelect = true;
        			if(selectFrame != null){ selectFrame.select(false); }
        			selectFrame = pageFrames[r][c];
        			selectFrame.select(true);
        			headerName.setText(selectFrame.getHeader());
        			addonSelect.refresh();
        			return;
        		}
        	}
        }
    }

    //Taking the mouse out of a frame removes its highlight (unless it is the selected frame)
    public void mouseExited(MouseEvent e) {
        for(int r=0; r<rows; r++){
        	for(int c=0; c<columns; c++){
        		if(e.getSource().equals(pageFrames[r][c])){
        			if(!e.getSource().equals(selectFrame)){
        				pageFrames[r][c].setHoverState(false);
        			}
        			return;
        		}
        	}
        }    	
    }
    
    //unused mouse methods
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    
    protected class AddOnSelect extends JComboBox{
    	public void refresh(){
    		if(!this.isEnabled()){
    			this.setEnabled(true);
    			changedSelect = false;
    		}
    		this.removeAllItems();
    		if(selectFrame != null){
    			if(selectFrame.getSupplement() != null){
    				this.addItem(selectFrame.getSupplement());
    				this.setSelectedItem(selectFrame.getSupplement());
    			}
    		}
    		this.addItem(null);
    		if(!vpPageTab.playerOption.isPlaced()){ this.addItem(vpPageTab.playerOption); }
    		for(int i=0; i<vpMain.addonData.size(); i++){
    			boolean addonRepeats = false;
    			int xlimit = pageFrames.length;
    			for(int j=0; j<xlimit; j++){
    				int ylimit = pageFrames[j].length;
    				for(int k=0; k<ylimit; k++){
    					if(vpMain.addonData.elementAt(i).equals(pageFrames[j][k].getSupplement())){
    						addonRepeats = true;
    						j = k = 100;
    					}
    				}
    			}
				if(!addonRepeats) { this.addItem(vpMain.addonData.elementAt(i)); }
    		}
    	}
    }
    
	protected static class PlayerOption extends JTextField{
	    protected boolean placedPlayer = false;
	    protected PageFrame loc = null;
		
		public PlayerOption(){
			this.setText("<html><b>Video Player</b></html>");
		}
		
		public String toString(){
			return "Video Player";
		}
		
		public void setPlaced(PageFrame location){
			placedPlayer = true;
			loc = location;
		}
		public PageFrame getPlace(){ return loc; }
		
		public void setPlaced(boolean place){ placedPlayer = place; loc = null; }
		public boolean isPlaced(){ return placedPlayer; }
	}
    
    public String getProjectXML(){
    	boolean hasData = false;
    	String savePage = "<pageStyle>" + vpDocLoader.rewrite(myCSS) + "</pageStyle>";
    	for(int row = 0; row < pageFrames.length; row++){
    		savePage += "<row>\r\n";
    		for(int col = 0; col < pageFrames[row].length; col++){
    			if(pageFrames[row][col].getSupplement() != null){
    				hasData = true;
    				savePage += ("<frame id=\"addon" + vpMain.addonData.indexOf(pageFrames[row][col].getSupplement()) + "\" header=\"" + vpDocLoader.rewrite(pageFrames[row][col].getHeader()) + "\">\r\n");
    				savePage += ("<style>" + vpDocLoader.rewrite(pageFrames[row][col].getCurrentCSS().trim()) + "</style>\r\n</frame>\r\n");
    			}
    			else{
    				if(pageFrames[row][col].equals(playerOption.getPlace())){
    					hasData = true;
    					savePage += ("<frame id=\"videoplayer\" header=\"" + vpDocLoader.rewrite(pageFrames[row][col].getHeader()) + "\">\r\n");
        				savePage += ("<style>\r\n" + vpDocLoader.rewrite(pageFrames[row][col].getCurrentCSS()) + "</style>\r\n</frame>\r\n");    					
    				}
    				else{
    					savePage += "<frame></frame>\r\n";
    				}
    			}
    		}
    		savePage += "</row>\r\n";
    	}
    	if(hasData){ return "<pageData>" + savePage + "</pageData>"; }
    	return null;
    }
    
    public void useProjectXML(String loadData, int addonOffset){
    	int rowCount = 0;
    	int colCount = 0;
    	int internalColCount = 0;
    	//determine how many rows and columns are in the project's page layout
    	String layoutFix = loadData + "<row<frame";
    	while(layoutFix.indexOf("<row") != layoutFix.lastIndexOf("<row")){
    		layoutFix = layoutFix.substring(layoutFix.indexOf("<row") + 4);
    		rowCount++;
    		internalColCount = 0;
    		while(layoutFix.indexOf("<frame") < layoutFix.indexOf("<row")){
    			layoutFix = layoutFix.substring(layoutFix.indexOf("<frame") + 6);
    			internalColCount++;
    		}
    		if(internalColCount > colCount){ colCount = internalColCount; }
    	}
    	if((rowCount <= 0)||(colCount <=0)){ return; }
    	//build page layout
    	layoutPanel.setLayout(new GridLayout(rowCount, colCount));
    	setPageFrame(layoutPanel);
    	//load page content
		Document saveDoc = vpDocLoader.getXMLfromString(loadData);
		try{
			myCSS = vpDocLoader.reread(saveDoc.getElementsByTagName("pageStyle").item(0).getTextContent());
		}
		catch(Exception e){ myCSS = basicStyle; }
		NodeList rows = saveDoc.getElementsByTagName("row");
		for(rowCount=0; rowCount<rows.getLength(); rowCount++){
			colCount = 0;
			Node frame = rows.item(rowCount).getFirstChild().getNextSibling();
			if(frame != null){
				do{
					NamedNodeMap frameData = frame.getAttributes();
					if(frameData != null){
						if(frameData.getNamedItem("header") != null){
							pageFrames[rowCount][colCount].setHeader(vpDocLoader.reread(frameData.getNamedItem("header").getTextContent()));
						}
						if(frameData.getNamedItem("id") != null){
							String addonID = frameData.getNamedItem("id").getTextContent();
							if(addonID.equals("videoplayer")){
								pageFrames[rowCount][colCount].setSupplement(playerOption);
							}
							else{
								addonID = addonID.substring(addonID.indexOf("addon") + 5);
								pageFrames[rowCount][colCount].setSupplement(vpMain.addonData.elementAt(Integer.parseInt(addonID) + addonOffset));
							}
						}
					}
					NodeList styles = frame.getChildNodes();
					if(styles.getLength() == 3){
						pageFrames[rowCount][colCount].setCSS(vpDocLoader.reread(styles.item(1).getTextContent().trim()));
					}
					colCount++;
					frame = frame.getNextSibling().getNextSibling();
				}while(frame != null);
			}
		}
    }
    
    public String getHTML(){
    	String onloadScript =
    		"<script type=\"text/javascript\">\r\n" +
    		"function loadAddons(){\r\n";
    	String refreshScript = "function refreshPage(){\r\n";
    	Vector<String> refreshedTypes = new Vector<String>();
    	String buildPage =
    		"<div id=\"headerMessage\"></div>\r\n" +
    		"<table>\r\n";
    	for(int row = 0; row < pageFrames.length; row++){
    		buildPage += "<tr>\r\n";
    		for(int col = 0; col < pageFrames[row].length; col++){
    			PageFrame cell = pageFrames[row][col];
    			//While the counter starts at zero, the ID will be built with the first element being 1
    			String frameID = "row" + (row+1) + "col" + (col+1); 
    			buildPage += "<td>\r\n";
				buildPage += "<style type=\"text/css\">\r\n" + pageFrames[row][col].getCurrentCSS().replace("#thisDiv", "#" + frameID + "_Div") + "</style>\r\n";
				String header = cell.getHeader();
				if(header != null){
					if(!header.equals("")){
						buildPage += "<h3>" + header + "</h3>\r\n";
					}
				}
    			if(cell.getSupplement() != null){
    				Supplement addon = cell.getSupplement();
    				buildPage += ("<div id=\"" + frameID + "_Div\">\r\n" + addon.getHTML(frameID));
    				String scripts = addon.getIndividualScripts(frameID);
    				if(scripts != null){ buildPage += scripts; }
    				buildPage += ("</div>\r\n");
    				scripts = addon.getLoadScript(frameID);
    				if(scripts != null){ onloadScript += scripts; }
    				if(addon.refreshType() == Supplement.EACH_REFRESHES){
    					refreshScript += ("		" + frameID + "_update();\r\n");
    				}
    				else{
    					if(addon.refreshType() == Supplement.TYPE_REFRESHES){
    						if(!refreshedTypes.contains(addon.getType())){
    							refreshScript += ("		" + addon.getType() + ".update();\r\n");
    							refreshedTypes.add(addon.getType());
    						}
    					}
    				}
    			}
    			else{
    				if(cell.equals(playerOption.getPlace())){
    		            //writing the player:   player is the video element placed inside playerdiv
    		            buildPage +=(
    		            	"\r\n<!--This div will contain the video player or an error message-->\r\n" +
    		            	"<div id=\"playerdiv\">Sorry, you need Flash player 8+ and JavaScript enabled to view this video.</div>\r\n" +
    		            	"  <script type=\"text/javascript\">\r\n" +
    		            	"    var params = { allowScriptAccess: \"always\", bgcolor: \"#cccccc\" };\r\n" +
    		            	"    var atts = { id: \"myytplayer\" };\r\n" +
    		            	"    swfobject.embedSWF(\"http://www.youtube.com/apiplayer?enablejsapi=1&amp;playerapiid=myytplayer\", \"playerdiv\", \"500\", \"375\", \"8\", null, null, params, atts);\r\n" +
    		            	"  </script>\r\n");
    		            
    		            //writing the player controls
    		            buildPage +=("\r\n"+
    		            "<!--This div contains the play button and time slider-->\r\n"+
    		            "<div style=\"background-image: url('http://sugo-katta.appspot.com/timeline.png'); background-repeat: no-repeat; -moz-user-select: none; -khtml-user-select: none; user-select: none;\">\r\n" +
    		            	"<table><tr><td>\r\n" +
    		            		"<img id=\"playpausebutton\" src=\"http://sugo-katta.appspot.com/playButton.png\" onclick=\"playpause();\"/>\r\n" +
    		            	"</td><td>\r\n" +
    		            		"<div id=\"slider\" style=\"margin: 10;\"><div id=\"sliderthumb\"><img src=\"http://sugo-katta.appspot.com/indicator.png\"/></div></div>\r\n" +
    		            		"<script type=\"text/javascript\">\r\n" +
    		            			"var slider = YAHOO.widget.Slider.getHorizSlider(\"slider\", \"sliderthumb\", 0, 400);\r\n" +
    		            			"slider.subscribe(\"slideEnd\", setVidTime);\r\n" +
    		            			"slider.subscribe(\"slideStart\", disableAutoSlide);\r\n" +
    		            		"</script>\r\n" +
    		            	"</td></tr></table>\r\n" +
    		            "</div>\r\n");
    				}
    			}
    			buildPage += "</td>\r\n";
    		}
    		buildPage += "</tr>\r\n";
    	}
    	buildPage += "</table>\r\n";
    	onloadScript += "}\r\n";
		refreshScript += "}\r\n</script>\r\n";
    	return ("<div class=\"pageTableDiv\">" + onloadScript + refreshScript + buildPage + "</div>\r\n");
    }
    //getPageCSS is kept separate from getCurrentCSS in case different processes are used later
    public String getPageCSS(){
    	return myCSS;
    }
	public String getCurrentCSS() {
		return myCSS;
	}
	public void setCurrentCSS(String set) {
		myCSS = set;
	}
	public String[] getCSSOptions() { return cssOptions; }
	public String[] getOptionNames() { return cssOptionNames; }
}