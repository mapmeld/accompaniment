//the video-manager tab

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class vpVideosTab extends JPanel implements ActionListener {
	protected final JTextField videoInput = new JTextField("Enter Video URL Here");
	protected String chosenVideo = "";  //the currently-manipulated video's unique URL
	protected final JButton addVideo = new JButton("Add Video");
	protected final JLabel videoTest = new JLabel("<html><br><br></html>");
	protected final JTextField videoName = new JTextField("Enter Video Name Here (optional)");
	protected final JButton editTape = new JButton("Edit");
	protected final JButton removeTape = new JButton("Remove");
	protected final JList tapeList = new JList(vpMain.videoData);
	
	public vpVideosTab(){
		//defining controls for entering a new video
	   	JPanel newVid = new JPanel();
    	newVid.setLayout(new BorderLayout());
    	newVid.add(videoInput, BorderLayout.NORTH);
    	videoInput.setMargin(new Insets(5,5,5,5));
    	videoInput.setFont(vpMain.urlFont);
    	//when the video URL textbox is clicked, all text is selected for speedy deletion
    	videoInput.addMouseListener(new MouseListener () {
              public void mouseClicked (MouseEvent e) {
            	  videoInput.selectAll();
              }
    		  public void mouseEntered (MouseEvent e) {}
              public void mousePressed (MouseEvent e) {}
              public void mouseReleased (MouseEvent e) {}
              public void mouseExited (MouseEvent e) {}
        });
    	//as keys are typed in the URL textbox, the URL is checked to see if it can possibly be a valid YouTube URL
    	//even when the URL is possibly valid, the thumbnail view is loaded so that the user can check their input
    	videoInput.addKeyListener(new KeyListener() {
			public void keyReleased(KeyEvent e) {
				//start with the assumption that the URL will not be valid
				//video-adding and video-editing controls are disabled
				addVideo.setEnabled(false);
				videoInput.setBackground(vpMain.failColor);
				videoTest.setText("");
				chosenVideo = "";
				editTape.setEnabled(false);
				removeTape.setEnabled(false);
				//NOTE that the imported URL will have no spaces or slashes: /
				String checkURL = videoInput.getText().replace(" ", "").replace("/", "");
				if(checkURL.indexOf("watch?v=") == -1){
					if(checkURL.indexOf("profile_videos?user=") != -1){
						//link to a user's video uploads
						checkURL = checkURL.substring(checkURL.indexOf("profile_videos?user=") + 20);
						addVideo.setEnabled(true);
						videoInput.setBackground(vpMain.validColor);
						videoTest.setText("<html><br/>Uploads for user: <b>" + checkURL + "</b><br/></html>");
						chosenVideo = "Uploads>" + checkURL;
						return;
					}
					else{
						if(checkURL.indexOf("profile_favorites?user=") != -1){
							//link to a user's favorites
							checkURL = checkURL.substring(checkURL.indexOf("profile_favorites?user=") + 23);
							addVideo.setEnabled(true);
							videoInput.setBackground(vpMain.validColor);
							videoTest.setText("<html><br/>Favorites for user: <b>" + checkURL + "</b><br/></html>");
							chosenVideo = "Favorites>" + checkURL;
							return;
						}
						else{
							if(checkURL.indexOf("view_play_list?p=") != -1){
								// link to a video playlist
								checkURL = checkURL.substring(checkURL.indexOf("view_play_list?p=") + 17);
								addVideo.setEnabled(true);
								videoInput.setBackground(vpMain.validColor);
								videoTest.setText("<html><br/>Playlist with ID: <b>" + checkURL + "</b><br/></html>");
								chosenVideo = "Playlist>" + checkURL;
								return;
							}
							else{
								if((checkURL.indexOf("profile?") * checkURL.indexOf("view=")) > 1){
									//set of view options
									if(checkURL.indexOf("view=videos") != -1){
										checkURL = checkURL.substring(checkURL.indexOf("user=") + 5, checkURL.indexOf("&"));
										addVideo.setEnabled(true);
										videoInput.setBackground(vpMain.validColor);
										videoTest.setText("<html><br/>Uploads for user: <b>" + checkURL + "</b><br/></html>");
										chosenVideo = "Uploads>" + checkURL;
										return;
									}
									else{
										if(checkURL.indexOf("view=favorites") != -1){
											checkURL = checkURL.substring(checkURL.indexOf("user=") + 5, checkURL.indexOf("&"));
											addVideo.setEnabled(true);
											videoInput.setBackground(vpMain.validColor);
											videoTest.setText("<html><br/>Favorites for user: <b>" + checkURL + "</b><br/></html>");
											chosenVideo = "Favorites>" + checkURL;
											return;
										}
									}
								}
							}
						}
					}
					return;
				}
				if(checkURL.indexOf("&") != -1){
					//remove any additional parameters (marked with an &)
					checkURL = checkURL.substring(0, checkURL.indexOf("&"));
				}
				if(checkURL.indexOf("#") != -1){
					//remove timestamps
					checkURL = checkURL.substring(0, checkURL.indexOf("#"));
				}
				checkURL = checkURL.substring(checkURL.indexOf("watch?v=") + 8);
				//video URL length parameters: correct as needed
				if(checkURL.length() < vpMain.MIN_YOUTUBE_CHARS){ return; }
				if(checkURL.length() > vpMain.MAX_YOUTUBE_CHARS){ return; }
				videoInput.setBackground(vpMain.validColor);
				videoTest.setText("<html><img src='http://img.youtube.com/vi/" + checkURL + "/2.jpg'><br>Video thumbnail</html>");
				chosenVideo = checkURL;
				for(int i=0; i<vpMain.videoData.size(); i++){
            		if(vpMain.videoData.elementAt(i).getURL().equals(chosenVideo)){
    					//if the video is already in the list, it is selected and the edit controls are enabled
    					tapeList.setSelectedIndex(i);
    					editTape.setEnabled(true);
    					removeTape.setEnabled(true);
            			return;
            		}
            	}
				//since the video is not on the list, video-adding controls are enabled
				addVideo.setEnabled(true);
			}
    		public void keyPressed(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}	
    	});
    	
    	//videoName allows the user to identify their videos
    	videoName.setMargin(new Insets(5,5,5,5));
    	videoName.setFont(vpMain.messageFont);
    	//videoName clears the default text when clicked, then removes the mouse listener, since it only runs once
    	videoName.addMouseListener(new MouseListener () {
            public void mouseClicked (MouseEvent e) {
          	  if(videoName.getText().equals("Enter Video Name Here (optional)")){ videoName.setText("");}
          	  videoName.removeMouseListener(this);
            }
  		  	public void mouseEntered (MouseEvent e) {}
            public void mousePressed (MouseEvent e) {}
            public void mouseReleased (MouseEvent e) {}
            public void mouseExited (MouseEvent e) {}
    	});
    	
    	//the addVideo button executes the addToVideoList method if the URL appears valid
    	addVideo.setEnabled(false);
    	addVideo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				if(videoInput.getBackground().equals(vpMain.validColor)){
					if(chosenVideo.indexOf(">") == -1){
						//regular video being added
						addToVideoList(videoName.getText(), chosenVideo);
					}
					else{
						//Adding a video stream
						
						//First, format the URL for any settings 
						if(chosenVideo.indexOf("&") == -1){
							//if there are no special parameters already, use the maximum 50-result query
							chosenVideo = chosenVideo + "?max-results=50";
						}
						else{
							//if there are special parameters already, fix the first & to a ?
							if(chosenVideo.indexOf("?") == -1){
								chosenVideo = chosenVideo.replaceFirst("&", "?");
							}
							//if max-results is not specified, specify the 50 max-results
							if(chosenVideo.indexOf("max-results=") == -1){
								chosenVideo = chosenVideo + "&max-results=50";
							}
						}
						if(chosenVideo.startsWith("Uploads>")){
							//import from a user's uploaded videos
							getVideos("http://gdata.youtube.com/feeds/api/users/" + chosenVideo.substring(8, chosenVideo.length() - 15) + "/uploads" + chosenVideo.substring(chosenVideo.length() - 15));
						}
						else{
							if(chosenVideo.startsWith("Favorites>")){
								//import from a user's favorites
								getVideos("http://gdata.youtube.com/feeds/api/users/" + chosenVideo.substring(10, chosenVideo.length() - 15) + "/favorites" + chosenVideo.substring(chosenVideo.length() - 15));
							}
							else{
								if(chosenVideo.startsWith("Playlist>")){
									//import from a video playlist
									getVideos("http://gdata.youtube.com/feeds/api/playlists/" + chosenVideo.substring(9));
								}
							}
						}
						tapeList.setListData(vpMain.videoData);
					}
				}
			}
    	});
    	newVid.add(addVideo, BorderLayout.EAST);
    	newVid.add(videoName, BorderLayout.CENTER);
    	
    	//videoTest is the thumbnail view for the input URL: mistyped URLs should be conspicuously wrong
    	//potential change: clicking the thumbnail could attempt to open the URL in the browser
		videoTest.setHorizontalAlignment(SwingConstants.CENTER);
    	newVid.add(videoTest, BorderLayout.SOUTH);
    	this.add(newVid);

    	//the tape__ variables define the data behind the list of videos
    	JPanel tapeCentral = new JPanel();
    	tapeCentral.setLayout(new BorderLayout());
    	JPanel tapePanel = new JPanel();
    	tapePanel.add(editTape);
    	editTape.setEnabled(false);
    	editTape.addActionListener(this);
    	tapePanel.add(removeTape);
    	removeTape.setEnabled(false);
    	removeTape.addActionListener(this);
    	tapeCentral.add(tapePanel, BorderLayout.NORTH);
    	tapeList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    	tapeList.setLayoutOrientation(JList.VERTICAL_WRAP);
    	tapeList.setVisibleRowCount(9);
    	tapeList.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e) {

				//when an existing video is selected, its information is displayed and made ready for edits
				if(tapeList.getSelectedIndex() != -1){
					videoName.setText(vpMain.videoData.elementAt(tapeList.getSelectedIndex()).getName());
					chosenVideo = vpMain.videoData.elementAt(tapeList.getSelectedIndex()).getURL();
					videoInput.setText("http://youtube.com/watch?v=" + chosenVideo);
					videoInput.setBackground(vpMain.validColor);
					videoTest.setText("<html><img src='http://img.youtube.com/vi/" + chosenVideo + "/2.jpg'><br>Video thumbnail</html>");
					videoTest.setToolTipText("youtube.com/watch?v=" + chosenVideo);
					addVideo.setEnabled(false);
					editTape.setEnabled(true);
					removeTape.setEnabled(true);
				}
			}
    	});
    	tapeCentral.add(new JScrollPane(tapeList), BorderLayout.CENTER);
    	this.add(tapeCentral);
    	
    	this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	}
	
	public void addToVideoList(String name, String tape){
		//any needless space on the left and right sides are removed from the name by trim
		name = name.trim();
		//the default name is the video's order of being added
    	if(name.equals("Enter Video Name Here (optional)")||name.equals("")){ name = "" + (vpMain.videoData.size() + 2); }
    	for(int i=0; i<vpMain.videoData.size(); i++){
    		if(vpMain.videoData.elementAt(i).getURL().equals(tape)){
    			return;
    		}
    	}
    	vpMain.videoData.add(new VideoListing(name, tape));
		tapeList.setListData(vpMain.videoData);
		addVideo.setEnabled(false);
	}

	public void actionPerformed(ActionEvent e) {
		//editTape changes the name and URL of the selected video
		if(e.getSource().equals(editTape)){
			int i = tapeList.getSelectedIndex();
			if(i != -1){
				VideoListing editItem = vpMain.videoData.elementAt(i);
				editItem.setName(videoName.getText());
				editItem.setURL(chosenVideo);
				tapeList.setListData(vpMain.videoData);
				tapeList.setSelectedIndex(i);
			}
			return;
		}
		//removeTape currently removes the video without question
		if(e.getSource().equals(removeTape)){
			int[] selected = tapeList.getSelectedIndices();
			for(int i=selected.length - 1; i>=0; i--){
				vpMain.videoData.removeElementAt(selected[i]);
			}
			addVideo.setEnabled(true);
			editTape.setEnabled(false);
			removeTape.setEnabled(false);
			tapeList.setListData(vpMain.videoData);
			return;
		}
	}
	
	public static void getVideos(String fromURL){
		try{
            Document doc = vpDocLoader.getXMLfromURL(fromURL);
            NodeList titles = doc.getElementsByTagName("title");
            NodeList allLinks = doc.getElementsByTagName("link");
            Vector<String> vidLinks = new Vector<String>();
            for(int i=0; i<allLinks.getLength(); i++){
            	if(allLinks.item(i).getAttributes().getNamedItem("rel").getTextContent().equals("alternate")){
            		vidLinks.add(allLinks.item(i).getAttributes().getNamedItem("href").getTextContent());
            	}
            }
            for(int v=1; v<titles.getLength(); v++){
            	String name = titles.item(v).getTextContent();
            	String vidURL = vidLinks.elementAt(v);
            	vidURL = vidURL.substring(vidURL.indexOf("watch?v=") + 8);
            	boolean isnewVid = true;
            	for(int i=0; i<vpMain.videoData.size(); i++){
            		if(vpMain.videoData.elementAt(i).getURL().equals(vidURL)){
            			isnewVid = false;
            			break;
            		}
            	}
            	if(isnewVid){  vpMain.videoData.add(new VideoListing(name, vidURL)); }
            }
		}catch(Exception e){}
	}
}