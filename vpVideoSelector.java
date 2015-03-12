import javax.swing.JComboBox;

//vpVideoSelector is a general-use drop-down list of videos

//if using in a table model, call refresh() on getValue()
//otherwise, add a FocusListener and call refresh() when focus is gained

public class vpVideoSelector extends JComboBox {
	
	public final static VideoListing blankVideo = new VideoListing("","Same");
	
	private boolean includeBlankVideo;
	private boolean includeNull;
	
	public vpVideoSelector(){
		//default vpVideoSelector
		includeBlankVideo = true;
		includeNull = false;
	}
	
	public vpVideoSelector(boolean blankvid, boolean nullvid){
		//specifically set whether to include the (Same) and null options
		includeBlankVideo = blankvid;
		includeNull = nullvid;
	}
	
	public void refresh(){
		if(vpMain.videoData.size() > 0){
			this.setEnabled(true);
			this.removeAllItems();
			if(includeBlankVideo){ this.addItem(blankVideo); }
			if(includeNull){ this.addItem(" "); }
			//adding all videos to the options
			for(int i=0; i<vpMain.videoData.size(); i++){
				this.addItem(vpMain.videoData.elementAt(i));
			}
		}
	}
}